package cn.reve.framework.handler.security;

import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.utils.Result;
import cn.reve.framework.utils.WebUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 简要描述
 * 认证失败的处理
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/7 9:32
 */
@Component
@Slf4j
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        log.error("认证出错"+e.getMessage());
        Result result=null;

        //登录凭证（密码）异常
        e.printStackTrace();
        if(e instanceof BadCredentialsException){
            result=Result.failure(HttpCodeEnum.LOGIN_ERROR);
        }else if(e instanceof InsufficientAuthenticationException){
            //登陆凭证不够充分而抛出的异常,没有登录过
            result=Result.failure(HttpCodeEnum.NEED_LOGIN);
        }else{
            result=Result.failure(HttpCodeEnum.SYSTEM_ERROR,"认证或授权失败");
        }
        WebUtils.renderString(response, JSON.toJSONString(result));
    }
}
