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

package cn.toint.okauth.server.interceptor;

import cn.toint.oktool.util.JacksonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.http.server.servlet.ServletUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author Toint
 * @date 2025/7/13
 */
@Slf4j
@Component
public class AccessLogInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIP = ServletUtil.getClientIP(request);
        String uri = request.getRequestURI();
        StringBuffer requestURL = request.getRequestURL();
        String method = request.getMethod();

        AccessLog accessLog = new AccessLog();
        accessLog.setClientIp(clientIP);
        accessLog.setUri(uri);
        accessLog.setMethod(method);
        log.info(JacksonUtil.writeValueAsString(accessLog));
        return true;
    }

    @Data
    private static class AccessLog {
        private String clientIp;
        private String uri;
        private String method;
    }
}
