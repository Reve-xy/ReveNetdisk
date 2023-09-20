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
 * 文件表
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("file_info")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件id
     */
    @TableId(value = "file_id",type=IdType.INPUT)
    private String fileId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 父节点id
     */
    private String filePid;

    /**
     * 文件md5
     */
    private String fileMd5;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件封面
     */
    private String fileCover;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件外链
     */
    private String fileUrl;

    /**
     * 0：文件夹 1：普通文件
     */
    private Integer folderType;

    /**
     * 0：全部 1：文档 2：视频 3：音频 4：图片 5：其他
     */
    private Integer fileCategory;

    /**
     * 0：视频 1：音频 2：图片 3：PDF 4：Excel 5：Word	6：txt 7：code 8：zip 9：其他
     */
    private Integer fileType;

    /**
     * 0：转码中 1：转码成功 2：转码失败
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 最后修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 进入回收站的时间
     */
    private LocalDateTime recycleTime;

    private LocalDateTime expireTime;

    /**
     * 0：正常，1：进入回收站
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;


}
