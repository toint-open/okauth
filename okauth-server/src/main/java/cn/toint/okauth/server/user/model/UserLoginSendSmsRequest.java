package cn.toint.okauth.server.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserLoginSendSmsRequest {
    /**
     * 客户端ID
     */
    @NotNull(message = "客户端ID不能为空")
    private Long clientId;

    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空")
    private String phone;

    /**
     * 行为验证
     */
    private String token;
}
