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

package cn.toint.okauth.permission.constant;

import cn.toint.oktool.util.KeyBuilderUtil;

/**
 * @author Toint
 * @date 2025/6/29
 */
public class OkAuthPermissionConstant {
    /**
     * 数据源名称
     */
    public static final String DATA_SOURCE = "okauth-permission";


    public static final KeyBuilderUtil roleMtmPermissionCacheKeyBuilder = KeyBuilderUtil.of("roleMtmPermission");
    public static final KeyBuilderUtil userMtmRoleCacheKeyBuilder = KeyBuilderUtil.of("userMtmRole");

    public static class Role {
        /**
         * 管理员角色
         */
        public static final String ADMIN = "admin";

        /**
         * 管理员角色ID
         */
        public static final long ADMIN_ID = 10000L;
    }
}
