package cn.reve.framework.domain.dto.auth;

import cn.reve.framework.constants.RegexValidConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/25 12:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {

    /**
     * 昵称
     */
    @Pattern(regexp= RegexValidConstants.INVAILD_SYMBOL_REGEX,message = "昵称不能包含非法字符")
    private String nickName;

    /**
     * email
     */
    @Pattern(regexp= RegexValidConstants.EMAIL_REGEX,message = "请输入正确的邮件地址")
    private String email;

    /**
     * 密码
     */
    @Pattern(regexp= RegexValidConstants.PWD_REGEX,message = "请输入合法的密码")
    private String password;

    /**
     * 密码
     */
    @Pattern(regexp= RegexValidConstants.PWD_REGEX,message = "请输入合法的密码")
    private String confirmPassword;

    /**
     * 邮箱验证码
     */
    private String emailCaptcha;
}
