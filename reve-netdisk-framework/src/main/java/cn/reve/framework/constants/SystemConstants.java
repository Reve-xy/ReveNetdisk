package cn.reve.framework.constants;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/24 17:08
 */

public class SystemConstants {
    public static final String EMAIL_SUBJECT="[Reve网盘]";


    /** 正常状态 */
    public static final Integer NORMAL = 0;

    public static final Integer USER_STATUS_NORMAL=1;

    public static final String UPDATE="update";
    public static final String INSERT="insert";

    public static final Integer ID_LENGTH=6;

    public static final Integer DESC = 1;
    public static final Integer ASC = 0;

    public static final Integer FILE_ID_LENGTH=8;
    public static final Integer FILE_NAME_LENGTH = 5;

    /**
     * 验证码长度
     */
    public static final Integer CODE_LENGTH=5;
    public static final String ROOT_PID="0";

    /*
    * 缩略图
    * */
    public static final Double SCALE_VAL=0.5;  //缩放大小

    public static final Double QUALITY_VAL=0.5;  //质量

    /*
    * 删除
    * */
    public static final Integer DEL_FLAG=-1;
    public static final Integer RECYCLE_FLAG=1;
    public static final Integer USING_FLAG=0;

    /*
    * 回收站保存时间
    * */
    public static final Long RECYCLE_TIME=10L;

    public static final Integer SHARE_ID_LENGTH=20;
}
