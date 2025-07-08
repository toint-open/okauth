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

import cn.dev33.satoken.stp.StpUtil;
import cn.toint.okauth.permission.constant.OkAuthConstant;
import cn.toint.okauth.permission.model.*;
import cn.toint.okauth.permission.service.PermissionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限
 *
 * @author Toint
 * @date 2025/6/30
 */
@RestController
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    /**
     * 查询权限树
     */
    @PostMapping("/permission/listTree")
    public Response<List<PermissionTreeResponse>> listPermissionTree() {
        StpUtil.checkRole(OkAuthConstant.Role.ROLE_ADMIN);
        List<PermissionTreeResponse> permissionTreeResponses = permissionService.listPermissionTree();
        return Response.success(permissionTreeResponses);
    }

    /**
     * 查询权限
     */
    @PostMapping("/permission/getById")
    Response<PermissionDo> getById(@RequestParam("id") Long id) {
        StpUtil.checkRole(OkAuthConstant.Role.ROLE_ADMIN);
        PermissionDo permissionDo = permissionService.getById(id);
        return Response.success(permissionDo);
    }

    /**
     * 添加权限
     */
    @PostMapping("/permission/create")
    Response<Void> createPermissionAdmin(@RequestBody PermissionCreateRequest request) {
        StpUtil.checkRole(OkAuthConstant.Role.ROLE_ADMIN);
        permissionService.create(request);
        return Response.success();
    }

    /**
     * 修改权限
     */
    @PostMapping("/permission/update")
    Response<Void> updatePermission(@RequestBody PermissionUpdateRequest request) {
        StpUtil.checkRole(OkAuthConstant.Role.ROLE_ADMIN);
        permissionService.updatePermission(request);
        return Response.success();
    }

    /**
     * 删除权限
     */
    @PostMapping("/permission/delete")
    Response<Void> deletePermission(@RequestParam("id") Long id) {
        StpUtil.checkRole(OkAuthConstant.Role.ROLE_ADMIN);
        permissionService.deletePermission(id);
        return Response.success();
    }

    /**
     * 查询角色
     */
    @PostMapping("/role/list")
    public Response<List<RoleDo>> listRole() {
        List<RoleDo> roleDos = permissionService.listRole(StpUtil.getLoginIdAsLong());
        return Response.success(roleDos);
    }

//    /**
//     * 列表查询角色-admin
//     */
//    @PostMapping("/role/list/admin")
//    @SaCheckPermission("role:list")
//    public Response<List<RoleDo>> listRoleAdmin() {
//        List<RoleDo> roleDos = permissionService.listRole();
//        return Response.success(roleDos);
//    }

}
