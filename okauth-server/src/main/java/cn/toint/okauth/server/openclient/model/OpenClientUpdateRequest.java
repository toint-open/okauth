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

package cn.toint.okauth.server.openclient.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author Toint
 * @date 2025/6/28
 */
@Data
public class OpenClientUpdateRequest {
    @NotNull(message = "应用ID不能为空")
    private Long id;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 主体ID
     */
    private String subjectId;

    /**
     * 应用秘钥
     */
    private String secret;

    /**
     * 应用授权回调地址
     */
    private List<String> allowRedirectUris;

    /**
     * 授权类型
     */
    private List<String> allowGrantTypes;

    /**
     * 状态
     */
    private Integer status;
}
