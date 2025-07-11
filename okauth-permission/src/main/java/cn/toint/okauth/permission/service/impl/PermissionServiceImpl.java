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

import cn.toint.okauth.permission.event.PermissionCacheClearEvent;
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
import org.dromara.hutool.extra.spring.SpringUtil;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final KeyBuilderUtil rolePermissionCacheKeyBuilder = KeyBuilderUtil.of("roleMtmPermission");
    private final String permissionTreeCacheKey = "permissionTree";

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

    @SuppressWarnings("unchecked")
    @Override
    public void create(PermissionCreateRequest request) {
        String code = request.getCode();

        // 1. 数据校验
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 权限码不能重复
        if (StringUtils.isNotBlank(code)) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .select(PermissionDo::getCode)
                    .eq(PermissionDo::getCode, code);
            PermissionDo permissionDo = permissionMapper.selectOneByQuery(queryWrapper);
            Assert.isNull(permissionDo, "权限码已存在");
        }

        // 2. 数据初始化
        PermissionDo permissionDo = new PermissionDo();
        BeanUtil.copyProperties(request, permissionDo);
        permissionDo.init();

        if (permissionDo.getOrder() == null) {
            permissionDo.setOrder(0);
        }

        // 3. 执行入库
        permissionMapper.insert(permissionDo, false);
    }

    @Override
    public void update(PermissionUpdateRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 检查是否存在
        PermissionDo permissionDo = getById(request.getId());
        Assert.notNull(permissionDo, "权限不存在");

        BeanUtil.copyProperties(request, permissionDo);
        permissionDo.freshUpdateTime();

        if (permissionDo.getOrder() == null) {
            permissionDo.setOrder(0);
        }

        int updated = permissionMapper.update(permissionDo, false);
        Assert.isTrue(SqlUtil.toBool(updated), "修改失败");

        // 清除缓存
        SpringUtil.publishEvent(new PermissionCacheClearEvent(List.of(permissionDo.getId())));
    }

    @Override
    public boolean hasById(Long id) {
        if (id == null) {
            return false;
        }
        return permissionMapper.selectOneById(id) != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public void bind(Long roleId, List<Long> permissionIds) {
        Assert.notNull(roleId, "角色ID不能为空");
        Assert.notEmpty(permissionIds, "权限ID不能为空");

        for (Long permissionId : permissionIds) {
            Assert.notNull(permissionId, "权限ID不能为空");
        }

        Assert.isTrue(roleService.hasById(roleId), "角色[{}]不存在", roleId);

        for (Long permissionId : permissionIds) {
            Assert.isTrue(hasById(permissionId), "权限[{}]不存在", permissionId);
        }

        // 检查角色和权限是否已经绑定关系, 绑定过了就跳过
        permissionIds.removeIf(permissionId -> roleMtmPermissionMapper.selectOneByQuery(QueryWrapper.create()
                .select(RoleMtmPermissionDo::getId)
                .eq(RoleMtmPermissionDo::getRoleId, roleId)
                .eq(RoleMtmPermissionDo::getPermissionId, permissionId)) != null);

        // 保存到数据库
        List<RoleMtmPermissionDo> roleMtmPermissionDos = new ArrayList<>();
        for (Long permissionId : permissionIds) {
            RoleMtmPermissionDo roleMtmPermissionDo = new RoleMtmPermissionDo();
            roleMtmPermissionDo.init();
            roleMtmPermissionDo.setRoleId(roleId);
            roleMtmPermissionDo.setPermissionId(permissionId);
            roleMtmPermissionDos.add(roleMtmPermissionDo);
        }
        roleMtmPermissionMapper.insertBatch(roleMtmPermissionDos);

        // 最后全部没问题, 清除缓存
        SpringUtil.publishEvent(new PermissionCacheClearEvent(permissionIds));
    }

    @Override
    public void unbind(Long roleId, List<Long> permissionIds) {
        Assert.notNull(roleId, "角色ID不能为空");
        if (CollUtil.isEmpty(permissionIds)) return;
        permissionIds.removeIf(Objects::isNull);

        // 删除角色和权限关联关系
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(RoleMtmPermissionDo::getRoleId, roleId)
                .in(RoleMtmPermissionDo::getPermissionId, permissionIds);
        roleMtmPermissionMapper.deleteByQuery(queryWrapper);

        // 清除缓存
        SpringUtil.publishEvent(new PermissionCacheClearEvent(permissionIds));
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;

        // 删除权限本身
        permissionMapper.deleteBatchByIds(ids);

        // 删除角色与权限关联信息
        QueryWrapper queryWrapper = QueryWrapper.create()
                .in(RoleMtmPermissionDo::getPermissionId, ids);
        roleMtmPermissionMapper.deleteByQuery(queryWrapper);

        // 清除缓存
        SpringUtil.publishEvent(new PermissionCacheClearEvent(ids));
    }

    @Override
    public List<PermissionTreeResponse> listTree() {
        // 先读取缓存
        String cacheKey = permissionTreeCacheKey;
        String cacheValue = cache.get(cacheKey);
        if (StringUtils.isNotEmpty(cacheValue)) {
            return JacksonUtil.readValue(cacheValue, new TypeReference<>() {
            });
        }

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

        // 加入缓存
        cacheValue = JacksonUtil.writeValueAsString(roots);
        cache.put(cacheKey, cacheValue, okAuthPermissionProperties.getCacheTimeout());

        return roots;
    }

    /**
     * 权限写入事件监听器, 当权限发生变动时清除对应的缓存
     */
    @SuppressWarnings("unchecked")
    @EventListener
    protected void onCacheClear(PermissionCacheClearEvent event) {
        List<Long> permissionIds = event.getSource();
        permissionIds.removeIf(Objects::isNull);
        if (CollUtil.isEmpty(permissionIds)) return;

        // 1. 查询权限对应的所有角色
        List<Long> roleIds = roleMtmPermissionMapper.selectListByQuery(QueryWrapper.create()
                        .select(RoleMtmPermissionDo::getRoleId)
                        .in(RoleMtmPermissionDo::getPermissionId, permissionIds))
                .stream()
                .map(RoleMtmPermissionDo::getRoleId)
                .toList();
        if (roleIds.isEmpty()) return;

        // 2. 清除角色对应的缓存
        roleIds.stream()
                .map(String::valueOf)
                .map(rolePermissionCacheKeyBuilder::build)
                .forEach(cache::delete);

        // 3. 清除权限树缓存
        cache.delete(permissionTreeCacheKey);
    }
}
