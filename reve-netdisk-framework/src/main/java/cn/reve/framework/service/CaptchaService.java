package cn.reve.framework.service;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/25 15:25
 */
public interface CaptchaService {
    void verifyCaptcha(String uuid, String captcha);
}
