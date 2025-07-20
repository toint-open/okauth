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

import cn.toint.okauth.permission.constant.OkAuthPermissionConstant;
import cn.toint.okauth.permission.event.ClearPermissionCacheEvent;
import cn.toint.okauth.permission.mapper.RoleMapper;
import cn.toint.okauth.permission.mapper.RoleMtmPermissionMapper;
import cn.toint.okauth.permission.mapper.UserMtmRoleMapper;
import cn.toint.okauth.permission.model.*;
import cn.toint.okauth.permission.properties.OkAuthPermissionProperties;
import cn.toint.okauth.permission.service.RoleService;
import cn.toint.oktool.spring.boot.cache.Cache;
import cn.toint.oktool.util.Assert;
import cn.toint.oktool.util.JacksonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Resource
    private RoleMtmPermissionMapper roleMtmPermissionMapper;

    @Override
    public boolean isAdmin(Long userId) {
        List<RoleDo> roleDos = listByUserId(userId);
        return roleDos.stream()
                .anyMatch(roleDo -> OkAuthPermissionConstant.Role.ADMIN.equals(roleDo.getCode()));
    }

    @Override
    public List<RoleDo> listByUserId(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");

        // 1. 尝试从缓存中获取
        String cacheKey = OkAuthPermissionConstant.userMtmRoleCacheKeyBuilder.build(String.valueOf(userId));
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
            // 管理员拥有全部角色
            if (roleIds.contains(OkAuthPermissionConstant.Role.ADMIN_ID)) {
                roleDos.addAll(roleMapper.selectAll());
            } else {
                QueryWrapper roleQueryWrapper = QueryWrapper.create().in(RoleDo::getId, roleIds);
                roleDos.addAll(roleMapper.selectListByQuery(roleQueryWrapper));
            }
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

        // 4. 清除缓存
        ClearPermissionCacheEvent.of()
                .addRoleId(roleDo.getId())
                .publishEvent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(RoleUpdateRequest request) {
        String code = request.getCode();
        Long id = request.getId();
        Assert.notEquals(code, OkAuthPermissionConstant.Role.ADMIN, "admin角色不允许修改");
        Assert.notEquals(id, OkAuthPermissionConstant.Role.ADMIN_ID, "admin角色不允许修改");

        // 1. 参数校验
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 角色码不能重复
        Assert.isNull(roleMapper.selectOneByQuery(QueryWrapper.create()
                .select(RoleDo::getCode)
                .eq(RoleDo::getCode, code)
                .ne(RoleDo::getId, request.getId())), "角色码不能重复");

        // 2. 数据初始化
        RoleDo roleDo = roleMapper.selectOneById(id);
        Assert.notNull(roleDo, "角色不存在");
        BeanUtils.copyProperties(request, roleDo);
        roleDo.freshUpdateTime();

        // 3. 数据入库
        roleMapper.update(roleDo, false);

        // 4. 清除缓存
        ClearPermissionCacheEvent.of()
                .addRoleId(roleDo.getId())
                .publishEvent();
    }

    @Override
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;

        // 查询是否存在admin角色
        Assert.isFalse(ids.contains(OkAuthPermissionConstant.Role.ADMIN_ID), "admin角色不允许删除");

        // 删除角色数据
        roleMapper.deleteBatchByIds(ids);

        // 删除用户与角色的绑定关系
        List<UserMtmRoleDo> userMtmRoleDos = userMtmRoleMapper.selectListByQuery(QueryWrapper.create()
                .in(UserMtmRoleDo::getRoleId, ids));

        if (!userMtmRoleDos.isEmpty()) {
            userMtmRoleMapper.deleteBatchByIds(userMtmRoleDos.stream()
                    .map(UserMtmRoleDo::getId)
                    .toList());
        }

        // 删除角色与权限关联信息
        List<RoleMtmPermissionDo> roleMtmPermissionDos = roleMtmPermissionMapper.selectListByQuery(QueryWrapper.create()
                .in(RoleMtmPermissionDo::getRoleId, ids));

        if (!roleMtmPermissionDos.isEmpty()) {
            roleMtmPermissionMapper.deleteBatchByIds(roleMtmPermissionDos.stream()
                    .map(RoleMtmPermissionDo::getId)
                    .toList());
        }

        // 清除缓存
        ClearPermissionCacheEvent clearPermissionCacheEvent = ClearPermissionCacheEvent.of();
        ids.forEach(clearPermissionCacheEvent::addRoleId);
        userMtmRoleDos.forEach(item -> {
            clearPermissionCacheEvent.adduserId(item.getUserId());
            clearPermissionCacheEvent.addRoleId(item.getRoleId());
        });
        roleMtmPermissionDos.forEach(item -> {
            clearPermissionCacheEvent.addRoleId(item.getRoleId());
            clearPermissionCacheEvent.addPermissionId(item.getPermissionId());
        });
        clearPermissionCacheEvent.publishEvent();
    }

    @Override
    public void bind(Long roleId, List<Long> userIds) {
        Assert.notNull(roleId, "角色ID不能为空");
        Assert.notEmpty(userIds, "用户ID不能为空");

        for (Long userId : userIds) {
            Assert.notNull(userId, "用户ID不能为空");
        }

        // 校验角色存在
        Assert.isTrue(hasById(roleId), "角色[{}]不存在", roleId);

        // 删除用户与角色已经绑定的数据
        userMtmRoleMapper.deleteByQuery(QueryWrapper.create()
                .eq(UserMtmRoleDo::getRoleId, roleId));

        // 批量绑定
        List<UserMtmRoleDo> userMtmRoleDos = new ArrayList<>();
        for (Long userId : userIds) {
            UserMtmRoleDo userMtmRoleDo = new UserMtmRoleDo();
            userMtmRoleDo.init();
            userMtmRoleDo.setRoleId(roleId);
            userMtmRoleDo.setUserId(userId);
            userMtmRoleDos.add(userMtmRoleDo);
        }
        userMtmRoleMapper.insertBatch(userMtmRoleDos);

        // 清除缓存
        ClearPermissionCacheEvent clearPermissionCacheEvent = ClearPermissionCacheEvent.of();
        clearPermissionCacheEvent.addRoleId(roleId);
        userIds.forEach(clearPermissionCacheEvent::adduserId);
        clearPermissionCacheEvent.publishEvent();
    }

    @Override
    public List<RoleDo> listAll() {
        return roleMapper.selectAll();
    }

    @Override
    public RoleDo getByCode(String code) {
        return roleMapper.selectOneByQuery(QueryWrapper.create()
                .eq(RoleDo::getCode, code));
    }

    @Override
    public List<Long> listUserIdByRoleId(Long roleId) {
        Assert.notNull(roleId, "角色ID不能为空");

        return userMtmRoleMapper.selectListByQuery(QueryWrapper.create()
                        .eq(UserMtmRoleDo::getRoleId, roleId))
                .stream()
                .map(UserMtmRoleDo::getUserId)
                .collect(Collectors.toList());
    }

    @EventListener
    protected void onClearPermissionCache(ClearPermissionCacheEvent event) {
        ClearPermissionCacheEvent.Detail detail = event.getSource();
        Set<Long> userIds = detail.getUserIds();
        Set<Long> permissionIds = detail.getPermissionIds();
        Set<Long> roleIds = detail.getRoleIds();

        // 只要动了角色或者权限, 一定清除admin的缓存
        roleIds.add(OkAuthPermissionConstant.Role.ADMIN_ID);

        // 根据权限ID, 找到需要清除的角色
        if (!permissionIds.isEmpty()) {
            roleMtmPermissionMapper.selectListByQuery(QueryWrapper.create()
                            .in(RoleMtmPermissionDo::getPermissionId, permissionIds))
                    .stream()
                    .map(RoleMtmPermissionDo::getRoleId)
                    .forEach(roleIds::add);
        }

        // 根据角色, 找到关联的用户
        if (!roleIds.isEmpty()) {
            userMtmRoleMapper.selectListByQuery(QueryWrapper.create()
                            .in(UserMtmRoleDo::getRoleId, roleIds))
                    .stream()
                    .map(UserMtmRoleDo::getUserId)
                    .forEach(userIds::add);
        }

        // 清除角色与权限关系缓存
        roleIds.stream()
                .map(String::valueOf)
                .map(OkAuthPermissionConstant.roleMtmPermissionCacheKeyBuilder::build)
                .forEach(cache::delete);

        // 清除用户与角色关系缓存
        userIds.stream()
                .map(String::valueOf)
                .map(OkAuthPermissionConstant.userMtmRoleCacheKeyBuilder::build)
                .forEach(cache::delete);
    }
}
