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
public class ResponseVo<T> implements WriteValue {
    private Integer code;

    private String msg;

    private T data;

    private Long timestamp = SystemClock.now();

    private String traceId = MdcUtil.getTraceId();

    public static <T> ResponseVo<T> success() {
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setCode(ErrEnum.SUCCESS.getCode());
        responseVo.setMsg(ErrEnum.SUCCESS.getMsg());
        return responseVo;
    }

    public static <T> ResponseVo<T> success(final T data) {
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setCode(ErrEnum.SUCCESS.getCode());
        responseVo.setMsg(ErrEnum.SUCCESS.getMsg());
        responseVo.setData(data);
        return responseVo;
    }

    public static <T> ResponseVo<T> fail(final String message) {
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setCode(ErrEnum.FAIL.getCode());
        responseVo.setMsg(message);
        return responseVo;
    }

    public static <T> ResponseVo<T> fail(final ErrEnum ErrEnum) {
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setCode(ErrEnum.getCode());
        responseVo.setMsg(ErrEnum.getMsg());
        return responseVo;
    }

    public static <T> ResponseVo<T> fail(int code, String message) {
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setCode(code);
        responseVo.setMsg(message);
        return responseVo;
    }
}

