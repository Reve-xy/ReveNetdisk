package cn.reve.framework.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
@Data
@Accessors(chain = true)
@TableName("user_info")
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 列号id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String userId;

    /**
     * 签名
     */
    private String signature;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * email
     */
    private String email;

    /**
     * qq
     */
    private String qqOpenId;

    /**
     * qq头像
     */
    private String qqAvatar;
    
        /**
     * 网盘头像
     */
    private String avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 创建日期
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 最近登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 状态，0禁用，1启用
     */
    private Integer status;

    /**
     * 使用的空间，byte
     */
    private Long useSpace;

    /**
     * 总空间
     */
    private Long totalSpace;

	 /**
     * 0：正常，1：进入回收站
     */
    private Integer delFlag;


}
