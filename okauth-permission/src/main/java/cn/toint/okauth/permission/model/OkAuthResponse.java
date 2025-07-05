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

package cn.toint.okauth.permission.model;

import cn.toint.oktool.model.WriteValue;
import cn.toint.oktool.util.MdcUtil;
import lombok.Data;
import org.dromara.hutool.core.date.SystemClock;

@Data
public class OkAuthResponse<T> implements WriteValue {
    private Integer code;

    private String msg;

    private T data;

    private Long timestamp = SystemClock.now();

    private String traceId = MdcUtil.getTraceId();

    public static <T> OkAuthResponse<T> success() {
        OkAuthResponse<T> okAuthResponse = new OkAuthResponse<>();
        okAuthResponse.setCode(ErrEnum.SUCCESS.getCode());
        okAuthResponse.setMsg(ErrEnum.SUCCESS.getMsg());
        return okAuthResponse;
    }

    public static <T> OkAuthResponse<T> success(final T data) {
        OkAuthResponse<T> okAuthResponse = new OkAuthResponse<>();
        okAuthResponse.setCode(ErrEnum.SUCCESS.getCode());
        okAuthResponse.setMsg(ErrEnum.SUCCESS.getMsg());
        okAuthResponse.setData(data);
        return okAuthResponse;
    }

    public static <T> OkAuthResponse<T> fail(final String message) {
        OkAuthResponse<T> okAuthResponse = new OkAuthResponse<>();
        okAuthResponse.setCode(ErrEnum.FAIL.getCode());
        okAuthResponse.setMsg(message);
        return okAuthResponse;
    }

    public static <T> OkAuthResponse<T> fail(final ErrEnum ErrEnum) {
        OkAuthResponse<T> okAuthResponse = new OkAuthResponse<>();
        okAuthResponse.setCode(ErrEnum.getCode());
        okAuthResponse.setMsg(ErrEnum.getMsg());
        return okAuthResponse;
    }

    public static <T> OkAuthResponse<T> fail(int code, String message) {
        OkAuthResponse<T> okAuthResponse = new OkAuthResponse<>();
        okAuthResponse.setCode(code);
        okAuthResponse.setMsg(message);
        return okAuthResponse;
    }
}

