package cn.reve.web.controller;


import cn.reve.framework.utils.RedisCache;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/24 13:47
 */
public class BaseController {
    @Resource
    HttpServletRequest req;

    @Resource
    RedisCache redisCache;
}
