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

import cn.toint.okauth.permission.mapper.*;
import cn.toint.okauth.permission.model.*;
import cn.toint.okauth.permission.service.PermissionService;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Toint
 * @date 2025/6/29
 */
@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {
    /**
     * 权限
     */
    @Resource
    private PermissionMapper permissionMapper;

    /**
     * 用户关联权限
     */
    @Resource
    private UserMtmPermissionMapper userMtmPermissionMapper;

    /**
     * 角色
     */
    @Resource
    private RoleMapper roleMapper;

    /**
     * 用户关联角色
     */
    @Resource
    private UserMtmRoleMapper userMtmRoleMapper;

    /**
     * 角色关联权限
     */
    @Resource
    private RoleMtmPermissionMapper roleMtmPermissionMapper;

    /**
     * 部门
     */
    @Resource
    private DeptMapper deptMapper;

    /**
     * 用户关联部门
     */
    @Resource
    private UserMtmDeptMapper userMtmDeptMapper;

    /**
     * 部门关联权限
     */
    @Resource
    private DeptMtmPermissionMapper deptMtmPermissionMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Set<String> listPermissionCode(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");

        // 从缓存找
        String key = KeyBuilderUtil.of("okauth").add("permissionCode").build(userId.toString());
        String cacheValue = stringRedisTemplate.opsForValue().get(key);

        if (StringUtils.isNotBlank(cacheValue)) {
            log.debug("listPermission命中缓存");
            return JacksonUtil.readValue(cacheValue, new TypeReference<>() {
            });
        }

        log.debug("listPermission未命中缓存");
        Set<Long> allPermissionIds = new HashSet<>();

        // 查询用户角色权限: 用户->角色->权限
        allPermissionIds.addAll(listPermissionByUserMtmRole(userId));

        // 查询用户直连权限: 用户->权限
        allPermissionIds.addAll(listPermissionByUserMtmPermission(userId));

        // 查询用户部门权限: 用户->部门->权限
        allPermissionIds.addAll(listPermissionByUserMtmDept(userId));

        // 所有权限code
        Set<String> codes = new HashSet<>();

        // 根据权限ID查询权限code
        if (!allPermissionIds.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<PermissionDo> permissionDos = permissionMapper.selectListByQuery(
                    QueryWrapper.create()
                            .select(PermissionDo::getCode)
                            .in(PermissionDo::getId, allPermissionIds));

            permissionDos.stream()
                    .map(PermissionDo::getCode)
                    .forEach(codes::add);
        }

        stringRedisTemplate.opsForValue().set(key,
                JacksonUtil.writeValueAsString(codes),
                Duration.ofHours(1));
        return codes;
    }

    private Set<Long> listPermissionByUserMtmDept(Long userId) {
        // 查询所有部门, 有缓存, 查询极快
        List<DeptTreeResponse> deptTrees = listDeptTree();

        // 查询用户关联的直接部门
        List<UserMtmDeptDo> userMtmDeptDos = userMtmDeptMapper.selectListByQuery(
                QueryWrapper.create().eq(UserMtmDeptDo::getUserId, userId));

        if (userMtmDeptDos.isEmpty()) {
            return new HashSet<>();
        }

        // 查询用户直属部门+子部门 (默认继承子部门权限)
        Set<Long> directDeptIds = userMtmDeptDos.stream().map(UserMtmDeptDo::getDeptId).collect(Collectors.toSet());
        Set<Long> allDeptIds = new HashSet<>(directDeptIds);

        // 遍历部门树，收集所有子部门
        for (DeptTreeResponse deptTree : deptTrees) {
            findAndAddChildDepts(deptTree, allDeptIds);
        }

        if (allDeptIds.isEmpty()) {
            return new HashSet<>();
        }

        // 部门关联查询权限
        List<DeptMtmPermissionDo> deptMtmPermissionDos = deptMtmPermissionMapper.selectListByQuery(
                QueryWrapper.create().in(DeptMtmPermissionDo::getDeptId, allDeptIds));

        return deptMtmPermissionDos.stream()
                .map(DeptMtmPermissionDo::getPermissionId)
                .collect(Collectors.toSet());
    }

    /**
     * 递归收集子部门
     * 如果部门的父部门已在集合中，则将该部门也加入集合
     */
    private void findAndAddChildDepts(DeptTreeResponse deptTree, Set<Long> allDeptIds) {
        if (allDeptIds.contains(deptTree.getParentId())) {
            allDeptIds.add(deptTree.getId());
        }

        // 递归处理子部门
        if (CollUtil.isNotEmpty(deptTree.getChildren())) {
            for (DeptTreeResponse child : deptTree.getChildren()) {
                findAndAddChildDepts(child, allDeptIds);
            }
        }
    }

    /**
     * 根据用户直连权限查询权限
     *
     * @param userId userId
     * @return 用户直连权限不重复列表
     */
    private Set<Long> listPermissionByUserMtmPermission(Long userId) {
        // 查询用户直接关联权限
        List<UserMtmPermissionDo> userMtmPermissionDos = userMtmPermissionMapper.selectListByQuery(
                QueryWrapper.create().eq(UserMtmPermissionDo::getUserId, userId));

        if (userMtmPermissionDos.isEmpty()) {
            return new HashSet<>();
        }

        return userMtmPermissionDos.stream()
                .map(UserMtmPermissionDo::getPermissionId)
                .collect(Collectors.toSet());
    }

    /**
     * 根据用户角色查询权限
     *
     * @param userId userId
     * @return 用户角色权限不重复列表
     */
    private Set<Long> listPermissionByUserMtmRole(Long userId) {
        // 查询用户关联所有角色
        List<UserMtmRoleDo> userMtmRoleDos = userMtmRoleMapper.selectListByQuery(
                QueryWrapper.create().eq(UserMtmRoleDo::getUserId, userId));

        if (userMtmRoleDos.isEmpty()) {
            return new HashSet<>();
        }

        // 角色ID不重复列表
        Set<Long> roleIds = userMtmRoleDos.stream()
                .map(UserMtmRoleDo::getRoleId)
                .collect(Collectors.toSet());

        // 查询角色关联所有权限
        List<RoleMtmPermissionDo> roleMtmPermissionDos = roleMtmPermissionMapper.selectListByQuery(
                QueryWrapper.create().in(RoleMtmPermissionDo::getRoleId, roleIds));

        if (roleMtmPermissionDos.isEmpty()) {
            return new HashSet<>();
        }

        return roleMtmPermissionDos.stream()
                .map(RoleMtmPermissionDo::getPermissionId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<RoleDo> listRole(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");

        // 用户关联角色
        List<UserMtmRoleDo> userMtmRoleDos = userMtmRoleMapper.selectListByQuery(
                QueryWrapper.create().eq(UserMtmRoleDo::getUserId, userId));

        if (userMtmRoleDos.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询角色
        Set<Long> roleIds = userMtmRoleDos.stream()
                .map(UserMtmRoleDo::getRoleId)
                .collect(Collectors.toSet());
        return roleMapper.selectListByQuery(QueryWrapper.create().in(RoleDo::getId, roleIds));
    }

    @Override
    public List<DeptTreeResponse> listDeptTree() {
        // 尝试从缓存中查找
        String key = KeyBuilderUtil.of("okauth").add("deptTree").build("all");
        String cacheStr = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(cacheStr)) {
            return JacksonUtil.readValue(cacheStr, new TypeReference<>() {
            });
        }

        // 查询所有部门
        List<DeptDo> deptDos = deptMapper.selectAll();
        if (deptDos.isEmpty()) {
            return new ArrayList<>();
        }

        // 全部对象转换为vo
        List<DeptTreeResponse> deptVos = new ArrayList<>();
        for (DeptDo deptDo : deptDos) {
            DeptTreeResponse deptVo = new DeptTreeResponse();
            BeanUtil.copyProperties(deptDo, deptVo);
            deptVo.setChildren(new ArrayList<>());
            deptVos.add(deptVo);
        }

        // 创建Map方便查找
        Map<Long, DeptTreeResponse> deptVoMap = new HashMap<>();
        for (DeptTreeResponse deptVo : deptVos) {
            deptVoMap.put(deptVo.getId(), deptVo);
        }

        // 检测循环依赖
        for (DeptTreeResponse deptVo : deptVos) {
            Set<Long> path = new HashSet<>();
            Long currentId = deptVo.getId();

            // 沿着parentId链向上查找，直到找到根节点或发现循环
            while (currentId != null && deptVoMap.containsKey(currentId)) {
                if (!path.add(currentId)) {  // add返回false说明已存在，即发现循环
                    throw ExceptionUtil.wrapRuntimeException("部门[{}]存在循环依赖", currentId);
                }
                DeptTreeResponse current = deptVoMap.get(currentId);
                currentId = (current.getParentId() == 0) ? null : current.getParentId();
            }
        }

        // 构建树关系
        List<DeptTreeResponse> roots = new ArrayList<>();
        for (DeptTreeResponse deptVo : deptVos) {
            if (deptVo.getParentId() == 0) {
                // 找到根节点
                roots.add(deptVo);
            } else {
                // 找到父节点，把当前节点加入父节点的children
                DeptTreeResponse parent = deptVoMap.get(deptVo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(deptVo);
                }
            }
        }

        stringRedisTemplate.opsForValue().set(key, JacksonUtil.writeValueAsString(roots), Duration.ofHours(1));
        return roots;
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
        if (permissionDo.getOrder() == null) {
            permissionDo.setOrder(0);
        }
        int inserted = permissionMapper.insert(permissionDo);
        Assert.isTrue(SqlUtil.toBool(inserted), "添加失败");
    }

    @Override
    public void updatePermission(PermissionUpdateRequest request) {
        Assert.notNull(request, "请求参数不能为空");

        PermissionDo permissionDo = BeanUtil.copyProperties(request, new PermissionDo());
        int updated = permissionMapper.update(permissionDo);
        Assert.isTrue(SqlUtil.toBool(updated), "修改失败");
    }

    @Override
    public void deletePermission(Long id) {
        Assert.notNull(id, "id不能为空");
        permissionMapper.deleteById(id);
    }

    @Override
    public List<PermissionTreeResponse> listPermissionTree() {
        // 尝试从缓存中查找
        String key = KeyBuilderUtil.of("okauth").add("permissionTree").build("all");
        String cacheStr = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(cacheStr)) {
            return JacksonUtil.readValue(cacheStr, new TypeReference<>() {
            });
        }

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

        stringRedisTemplate.opsForValue().set(key, JacksonUtil.writeValueAsString(roots), Duration.ofHours(1));
        return roots;
    }
}
