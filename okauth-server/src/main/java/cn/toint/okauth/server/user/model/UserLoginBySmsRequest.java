package cn.toint.okauth.server.user.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginBySmsRequest {
    @NotBlank(message = "验证码不能为空")
    private String code;
}
