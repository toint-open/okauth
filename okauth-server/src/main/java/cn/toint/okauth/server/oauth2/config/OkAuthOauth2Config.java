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

package cn.toint.okauth.server.oauth2.config;

import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.oauth2.data.generate.SaOAuth2DataGenerate;
import cn.dev33.satoken.oauth2.data.loader.SaOAuth2DataLoader;
import cn.dev33.satoken.oauth2.template.SaOAuth2Template;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Toint
 * @date 2025/7/2
 */
@Configuration
public class OkAuthOauth2Config {
    @Bean
    @Primary
    public SaOAuth2ServerConfig saOAuth2ServerConfig() {
        SaOAuth2ServerConfig saOAuth2ServerConfig = SaOAuth2Manager.getServerConfig();
        saOAuth2ServerConfig.setMaxAccessTokenCount(1);
        saOAuth2ServerConfig.setMaxRefreshTokenCount(1);
        saOAuth2ServerConfig.setMaxClientTokenCount(1);
        return saOAuth2ServerConfig;
    }

    @Bean
    @Primary
    public SaOAuth2DataGenerate saOAuth2DataGenerate() {
        return SaOAuth2Manager.getDataGenerate();
    }

    @Bean
    @Primary
    public SaOAuth2Template saOAuth2Template() {
        return SaOAuth2Manager.getTemplate();
    }

    @Bean
    @Primary
    public SaOAuth2DataLoader saOAuth2DataLoader() {
        return SaOAuth2Manager.getDataLoader();
    }
}
