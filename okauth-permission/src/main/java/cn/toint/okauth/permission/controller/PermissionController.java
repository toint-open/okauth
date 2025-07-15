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
import cn.toint.okauth.permission.constant.OkAuthPermissionConstant;
import cn.toint.okauth.permission.model.*;
import cn.toint.okauth.permission.service.PermissionService;
import cn.toint.oktool.model.Response;
import cn.toint.oktool.util.Assert;
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
    public Response<List<PermissionTreeResponse>> listTree() {
        long userId = StpUtil.getLoginIdAsLong();
        List<PermissionTreeResponse> permissionTreeResponse = permissionService.listTreeByUserId(userId);
        return Response.success(permissionTreeResponse);
    }

    /**
     * 查询权限
     */
    @PostMapping("/permission/getById")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<PermissionDo> getById(@RequestParam("id") Long id) {
        PermissionDo permissionDo = permissionService.getById(id);
        return Response.success(permissionDo);
    }

    /**
     * 添加权限
     */
    @PostMapping("/permission/create")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<Void> create(@RequestBody PermissionCreateRequest request) {
        permissionService.create(request);
        return Response.success();
    }

    /**
     * 修改权限
     */
    @PostMapping("/permission/update")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<Void> update(@RequestBody PermissionUpdateRequest request) {
        permissionService.update(request);
        return Response.success();
    }

    /**
     * 删除权限
     */
    @PostMapping("/permission/delete")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<Void> delete(@RequestBody PermissionDeleteRequest request) {
        permissionService.delete(request.getIds());
        return Response.success();
    }

    /**
     * 角色绑定权限
     */
    @PostMapping("/permission/bind")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<Void> bind(@RequestBody PermissionBindRequest request) {
        Assert.validate(request);
        permissionService.bind(request.getRoleId(), request.getPermissionIds());
        return Response.success();
    }

    /**
     * 查询角色已经绑定的权限ID集合
     */
    @PostMapping("/permission/listId")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<PermissionListIdByRoleIdResponse> listId(@RequestParam("roleId") Long roleId) {
        List<Long> permissionIds = permissionService.listByRoleId(roleId)
                .stream()
                .map(PermissionDo::getId)
                .toList();

        PermissionListIdByRoleIdResponse response = new  PermissionListIdByRoleIdResponse();
        response.setRoleId(roleId);
        response.setPermissionIds(permissionIds);

        return Response.success(response);
    }
}
