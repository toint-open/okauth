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

import cn.toint.okauth.permission.model.RoleCreateRequest;
import cn.toint.okauth.permission.model.RoleDo;
import cn.toint.okauth.permission.model.RoleUpdateRequest;

import java.util.List;

public interface RoleService {

    List<RoleDo> listByUserId(Long userId);

    boolean hasById(Long id);

    RoleDo getById(Long id);

    void create(RoleCreateRequest request);

    void update(RoleUpdateRequest request);

    void delete(List<Long> ids);

    void bind(Long roleId, List<Long> userIds);

    List<RoleDo> listAll();

    RoleDo getByCode(String code);

    /**
     * 根据角色ID查询对应用户ID集合
     *
     * @param roleId 角色ID
     * @return 用户ID集合 (非null)
     */
    List<Long> listUserIdByRoleId(Long roleId);
}
