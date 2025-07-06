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

package cn.toint.okauth.server.user.service;

import cn.toint.okauth.server.user.model.*;

/**
 * 用户
 *
 * @author Toint
 * @date 2025/6/29
 */
public interface UserService {
    /**
     * 账号密码登录
     */
    UserLoginResponse login(UserLoginByPasswordRequest request);

    /**
     * 查询用户
     */
    UserDo getById(Long userId);

    /**
     * 短信验证码登录
     */
    UserLoginResponse login(UserLoginBySmsRequest request);

    /**
     * 发送登录短信验证码
     */
    void sendLoginSms(UserLoginSendSmsRequest request);
}
