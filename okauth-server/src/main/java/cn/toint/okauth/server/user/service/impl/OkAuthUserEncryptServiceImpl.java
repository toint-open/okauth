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

package cn.toint.okauth.server.user.service.impl;

import cn.toint.okauth.server.properties.OkAuthProperties;
import cn.toint.okauth.server.user.service.OkAuthUserEncryptService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.codec.binary.Base64;
import org.dromara.hutool.core.lang.Assert;
import org.dromara.hutool.crypto.symmetric.AES;
import org.springframework.stereotype.Service;

/**
 * @author Toint
 * @date 2025/6/30
 */
@Service
@Slf4j
public class OkAuthUserEncryptServiceImpl implements OkAuthUserEncryptService {
    @Resource
    private OkAuthProperties okAuthProperties;

    private AES aes;

    @Override
    public String encrypt(String value) {
        Assert.notBlank(value, "加密内容不能为空");
        return aes.encryptBase64(value);
    }

    @PostConstruct
    private void init() {
        String userEncryptKey = okAuthProperties.getUserEncryptKey();
        aes = new AES(Base64.decode(userEncryptKey));
        log.info("用户加密模块初始化成功");
    }
}
