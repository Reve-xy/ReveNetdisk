package cn.reve.framework.constants;

/**
 * 简要描述
 * redis的键
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/6 15:35
 */
public class RedisKeyConstants {


    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_KEY = "net:disk:captcha:code:";

    /**
     * token redis key
     */
    public static final String LOGIN_AUT_KEY = "login:netdisk:";
    /**
     * email captcha redis key
     */
    public static final String EMAIL_CAPTCHA_KEY = "net:disk:email:captcha:code:";

    /**
     * resetPwd email redis key
     */
    public static final String RESET_EMAIL_KEY = "net:disk:reset:email:key";

    //USER

    /**
     * 用户信息
     */
    public static final String USER_INFO_KEY="net:disk:user:info:";

    /**
     * 用户存储空间
     */
    public static final String USER_SPACE_KEY="net:disk:user:space:";


    /**
     * 上传到哪一个chunk了
     */
    public static final String UPLOAD_CHUNK_KEY="upload:chunk:index:";

    public static final String UPLOAD_FILE_NAME_KEY="upload:file:name:";

    public static final String UPLOAD_FILE_TMP_SIZE_KEY="upload:file:tmp:size:";

    /**
     * 上传文件的md5
     */
    public static final String UPLOAD_FILE_MD5_KEY="upload:file:md5:";

    public static final String UPLOADING_FILE_MD5_KEY="uploading:file:md5:";

    public static final String SHARE_VIEW_COUNT_KEY="netdisk:share:view:count:";
    public static final String SHARE_SAVE_COUNT_KEY="netdisk:share:save:count:";

    public static final String SHARE_DOWNLOAD_COUNT_KEY="netdisk:share:download:count:";

    public static final String SHARE_CODE_VALID_PASS="share:code:valid:pass:";

}
