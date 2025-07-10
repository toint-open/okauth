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
import cn.toint.okauth.permission.model.RoleCreateRequest;
import cn.toint.okauth.permission.model.RoleDo;
import cn.toint.okauth.permission.model.RoleUpdateRequest;
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
import org.springframework.beans.BeanUtils;
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

    @Override
    public RoleDo getById(Long id) {
        Assert.notNull(id, "角色ID不能为空");
        return roleMapper.selectOneById(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void create(RoleCreateRequest request) {
        String code = request.getCode();

        // 1. 参数校验
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 角色码不能重复
        Assert.isNull(roleMapper.selectOneByQuery(QueryWrapper.create()
                .select(RoleDo::getCode)
                .eq(RoleDo::getCode, code)), "角色码不能重复");

        // 2. 数据初始化
        RoleDo roleDo = new RoleDo();
        roleDo.init();
        BeanUtils.copyProperties(request, roleDo);

        // 3. 数据入库
        roleMapper.insert(roleDo, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(RoleUpdateRequest request) {
        String code = request.getCode();
        Long id = request.getId();

        // 1. 参数校验
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 角色码不能重复
        Assert.isNull(roleMapper.selectOneByQuery(QueryWrapper.create()
                .select(RoleDo::getCode)
                .eq(RoleDo::getCode, code)), "角色码不能重复");

        // 2. 数据初始化
        RoleDo roleDo = roleMapper.selectOneById(id);
        Assert.notNull(roleDo, "角色不存在");
        BeanUtils.copyProperties(request, roleDo);
        roleDo.freshUpdateTime();

        // 3. 数据入库
        roleMapper.update(roleDo, false);

        // todo 4. 清除缓存
    }

    @Override
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;

        // 删除角色数据
        roleMapper.deleteBatchByIds(ids);

        // todo 删除用户-角色绑定关系

        // todo 删除角色-权限绑定关系

        // todo 清除缓存
    }

    @Override
    public void bind(Long roleId, List<Long> userIds) {
        // todo bind
    }
}
