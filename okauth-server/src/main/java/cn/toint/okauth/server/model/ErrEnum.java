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

package cn.toint.okauth.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Toint
 * @date 2025/6/27
 */
@Getter
@AllArgsConstructor
public enum ErrEnum {
    NOT_PERMISSION(403, "权限不足"),
    NOT_LOGIN(401, "未登录"),
    PASSWORD_ERROR(10001, "密码错误"),
    USER_NOT_EXIST(10000, "用户不存在"),
    FAIL(-1, "失败"),
    SUCCESS(0, "成功");

    private final int code;
    private final String msg;
}
