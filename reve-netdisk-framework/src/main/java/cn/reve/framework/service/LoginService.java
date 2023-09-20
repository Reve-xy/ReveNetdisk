package cn.reve.framework.service;

import cn.reve.framework.domain.dto.auth.LoginDto;
import cn.reve.framework.domain.dto.auth.RegisterDto;
import cn.reve.framework.domain.dto.auth.ResetPwd;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/24 15:34
 */
public interface LoginService {
    String login(LoginDto loginDto);

    void register(RegisterDto registerDto);

    void resetPassword(ResetPwd resetPwd);
}
