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

package cn.toint.okauth.server.model;

import cn.toint.oktool.model.WriteValue;
import cn.toint.oktool.util.MdcUtil;
import lombok.Data;
import org.dromara.hutool.core.date.SystemClock;

@Data
public class Response<T> implements WriteValue {
    private Integer code;

    private String msg;

    private T data;

    private Long timestamp = SystemClock.now();

    private String traceId = MdcUtil.getTraceId();

    public static <T> Response<T> success() {
        Response<T> response = new Response<>();
        response.setCode(ErrEnum.SUCCESS.getCode());
        response.setMsg(ErrEnum.SUCCESS.getMsg());
        return response;
    }

    public static <T> Response<T> success(final T data) {
        Response<T> response = new Response<>();
        response.setCode(ErrEnum.SUCCESS.getCode());
        response.setMsg(ErrEnum.SUCCESS.getMsg());
        response.setData(data);
        return response;
    }

    public static <T> Response<T> fail(final String message) {
        Response<T> response = new Response<>();
        response.setCode(ErrEnum.FAIL.getCode());
        response.setMsg(message);
        return response;
    }

    public static <T> Response<T> fail(final ErrEnum ErrEnum) {
        Response<T> response = new Response<>();
        response.setCode(ErrEnum.getCode());
        response.setMsg(ErrEnum.getMsg());
        return response;
    }

    public static <T> Response<T> fail(int code, String message) {
        Response<T> response = new Response<>();
        response.setCode(code);
        response.setMsg(message);
        return response;
    }
}

