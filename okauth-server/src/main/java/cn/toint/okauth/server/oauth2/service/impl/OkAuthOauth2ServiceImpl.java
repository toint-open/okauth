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

import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.consts.SaOAuth2Consts;
import cn.dev33.satoken.oauth2.data.model.CodeModel;
import cn.dev33.satoken.oauth2.data.model.request.RequestAuthModel;
import cn.toint.okauth.server.oauth2.model.OkAuthOauth2GetAuthorizeUrlRequest;
import cn.toint.okauth.server.oauth2.model.OkAuthOauth2GetAuthorizeUrlResponse;
import cn.toint.okauth.server.oauth2.model.OkAuthOauth2LoginByPasswordRequest;
import cn.toint.okauth.server.oauth2.model.OkAuthOauth2LoginResponse;
import cn.toint.okauth.server.oauth2.service.OkAuthOauth2Service;
import cn.toint.okauth.server.openclient.model.OkAuthOpenClientDo;
import cn.toint.okauth.server.openclient.service.OkAuthOpenClientService;
import cn.toint.okauth.server.user.service.OkAuthUserService;
import cn.toint.oktool.util.Assert;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.net.url.UrlBuilder;
import org.dromara.hutool.core.net.url.UrlEncoder;
import org.dromara.hutool.core.net.url.UrlQuery;
import org.springframework.stereotype.Service;

/**
 * @author Toint
 * @date 2025/7/1
 */
@Service
@Slf4j
public class OkAuthOauth2ServiceImpl implements OkAuthOauth2Service {
    @Resource
    private OkAuthOpenClientService openClientService;

    @Resource
    private OkAuthUserService userService;

    @Override
    public OkAuthOauth2GetAuthorizeUrlResponse getAuthorizeUrl(OkAuthOauth2GetAuthorizeUrlRequest request) {
        // 参数校验
        Assert.notNull(openClientService, "请求参数不能为空");
        Assert.validate(request);

        String responseType = request.getResponseType();
        Long clientId = request.getClientId();
        String redirectUri = request.getRedirectUri();

        // 校验授权
        Assert.equals(responseType, SaOAuth2Consts.ResponseType.code, "不支持的responseType");

        // 校验回调地址
        SaOAuth2Manager.getTemplate().checkRedirectUri(String.valueOf(clientId), redirectUri);

        // 获取客户端信息
        OkAuthOpenClientDo openClientDo = openClientService.getById(request.getClientId());
        Assert.notNull(openClientDo, "开放应用不存在");

        // 构建授权地址
        // redirect_uri=http://127.0.0.1:5500/client.html&scope=userinfo,userid,openid,unionid,oidc
        UrlQuery urlQuery = UrlQuery.of()
                .add("response_type", request.getResponseType())
                .add("client_id", request.getClientId())
                .add("redirect_uri", UrlEncoder.encodeAll(request.getRedirectUri()))
                .add("scope", request.getScope())
                .add("state", request.getState());

        String authorizeUrl = UrlBuilder.of(openClientDo.getAuthorizeUrl())
                .setQuery(urlQuery)
                .toString();

        OkAuthOauth2GetAuthorizeUrlResponse response = new OkAuthOauth2GetAuthorizeUrlResponse();
        response.setAuthorizeUrl(authorizeUrl);
        return response;
    }

    @Override
    public OkAuthOauth2LoginResponse login(OkAuthOauth2LoginByPasswordRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 登录账号
        userService.login(request);

        // 获取code
        RequestAuthModel requestAuthModel = BeanUtil.copyProperties(request, new RequestAuthModel());
        CodeModel codeModel = SaOAuth2Manager.getDataGenerate().generateCode(requestAuthModel);

        OkAuthOauth2LoginResponse oauth2LoginResponse = new OkAuthOauth2LoginResponse();
        oauth2LoginResponse.setCode(codeModel.getCode());
        return oauth2LoginResponse;
    }
}
