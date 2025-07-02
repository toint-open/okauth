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

package cn.toint.okauth.demo.clientserver;

import cn.toint.okauth.client.OkAuthClient;
import cn.toint.okauth.client.OkAuthConfig;
import cn.toint.okauth.client.constant.OkAuthConstant;
import cn.toint.okauth.client.impl.OkAuthClientImpl;
import cn.toint.okauth.client.model.*;
import cn.toint.oktool.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.data.id.IdUtil;
import org.dromara.hutool.core.lang.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用端服务
 *
 * @author Toint
 * @date 2025/7/2
 */
@Slf4j
@RestController
public class OkAuthDemoClientServerController {

    private final OkAuthClient okAuthClient = initClient();

    private OkAuthClient initClient() {
        OkAuthConfig okAuthConfig = new OkAuthConfig();
        okAuthConfig.setServerUri("http://127.0.0.1:8080");
        okAuthConfig.setAuthorizeUri("http://127.0.0.1:18080/oauth2/authorize");
        okAuthConfig.setClientId("10000");
        okAuthConfig.setClientSecret("okauth");
        okAuthConfig.setRedirectUri("http://127.0.0.1:18080/client");
        return new OkAuthClientImpl(okAuthConfig);
    }

    private final Map<String, String> stateCache = new ConcurrentHashMap<>();

    /**
     * 1. 获取前端授权地址
     * 2. 跳转前端授权地址
     */
    @GetMapping("/login")
    public RedirectView login() {
        // 缓存state, 等待验证
        String state = IdUtil.fastSimpleUUID();
        stateCache.put(state, "");

        OkAuthOauth2BuildAuthorizeUriRequest request = new OkAuthOauth2BuildAuthorizeUriRequest();
        request.setResponseType(OkAuthConstant.ResponseType.code);
        request.setScope(null);
        request.setState(state);
        OkAuthOauth2BuildAuthorizeUriResponse response = okAuthClient.buildAuthorizeUri(request);
        log.info("授权地址={}", response.getAuthorizeUri());
        return new RedirectView(response.getAuthorizeUri());
    }

    /**
     * 3. 账号登录
     * 4. 访问后端执行授权[authorize]获取带code的回调地址
     */
    @GetMapping("/oauth2/authorize")
    public RedirectView authorize(@RequestParam(required = false) String state) {
        // 账号密码登录
        OkAuthUserLoginByPasswordRequest request = new OkAuthUserLoginByPasswordRequest();
        request.setUsername("admin");
        request.setPassword("okauth-admin");
        OkAuthUserLoginResponse loginResponse = okAuthClient.login(request);
        log.info("密码登录成功, 响应={}", JacksonUtil.writeValueAsString(loginResponse));

        // 访问后端执行授权[authorize]获取带code的回调地址
        OkAuthOauth2AuthorizeRequest codeRequest = new OkAuthOauth2AuthorizeRequest();
        codeRequest.setResponseType(OkAuthConstant.ResponseType.code);
        codeRequest.setClientId("10000");
        codeRequest.setRedirectUri("http://127.0.0.1:18080/client");
        codeRequest.setScope("");
        codeRequest.setState(state);
        OkAuthOauth2AuthorizeResponse codeResponse = okAuthClient.authorize(loginResponse.getToken().getTokenValue(), codeRequest);
        String redirectUri = codeResponse.getRedirectUri();
        log.info("获取code成功, 回调地址={}", redirectUri);
        return new RedirectView(redirectUri);
    }

    /**
     * 5. 拿code换取accessToken
     * 6. 刷新accessToken
     */
    @GetMapping("/client")
    public void client(@RequestParam String code, @RequestParam String state) {
        // 验证state
        Assert.isTrue(stateCache.containsKey(state), "state不匹配");
        stateCache.remove(state);

        //  拿code换取accessToken
        OkAuthConfig okAuthConfig = okAuthClient.getConfig();
        OkAuthOauth2TokenRequest okAuthOauth2TokenRequest = new OkAuthOauth2TokenRequest();
        okAuthOauth2TokenRequest.setClientId(okAuthConfig.getClientId());
        okAuthOauth2TokenRequest.setClientSecret(okAuthConfig.getClientSecret());
        okAuthOauth2TokenRequest.setGrantType(OkAuthConstant.GrantType.AUTHORIZATION_CODE);
        okAuthOauth2TokenRequest.setScope(null);
        okAuthOauth2TokenRequest.setCode(code);
        OkAuthOauth2TokenResponse tokenResponse = okAuthClient.token(okAuthOauth2TokenRequest);
        log.info("获取token成功, token={}", JacksonUtil.writeValueAsString(tokenResponse));

        // 刷新accessToken
        OkAuthOauth2RefreshRequest okAuthOauth2RefreshRequest = new OkAuthOauth2RefreshRequest();
        okAuthOauth2RefreshRequest.setClientId(okAuthConfig.getClientId());
        okAuthOauth2RefreshRequest.setClientSecret(okAuthConfig.getClientSecret());
        okAuthOauth2RefreshRequest.setGrantType(OkAuthConstant.GrantType.REFRESH_TOKEN);
        okAuthOauth2RefreshRequest.setRefreshToken(tokenResponse.getRefreshToken());
        OkAuthOauth2TokenResponse refreshResponse = okAuthClient.refresh(okAuthOauth2RefreshRequest);
        log.info("刷新token成功, token={}", JacksonUtil.writeValueAsString(refreshResponse));

        // 获取userInfo
        OkAuthOauth2UserInfoRequest userInfoRequest = new OkAuthOauth2UserInfoRequest();
        userInfoRequest.setAccessToken(refreshResponse.getAccessToken());
        OkAuthOauth2UserInfoResponse okAuthOauth2UserInfoResponse = okAuthClient.userInfo(userInfoRequest);
        log.info("获取userInfo成功, userInfo={}", JacksonUtil.writeValueAsString(okAuthOauth2UserInfoResponse));
    }
}
