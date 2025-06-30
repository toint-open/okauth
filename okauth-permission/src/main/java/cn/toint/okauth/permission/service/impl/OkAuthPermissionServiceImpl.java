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
import cn.toint.okauth.permission.service.OkAuthPermissionService;
import cn.toint.oktool.util.Assert;
import cn.toint.oktool.util.ExceptionUtil;
import cn.toint.oktool.util.JacksonUtil;
import cn.toint.oktool.util.KeyBuilderUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mybatisflex.core.query.QueryWrapper;
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
public class OkAuthPermissionServiceImpl implements OkAuthPermissionService {
    /**
     * 权限
     */
    @Resource
    private OkAuthPermissionMapper permissionMapper;

    /**
     * 用户关联权限
     */
    @Resource
    private OkAuthUserMtmPermissionMapper userMtmPermissionMapper;

    /**
     * 角色
     */
    @Resource
    private OkAuthRoleMapper roleMapper;

    /**
     * 用户关联角色
     */
    @Resource
    private OkAuthUserMtmRoleMapper userMtmRoleMapper;

    /**
     * 角色关联权限
     */
    @Resource
    private OkAuthRoleMtmPermissionMapper roleMtmPermissionMapper;

    /**
     * 部门
     */
    @Resource
    private OkAuthDeptMapper deptMapper;

    /**
     * 用户关联部门
     */
    @Resource
    private OkAuthUserMtmDeptMapper userMtmDeptMapper;

    /**
     * 部门关联权限
     */
    @Resource
    private OkAuthDeptMtmPermissionMapper deptMtmPermissionMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Set<String> listPermissionCode(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");

        // 从缓存找
        String key = KeyBuilderUtil.of("okauth").add("permissionCode").build(userId.toString());
        String cacheValue = stringRedisTemplate.opsForValue().get(key);

        if (StringUtils.isNotBlank(cacheValue)) {
            log.info("listPermission命中缓存");
            return JacksonUtil.readValue(cacheValue, new TypeReference<>() {
            });
        }

        log.info("listPermission未命中缓存");
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
            List<OkAuthPermissionDo> permissionDos = permissionMapper.selectListByQuery(
                    QueryWrapper.create()
                            .select(OkAuthPermissionDo::getCode)
                            .in(OkAuthPermissionDo::getId, allPermissionIds));

            permissionDos.stream()
                    .map(OkAuthPermissionDo::getCode)
                    .forEach(codes::add);
        }

