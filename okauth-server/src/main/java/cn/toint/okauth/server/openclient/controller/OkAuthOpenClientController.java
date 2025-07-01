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

package cn.toint.okauth.server.openclient.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.toint.okauth.server.model.Response;
import cn.toint.okauth.server.openclient.model.OkAuthOpenClientDo;
import cn.toint.okauth.server.openclient.model.OkAuthOpenClientSaveRequest;
import cn.toint.okauth.server.openclient.model.OkAuthOpenClientUpdateRequest;
import cn.toint.okauth.server.openclient.service.OkAuthOpenClientService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 开放应用
 *
 * @author Toint
 * @date 2025-06-27
 */
@RestController
public class OkAuthOpenClientController {
    @Resource
    private OkAuthOpenClientService okAuthOpenClientService;

    /**
     * 添加开放应用
     */
    @PostMapping("/openClient/save")
    @SaCheckPermission(value = "openClient:save", orRole = "admin")
    public Response<Void> save(@RequestBody OkAuthOpenClientSaveRequest res) {
        okAuthOpenClientService.save(res);
        return Response.success();
    }

    /**
     * 修改开放应用
     */
    @PostMapping("/openClient/update")
    @SaCheckPermission(value = "openClient:update", orRole = "admin")
    public Response<Void> update(@RequestBody OkAuthOpenClientUpdateRequest res) {
        okAuthOpenClientService.update(res);
        return Response.success();
    }

    /**
     * 查询开放应用列表
     */
    @PostMapping("/openClient/listAll")
    @SaCheckPermission(value = "openClient:listAll", orRole = "admin")
    public Response<List<OkAuthOpenClientDo>> listAll() {
        List<OkAuthOpenClientDo> openClientDos = okAuthOpenClientService.listAll();
        return Response.success(openClientDos);
    }
}
