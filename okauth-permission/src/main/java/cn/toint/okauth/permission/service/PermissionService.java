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

package cn.toint.okauth.permission.service;

import cn.toint.okauth.permission.model.*;

import java.util.List;

/**
 * 菜单权限
 *
 * @author Toint
 * @date 2025/6/29
 */
public interface PermissionService {
    /**
     * 根据用户查询所有权限
     * 内置缓存功能
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    List<PermissionDo> listByUserId(Long userId);

    /**
     * 查询权限树
     * 内置缓存功能
     */
    List<PermissionTreeResponse> listTreeByUserId(Long userId);

    /**
     * 查询权限
     *
     * @param id 权限ID
     * @return 权限对象
     */
    PermissionDo getById(Long id);

    /**
     * 添加权限
     */
    void create(PermissionCreateRequest request);

    /**
     * 修改权限
     */
    void update(PermissionUpdateRequest request);

    /**
     * 删除权限
     */
    void delete(List<Long> ids);

    /**
     * 是否存在权限
     *
     * @param id 权限ID
     * @return 是否存在
     */
    boolean hasById(Long id);

    /**
     * 角色绑定权限
     */
    void bind(Long roleId, List<Long> permissionIds);

    /**
     * 根据角色ID查询对应权限
     * 内置缓存处理能力
     *
     * @param roleId 角色ID
     * @return 权限集合 (非null)
     */
    List<PermissionDo> listByRoleId(Long roleId);
}
