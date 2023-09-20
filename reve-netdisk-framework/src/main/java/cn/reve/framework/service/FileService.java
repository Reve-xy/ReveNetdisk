package cn.reve.framework.service;

import cn.reve.framework.domain.dto.file.*;
import cn.reve.framework.domain.entity.FileInfo;
import cn.reve.framework.domain.vo.FileListVo;
import cn.reve.framework.domain.vo.PageVo;
import cn.reve.framework.domain.vo.UploadStatusVo;
import cn.reve.framework.utils.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 文件表 服务类
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
public interface FileService extends IService<FileInfo> {

    /**
     * @param queryFileDto
     * @return PageVo
     * @date 2023/6/2 13:33
     * @description 列出所有文件
     */
    PageVo listFilePage(QueryFileDto queryFileDto);

    /**
     * @param uploadsFileDtos
     * @return UploadStatusVo
     * @date 2023/6/2 13:34
     * @description 上传文件
     */
    UploadStatusVo uploadFile(UploadFileDto[] uploadsFileDtos);

    /**
     * @param addFolderDTO
     * @return void
     * @date 2023/6/2 13:34
     * @description 新建文件夹
     */
    void createFolder(AddFolderDTO addFolderDTO);

    /**
     * @param renameFileDto
     * @return void
     * @date 2023/6/2 13:34
     * @description 重命名文件或文件夹
     */
    void rename(RenameFileDto renameFileDto);

    /**
     * @param filePid
     * @return List<FileListVo>
     * @date 2023/6/2 13:35
     * @description 列出所有文件夹
     */
    List<FileListVo> listFolder(String filePid);

    /**
     * @param moveFileDto
     * @return void
     * @date 2023/6/2 13:36
     * @description 移动文件
     */
    void moveFile(MoveFileDto moveFileDto);

    Result initMultipartUpload(UploadFileDto uploadFileDto);

    Result checkFileUploadedByMd5(UploadFileDto uploadFileDto);

    void mergeMultipartUpload(UploadFileDto uploadFileDto);

    void setCover(String fileId, String url);

    Result<?> getFolderNavigation(String path, String userId);

    void removeFileByIdsToRecycle(List<String> fileIds);
     String renameFileName(String userId, String pid, String fileName, Integer fileType);
    void getSubFileIdList(List<FileInfo> fileList, String userId, String fileId, Integer delFlag);

    void saveShare(String[] shareFileIds, String myFolderId,
                    String shareUserId, String currentUserId);
}
