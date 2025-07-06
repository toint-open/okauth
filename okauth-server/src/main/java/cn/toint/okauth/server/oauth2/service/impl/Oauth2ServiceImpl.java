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
import cn.toint.okauth.server.constant.OkAuthConstant;
import cn.toint.okauth.server.oauth2.manager.Oauth2Manager;
import cn.toint.okauth.server.oauth2.model.*;
import cn.toint.okauth.server.oauth2.service.Oauth2Service;
import cn.toint.okauth.server.user.model.UserDo;
import cn.toint.okauth.server.user.service.UserService;
import cn.toint.oktool.util.Assert;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.text.split.SplitUtil;
import org.springframework.stereotype.Service;

/**
 * @author Toint
 * @date 2025/7/1
 */
@Service
@Slf4j
public class Oauth2ServiceImpl implements Oauth2Service {
    @Resource
    private Oauth2Manager oauth2Manager;

    @Resource
    private UserService userService;

    @Override
    public Oauth2AuthorizeResponse authorize(Long userId, Oauth2AuthorizeRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        String responseType = request.getResponseType();
        String clientId = request.getClientId();
        String redirectUri = request.getRedirectUri();
        String state = request.getState();

        // 校验授权方式
        Assert.equals(responseType, SaOAuth2Consts.ResponseType.code, "不支持的responseType");
        // 校验客户端
        oauth2Manager.getSaOAuth2Template().checkClientModel(clientId);
        // 校验回调地址
        oauth2Manager.getSaOAuth2Template().checkRedirectUri(String.valueOf(clientId), redirectUri);

        // 获取code
        RequestAuthModel requestAuthModel = new RequestAuthModel();
        requestAuthModel.setClientId(clientId);
        requestAuthModel.setScopes(SplitUtil.splitTrim(request.getScope(), ","));
        requestAuthModel.setLoginId(userId);
        requestAuthModel.setRedirectUri(redirectUri);
        requestAuthModel.setResponseType(responseType);
        requestAuthModel.setState(state);
        CodeModel codeModel = oauth2Manager.getSaOAuth2DataGenerate().generateCode(requestAuthModel);

        // 构造回调地址
        String clientRedirectUri = oauth2Manager.getSaOAuth2DataGenerate()
                .buildRedirectUri(redirectUri, codeModel.getCode(), state);

        // 响应对象
        Oauth2AuthorizeResponse response = new Oauth2AuthorizeResponse();
        response.setRedirectUri(clientRedirectUri);
        return response;
    }

    @Override
    public Oauth2TokenResponse token(Oauth2TokenRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        String clientId = request.getClientId();
        String clientSecret = request.getClientSecret();
        String grantType = request.getGrantType();
        String scope = request.getScope();
        String code = request.getCode();

        // 校验授权方式
        Assert.equals(grantType, OkAuthConstant.GrantType.AUTHORIZATION_CODE, "不支持的grantType");
        // 校验客户端密钥
        oauth2Manager.getSaOAuth2Template().checkClientSecret(clientId, clientSecret);
        // 校验code
        oauth2Manager.getSaOAuth2Template().checkCode(code);
        // 生成token, 每次生成会覆盖原有的
        AccessTokenModel accessTokenModel = oauth2Manager.getSaOAuth2DataGenerate().generateAccessToken(code);
        return BeanUtil.copyProperties(accessTokenModel, new Oauth2TokenResponse());
    }

    @Override
    public Oauth2TokenResponse refresh(Oauth2RefreshRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        String clientId = request.getClientId();
        String clientSecret = request.getClientSecret();
        String grantType = request.getGrantType();
        String refreshToken = request.getRefreshToken();

        Assert.equals(grantType, OkAuthConstant.GrantType.REFRESH_TOKEN, "不支持的grantType");
        // 校验客户端密钥
        oauth2Manager.getSaOAuth2Template().checkClientSecret(clientId, clientSecret);
        // 校验token
        oauth2Manager.getSaOAuth2Template().checkRefreshToken(refreshToken);
        AccessTokenModel accessTokenModel = oauth2Manager.getSaOAuth2DataGenerate().refreshAccessToken(refreshToken);
        return BeanUtil.copyProperties(accessTokenModel, new Oauth2TokenResponse());
    }

    @Override
    public Oauth2UserInfoResponse userInfo(Oauth2UserInfoRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 校验token获取用户信息
        String accessToken = request.getAccessToken();
        AccessTokenModel accessTokenModel = oauth2Manager.getSaOAuth2Template().checkAccessToken(accessToken);
        Long userId = Long.valueOf(accessTokenModel.getLoginId().toString());
        UserDo userDo = userService.getById(userId);

        // 返回部分用户信息给客户端
        Oauth2UserInfoResponse oauth2UserInfoResponse = new Oauth2UserInfoResponse();
        BeanUtil.copyProperties(userDo, oauth2UserInfoResponse);
        return oauth2UserInfoResponse;
    }
}
