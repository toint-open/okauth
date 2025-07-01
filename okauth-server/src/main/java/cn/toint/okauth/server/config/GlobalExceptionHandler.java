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

package cn.toint.okauth.server.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.toint.okauth.server.exception.OkAuthUserNotExistException;
import cn.toint.okauth.server.exception.OkAuthUserPasswordException;
import cn.toint.okauth.server.model.ErrEnum;
import cn.toint.okauth.server.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author Toint
 * @date 2025/6/29
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 用户不存在
     */
    @ExceptionHandler(OkAuthUserNotExistException.class)
    public Response<Void> exception(OkAuthUserNotExistException e) {
        log.error(e.getMessage());
        return Response.fail(OkAuthUserNotExistException.ERR);
    }

    /**
     * 密码异常
     */
    @ExceptionHandler(OkAuthUserPasswordException.class)
    public Response<Void> exception(OkAuthUserPasswordException e) {
        log.error(e.getMessage());
        return Response.fail(OkAuthUserPasswordException.ERR);
    }

    /**
     * 无权限
     */
    @ExceptionHandler(NotPermissionException.class)
    public Response<Void> exception(NotPermissionException e) {
        log.error(e.getMessage());
        return Response.fail(ErrEnum.NOT_PERMISSION);
    }

    /**
     * 未登录
     */
    @ExceptionHandler(NotLoginException.class)
    public Response<Void> exception(NotLoginException e) {
        log.error(e.getMessage());
        return Response.fail(ErrEnum.NOT_LOGIN);
    }

    /**
     * 通用异常
     */
    @ExceptionHandler(Exception.class)
    public Response<Void> exception(Exception e) {
        log.error(e.getMessage(), e);
        return Response.fail(e.getMessage());
    }
}
