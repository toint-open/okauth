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

import lombok.Data;

/**
 * @author Toint
 * @date 2025/7/2
 */
@Data
public class OkAuthUserLoginResponse {
    /**
     * 用户信息
     */
    private user user;

    /**
     * token信息
     */
    private Token token;

    @Data
    public static class user {
        private Long id;

        private String username;

        private String name;

        private String phone;
    }

    @Data
    public static class Token {
        /**
         * token 名称
         */
        public String tokenName;

        /**
         * token 值
         */
        public String tokenValue;

        /**
         * token 剩余有效期（单位: 秒）
         */
        public long tokenTimeout;
    }
}
