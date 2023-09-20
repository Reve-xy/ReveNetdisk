package cn.reve.web.controller;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.map.MapUtil;
import cn.reve.framework.annotation.SystemLog;
import cn.reve.framework.config.ReveConfig;
import cn.reve.framework.constants.RedisExprieConstants;
import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.utils.Result;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/27 22:12
 */
@Slf4j
@RestController
public class CaptchaController extends BaseController {
    @Resource
    ReveConfig config;
    @Resource(name = "strCaptchaProducer")
    Producer strProducer;

    @Resource(name = "mathCaptchaProducer")
    Producer mathProducer;

    /**
     * @param
     * @return Result
     * @date 2023/4/23 19:34
     * @description 获取验证码，并返回客户端uuid
     */
    @GetMapping("/captchaImage")
    @SystemLog("获取验证码")
    public Result getCaptchaImage() {
        //验证码开关
        Boolean captchaEnabled = config.getCaptchaEnabled();
        if(!captchaEnabled){
            return Result.success(MapUtil.builder().put("captchaEnabled", captchaEnabled).build());
        }

        // strText暂存math的运算式，resText为验证码最终结果，需存redis
        String text, strText, resText, captcha, uuid;
        //输出流
        BufferedImage image;
        /*
        * 验证码属性
        * */
        String captchaType = config.getCaptchaType();
        uuid = UUID.randomUUID().toString();
        String captchaKey = RedisKeyConstants.CAPTCHA_KEY + uuid;

        if ("math".equals(captchaType)) {
            text = mathProducer.createText();
            strText = text.substring(0, text.lastIndexOf("@"));
            resText = text.substring(text.lastIndexOf("@") + 1);
            log.info("客户端ID为：{}，验证码为：{}，结果为：{}", uuid, strText, resText);
           image = mathProducer.createImage(strText);
        } else {
            resText = strProducer.createText();
            log.info("客户端ID为：{}，验证码为：{}", uuid, resText);
            image = strProducer.createImage(resText);
        }


        captcha = "data:image/png;base64," + ImgUtil.toBase64(image, "png");
        redisCache.setCacheObject(captchaKey, resText, RedisExprieConstants.CAPTCHA_EXPIRE, TimeUnit.MINUTES);
        return Result.success(MapUtil.builder().put("uuid", uuid).put("captcha", captcha).build());
    }
}