        stringRedisTemplate.opsForValue().set(key,
                JacksonUtil.writeValueAsString(codes),
                Duration.ofHours(1));
        return codes;
    }

    private Set<Long> listPermissionByUserMtmDept(Long userId) {
        // 查询所有部门, 有缓存, 查询极快
        List<OkAuthDeptTreeVo> deptTrees = listDeptTree();

        // 查询用户关联的直接部门
        List<OkAuthUserMtmDeptDo> userMtmDeptDos = userMtmDeptMapper.selectListByQuery(
                QueryWrapper.create().eq(OkAuthUserMtmDeptDo::getUserId, userId));

        if (userMtmDeptDos.isEmpty()) {
            return new HashSet<>();
        }

        // 查询用户直属部门+子部门 (默认继承子部门权限)
        Set<Long> directDeptIds = userMtmDeptDos.stream().map(OkAuthUserMtmDeptDo::getDeptId).collect(Collectors.toSet());
        Set<Long> allDeptIds = new HashSet<>(directDeptIds);

        // 遍历部门树，收集所有子部门
        for (OkAuthDeptTreeVo deptTree : deptTrees) {
            findAndAddChildDepts(deptTree, allDeptIds);
        }

        if (allDeptIds.isEmpty()) {
            return new HashSet<>();
        }

        // 部门关联查询权限
        List<OkAuthDeptMtmPermissionDo> deptMtmPermissionDos = deptMtmPermissionMapper.selectListByQuery(
                QueryWrapper.create().in(OkAuthDeptMtmPermissionDo::getDeptId, allDeptIds));

        return deptMtmPermissionDos.stream()
                .map(OkAuthDeptMtmPermissionDo::getPermissionId)
                .collect(Collectors.toSet());
    }

    /**
     * 递归收集子部门
     * 如果部门的父部门已在集合中，则将该部门也加入集合
     */
    private void findAndAddChildDepts(OkAuthDeptTreeVo deptTree, Set<Long> allDeptIds) {
        if (allDeptIds.contains(deptTree.getParentId())) {
            allDeptIds.add(deptTree.getId());
        }

        // 递归处理子部门
        if (CollUtil.isNotEmpty(deptTree.getChildren())) {
            for (OkAuthDeptTreeVo child : deptTree.getChildren()) {
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
        List<OkAuthUserMtmPermissionDo> userMtmPermissionDos = userMtmPermissionMapper.selectListByQuery(
                QueryWrapper.create().eq(OkAuthUserMtmPermissionDo::getUserId, userId));

        if (userMtmPermissionDos.isEmpty()) {
            return new HashSet<>();
        }

        return userMtmPermissionDos.stream()
                .map(OkAuthUserMtmPermissionDo::getPermissionId)
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
        List<OkAuthUserMtmRoleDo> userMtmRoleDos = userMtmRoleMapper.selectListByQuery(
                QueryWrapper.create().eq(OkAuthUserMtmRoleDo::getUserId, userId));

        if (userMtmRoleDos.isEmpty()) {
            return new HashSet<>();
        }

        // 角色ID不重复列表
        Set<Long> roleIds = userMtmRoleDos.stream()
                .map(OkAuthUserMtmRoleDo::getRoleId)
                .collect(Collectors.toSet());

        // 查询角色关联所有权限
        List<OkAuthRoleMtmPermissionDo> roleMtmPermissionDos = roleMtmPermissionMapper.selectListByQuery(
                QueryWrapper.create().in(OkAuthRoleMtmPermissionDo::getRoleId, roleIds));

        if (roleMtmPermissionDos.isEmpty()) {
            return new HashSet<>();
        }

        return roleMtmPermissionDos.stream()
                .map(OkAuthRoleMtmPermissionDo::getPermissionId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<OkAuthRoleDo> listRole(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");

        // 用户关联角色
        List<OkAuthUserMtmRoleDo> userMtmRoleDos = userMtmRoleMapper.selectListByQuery(
                QueryWrapper.create().eq(OkAuthUserMtmRoleDo::getUserId, userId));

        if (userMtmRoleDos.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询角色
        Set<Long> roleIds = userMtmRoleDos.stream()
                .map(OkAuthUserMtmRoleDo::getRoleId)
                .collect(Collectors.toSet());
        return roleMapper.selectListByQuery(QueryWrapper.create().in(OkAuthRoleDo::getId, roleIds));
    }

    @Override
    public List<OkAuthDeptTreeVo> listDeptTree() {
        // 尝试从缓存中查找
        String key = KeyBuilderUtil.of("okauth").add("deptTree").build("all");
        String cacheStr = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(cacheStr)) {
            return JacksonUtil.readValue(cacheStr, new TypeReference<>() {
            });
        }

        // 查询所有部门
        List<OkAuthDeptDo> deptDos = deptMapper.selectAll();
        if (deptDos.isEmpty()) {
            return new ArrayList<>();
        }

        // 全部对象转换为vo
        List<OkAuthDeptTreeVo> deptVos = new ArrayList<>();
        for (OkAuthDeptDo deptDo : deptDos) {
            OkAuthDeptTreeVo deptVo = new OkAuthDeptTreeVo();
            BeanUtil.copyProperties(deptDo, deptVo);
            deptVo.setChildren(new ArrayList<>());
            deptVos.add(deptVo);
        }

        // 创建Map方便查找
        Map<Long, OkAuthDeptTreeVo> deptVoMap = new HashMap<>();
        for (OkAuthDeptTreeVo deptVo : deptVos) {
            deptVoMap.put(deptVo.getId(), deptVo);
        }

        // 检测循环依赖
        for (OkAuthDeptTreeVo deptVo : deptVos) {
            Set<Long> path = new HashSet<>();
            Long currentId = deptVo.getId();

            // 沿着parentId链向上查找，直到找到根节点或发现循环
            while (currentId != null && deptVoMap.containsKey(currentId)) {
                if (!path.add(currentId)) {  // add返回false说明已存在，即发现循环
                    throw ExceptionUtil.wrapRuntimeException("部门[{}]存在循环依赖", currentId);
                }
                OkAuthDeptTreeVo current = deptVoMap.get(currentId);
                currentId = (current.getParentId() == 0) ? null : current.getParentId();
            }
        }

        // 构建树关系
        List<OkAuthDeptTreeVo> roots = new ArrayList<>();
        for (OkAuthDeptTreeVo deptVo : deptVos) {
            if (deptVo.getParentId() == 0) {
                // 找到根节点
                roots.add(deptVo);
            } else {
                // 找到父节点，把当前节点加入父节点的children
                OkAuthDeptTreeVo parent = deptVoMap.get(deptVo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(deptVo);
                }
            }
        }

        stringRedisTemplate.opsForValue().set(key, JacksonUtil.writeValueAsString(roots), Duration.ofHours(1));
        return roots;
    }

    @Override
    public List<OkAuthPermissionTreeVo> listPermissionTree() {
        // 尝试从缓存中查找
        String key = KeyBuilderUtil.of("okauth").add("permissionTree").build("all");
        String cacheStr = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(cacheStr)) {
            return JacksonUtil.readValue(cacheStr, new TypeReference<>() {
            });
        }

        // 查询所有权限
        List<OkAuthPermissionDo> permissionDos = permissionMapper.selectAll();
        if (permissionDos.isEmpty()) {
            return new ArrayList<>();
        }

        // 全部对象转为vo
        ArrayList<OkAuthPermissionTreeVo> permissionVos = new ArrayList<>();
        for (OkAuthPermissionDo permissionDo : permissionDos) {
            OkAuthPermissionTreeVo permissionVo = new OkAuthPermissionTreeVo();
            BeanUtil.copyProperties(permissionDo, permissionVo);
            permissionVo.setChildren(new ArrayList<>());
            permissionVos.add(permissionVo);
        }

        // 创建Map方便查找
        Map<Long, OkAuthPermissionTreeVo> permissionVoMap = new HashMap<>();
        for (OkAuthPermissionTreeVo permissionVo : permissionVos) {
            permissionVoMap.put(permissionVo.getId(), permissionVo);
        }

        // 检测循环依赖
        for (OkAuthPermissionTreeVo permissionVo : permissionVos) {
            Set<Long> path = new HashSet<>();
            Long currentId = permissionVo.getId();

            // 沿着parentId链向上查找，直到找到根节点或发现循环
            while (currentId != null && permissionVoMap.containsKey(currentId)) {
                if (!path.add(currentId)) {  // add返回false说明已存在，即发现循环
                    throw ExceptionUtil.wrapRuntimeException("权限[{}]存在循环依赖", currentId);
                }
                OkAuthPermissionTreeVo current = permissionVoMap.get(currentId);
                currentId = (current.getParentId() == 0) ? null : current.getParentId();
            }
        }

        // 构建树关系
        List<OkAuthPermissionTreeVo> roots = new ArrayList<>();
        for (OkAuthPermissionTreeVo permissionVo : permissionVos) {
            if (permissionVo.getParentId() == 0) {
                roots.add(permissionVo);
            } else {
                OkAuthPermissionTreeVo parent = permissionVoMap.get(permissionVo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(permissionVo);
                }
            }
        }

        stringRedisTemplate.opsForValue().set(key, JacksonUtil.writeValueAsString(roots), Duration.ofHours(1));
        return roots;
    }
}
