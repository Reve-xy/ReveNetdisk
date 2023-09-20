package cn.reve.framework.service.impl;

import cn.reve.framework.constants.MqConstants;
import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.domain.dto.SendEmailDto;
import cn.reve.framework.domain.entity.User;
import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.mapper.UserMapper;
import cn.reve.framework.service.CaptchaService;
import cn.reve.framework.service.EmailService;
import cn.reve.framework.utils.RedisCache;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/25 15:21
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {


    @Resource
    RedisCache redisCache;

    @Resource
    CaptchaService captchaService;

    @Resource
    RocketMQTemplate rocketMQTemplate;

    @Override
    public void sendEmailCode(SendEmailDto sendEmailDto) {
        String captcha = sendEmailDto.getCaptcha();
        String email = sendEmailDto.getEmail().trim();
        String uuid = sendEmailDto.getUuid();
        if (StringUtils.hasText(captcha)) {
            captcha = captcha.trim();
            captchaService.verifyCaptcha(uuid, captcha);
        }
        if (StringUtils.hasText(email)) {
            sendEmailDto.setEmail(email.trim());
        }
        rocketMQTemplate.asyncSend(MqConstants.MAIL_TOPIC, sendEmailDto, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info(MqConstants.MAIL_TOPIC+"--->SUCCESS");
            }
            @Override
            public void onException(Throwable throwable) {
                log.error(MqConstants.MAIL_TOPIC+"--->FAIL");
            }
        });
    }

    /**
     * @param email
     * @param captcha
     * @return void
     * @date 2023/5/25 13:08
     * @description 邮箱验证码不能一次性使用，只能等待他过期
     */
    @Override
    public void verifyEmailCaptcha(String email, String captcha) {
        String captchaKey = RedisKeyConstants.EMAIL_CAPTCHA_KEY +email ;
        String captchaCache = (String) redisCache.getCacheObject(captchaKey);
        if (!StringUtils.hasText(captchaCache)) {
            throw new SystemException(HttpCodeEnum.CAPTCHA_EXPIRE);
        } else if (!captcha.equals(captchaCache)) {
            throw new SystemException(HttpCodeEnum.CAPTCHA_ERR);
        }
    }

    @Resource
    UserMapper userMapper;
    @Override
    public List<User> verifyEmail(String email) {
        return userMapper.selectList(Wrappers.<User>lambdaQuery().eq(User::getEmail,email).select(User::getEmail));
    }
}
