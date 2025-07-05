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

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Toint
 * @date 2025/7/2
 */
@Data
public class OkAuthClientConfig {
    /**
     * 认证服务地址
     */
    @NotBlank(message = "认证服务器地址不能为空")
    private String serverUri;

    /**
     * 授权服务地址
     */
    @NotBlank(message = "授权地址不能为空")
    private String authorizeUri;

    /**
     * 客户端ID
     */
    @NotBlank(message = "客户端ID不能为空")
    private String clientId;

    /**
     * 客户端密钥
     */
    @NotBlank(message = "客户端密钥不能为空")
    private String clientSecret;

    /**
     * 回调地址
     */
    @NotBlank(message = "回调地址不能为空")
    private String redirectUri;
}
