package org.tinycloud.tinyid.bean.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 *     系统登录-DTO
 * </p>
 *
 * @author liuxingyu01
 * @since 2023-06-01 17:17
 */
@Setter
@Getter
public class AuthLoginDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "登录账户不能为空")
    private String username;

    @NotEmpty(message = "登录密码不能为空")
    private String password;

    /**
     * 获取验证码时返回的唯一标识，服务端用它定位 session 中对应的验证码
     */
    @NotEmpty(message = "验证码标识不能为空")
    private String captchaKey;

    /**
     * 用户输入的图形验证码
     */
    @NotEmpty(message = "验证码不能为空")
    private String captchaCode;
}
