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
import cn.toint.okauth.permission.constant.OkAuthConstant;
import cn.toint.okauth.permission.model.DictCreateRequest;
import cn.toint.okauth.permission.model.DictDo;
import cn.toint.okauth.permission.model.DictUpdateRequest;
import cn.toint.okauth.permission.model.Response;
import cn.toint.okauth.permission.service.DictService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典
 */
@RestController
public class DictController {

    @Resource
    private DictService dictService;

    @PostMapping("/dict/list")
    public Response<List<DictDo>> list() {
        List<DictDo> dictDos = dictService.list();
        return Response.success(dictDos);
    }

    @PostMapping("/dict/create")
    @SaCheckRole(OkAuthConstant.Role.ROLE_ADMIN)
    public Response<Void> create(@RequestBody DictCreateRequest request) {
        dictService.create(request);
        return Response.success();
    }

    @PostMapping("/dict/update")
    @SaCheckRole(OkAuthConstant.Role.ROLE_ADMIN)
    public Response<Void> update(@RequestBody DictUpdateRequest request) {
        dictService.update(request);
        return Response.success();
    }

}
