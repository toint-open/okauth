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

package cn.toint.okauth.client;

import cn.toint.okauth.client.model.*;

/**
 * OkAuth客户端
 *
 * @author Toint
 * @date 2025/7/2
 */
public interface OkAuthClient {

    OkAuthConfig getConfig();

    /**
     * 账号密码登录
     */
    OkAuthUserLoginResponse login(OkAuthUserLoginByPasswordRequest request);

    /**
     * 获取oauth2授权地址
     */
    OkAuthOauth2BuildAuthorizeUriResponse buildAuthorizeUri(OkAuthOauth2BuildAuthorizeUriRequest request);

    /**
     * 获取token
     */
    OkAuthOauth2TokenResponse token(OkAuthOauth2TokenRequest request);

    /**
     * 刷新token
     */
    OkAuthOauth2TokenResponse refresh(OkAuthOauth2RefreshRequest request);
}
