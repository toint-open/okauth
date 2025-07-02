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
import cn.toint.okauth.server.user.model.OkAuthUserLoginByPasswordRequest;
import cn.toint.okauth.server.user.model.OkAuthUserLoginResponse;
import cn.toint.okauth.server.user.service.OkAuthUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Toint
 * @date 2025/6/29
 */
@RestController
public class OkAuthUserController {

    @Resource
    private OkAuthUserService okAuthUserService;

    /**
     * 账号密码登录
     */
    @PostMapping("/user/login/password")
    @SaIgnore
    public OkAuthResponse<OkAuthUserLoginResponse> loginByPassword(@RequestBody OkAuthUserLoginByPasswordRequest request) {
        OkAuthUserLoginResponse response = okAuthUserService.login(request);
        return OkAuthResponse.success(response);
    }
}
