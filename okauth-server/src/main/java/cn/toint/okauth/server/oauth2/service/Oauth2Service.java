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

package cn.toint.okauth.server.oauth2.service;

import cn.toint.okauth.server.oauth2.model.*;

/**
 * oauth2
 *
 * @author Toint
 * @date 2025/7/1
 */
public interface Oauth2Service {
    /**
     * 生成code
     *
     * @param userId 用户ID
     * @return 登录响应信息
     */
    Oauth2AuthorizeResponse authorize(Long userId, Oauth2AuthorizeRequest request);

    /**
     * 获取token
     */
    Oauth2TokenResponse token(Oauth2TokenRequest request);

    /**
     * 刷新token
     */
    Oauth2TokenResponse refresh(Oauth2RefreshRequest request);

    /**
     * 获取用户信息
     */
    Oauth2UserInfoResponse userInfo(Oauth2UserInfoRequest request);
}
