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

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Toint
 * @date 2025/7/2
 */
@Data
public class OkAuthUserLoginByPasswordRequest {
    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "授权类型不能为空")
    public String responseType;

    @NotBlank(message = "应用ID不能为空")
    public String clientId;

    @NotBlank(message = "重定向URL不能为空")
    public String redirectUri;

    /**
     * 授权范围
     */
    public String scope;

    /**
     * 状态标识
     */
    public String state;
}
