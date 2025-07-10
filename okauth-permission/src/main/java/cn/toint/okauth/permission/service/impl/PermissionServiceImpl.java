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
        List<PermissionDo> allPermissionDos = new ArrayList<>();

        // 查询用户的所有角色
        List<RoleDo> roleDos = roleService.listByUserId(userId);
        List<Long> roleIds = roleDos.stream()
                .map(RoleDo::getId)
                .filter(Objects::nonNull)
                .toList();
        if (roleIds.isEmpty()) return allPermissionDos;

        // 尝试从缓存中查找所有角色拥有的所有权限
        List<Long> uncacheRoleIds = new ArrayList<>(); // 未命中缓存的权限, 需要到数据库加载
        List<String> cacheKeys = roleIds.stream()
                .map(String::valueOf)
                .map(rolePermissionCacheKeyBuilder::build)
                .toList();
        List<String> cacheValues = cache.multiGet(cacheKeys);
        for (int i = 0, cacheValuesSize = cacheValues.size(); i < cacheValuesSize; i++) {
            String cacheValue = cacheValues.get(i);
            if (StringUtils.isNotBlank(cacheValue)) {
                List<PermissionDo> cachePermissionDos = JacksonUtil.readValue(cacheValue, new TypeReference<>() {
                });
                cachePermissionDos.stream()
                        .filter(Objects::nonNull)
                        .forEach(allPermissionDos::add);
            } else {
                uncacheRoleIds.add(roleIds.get(i));
            }
        }

        // 用户角色全部命中缓存
        if (uncacheRoleIds.isEmpty()) {
            return allPermissionDos;
        }

        // 查询角色与权限关联关系
        QueryWrapper roleMtmPermissionQueryWrapper = QueryWrapper.create().in(RoleMtmPermissionDo::getRoleId, uncacheRoleIds);
        List<RoleMtmPermissionDo> roleMtmPermissionDos = roleMtmPermissionMapper.selectListByQuery(roleMtmPermissionQueryWrapper);
        if (roleMtmPermissionDos.isEmpty()) return allPermissionDos;

        // 角色与权限的映射
        Map<Long, List<Long>> roleMtmPermissionMap = new HashMap<>();
        for (RoleMtmPermissionDo roleMtmPermissionDo : roleMtmPermissionDos) {
            Long roleId = roleMtmPermissionDo.getRoleId();
            Long permissionId = roleMtmPermissionDo.getPermissionId();
            roleMtmPermissionMap.computeIfAbsent(roleId, k -> new ArrayList<>())
                    .add(permissionId);
        }

        // 从数据库查询角色对应的权限
        List<Long> permissionIds = roleMtmPermissionDos.stream().map(RoleMtmPermissionDo::getPermissionId).toList();
        QueryWrapper permissionQueryWrapper = QueryWrapper.create().in(PermissionDo::getId, permissionIds);
        List<PermissionDo> permissionDos = permissionMapper.selectListByQuery(permissionQueryWrapper);
        if (permissionDos.isEmpty()) return allPermissionDos;
        allPermissionDos.addAll(permissionDos);

        Map<Long, PermissionDo> permissionDoMap = new HashMap<>();
        permissionDos.forEach(permissionDo -> {
            permissionDoMap.put(permissionDo.getId(), permissionDo);
        });

        // 加入缓存
        roleMtmPermissionMap.forEach((roleId, permissionIds2) -> {
            String cacheKey = rolePermissionCacheKeyBuilder.build(String.valueOf(roleId));
            List<PermissionDo> cachePermissionDos = permissionIds2.stream()
                    .map(permissionDoMap::get)
                    .toList();
            String cacheValue = JacksonUtil.writeValueAsString(cachePermissionDos);
            cache.put(cacheKey, cacheValue, okAuthPermissionProperties.getCacheTimeout());
        });

        return allPermissionDos;
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
        ArrayList<PermissionTreeResponse> permissionVos = new ArrayList<>();
        for (PermissionDo permissionDo : permissionDos) {
            PermissionTreeResponse permissionVo = new PermissionTreeResponse();
            BeanUtil.copyProperties(permissionDo, permissionVo);
            permissionVo.setChildren(new ArrayList<>());
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
                    parent.getChildren().add(permissionVo);
                }
            }
        }

        return roots;
    }
}
