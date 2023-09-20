package cn.reve.framework.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/22 23:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
    private String userName;

    @NotBlank(message = "密码不能为空")
    // @Pattern(regexp = RegexValidConstants.PWD_REGEX,message = "请输入正确的密码")
    private String password;

    private String captcha;

    private String uuid;


}
