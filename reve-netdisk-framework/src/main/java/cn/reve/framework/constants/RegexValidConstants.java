package cn.reve.framework.constants;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/12 21:04
 */
public class RegexValidConstants {
    /**
     * 密码规则，区分大小写，必须有英文和数字，8-18位
     */
    public static final String PWD_REGEX="^(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9]{8,18}$";

    public static final String PHONE_REGEX="^(?:(?:\\+|00)86)?1(?:(?:3[\\d])|(?:4[5-7|9])|(?:5[0-3|5-9])|(?:6[5-7])|(?:7[0-8])|(?:8[\\d])|(?:9[1|8|9]))\\d{8}$";
    public static final String EMAIL_REGEX="^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     *    ！@~#￥%……&*()——+{}|：“《》？-=【】、；‘，。、
     */
    public static final String INVAILD_SYMBOL_REGEX="\"[~!/@#$%^&*()\\\\-_=+\\\\|[{}];:\\'\\\",<.>/?]+\"";
    /**
     *    不能为空且只能为数字
     */
    public static final String NULL_AND_NUM_REGEX="^[0-9]+$|^$";
}
