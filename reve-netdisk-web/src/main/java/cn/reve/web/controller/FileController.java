package cn.reve.web.controller;

import cn.reve.framework.annotation.SystemLog;
import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.dto.file.*;
import cn.reve.framework.domain.entity.FileInfo;
import cn.reve.framework.domain.vo.FileListVo;
import cn.reve.framework.domain.vo.PageVo;
import cn.reve.framework.domain.vo.UploadStatusVo;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.service.FileService;
import cn.reve.framework.utils.MinioUtils;
import cn.reve.framework.utils.Result;
import cn.reve.framework.utils.WebUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/25 15:18
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController extends BaseController{

    @Resource
    FileService fileService;


    /**
     * @param queryFileDto
     * @return Result
     * @date 2023/5/26 23:51
     * @description 分页分类查询接口
     */
    @GetMapping("/listFile")
    @SystemLog("分页分类查询接口")
    public Result<?> listFile(QueryFileDto queryFileDto) {
        PageVo<FileListVo> filePageVos=fileService.listFilePage(queryFileDto);
        return Result.success(filePageVos);
    }

    @PostMapping("/uploadFile")
    @SystemLog("上传文件接口")
    public Result<?> uploadFile(@RequestBody UploadFileDto[] uploadFilesDtos) {
        UploadStatusVo uploadStatusVo=fileService.uploadFile(uploadFilesDtos);
        return Result.success(uploadStatusVo);
    }

    /**
     * @param uploadFileDto
     * @return Result
     * @date 2023/6/21 23:07
     * @description 初始化分片上传
     */
    @PostMapping("/initMultipart")
    @SystemLog("初始化分片上传")
    public Result initMultipartUpload(@RequestBody UploadFileDto uploadFileDto){
        return fileService.initMultipartUpload(uploadFileDto);
    }

    @PostMapping("/mergeMultipartUpload")
    @SystemLog("合并分片上传")
    public Result mergeMultipartUpload(@RequestBody UploadFileDto uploadFileDto){
        fileService.mergeMultipartUpload(uploadFileDto);
        return Result.success();
    }

    /**
     * 重新上传文件，查看文件是否存在（秒传逻辑）
     * @param uploadFileDto
     * @return
     */
    @PostMapping("/multipart/check")
    @SystemLog("检查文件是否存在")
    public Result checkFileUploadedByMd5(@RequestBody UploadFileDto uploadFileDto) {
        log.info("REST: 通过查询 <{}> 文件是否存在、是否进行断点续传", uploadFileDto);
        if (Objects.isNull(uploadFileDto)) {
            log.error("查询文件是否存在、入参无效");
            return Result.failure(400,"参数有误");
        }
        return fileService.checkFileUploadedByMd5(uploadFileDto);
    }

    @PostMapping("/folder")
    @SystemLog("新建文件夹")
    public Result<?> createFolder(@RequestBody AddFolderDTO addFolderDTO) {
        fileService.createFolder(addFolderDTO);
        return Result.success();
    }

    @GetMapping("/search")
    @SystemLog("根据名称查询文件")
    public Result<?> search(QueryFileDto queryFileDto) {
        PageVo<FileListVo> filePageSearchVos=fileService.listFilePage(queryFileDto);
        return Result.success(filePageSearchVos);
    }

    @PutMapping("/rename")
    @SystemLog("文件重命名")
    public Result<?> rename(@RequestBody RenameFileDto renameFileDto) {
        fileService.rename(renameFileDto);
        return Result.success();
    }


    @GetMapping("/getAllFolder")
    @SystemLog("获取各目录下的所有文件夹")
    public Result<?> getAllFolder(String filePid) {
        List<FileListVo> folderListVos=fileService.listFolder(filePid);
        return Result.success(folderListVos);
    }

    /**
     * @param path
     * @param userId
     * @return Result<?>
     * @date 2023/8/25 20:22
     * @description 用于前端导航面包屑的路径获取，根据url上不同的参数，返回对应面包屑应该渲染的值
     */
    @GetMapping("/getFolderNavigation")
    @SystemLog("获取当前路径下的文件夹导航")
    public Result<?> getFolderNavigation(String path,String userId) {
        return fileService.getFolderNavigation(path,userId);
    }

    @PutMapping("moveFile")
    @SystemLog("移动文件")
    public Result<?> moveFile(@RequestBody @Valid MoveFileDto moveFileDto) {
        fileService.moveFile(moveFileDto);
        return Result.success();
    }

    @DeleteMapping("/deleteFiles")
    @SystemLog("删除文件或文件夹到回收站")
    public Result<?> deleteFiles(@RequestBody List<String> fileIds) {
        if(fileIds==null||fileIds.size()==0){
            throw new SystemException(555,"您暂未选择需要删除的文件或文件夹");
        }
        fileService.removeFileByIdsToRecycle(fileIds);
        return Result.success();
    }

    @Resource
    MinioUtils minioUtils;

    @GetMapping("/downloadFile")
    @SystemLog("下载文件")
    public void downloadFile(@RequestParam("fileName") String fileName, HttpServletResponse response){
        if(!StringUtils.hasText(fileName)){
            WebUtils.renderString(response,"文件下载失败，文件名为空");
            return ;
        }
        FileInfo fileInfo =
                fileService.getOne(Wrappers.<FileInfo>lambdaQuery().
                        eq(FileInfo::getDelFlag, SystemConstants.USING_FLAG)
                        .eq(FileInfo::getFileName, fileName).select(FileInfo::getFilePath));
        String filePath = fileInfo.getFilePath();
        if(!StringUtils.hasText(filePath)){
            WebUtils.renderString(response,"文件下载失败，文件不存在");
            return ;
        }
        minioUtils.downloadFile(fileName,filePath,response);
    }

}
