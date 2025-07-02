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

package cn.toint.okauth.client.impl;

import cn.toint.okauth.client.OkAuthClient;
import cn.toint.okauth.client.OkAuthConfig;
import cn.toint.okauth.client.model.*;
import cn.toint.okauth.client.util.OkAuthHttpUtil;
import cn.toint.oktool.util.Assert;
import cn.toint.oktool.util.JacksonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.dromara.hutool.core.data.id.IdUtil;
import org.dromara.hutool.core.net.url.UrlBuilder;
import org.dromara.hutool.core.net.url.UrlQuery;
import org.dromara.hutool.http.HttpUtil;
import org.dromara.hutool.http.client.Request;
import org.dromara.hutool.http.meta.Method;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

/**
 * @author Toint
 * @date 2025/7/2
 */
public class OkAuthClientImpl implements OkAuthClient {
    /**
     * 配置信息
     */
    private OkAuthConfig okAuthConfig;

    public OkAuthClientImpl(OkAuthConfig okAuthConfig) {
        Assert.notNull(okAuthConfig, "okAuthConfig不能为空");
        Assert.validate(okAuthConfig);
        this.okAuthConfig = okAuthConfig;
    }

    @Override
    public OkAuthConfig getConfig() {
        return okAuthConfig;
    }

    @Override
    public OkAuthOauth2BuildAuthorizeUriResponse buildAuthorizeUri(OkAuthOauth2BuildAuthorizeUriRequest request) {
        // 参数校验
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 构建授权地址
        // redirect_uri=http://127.0.0.1:5500/client.html&scope=userinfo,userid,openid,unionid,oidc
        String state = Optional.ofNullable(request.getState()).orElse(IdUtil.fastSimpleUUID());
        UrlQuery urlQuery = UrlQuery.of()
                .add("response_type", request.getResponseType())
                .add("client_id", okAuthConfig.getClientId())
                .add("redirect_uri", okAuthConfig.getRedirectUri())
                .add("scope", request.getScope())
                .add("state", state);

        String authorizeUrl = UrlBuilder.of(okAuthConfig.getAuthorizeUri())
                .setQuery(urlQuery)
                .toString();

        OkAuthOauth2BuildAuthorizeUriResponse response = new OkAuthOauth2BuildAuthorizeUriResponse();
        response.setAuthorizeUri(authorizeUrl);
        return response;
    }

    @Override
    public OkAuthOauth2TokenResponse token(OkAuthOauth2TokenRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 请求认证服务端
        Request tokenRequest = HttpUtil.createPost(okAuthConfig.getServerUri() + "/oauth2/token")
                .body(JacksonUtil.writeValueAsString(request));
        OkAuthResponse<OkAuthOauth2TokenResponse> response = JacksonUtil.readValue(OkAuthHttpUtil.request(tokenRequest), new TypeReference<>() {
        });
        Assert.isTrue(response.isSuccess(), response.getMsg());
        return response.getData();
    }

    @Override
    public OkAuthOauth2TokenResponse refresh(OkAuthOauth2RefreshRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 请求认证服务端
        Request tokenRequest = HttpUtil.createPost(okAuthConfig.getServerUri() + "/oauth2/refresh")
                .body(JacksonUtil.writeValueAsString(request));
        OkAuthResponse<OkAuthOauth2TokenResponse> response = JacksonUtil.readValue(OkAuthHttpUtil.request(tokenRequest), new TypeReference<>() {
        });
        Assert.isTrue(response.isSuccess(), response.getMsg());
        return response.getData();
    }

    @Override
    public OkAuthOauth2UserInfoResponse userInfo(OkAuthOauth2UserInfoRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 请求认证服务端
        Request tokenRequest = HttpUtil.createPost(okAuthConfig.getServerUri() + "/oauth2/userInfo")
                .body(JacksonUtil.writeValueAsString(request));
        OkAuthResponse<OkAuthOauth2UserInfoResponse> response = JacksonUtil.readValue(OkAuthHttpUtil.request(tokenRequest), new TypeReference<>() {
        });
        Assert.isTrue(response.isSuccess(), response.getMsg());
        return response.getData();
    }

    @Override
    public OkAuthUserLoginResponse login(OkAuthUserLoginByPasswordRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        Request httpRequest = Request.of(okAuthConfig.getServerUri() + "/user/login/password")
                .method(Method.POST)
                .body(JacksonUtil.writeValueAsString(request));
        String resBodyStr = OkAuthHttpUtil.request(httpRequest);
        OkAuthResponse<OkAuthUserLoginResponse> okAuthResponse = JacksonUtil.readValue(resBodyStr, new TypeReference<OkAuthResponse<OkAuthUserLoginResponse>>() {
        });
        Assert.isTrue(okAuthResponse.isSuccess(), okAuthResponse.getMsg());
        return okAuthResponse.getData();
    }

    @Override
    public OkAuthOauth2AuthorizeResponse authorize(String token, OkAuthOauth2AuthorizeRequest request) {
        Assert.notBlank(token, "token不能为空");
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        Request httpRequest = Request.of(okAuthConfig.getServerUri() + "/oauth2/authorize")
                .method(Method.POST)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(JacksonUtil.writeValueAsString(request));
        String resBodyStr = OkAuthHttpUtil.request(httpRequest);
        OkAuthResponse<OkAuthOauth2AuthorizeResponse> codeResponse = JacksonUtil.readValue(resBodyStr, new TypeReference<>() {
        });
        Assert.isTrue(codeResponse.isSuccess(), codeResponse.getMsg());
        return codeResponse.getData();
    }
}
