package cn.reve.web.controller;

import cn.reve.framework.annotation.SystemLog;
import cn.reve.framework.domain.vo.UserInfoVo;
import cn.reve.framework.domain.vo.UserSpaceVo;
import cn.reve.framework.service.UserService;
import cn.reve.framework.utils.Result;
import cn.reve.framework.utils.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/27 10:14
 */
@RestController
public class UserController {

    @Resource
    UserService userService;

    @GetMapping("/userInfo")
    @SystemLog("获取用户信息")
    public Result getUserInfo(){
        String userId = SecurityUtils.getUserId();
        UserInfoVo userInfoVo=userService.getUserInfo(userId);
        return Result.success(userInfoVo);
    }

    /**
     * @return null
     * @date 2023/5/27 19:34
     * @description 获取网盘空间
     */
    @GetMapping("/userSpace")
    @SystemLog("获取用户网盘空间")
    public Result getUserSpace(){
        String userId = SecurityUtils.getUserId();
        UserSpaceVo userSpaceVo=userService.getUserSpace(userId);
        return Result.success(userSpaceVo);
    }

}
