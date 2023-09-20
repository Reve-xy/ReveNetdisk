package cn.reve.framework.service.impl;

import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.service.CaptchaService;
import cn.reve.framework.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/25 15:25
 */
@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {
    @Resource
    RedisCache redisCache;
    /**
     * @param uuid
     * @param captcha
     * @return void
     * @date 2023/5/3 21:13
     * @description 校验验证码
     */
    @Override
    public void verifyCaptcha(String uuid, String captcha) {
        String captchaKey = RedisKeyConstants.CAPTCHA_KEY + uuid;
        String captchaCache = (String) redisCache.getCacheObject(captchaKey);
        if (!StringUtils.hasText(captchaCache)) {
            throw new SystemException(HttpCodeEnum.CAPTCHA_EXPIRE);
        } else if (!captcha.equals(captchaCache)) {
            // 一次性使用
            redisCache.deleteObject(captchaKey);
            throw new SystemException(HttpCodeEnum.CAPTCHA_ERR);
        }
    }
}
