package cn.reve.framework.handler.listener;

import cn.reve.framework.config.ReveConfig;
import cn.reve.framework.constants.MqConstants;
import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.dto.SendEmailDto;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.utils.RandomCodeGenerator;
import cn.reve.framework.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/7/7 14:19
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = MqConstants.MAIL_TOPIC,
consumerGroup = MqConstants.QQ_MAIL_GROUP)
public class SendMailListener implements RocketMQListener<SendEmailDto> {

    @Resource
    JavaMailSender javaMailSender;
    @Resource
    ReveConfig reveConfig;

    @Resource
    RedisCache redisCache;

    @Override
    public void onMessage(SendEmailDto sendEmailDto) {
        String email = sendEmailDto.getEmail();
        // 生成六位随机验证码
        String htmlPrefix="<span style=\"font-size: 20px; font-weight: bold;\">";
        String htmlSuffix="</span>";
        // 生成六位随机验证码
        String code = RandomCodeGenerator.generateRandomCode(reveConfig.getCaptchaLength());
        String context =new StringBuilder().append(htmlPrefix).append(reveConfig.getContext())
                .append(code).append(reveConfig.getCommon()).append(htmlSuffix).toString();

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(reveConfig.getSendUserName());
            helper.setTo(email);
            helper.setSubject(SystemConstants.EMAIL_SUBJECT);
            helper.setText(context, true);        // 此处设置正文支持html解析
            javaMailSender.send(message);
            log.info("发送邮件成功!验证码为--->{}",code);
            redisCache.setCacheObject(RedisKeyConstants.EMAIL_CAPTCHA_KEY + email, code, 5 , TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new SystemException(533,"邮箱发送失败");
        }
    }
}
