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
import java.util.Set;

/**
 * 菜单权限
 *
 * @author Toint
 * @date 2025/6/29
 */
public interface PermissionService {
    /**
     * 根据用户查询所有权限
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    Set<String> listPermissionCode(Long userId);

    /**
     * 全量查询权限树
     * 有缓存
     */
    List<PermissionTreeResponse> listPermissionTree();

    /**
     * 根据用户查询所有角色
     */
    List<RoleDo> listRole(Long userId);

    /**
     * 全量查询部门树
     * 有缓存
     */
    List<DeptTreeResponse> listDeptTree();

    PermissionDo getById(Long id);

    void create(PermissionCreateRequest request);

    void updatePermission(PermissionUpdateRequest request);

    void deletePermission(Long id);
}
