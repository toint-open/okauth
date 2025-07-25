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

package cn.toint.okauth.server;

import cn.dev33.satoken.spring.oauth2.SaOAuth2BeanInject;
import cn.toint.oktool.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.http.client.ClientConfig;
import org.dromara.hutool.http.client.engine.okhttp.OkHttpEngine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;

import java.time.Duration;

@SpringBootApplication(exclude = {SpringDataWebAutoConfiguration.class, SaOAuth2BeanInject.class})
@Slf4j
public class OkAuthApplication {

    static {
        Duration timeout = Duration.ofSeconds(10);
        ClientConfig clientConfig = ClientConfig.of().setTimeout((int) timeout.toMillis());
        HttpClientUtil.initGlobalConfig(OkHttpEngine.class, clientConfig, timeout);
    }

    public static void main(String[] args) {
        SpringApplication.run(OkAuthApplication.class, args);
    }
}
