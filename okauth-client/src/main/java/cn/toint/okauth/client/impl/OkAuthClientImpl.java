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
import cn.toint.okauth.client.OkAuthClientConfig;
import cn.toint.okauth.client.model.*;
import cn.toint.okauth.client.util.OkAuthHttpUtil;
import cn.toint.oktool.model.Response;
import cn.toint.oktool.util.Assert;
import cn.toint.oktool.util.JacksonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.dromara.hutool.core.data.id.IdUtil;
import org.dromara.hutool.core.net.url.UrlBuilder;
import org.dromara.hutool.core.net.url.UrlQuery;
import org.dromara.hutool.http.HttpUtil;
import org.dromara.hutool.http.client.Request;
import org.dromara.hutool.http.meta.HeaderName;
import org.dromara.hutool.http.meta.Method;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Toint
 * @date 2025/7/2
 */
public class OkAuthClientImpl implements OkAuthClient {
    /**
     * 配置信息
     */
    private OkAuthClientConfig okAuthClientConfig;

    public OkAuthClientImpl(OkAuthClientConfig okAuthClientConfig) {
        Assert.notNull(okAuthClientConfig, "okAuthConfig不能为空");
        Assert.validate(okAuthClientConfig);
        this.okAuthClientConfig = okAuthClientConfig;
    }

    @Override
    public OkAuthClientConfig getConfig() {
        return okAuthClientConfig;
    }

    @Override
    public Oauth2BuildAuthorizeUriResponse buildAuthorizeUri(Oauth2BuildAuthorizeUriRequest request) {
        // 参数校验
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 构建授权地址
        // redirect_uri=http://127.0.0.1:5500/client.html&scope=userinfo,userid,openid,unionid,oidc
        String state = Optional.ofNullable(request.getState()).orElse(IdUtil.fastSimpleUUID());
        UrlQuery urlQuery = UrlQuery.of()
                .add("response_type", request.getResponseType())
                .add("client_id", okAuthClientConfig.getClientId())
                .add("redirect_uri", okAuthClientConfig.getRedirectUri())
                .add("scope", request.getScope())
                .add("state", state);

        String authorizeUrl = UrlBuilder.of(okAuthClientConfig.getAuthorizeUri())
                .setQuery(urlQuery)
                .toString();

        Oauth2BuildAuthorizeUriResponse response = new Oauth2BuildAuthorizeUriResponse();
        response.setAuthorizeUri(authorizeUrl);
        return response;
    }

    @Override
    public Oauth2TokenResponse token(Oauth2TokenRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 请求认证服务端
        Request tokenRequest = HttpUtil.createPost(okAuthClientConfig.getServerUri() + "/oauth2/token")
                .body(JacksonUtil.writeValueAsString(request));
        Response<Oauth2TokenResponse> response = JacksonUtil.readValue(OkAuthHttpUtil.request(tokenRequest), new TypeReference<>() {
        });
        Assert.isTrue(Objects.equals(response.getCode(), 0), response.getMsg());
        return response.getData();
    }

    @Override
    public Oauth2TokenResponse refresh(Oauth2RefreshRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 请求认证服务端
        Request tokenRequest = HttpUtil.createPost(okAuthClientConfig.getServerUri() + "/oauth2/refresh")
                .body(JacksonUtil.writeValueAsString(request));
        Response<Oauth2TokenResponse> response = JacksonUtil.readValue(OkAuthHttpUtil.request(tokenRequest), new TypeReference<>() {
        });
        Assert.isTrue(Objects.equals(response.getCode(), 0), response.getMsg());
        return response.getData();
    }

    @Override
    public Oauth2UserInfoResponse userInfo(Oauth2UserInfoRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 请求认证服务端
        Request tokenRequest = HttpUtil.createPost(okAuthClientConfig.getServerUri() + "/oauth2/userInfo")
                .body(JacksonUtil.writeValueAsString(request));
        Response<Oauth2UserInfoResponse> response = JacksonUtil.readValue(OkAuthHttpUtil.request(tokenRequest), new TypeReference<>() {
        });
        Assert.isTrue(Objects.equals(response.getCode(), 0), response.getMsg());
        return response.getData();
    }

    @Override
    public Oauth2LoginResponse login(Oauth2LoginByPasswordRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        Request httpRequest = Request.of(okAuthClientConfig.getServerUri() + "/user/login/password")
                .method(Method.POST)
                .body(JacksonUtil.writeValueAsString(request));
        String resBodyStr = OkAuthHttpUtil.request(httpRequest);
        Response<Oauth2LoginResponse> response = JacksonUtil.readValue(resBodyStr, new TypeReference<>() {
        });
        Assert.isTrue(Objects.equals(response.getCode(), 0), response.getMsg());
        return response.getData();
    }

    @Override
    public Oauth2AuthorizeResponse authorize(String token, Oauth2AuthorizeRequest request) {
        Assert.notBlank(token, "token不能为空");
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        Request httpRequest = Request.of(okAuthClientConfig.getServerUri() + "/oauth2/authorize")
                .method(Method.POST)
                .header(HeaderName.AUTHORIZATION, "Bearer " + token)
                .body(JacksonUtil.writeValueAsString(request));
        String resBodyStr = OkAuthHttpUtil.request(httpRequest);
        Response<Oauth2AuthorizeResponse> codeResponse = JacksonUtil.readValue(resBodyStr, new TypeReference<>() {
        });
        Assert.isTrue(Objects.equals(codeResponse.getCode(), 0), codeResponse.getMsg());
        return codeResponse.getData();
    }
}
