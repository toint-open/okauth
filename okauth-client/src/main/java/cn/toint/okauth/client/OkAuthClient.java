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

import cn.toint.okauth.client.model.OkAuthGetOauth2AuthorizeUriRequest;
import cn.toint.okauth.client.model.OkAuthGetOauth2AuthorizeUriResponse;

/**
 * OkAuth客户端
 *
 * @author Toint
 * @date 2025/7/2
 */
public interface OkAuthClient {
    /**
     * 获取oauth2授权地址
     */
    OkAuthGetOauth2AuthorizeUriResponse getOauth2AuthorizeUri(OkAuthGetOauth2AuthorizeUriRequest request);

//    /**
//     * 缓存state, 以便回调时校验来源合法
//     *
//     * @param state   state
//     * @param timeout 超时时间, 默认5分钟
//     */
//    void cacheState(String state, Duration timeout);
}
