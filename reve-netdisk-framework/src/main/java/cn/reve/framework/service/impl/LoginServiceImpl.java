package cn.reve.framework.service.impl;

import cn.reve.framework.config.ReveConfig;
import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.dto.auth.LoginDto;
import cn.reve.framework.domain.dto.auth.RegisterDto;
import cn.reve.framework.domain.dto.auth.ResetPwd;
import cn.reve.framework.domain.entity.LoginUser;
import cn.reve.framework.domain.entity.User;
import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.mapper.UserMapper;
import cn.reve.framework.service.CaptchaService;
import cn.reve.framework.service.EmailService;
import cn.reve.framework.service.LoginService;
import cn.reve.framework.utils.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/24 15:34
 */
@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Resource
    ReveConfig reveConfig;
    @Resource
    RedisCache redisCache;

    @Resource
    JwtUtils jwtUtils;
    @Resource
    CheckRepeatUtils checkRepeatUtils;
    @Resource
    PasswordEncoder passwordEncoder;
    @Resource
    AuthenticationManager authenticationManager;
    @Resource
    UserMapper userMapper;

@Resource
    CaptchaService captchaService;

@Resource
    EmailService emailService;
    /**
     * @param loginDto
     * @return String
     * @date 2023/5/25 15:01
     * @description 登录接口
     */
    @Override
    public String login(LoginDto loginDto) {
        String userName = loginDto.getUserName();
        if(!StringUtils.hasText(userName)){
            throw new SystemException(HttpCodeEnum.REQUIRE_ACCOUNT);
        }
        if(reveConfig.getCaptchaEnabled()){
            captchaService.verifyCaptcha(loginDto.getUuid(),loginDto.getCaptcha());
        }
        // 传入用户需要认证的信息
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userName, loginDto.getPassword());
        Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        Long id = loginUser.getUser().getId();
        String jwt = jwtUtils.createJWT(id.toString());
        // 登录后的用户存入redis
        redisCache.setCacheObject(RedisKeyConstants.LOGIN_AUT_KEY + id, loginUser, jwtUtils.getExpire(), TimeUnit.DAYS);
        return jwt;
    }




    @Override
    public void register(RegisterDto registerDto) {
        String emailCaptcha = registerDto.getEmailCaptcha();
        String email = registerDto.getEmail();
        emailService.verifyEmailCaptcha(email,emailCaptcha);

        // 验证码通过，可以注册
        if(registerDto.getPassword().equals(registerDto.getConfirmPassword())){
            throw new SystemException(HttpCodeEnum.PWD_NOT_SAME);
        }
        User user = BeanCopyUtils.copyBean(registerDto, User.class);
        //是否重复
        checkRepeatUtils.setType(SystemConstants.INSERT).checkNickNameRepeat(user).checkEmailRepeat(user);

        int i=0;
        String userId = "";
        //若生成重复账号，循环再次生成，五次不重复为止
        while(i>5){
            i++;
            userId = RandomCodeGenerator.getNumberId(SystemConstants.ID_LENGTH);
            if(checkRepeatUtils.checkUserIdRepeat(userId)) {
                break;
            }
            if(i==5){
                throw new SystemException(555,"生成用户账号失败，请重新注册");
            }
        }

        user.setUserId(userId);
        String encode = passwordEncoder.encode(user.getPassword());
        user.setPassword(encode);

        userMapper.insert(user);
    }



    @Override
    public void resetPassword(ResetPwd resetPwd) {
        String  email = (String ) redisCache.getCacheObject(RedisKeyConstants.RESET_EMAIL_KEY);
        if(!StringUtils.hasText(email)){
            throw new SystemException(HttpCodeEnum.EXPIRE_RESET_EMAIL);
        }
        String password = resetPwd.getPassword();
        String confirmPassword = resetPwd.getConfirmPassword();
        if(!password.equals(confirmPassword)){
            throw new SystemException(HttpCodeEnum.PWD_NOT_SAME);
        }
        User user = new User();
        user.setPassword(passwordEncoder.encode(password));
        userMapper.update(user, Wrappers.<User>lambdaQuery().eq(User::getEmail,email));
    }
}
