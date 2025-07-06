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

package cn.toint.okauth.server.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.dromara.hutool.core.codec.binary.Base64;
import org.dromara.hutool.core.lang.Assert;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * @author Toint
 * @date 2025/6/30
 */
@Component
@ConfigurationProperties("okauth")
@Data
public class OkAuthProperties {
    /**
     * 是否开启跨域配置
     */
    private boolean corsEnabled = false;

    /**
     * 用户加密Key
     * 16位字节数组Base64后的字符串
     */
    private String userEncryptKey;

    private Sms sms = new Sms();


    @PostConstruct
    public void init() {
        Assert.notBlank(userEncryptKey, "用户加密Key不能为空");
        int passwordAesKeyLength = Base64.decode(userEncryptKey).length;
        Assert.isTrue(passwordAesKeyLength == 16, "用户加密Key应为16位字节数组Base64后的字符串, 实际长度: {}", passwordAesKeyLength);
    }

    /**
     * 短信
     */
    @Data
    public static class Sms {
        private boolean enableSms;
        private String smsKey;
        private String smsSecret;

        /**
         * 登录短信
         */
        private LoginCode loginCode = new LoginCode();

        @Data
        public static class LoginCode {
            private String signName;
            private String templateCode;

            /**
             * 验证码有效时间
             */
            private Duration timeout =  Duration.ofMinutes(5);

            /**
             * 模版参数
             * 可选生成器:
             * - code4()
             * - code6()
             */
            private Map<String, Object> templateParam;

        }
    }
}
