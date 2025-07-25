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
 * See the License for the specific language governing roles and
 * limitations under the License.
 */

package cn.toint.okauth.permission.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.toint.okauth.permission.constant.OkAuthPermissionConstant;
import cn.toint.okauth.permission.model.*;
import cn.toint.okauth.permission.service.RoleService;
import cn.toint.oktool.model.Response;
import cn.toint.oktool.util.Assert;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色
 *
 * @author Toint
 * @date 2025/6/30
 */
@RestController
public class RoleController {

    @Resource
    private RoleService roleService;

    @PostMapping("/role/listAll")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<List<RoleDo>> listAll() {
        List<RoleDo> roleDos = roleService.listAll();
        return Response.success(roleDos);
    }

    @PostMapping("/role/list")
    public Response<List<RoleDo>> list() {
        long userId = StpUtil.getLoginIdAsLong();
        List<RoleDo> roleDos = roleService.listByUserId(userId);
        return Response.success(roleDos);
    }

    @PostMapping("/role/getById")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<RoleDo> getById(@RequestParam("id") Long id) {
        RoleDo roleDo = roleService.getById(id);
        return Response.success(roleDo);
    }

    @PostMapping("/role/create")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<Void> create(@RequestBody RoleCreateRequest request) {
        roleService.create(request);
        return Response.success();
    }

    @PostMapping("/role/update")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<Void> update(@RequestBody RoleUpdateRequest request) {
        roleService.update(request);
        return Response.success();
    }

    @PostMapping("/role/delete")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<Void> delete(@RequestBody RoleDeleteRequest request) {
        roleService.delete(request.getIds());
        return Response.success();
    }

    /**
     * 全量覆盖模式
     */
    @PostMapping("/role/bind")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<Void> bind(@RequestBody RoleBindRequest request) {
        Assert.validate(request);
        roleService.bind(request.getRoleId(), request.getUserIds());
        return Response.success();
    }

    /**
     * 根据角色ID查询对应用户ID集合
     */
    @PostMapping("/role/listUserId")
    @SaCheckRole(OkAuthPermissionConstant.Role.ADMIN)
    public Response<List<Long>> listUserIdByRoleId(@RequestParam("roleId") Long roleId) {
        List<Long> userIds = roleService.listUserIdByRoleId(roleId);
        return Response.success(userIds);
    }
}
