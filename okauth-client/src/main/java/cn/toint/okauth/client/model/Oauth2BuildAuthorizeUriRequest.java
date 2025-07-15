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

package cn.toint.okauth.client.model;

import cn.toint.okauth.client.constant.OkAuthClientConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 构建授权链接
 *
 * @author Toint
 * @date 2025/7/1
 */
@Data
public class Oauth2BuildAuthorizeUriRequest {
    /**
     * 客户端ID
     */
    @NotBlank(message = "客户端ID不能为空")
    private String clientId;

    /**
     * 回调地址
     */
    @NotBlank(message = "回调地址不能为空")
    private String redirectUri;

    /**
     * 授权服务地址
     */
    @NotBlank(message = "授权地址不能为空")
    private String authorizeUri;

    /**
     * @see OkAuthClientConstant.ResponseType
     */
    @NotBlank(message = "responseType不能为空")
    private String responseType;

    /**
     * 授权范围
     */
    private String scope;

    /**
     * 随机值, 此参数会在重定向时追加到url末尾
     * 建议开发者把该值缓存起来, 回调时校验该值
     */
    private String state;
}
