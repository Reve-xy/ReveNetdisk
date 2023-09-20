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
 * @date : 2023/5/25 14:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPwd {

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
}
