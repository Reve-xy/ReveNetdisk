package cn.reve.framework.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/3 21:20
 */
@Data
@Component
@ConfigurationProperties("reve")
public class ReveConfig {
    /**
 * 验证码类型
 */
private String captchaType;
    /**
     * 字符验证码范围
     */
    private String charCaptchaSize;
    /**
     * 验证码开关
     */
    private Boolean captchaEnabled;

    @Value("${spring.mail.username}")
    private String sendUserName;

    private String subject;

    private String context;
    private String common;
    private Integer captchaLength;
}
