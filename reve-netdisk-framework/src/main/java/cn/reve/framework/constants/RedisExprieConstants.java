package cn.reve.framework.constants;

/**
 * 简要描述
 * redis键对应过期时间
 * 注意设置不同时间防止缓存雪崩
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/4 11:35
 */
public class RedisExprieConstants {
    //一分钟过期验证码
    public static final Integer CAPTCHA_EXPIRE=1;

    //邮箱验证5分钟有效
    public static final Integer EMAIL_EXPIRE=5;

    /**
     * 用公共过期时间代替，暂时不做大量的缓存雪崩处理
     */
    public static final int PUBLIC_EXPIRE=16;

    // TODO: 2023/5/4
    // hours
    public static final int USER_ROUTES_EXPIRE=24;
    public static final int USER_PERMISSIONS_EXPIRE=22;

    public static final int SHARE_TOKEN_EXPIRE=1;

}
