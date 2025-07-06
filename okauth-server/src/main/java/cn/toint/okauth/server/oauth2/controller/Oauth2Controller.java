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
import cn.dev33.satoken.stp.StpUtil;
import cn.toint.okauth.server.model.Response;
import cn.toint.okauth.server.oauth2.model.*;
import cn.toint.okauth.server.oauth2.service.Oauth2Service;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Oauth2Controller {

    @Resource
    private Oauth2Service oauth2Service;

    /**
     * 授权认证
     * 访问本接口用户必须已登录
     */
    @PostMapping("/oauth2/authorize")
    public Response<Oauth2AuthorizeResponse> authorize(@RequestBody Oauth2AuthorizeRequest request) {
        Oauth2AuthorizeResponse response = oauth2Service.authorize(StpUtil.getLoginIdAsLong(), request);
        return Response.success(response);
    }

    /**
     * 获取accessToken
     */
    @SaIgnore
    @PostMapping("/oauth2/token")
    public Response<Oauth2TokenResponse> token(@RequestBody Oauth2TokenRequest request) {
        Oauth2TokenResponse response = oauth2Service.token(request);
        return Response.success(response);
    }

    /**
     * 刷新accessToken
     */
    @SaIgnore
    @PostMapping("/oauth2/refresh")
    public Response<Oauth2TokenResponse> refresh(@RequestBody Oauth2RefreshRequest request) {
        Oauth2TokenResponse response = oauth2Service.refresh(request);
        return Response.success(response);
    }

    /**
     * 获取用户信息
     */
    @SaIgnore
    @PostMapping("/oauth2/userInfo")
    public Response<Oauth2UserInfoResponse> userInfo(@RequestBody Oauth2UserInfoRequest request) {
        Oauth2UserInfoResponse oauth2UserInfoResponse = oauth2Service.userInfo(request);
        return Response.success(oauth2UserInfoResponse);
    }
}