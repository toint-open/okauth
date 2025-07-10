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

import cn.toint.okauth.permission.mapper.PermissionMapper;
import cn.toint.okauth.permission.mapper.RoleMtmPermissionMapper;
import cn.toint.okauth.permission.model.*;
import cn.toint.okauth.permission.properties.OkAuthPermissionProperties;
import cn.toint.okauth.permission.service.PermissionService;
import cn.toint.okauth.permission.service.RoleService;
import cn.toint.oktool.spring.boot.cache.Cache;
import cn.toint.oktool.util.Assert;
import cn.toint.oktool.util.ExceptionUtil;
import cn.toint.oktool.util.JacksonUtil;
import cn.toint.oktool.util.KeyBuilderUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.SqlUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * @author Toint
 * @date 2025/6/29
 */
@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private RoleService roleService;

    @Resource
    private RoleMtmPermissionMapper roleMtmPermissionMapper;

    @Resource
    private Cache cache;

    @Resource
    private OkAuthPermissionProperties okAuthPermissionProperties;

    private final KeyBuilderUtil rolePermissionCacheKeyBuilder = KeyBuilderUtil.of("rolePermission");

    @Override
    public List<PermissionDo> listByUserId(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");

        // 当前用户拥有的所有权限
        Set<PermissionDo> allPermissionDos = new HashSet<>();

        // 1. 查询用户的所有角色
        List<RoleDo> roleDos = roleService.listByUserId(userId);
        List<Long> roleIds = roleDos.stream()
                .map(RoleDo::getId)
                .filter(Objects::nonNull)
                .toList();

        // 2. 当前用户没有任何角色, 退出
        if (roleIds.isEmpty()) return new ArrayList<>();

        // 3. 尝试从缓存中查找所有角色拥有的所有权限
        List<String> cacheKeys = roleIds.stream()
                .map(String::valueOf)
                .map(rolePermissionCacheKeyBuilder::build)
                .toList();

        // 角色在缓存中的权限字符串, 未找到对应的缓存用null占位
        List<String> cacheValues = cache.multiGet(cacheKeys);
        // 未命中缓存的权限ID集合, 需要到数据库加载
        List<Long> uncacheRoleIds = new ArrayList<>();
        for (int i = 0, cacheValuesSize = cacheValues.size(); i < cacheValuesSize; i++) {
            // 单个角色的权限缓存
            // 如果是空值, 说明这个角色没有权限缓存, 需要到数据库加载
            // 如果是[], 说明这个角色没有对应的权限, 不需要到数据库加载
            String cacheValue = cacheValues.get(i);
            if (StringUtils.isBlank(cacheValue)) {
                uncacheRoleIds.add(roleIds.get(i));
            } else {
                List<PermissionDo> cachePermissionDos = JacksonUtil.readValue(cacheValue, new TypeReference<>() {
                });
                cachePermissionDos.stream()
                        .filter(Objects::nonNull)
                        .forEach(allPermissionDos::add);
            }
        }

        // 4. 用户角色全部命中缓存, 直接返回缓存内容
        if (uncacheRoleIds.isEmpty()) {
            return new ArrayList<>(allPermissionDos);
        }

        // 5. 剩余未命中缓存的角色, 查询其对应的所有权限
        List<PermissionDo> uncachePermissionDos = new ArrayList<>();
        List<RoleMtmPermissionDo> uncacheRoleMtmPermissionDos = roleMtmPermissionMapper.selectListByQuery(
                QueryWrapper.create().in(RoleMtmPermissionDo::getRoleId, uncacheRoleIds));
        if (CollUtil.isNotEmpty(uncacheRoleMtmPermissionDos)) {
            List<Long> permissionIds = uncacheRoleMtmPermissionDos.stream().map(RoleMtmPermissionDo::getPermissionId).toList();
            uncachePermissionDos.addAll(permissionMapper.selectListByIds(permissionIds));
            allPermissionDos.addAll(uncachePermissionDos);
        }

        // 6. 每个角色对应的缓存集合, 如果角色没有对应的权限, 用空集合占位
        Map<Long, List<PermissionDo>> uncacheRolePermissionMap = new HashMap<>(); // 最终要得到的

        // 权限ID:权限对象, 方便查找
        Map<Long, PermissionDo> uncachePermissionMap = new HashMap<>();
        for (PermissionDo permissionDo : uncachePermissionDos) {
            uncachePermissionMap.put(permissionDo.getId(), permissionDo);
        }

        for (RoleMtmPermissionDo roleMtmPermission : uncacheRoleMtmPermissionDos) {
            Long roleId = roleMtmPermission.getRoleId();
            Long permissionId = roleMtmPermission.getPermissionId();

            // 从权限映射中获取权限对象
            PermissionDo permissionDo = uncachePermissionMap.get(permissionId);
            if (permissionDo != null) {
                uncacheRolePermissionMap.computeIfAbsent(roleId, k -> new ArrayList<>())
                        .add(permissionDo);
            }
        }

        // 检查映射中是否存在所有角色, 不包含的用空集合占位
        for (Long uncacheRoleId : uncacheRoleIds) {
            uncacheRolePermissionMap.putIfAbsent(uncacheRoleId, new ArrayList<>());
        }

        // 7. 缓存角色与权限信息
        for (Map.Entry<Long, List<PermissionDo>> entry : uncacheRolePermissionMap.entrySet()) {
            Long roleId = entry.getKey();
            List<PermissionDo> permissionDos = entry.getValue();

            // 过滤掉null权限
            permissionDos.removeIf(Objects::isNull);

            String cacheKey = rolePermissionCacheKeyBuilder.build(String.valueOf(roleId));
            String cacheValue = JacksonUtil.writeValueAsString(permissionDos);
            Duration cacheTimeout = okAuthPermissionProperties.getCacheTimeout();

            cache.put(cacheKey, cacheValue, cacheTimeout);
        }

        return new ArrayList<>(allPermissionDos);
    }

    @Override
    public PermissionDo getById(Long id) {
        Assert.notNull(id, "id不能为空");
        return permissionMapper.selectOneById(id);
    }

    @Override
    public void create(PermissionCreateRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        PermissionDo permissionDo = BeanUtil.copyProperties(request, new PermissionDo());
        permissionDo.init();
        if (permissionDo.getOrder() == null) {
            permissionDo.setOrder(0);
        }
        int inserted = permissionMapper.insert(permissionDo);
        Assert.isTrue(SqlUtil.toBool(inserted), "添加失败");
    }

    @Override
    public void update(PermissionUpdateRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 检查是否存在
        boolean hasPermissionById = hasById(request.getId());
        Assert.isTrue(hasPermissionById, "权限不存在");

        PermissionDo permissionDo = BeanUtil.copyProperties(request, new PermissionDo());
        int updated = permissionMapper.update(permissionDo);
        Assert.isTrue(SqlUtil.toBool(updated), "修改失败");
    }

    @Override
    public boolean hasById(Long id) {
        if (id == null) {
            return false;
        }
        return permissionMapper.selectOneById(id) != null;
    }

    @Override
    public void delete(PermissionDeleteRequest request) {
        List<Long> ids = request.getIds();
        if (CollUtil.isNotEmpty(ids)) {
            permissionMapper.deleteBatchByIds(ids);
        }
    }

    @Override
    public List<PermissionTreeResponse> listTree() {
        // 查询所有权限
        List<PermissionDo> permissionDos = permissionMapper.selectAll();
        if (permissionDos.isEmpty()) {
            return new ArrayList<>();
        }

        // 全部对象转为vo
        List<PermissionTreeResponse> permissionVos = new ArrayList<>();
        for (PermissionDo permissionDo : permissionDos) {
            PermissionTreeResponse permissionVo = new PermissionTreeResponse();
            BeanUtil.copyProperties(permissionDo, permissionVo);
            permissionVos.add(permissionVo);
        }

        // 创建Map方便查找
        Map<Long, PermissionTreeResponse> permissionVoMap = new HashMap<>();
        for (PermissionTreeResponse permissionVo : permissionVos) {
            permissionVoMap.put(permissionVo.getId(), permissionVo);
        }

        // 检测循环依赖
        for (PermissionTreeResponse permissionVo : permissionVos) {
            Set<Long> path = new HashSet<>();
            Long currentId = permissionVo.getId();

            // 沿着parentId链向上查找，直到找到根节点或发现循环
            while (currentId != null && permissionVoMap.containsKey(currentId)) {
                if (!path.add(currentId)) {  // add返回false说明已存在，即发现循环
                    throw ExceptionUtil.wrapRuntimeException("权限[{}]存在循环依赖", currentId);
                }
                PermissionTreeResponse current = permissionVoMap.get(currentId);
                currentId = (current.getParentId() == 0) ? null : current.getParentId();
            }
        }

        // 构建树关系
        List<PermissionTreeResponse> roots = new ArrayList<>();
        for (PermissionTreeResponse permissionVo : permissionVos) {
            if (permissionVo.getParentId() == 0) {
                roots.add(permissionVo);
            } else {
                PermissionTreeResponse parent = permissionVoMap.get(permissionVo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(permissionVo);
                }
            }
        }

        return roots;
    }
}
