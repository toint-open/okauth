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
import cn.toint.okauth.server.model.Response;
import cn.toint.okauth.server.oauth2.model.OkAuthOauth2GetAuthorizeUrlRequest;
import cn.toint.okauth.server.oauth2.model.OkAuthOauth2GetAuthorizeUrlResponse;
import cn.toint.okauth.server.oauth2.model.OkAuthOauth2LoginByPasswordRequest;
import cn.toint.okauth.server.oauth2.model.OkAuthOauth2LoginResponse;
import cn.toint.okauth.server.oauth2.service.OkAuthOauth2Service;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OkAuthOauth2Controller {

    @Resource
    private OkAuthOauth2Service oauth2Service;

    /**
     * 获取授权链接
     */
    @PostMapping("/oauth2/getAuthorizeUrl")
    @SaIgnore
    public Response<OkAuthOauth2GetAuthorizeUrlResponse> getAuthorizeUrl(@RequestBody OkAuthOauth2GetAuthorizeUrlRequest request) {
        OkAuthOauth2GetAuthorizeUrlResponse res = oauth2Service.getAuthorizeUrl(request);
        return Response.success(res);
    }

    /**
     * 账号密码登录
     */
    @PostMapping("/oauth2/loginByPassword")
    @SaIgnore
    public Response<OkAuthOauth2LoginResponse> loginByPassword(@RequestBody OkAuthOauth2LoginByPasswordRequest request) {
        OkAuthOauth2LoginResponse response = oauth2Service.login(request);
        return Response.success(response);
    }

//    // 模式一：Code授权码 || 模式二：隐藏式
//    @RequestMapping("/oauth2/authorize")
//    @SaIgnore
//    public Object authorize() {
//        return SaOAuth2ServerProcessor.instance.authorize();
//    }
//
//    // 用户登录
//    @RequestMapping("/oauth2/doLogin")
//    @SaIgnore
//    public Object doLogin() {
//        return SaOAuth2ServerProcessor.instance.doLogin();
//    }
//
//    // 用户确认授权
//    @SaIgnore
//    @RequestMapping("/oauth2/doConfirm")
//    public Object doConfirm() {
//        return SaOAuth2ServerProcessor.instance.doConfirm();
//    }
//
//    // Code 换 Access-Token || 模式三：密码式
//    @SaIgnore
//    @RequestMapping("/oauth2/token")
//    public Object token() {
//        return SaOAuth2ServerProcessor.instance.token();
//    }
//
//    // Refresh-Token 刷新 Access-Token
//    @SaIgnore
//    @RequestMapping("/oauth2/refresh")
//    public Object refresh() {
//        return SaOAuth2ServerProcessor.instance.refresh();
//    }
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