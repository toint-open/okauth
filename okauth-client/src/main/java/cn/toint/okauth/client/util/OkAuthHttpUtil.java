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

package cn.toint.okauth.client.util;

import cn.toint.oktool.util.Assert;
import cn.toint.oktool.util.HttpClientUtil;
import cn.toint.oktool.util.RetryUtil;
import org.dromara.hutool.http.client.Request;
import org.dromara.hutool.http.client.Response;
import org.dromara.hutool.http.client.engine.ClientEngine;
import org.dromara.hutool.http.client.engine.okhttp.OkHttpEngine;

import java.time.Duration;

/**
 * @author Toint
 * @date 2025/7/2
 */
public class OkAuthHttpUtil {

    private static final ClientEngine clientEngine = HttpClientUtil.clientEngine(OkHttpEngine.class, null);

    public static String request(Request request) {
        return RetryUtil.execute(() -> {
                    try (Response response = clientEngine.send(request)) {
                        String bodyStr = response.bodyStr();
                        Assert.isTrue(response.isOk(), "请求认证服务器失败, status={}, body={}", response.getStatus(), bodyStr);
                        return bodyStr;
                    }
                },
                3,
                Duration.ofSeconds(1),
                true);
    }
}
