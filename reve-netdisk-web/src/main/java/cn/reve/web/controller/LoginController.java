package cn.reve.web.controller;

import cn.hutool.core.map.MapUtil;
import cn.reve.framework.annotation.SystemLog;
import cn.reve.framework.domain.dto.auth.LoginDto;
import cn.reve.framework.domain.dto.auth.RegisterDto;
import cn.reve.framework.domain.dto.auth.ResetPwd;
import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.service.LoginService;
import cn.reve.framework.utils.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/24 15:26
 */
@RestController
public class LoginController extends BaseController{

    @Resource
    LoginService loginService;

    @PostMapping("/login")
    @SystemLog("登录")
    public Result login(@Valid @RequestBody LoginDto loginDto){
        String jwt = loginService.login(loginDto);
        return Result.success(HttpCodeEnum.SUCCESS_LOGIN, MapUtil.builder().put("token",jwt).build());
    }

    @PostMapping("/register")
    @SystemLog("注册")
    public Result register(@Valid @RequestBody RegisterDto registerDto) {
        loginService.register(registerDto);
        return Result.success();
    }

    @PostMapping("/resetPassword")
    @SystemLog("重置密码")
    public Result  resetPassword( @RequestBody ResetPwd resetPwd) {
        loginService.resetPassword(resetPwd);
        return Result.success();
    }
}
