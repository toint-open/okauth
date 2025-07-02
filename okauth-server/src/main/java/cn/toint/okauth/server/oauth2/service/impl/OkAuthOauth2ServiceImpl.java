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

package cn.toint.okauth.server.oauth2.service.impl;

import cn.dev33.satoken.oauth2.consts.SaOAuth2Consts;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.data.model.CodeModel;
import cn.dev33.satoken.oauth2.data.model.request.RequestAuthModel;
import cn.toint.okauth.server.oauth2.manager.OkAuthOauth2Manager;
import cn.toint.okauth.server.oauth2.model.*;
import cn.toint.okauth.server.oauth2.service.OkAuthOauth2Service;
import cn.toint.okauth.server.user.model.OkAuthUserLoginResponse;
import cn.toint.okauth.server.user.service.OkAuthUserService;
import cn.toint.oktool.util.Assert;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.data.id.IdUtil;
import org.dromara.hutool.core.text.split.SplitUtil;
import org.springframework.stereotype.Service;

/**
 * @author Toint
 * @date 2025/7/1
 */
@Service
@Slf4j
public class OkAuthOauth2ServiceImpl implements OkAuthOauth2Service {

    @Resource
    private OkAuthUserService okAuthUserService;

    @Resource
    private OkAuthOauth2Manager okAuthOauth2Manager;

    @Override
    public OkAuthOauth2LoginByPasswordResponse login(OkAuthOauth2LoginByPasswordRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        String responseType = request.getResponseType();
        String clientId = request.getClientId();
        String redirectUri = request.getRedirectUri();
        String state = request.getState();

        // 校验授权方式
        Assert.equals(responseType, SaOAuth2Consts.ResponseType.code, "不支持的responseType");
        // 校验客户端
        okAuthOauth2Manager.getSaOAuth2Template().checkClientModel(clientId);
        // 校验回调地址
        okAuthOauth2Manager.getSaOAuth2Template().checkRedirectUri(String.valueOf(clientId), redirectUri);

        // 登录账号
        OkAuthUserLoginResponse loginResponse = okAuthUserService.login(request);
        Long userId = loginResponse.getUser().getId();

        // 获取code并构造回调地址
        RequestAuthModel requestAuthModel = BeanUtil.copyProperties(request, new RequestAuthModel());
        requestAuthModel.setClientId(clientId);
        requestAuthModel.setScopes(SplitUtil.splitTrim(request.getScope(), ","));
        requestAuthModel.setLoginId(userId);
        requestAuthModel.setRedirectUri(redirectUri);
        requestAuthModel.setResponseType(responseType);
        requestAuthModel.setState(state);
        requestAuthModel.setNonce(IdUtil.fastSimpleUUID());
        CodeModel codeModel = okAuthOauth2Manager.getSaOAuth2DataGenerate().generateCode(requestAuthModel);
        String clientRedirectUri = okAuthOauth2Manager.getSaOAuth2DataGenerate()
                .buildRedirectUri(redirectUri, codeModel.getCode(), state);

        OkAuthOauth2LoginByPasswordResponse response = new OkAuthOauth2LoginByPasswordResponse();
        response.setRedirectUri(clientRedirectUri);
        return response;
    }

    @Override
    public OkAuthOauth2TokenResponse token(OkAuthOauth2TokenRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        String clientId = request.getClientId();
        String clientSecret = request.getClientSecret();
        String grantType = request.getGrantType();
        String scope = request.getScope();
        String code = request.getCode();

        // 校验客户端
        okAuthOauth2Manager.getSaOAuth2Template().checkClientModel(clientId);
        // 校验授权方式
        Assert.equals(grantType, "authorization_code", "不支持的grantType");
        // 校验密钥
        okAuthOauth2Manager.getSaOAuth2Template().checkClientSecret(clientId, clientSecret);
        // 校验code
        okAuthOauth2Manager.getSaOAuth2Template().checkCode(code);
        AccessTokenModel accessTokenModel = okAuthOauth2Manager.getSaOAuth2DataGenerate().generateAccessToken(code);
        return BeanUtil.copyProperties(accessTokenModel, new OkAuthOauth2TokenResponse());
    }

    @Override
    public OkAuthOauth2TokenResponse refresh(OkAuthOauth2RefreshRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        String clientId = request.getClientId();
        String clientSecret = request.getClientSecret();
        String grantType = request.getGrantType();
        String refreshToken = request.getRefreshToken();
        
        Assert.equals(grantType, "refresh_token", "不支持的grantType");
        // 校验客户端
        okAuthOauth2Manager.getSaOAuth2Template().checkClientModel(clientId);
        // 校验密钥
        okAuthOauth2Manager.getSaOAuth2Template().checkClientSecret(clientId, clientSecret);
        // 校验token
        okAuthOauth2Manager.getSaOAuth2Template().checkRefreshToken(refreshToken);
        AccessTokenModel accessTokenModel = okAuthOauth2Manager.getSaOAuth2DataGenerate().refreshAccessToken(refreshToken);
        return BeanUtil.copyProperties(accessTokenModel, new OkAuthOauth2TokenResponse());
    }
}
