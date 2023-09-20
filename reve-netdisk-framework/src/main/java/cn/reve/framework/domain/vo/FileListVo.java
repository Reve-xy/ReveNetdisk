package cn.reve.framework.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/26 22:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileListVo {

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 父节点id
     */
    private String filePid;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件封面
     */
    private String fileCover;

    /**
     * 0：普通文件 1：文件夹
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
     * 最后修改时间
     */
    private LocalDateTime updateTime;

    //回收时间
    private LocalDateTime recycleTime;

    //剩余时间（单位day）
    private Long timeLeft;
}
