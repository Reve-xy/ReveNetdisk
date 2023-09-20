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
 * 分享列表
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
@Data
@Accessors(chain = true)
@TableName("share_info")
@AllArgsConstructor
@NoArgsConstructor
public class ShareInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分享链接id
     */
    @TableId(value = "share_id", type = IdType.INPUT)
    private String shareId;

    /**
     * 文件id
     */
    private String fileId;

    private String fileName;

    /**
     * 文件封面
     */
    private String fileCover;

    /**
     * 0：文件夹 1：普通文件
     */
    private Integer folderType;
    /**
     * 0：视频 1：音频 2：图片 3：PDF 4：Excel 5：Word	6：txt 7：code 8：zip 9：其他
     */
    private Integer fileType;

    /**
     * 用户id
     */
    private String userId;

    /**
     *-1:永久有效
     */
    private Long expireType;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 提取码
     */
    private String code;

    /**
     * 访问量
     */
    private Long viewCount;
    /**
     * 保存量
     */
    private Long saveCount;

    /**
     * 下载量
     */
    private Long downloadCount;

    /**
     * 0正常，1删除-取消分享
     */
    private Integer delFlag;

    @TableField(exist = false)
    private Long timeLeft;

    @TableField(exist = false)
    private Boolean isCurrentUser;
}
