package cn.reve.framework.domain.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/27 13:49
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileDto {

    /**
     * 文件对象
     */
    private MultipartFile file;

    /**
     * 文件名称
     */
    private String fileName;


    /**
     * 文件id,后端生成，故第一片上传没有uploadId
     */
    private String uploadId;

    /**
     * 父节点id
     */
    private String filePid;

    /**
     * 文件md5，前端分页的md5
     */
    private String fileMd5;

    /**
     * 分片片码
     */
    private Integer chunkIndex;

    /**
     * 总分片数
     */
    private Integer chunks;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件保存的路径
     */
    private String filePath;
}
