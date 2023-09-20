package cn.reve.framework.enums;

public enum HttpCodeEnum {
    // 成功
    SUCCESS(200,"操作成功"),
    FAILURE(400,"操作失败"),

    SUCCESS_LOGOUT(200,"退出成功"),
    SUCCESS_REGISTER(200,"注册成功"),
    SUCCESS_LOGIN(200,"登录成功"),

    // 登录
    LOGIN_ERROR(61,"用户名或密码错误"),
    REQUIRE_ACCOUNT(61,"必须填写账号"),
    USER_NOT_FOUND(61,"用户不存在"),
    CAPTCHA_ERR(62,"验证码错误"),
    CAPTCHA_EXPIRE(62,"验证码已过期"),
    CERTIFICATE_EXPIRE(88,"凭证已过期，请重新验证"),   //忘记密码凭证过期
    USER_DISABLED(77,"您的账号存在风险暂时无法使用"),

    NEED_LOGIN(401,"需要登录后操作"),
    NO_OPERATOR_AUTH(403,"无权限操作"),
    SYSTEM_ERROR(500,"服务器错误"),
    USERNAME_EXIST(501,"用户名已存在"),
    EMAIL_EXIST(503, "邮箱已存在"),
    REQUIRE_USERNAME(504, "必需填写用户名"),
    USERNAME_NOT_NULL(508, "用户名不能为空"),
    NICKNAME_NOT_NULL(509, "昵称不能为空"),
    PASSWORD_NOT_NULL(510, "密码不能为空"),
    EMAIL_NOT_NULL(511, "邮箱不能为空"),
    NICKNAME_EXIST(512, "昵称已存在"),
    CAPTCHA_NULL(512,"验证码不能为空"),
    UUID_NULL(514,"客户端异常"),

    REPEAT_USERNAME(515,"该账号名已存在了，换个试试吧"),

    REPEAT_EMAIL(516,"该邮箱已注册过了"),

    PHONE_EXIST(517,"该手机号已注册过了"),
    PWD_NOT_SAME(518,"两次密码不一致"),

    SEX_ERR(519,"性别选择异常"),

    HAS_CHILD_MENU(520,"存在子菜单，无法删除"),
    ILLEGAL_ID(523,"请输入正确的用户名"),
    ILLEGAL_EMAIL(523,"请输入正确的邮箱"),

    FAILURE_UPLOAD(523,"文件上传失败"),
    NO_EXIST_EMAIL(524,"该邮箱还没有注册过哦"),

    EXPIRE_RESET_EMAIL(525,"凭证已过期，请重新验证"),


    NO_SPACE_TO_SAVE(526,"没有可以存储的空间了"),

    NO_EXIST_FILE(527,"没有可以存储的空间了"),
    UPLOADING_ERR(528,"续传错误"),

    INIT_UPLOAD_ERR(529,"初始化上传任务失败"),
    MERGE_MULTIPART_ERR(550,"合并文件失败" ),
    NO_SUCH_FOLDER(551,"文件被所有者删除，操作失败,已跳转至首页"),
    NO_SUCH_FILE(552,"所选文件不存在，请重试"),
    PAGE_NULL(553,"页码不能为空"),
    NO_SELECT_DATA(555,"暂未选择数据"),
    NO_SOURCE_FILE_ID(556,"暂未选择移动到的目标文件夹"),

    SHARE_TYPE_NULL(554, "分享类型出错"),
    //外链分享
    NO_SUCH_SHARE_LINK(557,"暂未找到该分享链接"),
    SHARE_LINK_EXPIRED(558,"链接已过期"),
    SHARE_CODE_ERR(559,"提取码错误"),
    SHARE_TOKEN_VALID_EXPIRED(560,"验证已失效，请重新认证"),
    NO_SAVE_BY_MYSERLF(561,"不能保存在自己的网盘中"), NO_SELECT_SHARE_FILE(562,"暂未选择分享的文件" );


    int code;
    String msg;

    HttpCodeEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
