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

import cn.toint.okauth.server.interceptor.AccessLogInterceptor;
import cn.toint.okauth.server.interceptor.LoginInterceptor;
import cn.toint.okauth.server.properties.OkAuthProperties;
import cn.toint.oktool.spring.boot.interceptor.TraceIdInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private OkAuthProperties okAuthProperties;

    @Resource
    private LoginInterceptor loginInterceptor;

    @Resource
    private AccessLogInterceptor accessLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 任务ID拦截器
        registry.addInterceptor(new TraceIdInterceptor())
                .addPathPatterns("/**");
        log.info("任务ID拦截器初始化成功");

        // 访问日志拦截器
        registry.addInterceptor(accessLogInterceptor)
                .addPathPatterns("/**");
        log.info("访问日志拦截器初始化成功");

        // 登录拦截器
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**");
        log.info("登录拦截器初始化成功");

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (okAuthProperties.isCorsEnabled()) {
            registry.addMapping("/**") // 所有接口
                    .allowCredentials(true) // 是否发送 Cookie
                    .allowedOriginPatterns("*") // 支持域
                    .allowedMethods("*") // 支持方法
                    .allowedHeaders("*")
                    .exposedHeaders("*");
            log.info("跨域配置开启成功");
        }
    }
}