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

import cn.toint.oktool.util.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * @author Toint
 * @date 2025/6/30
 */
@Configuration
@Slf4j
public class JacksonConfig {

    @Resource
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        // 替换JacksonUtil的Jackson ObjectMapper
        JacksonUtil.setObjectMapper(objectMapper);
        // 设置mybatis-flex的Jackson ObjectMapper
        JacksonTypeHandler.setObjectMapper(objectMapper);
        log.info("objectMapper初始化成功, moduleIds={}", objectMapper.getRegisteredModuleIds());
    }
}
