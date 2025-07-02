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

package cn.toint.okauth.server.user.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.toint.okauth.server.model.OkAuthResponse;
import cn.toint.okauth.server.user.model.OkAuthAdminLoginByPasswordRequest;
import cn.toint.okauth.server.user.model.OkAuthAdminLoginResponse;
import cn.toint.okauth.server.user.service.OkAuthAdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 超管
 *
 * @author Toint
 * @date 2025/6/30
 */
@RestController
public class OkAuthAdminController {

    @Resource
    private OkAuthAdminService okAuthAdminService;

    /**
     * 超管登录
     */
    @PostMapping("/admin/login")
    @SaIgnore
    public OkAuthResponse<OkAuthAdminLoginResponse> login(@RequestBody OkAuthAdminLoginByPasswordRequest request) {
        OkAuthAdminLoginResponse loginVo = okAuthAdminService.login(request);
        return OkAuthResponse.success(loginVo);
    }
}
