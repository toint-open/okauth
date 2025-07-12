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

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.toint.okauth.permission.constant.OkAuthConstant;
import cn.toint.okauth.permission.model.*;
import cn.toint.okauth.permission.service.PermissionService;
import cn.toint.oktool.model.Response;
import cn.toint.oktool.util.Assert;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

//    /**
//     * 查询权限树
//     */
//    @PostMapping("/permission/listAllTree")
//    public Response<List<PermissionTreeResponse>> listAllTree() {
//        long userId = StpUtil.getLoginIdAsLong();
//        List<PermissionTreeResponse> permissionTreeResponses = permissionService.listAllTree();
//        return Response.success(permissionTreeResponses);
//    }

    /**
     * 查询权限树
     */
    @PostMapping("/permission/listTree")
    public Response<PermissionTreeResponse> listTree() {
        long userId = StpUtil.getLoginIdAsLong();
        PermissionTreeResponse permissionTreeResponse = permissionService.listTree(userId);
        return Response.success(permissionTreeResponse);
    }

    /**
     * 查询权限
     */
    @PostMapping("/permission/getById")
    @SaCheckRole(OkAuthConstant.Role.ADMIN)
    public Response<PermissionDo> getById(@RequestParam("id") Long id) {
        PermissionDo permissionDo = permissionService.getById(id);
        return Response.success(permissionDo);
    }

    /**
     * 添加权限
     */
    @PostMapping("/permission/create")
    @SaCheckRole(OkAuthConstant.Role.ADMIN)
    public Response<Void> create(@RequestBody PermissionCreateRequest request) {
        permissionService.create(request);
        return Response.success();
    }

    /**
     * 修改权限
     */
    @PostMapping("/permission/update")
    @SaCheckRole(OkAuthConstant.Role.ADMIN)
    public Response<Void> update(@RequestBody PermissionUpdateRequest request) {
        permissionService.update(request);
        return Response.success();
    }

    /**
     * 删除权限
     */
    @PostMapping("/permission/delete")
    @SaCheckRole(OkAuthConstant.Role.ADMIN)
    public Response<Void> delete(@RequestBody PermissionDeleteRequest request) {
        permissionService.delete(request.getIds());
        return Response.success();
    }

    /**
     * 角色绑定权限
     */
    @PostMapping("/permission/bind")
    @SaCheckRole(OkAuthConstant.Role.ADMIN)
    public Response<Void> bind(@RequestBody PermissionBindRequest request) {
        Assert.validate(request);
        permissionService.bind(request.getRoleId(), request.getPermissionIds());
        return Response.success();
    }

    /**
     * 角色绑定权限
     */
    @PostMapping("/permission/unbind")
    @SaCheckRole(OkAuthConstant.Role.ADMIN)
    public Response<Void> unbind(@RequestBody PermissionBindRequest request) {
        Assert.validate(request);
        permissionService.unbind(request.getRoleId(), request.getPermissionIds());
        return Response.success();
    }
}
