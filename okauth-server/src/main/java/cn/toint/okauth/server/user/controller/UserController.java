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

package cn.toint.okauth.server.user.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.toint.okauth.server.user.model.*;
import cn.toint.oktool.model.Response;
import cn.toint.okauth.server.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Toint
 * @date 2025/6/29
 */
@RestController
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 发送登录短信验证码
     */
    @PostMapping("/user/login/sendSms")
    @SaIgnore
    public Response<Void> sendLoginSms(@RequestBody UserLoginSendSmsRequest request) {
        userService.sendLoginSms(request);
        return Response.success();
    }

    /**
     * 短信登录
     */
    @PostMapping("/user/login/sms")
    @SaIgnore
    public Response<UserLoginResponse> login(@RequestBody UserLoginBySmsRequest request) {
        UserLoginResponse response = userService.login(request);
        return Response.success(response);
    }

    /**
     * 账号密码登录
     */
    @PostMapping("/user/login/password")
    @SaIgnore
    public Response<UserLoginResponse> login(@RequestBody UserLoginByPasswordRequest request) {
        UserLoginResponse response = userService.login(request);
        return Response.success(response);
    }

    /**
     * 修改密码
     */
    @PostMapping("/user/updatePassword")
    public Response<Void> updatePassword(@RequestBody UserUpdatePasswordRequest request) {
        long userId = StpUtil.getLoginIdAsLong();
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();
        userService.updatePassword(userId, oldPassword, newPassword);
        return Response.success();
    }
}
