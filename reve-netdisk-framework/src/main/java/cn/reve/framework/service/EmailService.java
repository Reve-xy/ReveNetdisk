package cn.reve.framework.service;

import cn.reve.framework.domain.dto.SendEmailDto;
import cn.reve.framework.domain.entity.User;

import java.util.List;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/25 15:21
 */
public interface EmailService {
    void sendEmailCode(SendEmailDto sendEmailDto);

    void verifyEmailCaptcha(String email, String captcha);

    List<User> verifyEmail(String email);
}
