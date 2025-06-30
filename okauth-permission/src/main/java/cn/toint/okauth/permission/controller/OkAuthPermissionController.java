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

package cn.toint.okauth.permission.controller;

import cn.toint.okauth.permission.service.OkAuthPermissionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限
 *
 * @author Toint
 * @date 2025/6/30
 */
@RestController
public class OkAuthPermissionController {
    @Resource
    private OkAuthPermissionService permissionService;

//    /**
//     * 列表查询角色
//     */
//    @PostMapping("/role/list")
//    public ResponseVo<List<OkAuthRoleDo>> listRole() {
//        List<OkAuthRoleDo> roleDos = permissionService.listRole(StpUtil.getLoginIdAsLong());
//        return ResponseVo.success(roleDos);
//    }
//
//    /**
//     * 列表查询角色-admin
//     */
//    @PostMapping("/role/list/admin")
//    @SaCheckPermission("role:list")
//    public ResponseVo<List<OkAuthRoleDo>> listRoleAdmin() {
//        List<OkAuthRoleDo> roleDos = permissionService.listRole();
//        return ResponseVo.success(roleDos);
//    }

}
