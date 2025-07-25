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

package cn.toint.okauth.server.exception;

import cn.toint.okauth.server.model.ErrEnum;

/**
 * 密码错误
 *
 * @author Toint
 * @date 2025/6/30
 */
public class UserPasswordException extends RuntimeException{
    public static final ErrEnum ERR = ErrEnum.PASSWORD_ERROR;

    public UserPasswordException(String message) {
        super(message);
    }
}
