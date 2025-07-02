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

package cn.toint.okauth.server.oauth2.manager;

import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.oauth2.data.generate.SaOAuth2DataGenerate;
import cn.dev33.satoken.oauth2.template.SaOAuth2Template;
import cn.dev33.satoken.spring.oauth2.SaOAuth2BeanInject;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * {@link SaOAuth2BeanInject}
 *
 * @author Toint
 * @date 2025/7/2
 */
@Service
@DependsOn()
public class OkAuthOauth2Manager {
    /**
     * OAuth2 配置对象
     */
    @Resource
    @Getter
    private SaOAuth2ServerConfig saOAuth2ServerConfig;

    @Resource
    @Getter
    private SaOAuth2DataGenerate saOAuth2DataGenerate;

    @Resource
    @Getter
    private SaOAuth2Template saOAuth2Template;
}
