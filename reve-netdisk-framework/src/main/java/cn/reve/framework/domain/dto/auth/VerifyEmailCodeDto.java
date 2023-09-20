package cn.reve.framework.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/25 14:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyEmailCodeDto {
    @NotNull(message = "验证码不能为空")
    private String captcha;

    @Pattern(regexp="^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\\\.[a-zA-Z0-9_-]+)+$",message = "请输入正确的邮件地址")
    private String email;
}
