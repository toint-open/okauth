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

package cn.toint.okauth.server.oauth2.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.toint.okauth.server.model.OkAuthResponse;
import cn.toint.okauth.server.oauth2.model.*;
import cn.toint.okauth.server.oauth2.service.OkAuthOauth2Service;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OkAuthOauth2Controller {

    @Resource
    private OkAuthOauth2Service oauth2Service;

    /**
     * 账号密码登录
     */
    @PostMapping("/oauth2/loginByPassword")
    @SaIgnore
    public OkAuthResponse<OkAuthOauth2LoginByPasswordResponse> loginByPassword(@RequestBody OkAuthOauth2LoginByPasswordRequest request) {
        OkAuthOauth2LoginByPasswordResponse response = oauth2Service.login(request);
        return OkAuthResponse.success(response);
    }


    /**
     * code换accessToken
     */
    @SaIgnore
    @PostMapping("/oauth2/token")
    public OkAuthResponse<OkAuthOauth2TokenResponse> token(@RequestBody OkAuthOauth2TokenRequest request) {
        OkAuthOauth2TokenResponse response = oauth2Service.token(request);
        return OkAuthResponse.success(response);
    }

    /**
     * Refresh-Token刷新 Access-Token
     */
    @SaIgnore
    @RequestMapping("/oauth2/refresh")
    public OkAuthResponse<OkAuthOauth2TokenResponse> refresh(@RequestBody OkAuthOauth2RefreshRequest request) {
        OkAuthOauth2TokenResponse response = oauth2Service.refresh(request);
        return OkAuthResponse.success(response);
    }
//
//    // 回收 Access-Token
//    @RequestMapping("/oauth2/revoke")
//    @SaIgnore
//    public Object revoke() {
//        return SaOAuth2ServerProcessor.instance.revoke();
//    }
//
//    // 模式四：凭证式
//    @SaIgnore
//    @RequestMapping("/oauth2/client_token")
//    public Object clientToken() {
//        return SaOAuth2ServerProcessor.instance.clientToken();
//    }
}