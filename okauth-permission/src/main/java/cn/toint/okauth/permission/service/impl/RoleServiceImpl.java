/*
 * Copyright 2025 Toint (599818663@qq.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.toint.okauth.permission.service.impl;

import cn.toint.okauth.permission.mapper.RoleMapper;
import cn.toint.okauth.permission.mapper.UserMtmRoleMapper;
import cn.toint.okauth.permission.model.RoleDo;
import cn.toint.okauth.permission.model.UserMtmRoleDo;
import cn.toint.okauth.permission.properties.OkAuthPermissionProperties;
import cn.toint.okauth.permission.service.RoleService;
import cn.toint.oktool.spring.boot.cache.Cache;
import cn.toint.oktool.util.Assert;
import cn.toint.oktool.util.JacksonUtil;
import cn.toint.oktool.util.KeyBuilderUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {
    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserMtmRoleMapper userMtmRoleMapper;

    @Resource
    private Cache cache;

    @Resource
    private OkAuthPermissionProperties okAuthPermissionProperties;

    private final KeyBuilderUtil userRoleCacheKeyBuilder = KeyBuilderUtil.of("userMtmRole");

    @Override
    public List<RoleDo> listByUserId(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");

        // 1. 尝试从缓存中获取
        String cacheKey = userRoleCacheKeyBuilder.build(String.valueOf(userId));
        String cacheValue = cache.get(cacheKey);
        if (StringUtils.isNotBlank(cacheValue)) {
            return JacksonUtil.readValue(cacheValue, new TypeReference<>() {
            });
        }

        // 2. 从数据库中获取用户的所有角色
        QueryWrapper userMtmRoleQueryWrapper = QueryWrapper.create().eq(UserMtmRoleDo::getUserId, userId);
        List<UserMtmRoleDo> userMtmRoleDos = userMtmRoleMapper.selectListByQuery(userMtmRoleQueryWrapper);
        List<Long> roleIds = userMtmRoleDos.stream()
                .map(UserMtmRoleDo::getRoleId)
                .toList();

        List<RoleDo> roleDos = new ArrayList<>();
        if (CollUtil.isNotEmpty(roleIds)) {
            QueryWrapper roleQueryWrapper = QueryWrapper.create().in(RoleDo::getId, roleIds);
            roleDos.addAll(roleMapper.selectListByQuery(roleQueryWrapper));
        }

        // 3. 加入缓存
        cache.put(cacheKey, JacksonUtil.writeValueAsString(roleDos), okAuthPermissionProperties.getCacheTimeout());
        return roleDos;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasById(Long id) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(RoleDo::getId)
                .eq(RoleDo::getId, id);
        return roleMapper.selectOneByQuery(queryWrapper) != null;
    }
}
