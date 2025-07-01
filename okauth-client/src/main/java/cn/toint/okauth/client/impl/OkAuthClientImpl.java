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
import cn.toint.okauth.client.model.OkAuthGetOauth2AuthorizeUriRequest;
import cn.toint.okauth.client.model.OkAuthGetOauth2AuthorizeUriResponse;
import cn.toint.oktool.util.Assert;
import org.dromara.hutool.core.data.id.IdUtil;
import org.dromara.hutool.core.net.url.UrlBuilder;
import org.dromara.hutool.core.net.url.UrlEncoder;
import org.dromara.hutool.core.net.url.UrlQuery;

import java.util.Optional;

/**
 * @author Toint
 * @date 2025/7/2
 */
public class OkAuthClientImpl implements OkAuthClient {
    @Override
    public OkAuthGetOauth2AuthorizeUriResponse getOauth2AuthorizeUri(OkAuthGetOauth2AuthorizeUriRequest request) {
        // 参数校验
        Assert.notNull(request, "请求参数不能为空");
        Assert.validate(request);

        // 构建授权地址
        // redirect_uri=http://127.0.0.1:5500/client.html&scope=userinfo,userid,openid,unionid,oidc
        String state = Optional.ofNullable(request.getState()).orElse(IdUtil.fastSimpleUUID());
        UrlQuery urlQuery = UrlQuery.of()
                .add("response_type", request.getResponseType())
                .add("client_id", request.getClientId())
                .add("redirect_uri", UrlEncoder.encodeAll(request.getRedirectUri()))
                .add("scope", request.getScope())
                .add("state", state);

        String authorizeUrl = UrlBuilder.of(request.getAuthorizeUri())
                .setQuery(urlQuery)
                .toString();

        OkAuthGetOauth2AuthorizeUriResponse response = new OkAuthGetOauth2AuthorizeUriResponse();
        response.setAuthorizeUri(authorizeUrl);
        response.setState(state);
        return response;
    }
}
