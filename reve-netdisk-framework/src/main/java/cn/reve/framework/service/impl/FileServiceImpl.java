package cn.reve.framework.service.impl;

import cn.reve.framework.config.MinioConfig;
import cn.reve.framework.constants.MqConstants;
import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.dto.file.*;
import cn.reve.framework.domain.entity.FileInfo;
import cn.reve.framework.domain.vo.*;
import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.enums.file.*;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.handler.listener.ImageThumbnailListener;
import cn.reve.framework.mapper.FileMapper;
import cn.reve.framework.service.FileService;
import cn.reve.framework.service.UserService;
import cn.reve.framework.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 * 文件表 服务实现类
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
@Service
@Slf4j
public class FileServiceImpl extends ServiceImpl<FileMapper, FileInfo> implements FileService {
    @Resource
    RedisCache redisCache;
    @Resource
    UserService userService;

    @Resource
    MinioUtils minioUtils;
    @Resource
    MinioConfig minioConfig;

    @Resource
    RocketMQTemplate rocketMQTemplate;
    /**
     * @param queryFileDto
     * @return FileListVo
     * @date 2023/5/26 23:10
     * @description 分页分类查询
     */
    @Override
    public PageVo listFilePage(QueryFileDto queryFileDto) {
        // 根据文件类型的en去检索出对应id
        String category = queryFileDto.getCategory();
        Integer categoryId = 0;
        if (StringUtils.hasText(category)) {
            categoryId = FileCategoryEnum.getIdByCategory(category);
        }
        // 获取排序列
        String order = queryFileDto.getOrder();
        SFunction<FileInfo, Object> orderField;
        if (order.contains("time")) {
            orderField = FileInfo::getUpdateTime;
        } else {
            orderField = FileInfo::getFileSize;
        }

        Integer desc = queryFileDto.getDesc();
        Integer pageSize = queryFileDto.getPageSize();
        Integer pageNum = queryFileDto.getPageNum();
        String filePid = queryFileDto.getFilePid();
        String fileName = queryFileDto.getFileName();
        String userId = SecurityUtils.getUserId();

        LambdaQueryWrapper<FileInfo> fl = new LambdaQueryWrapper<>();
        // 有则查，无则不查
        fl.like(StringUtils.hasText(fileName), FileInfo::getFileName, fileName);
        fl.eq(FileInfo::getUserId, userId);
        // 通过父级id查询目录下的文件
        fl.eq(StringUtils.hasText(filePid),FileInfo::getFilePid, filePid);
        fl.eq(!categoryId.equals(0),FileInfo::getFileCategory, categoryId);
        fl.eq(FileInfo::getDelFlag,SystemConstants.USING_FLAG);
        // 判断升序还是降序
        if (desc == SystemConstants.DESC) {
            fl.orderByDesc(orderField);
        } else {
            fl.orderByAsc(orderField);
        }
        // 开始查询
        PageHelper.startPage(pageNum, pageSize);
        List<FileInfo> list = list(fl);
        PageInfo<FileInfo> filePageInfo = new PageInfo<>(list);
        List<FileInfo> fileInfoList = filePageInfo.getList();
        List<FileListVo> fileListVos = BeanCopyUtils.copyBeanList(fileInfoList, FileListVo.class);
        return new PageVo<FileListVo>(fileListVos, (long) fileListVos.size());
    }

    /**
     * @param uploadFileDtos
     * @return UploadStatusVo
     * @date 2023/5/27 19:47
     * @description 上传文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadStatusVo uploadFile(UploadFileDto[] uploadFileDtos) {
        String userId = SecurityUtils.getUserId();
        AtomicInteger sumSize=new AtomicInteger(0);
        Arrays.stream(uploadFileDtos).forEach(m->sumSize.addAndGet(m.getFileSize().intValue()));
        checkUserSpace(userId,sumSize.longValue());

        int i=1;
        for (UploadFileDto uploadFileDto:uploadFileDtos) {
            //上传文件
            String filePath="";
            String url = null;
            try {
                filePath= FileUtils.generateFilePath(FileUtils.getFileSuffix(uploadFileDto.getFileName()));
                minioUtils.upload(uploadFileDto.getFile(),filePath);
                url = minioUtils.getFileUrl(filePath);
            } catch (Exception e) {
                if(!StringUtils.hasText(url)){
                    throw new SystemException(300,"第"+i+"个文件上传出现错误,文件名为"+uploadFileDto.getFileName());
                }
            }

            //重命名
            uploadFileDto.setFileName(renameFileName(userId,uploadFileDto.getFilePid(),uploadFileDto.getFileName(),
                    FileFolderTypeEnum.FILE.getTypeId()));
            FileInfo fileInfo = BeanCopyUtils.copyBean(uploadFileDto, FileInfo.class);
            fileInfo.setFileId(RandomCodeGenerator.generateRandomCode(SystemConstants.FILE_ID_LENGTH));
            fileInfo.setFilePath(filePath);
            fileInfo.setFileUrl(url);
            FileTypeEnum fileTypeEnumBySuffix = FileTypeEnum.getFileTypeEnumBySuffix(FileUtils.getFileSuffix(fileInfo.getFileName()));
            fileInfo.setFileType(fileTypeEnumBySuffix.getTypeId());
            fileInfo.setFileCategory(fileTypeEnumBySuffix.getFileCategoryEnum().getCategoryId());
            fileInfo.setStatus(FileStatusEnum.TANSFER_SUCCESS.getStatus());
            fileInfo.setDelFlag(SystemConstants.USING_FLAG);
            save(fileInfo);
            userService.increaseUserSpace(userId,uploadFileDto.getFileSize());
            //缩略图推送到mq
            if(fileTypeEnumBySuffix.equals(FileTypeEnum.IMAGE)){
                rocketMQTemplate.asyncSend(MqConstants.IMG_THUMB_TOPIC, fileInfo, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info(MqConstants.IMG_THUMB_TOPIC+ fileInfo.getFileName()+sendResult.getSendStatus());
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.info(MqConstants.IMG_THUMB_TOPIC+throwable.getMessage());
                    }
                });
            }
        }
        return new UploadStatusVo();
    }


    /**
     * @param uploadFileDto
     * @return Result
     * @date 2023/6/21 22:49
     * @description 初始化分片上传
     */
    @Override
    public Result initMultipartUpload(UploadFileDto uploadFileDto) {
        String userId = SecurityUtils.getUserId();
        checkUserSpace(userId,uploadFileDto.getFileSize());

        //当前上传的分片offset，默认偏移为1
        Integer  chunkIndex=uploadFileDto.getChunkIndex();

        //是否续传
        String cacheKey = RedisKeyConstants.UPLOADING_FILE_MD5_KEY + userId +":"+ uploadFileDto.getFileMd5();
        UploadFileDto cacheUploadFileDto = redisCache.getCacheObject(cacheKey);
        String filePath;
        if (Objects.nonNull(cacheUploadFileDto)) {
            uploadFileDto=cacheUploadFileDto;
            filePath=uploadFileDto.getFilePath();
        }else{
            filePath = FileUtils.generateFilePath(FileUtils.getFileSuffix(uploadFileDto.getFileName()));
            uploadFileDto.setFilePath(filePath);
        }
        log.info("tip message: 通过 <{}> 开始初始化<分片上传>任务", uploadFileDto);
        log.info("tip message: 当前分片数量 <{}> 进行分片上传", uploadFileDto.getChunks());
        UploadingResVo uploadingResVo = minioUtils.initMultiPartUpload(uploadFileDto.getUploadId(), filePath,chunkIndex, uploadFileDto.getChunks());
        //保存uploadId
        if(!StringUtils.hasText(uploadFileDto.getUploadId())){
            uploadFileDto.setUploadId(uploadingResVo.getUploadId());
        }
        redisCache.setCacheObject(cacheKey, uploadFileDto, minioConfig.getBreakpointTime(), TimeUnit.DAYS);
        return Result.success(uploadingResVo);
    }

    /**
     * 检查文件是否存在，Redis存在，续传，mysql存在，秒传
     *
     * @param uploadFileDto 前端上传的文件对象
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result checkFileUploadedByMd5(UploadFileDto uploadFileDto) {
        // 查询用户的存储空间
        String userId = SecurityUtils.getUserId();
        checkUserSpace(userId, uploadFileDto.getFileSize());
        String fileMd5 = uploadFileDto.getFileMd5();
        log.info("tip message: 通过 <{}> 查询mysql是否存在", fileMd5);
        // 查询数据库是否上传成功
        FileInfo fileInfo = getOne(Wrappers.<FileInfo>lambdaQuery()
                .eq(FileInfo::getFileMd5, fileMd5)
                .eq(FileInfo::getStatus, FileStatusEnum.TANSFER_SUCCESS.getStatus())
                .eq(FileInfo::getDelFlag,SystemConstants.USING_FLAG));
        // 是秒传
        if (Objects.nonNull(fileInfo)) {
            fileInfo.setUserId(userId);
            // 文件id唯一主键，不能重复
            String fileId = RandomCodeGenerator.generateRandomCode(SystemConstants.FILE_ID_LENGTH);
            fileInfo.setFileId(fileId);
            fileInfo.setFileName(renameFileName(userId, uploadFileDto.getFilePid(), uploadFileDto.getFileName(), FileFolderTypeEnum.FILE.getTypeId()));
            fileInfo.setFilePid(uploadFileDto.getFilePid());
            fileInfo.setUserId(userId);
            fileInfo.setDelFlag(SystemConstants.USING_FLAG);
            save(fileInfo);
            userService.increaseUserSpace(userId, uploadFileDto.getFileSize());
            return Result.success(new UploadStatusVo(null, UploadStatusEnum.UPLOAD_SECONDS.getStatus()));
        }
        log.info("tip message: 通过 <{}> 查询redis是否存在", fileMd5);
        // 是否有断点续传
        UploadFileDto cacheUploadFileDto = redisCache.<UploadFileDto>getCacheObject(RedisKeyConstants.UPLOADING_FILE_MD5_KEY + userId+":"+uploadFileDto.getFileMd5());
        if (Objects.nonNull(cacheUploadFileDto)) {
            // 正在上传，查询上传后的分片数据
            List<Integer> chunkList = minioUtils.getChunkByFileMD5(cacheUploadFileDto.getFilePath(), cacheUploadFileDto.getUploadId());
            return Result.success(new UploadStatusVo(cacheUploadFileDto.getUploadId(), UploadStatusEnum.UPLOADING.getStatus(), chunkList));
        }
        // 未上传过
        return Result.success();
    }

    @Resource
    ImageThumbnailListener imageThumbnailListener;
    /**
     * @param uploadFileDto
     * @return void
     * @date 2023/7/7 19:53
     * @description 合并文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeMultipartUpload(UploadFileDto uploadFileDto) {
        log.info("tip message: 通过 <{}> 开始合并<分片上传>任务", uploadFileDto);
        String userId = SecurityUtils.getUserId();
        UploadFileDto cacheUploadFileDto = redisCache.<UploadFileDto>getCacheObject(RedisKeyConstants.UPLOADING_FILE_MD5_KEY + userId+":"+uploadFileDto.getFileMd5());
        //缓存信息存在
        if(cacheUploadFileDto!=null){
            String fileId = RandomCodeGenerator.generateRandomCode(SystemConstants.FILE_ID_LENGTH);
            FileTypeEnum fileTypeEnumBySuffix = FileTypeEnum.getFileTypeEnumBySuffix(FileUtils.getFileSuffix(cacheUploadFileDto.getFileName()));
            Integer typeId = fileTypeEnumBySuffix.getTypeId();
            String filePath = cacheUploadFileDto.getFilePath();
            String filePid = cacheUploadFileDto.getFilePid();

            //异步合并
            // ThreadPoolsUtils.exec.execute(()->{
                    boolean b = minioUtils.mergeMultipartUpload(cacheUploadFileDto.getUploadId(), filePath);
                    if(!b){
                        throw new SystemException(HttpCodeEnum.MERGE_MULTIPART_ERR);
                    }
                    //删除缓存内容
                    redisCache.deleteObject(RedisKeyConstants.UPLOADING_FILE_MD5_KEY + userId+":"+uploadFileDto.getFileMd5());
                    // if(FileTypeEnum.VIDEO.getTypeId().equals(typeId)||FileTypeEnum.IMAGE.getTypeId().equals(typeId)) {
                        /*rocketMQTemplate.syncSend(MqConstants.IMG_THUMB_TOPIC,
                                new ThumbnailMqDTO(userId,fileId, typeId, filePath, cacheUploadFileDto.getFileName()));*/
                        // imageThumbnailListener.onMessage( new ThumbnailMqDTO(userId,fileId, typeId, filePath, cacheUploadFileDto.getFileName()));
                    // }
            // });
            FileInfo fileInfo = BeanCopyUtils.copyBean(cacheUploadFileDto, FileInfo.class);
            fileInfo.setUserId(userId);
            fileInfo.setFileId(fileId);
            fileInfo.setFilePath(filePath);
            fileInfo.setFilePid(filePid);
            fileInfo.setFolderType(FileFolderTypeEnum.FILE.getTypeId());
            fileInfo.setFileUrl(minioUtils.getFileUrl(filePath));
            fileInfo.setFileType(typeId);
            fileInfo.setFileCategory(fileTypeEnumBySuffix.getFileCategoryEnum().getCategoryId());
            fileInfo.setStatus(FileStatusEnum.TANSFER_SUCCESS.getStatus());
            fileInfo.setDelFlag(SystemConstants.USING_FLAG);
            save(fileInfo);
            userService.increaseUserSpace(userId,cacheUploadFileDto.getFileSize());
            if(FileTypeEnum.VIDEO.getTypeId().equals(typeId)||FileTypeEnum.IMAGE.getTypeId().equals(typeId)) {
                        /*rocketMQTemplate.syncSend(MqConstants.IMG_THUMB_TOPIC,
                                new ThumbnailMqDTO(userId,fileId, typeId, filePath, cacheUploadFileDto.getFileName()));*/
                imageThumbnailListener.onMessage( new ThumbnailMqDTO(userId,fileId, typeId, filePath, cacheUploadFileDto.getFileName()));
            }
        }else{
            throw new SystemException(560,"未发现该文件");
        }
    }

    /**
     * 设置缩略图
     * @param fileId
     * @param url
     */
    @Override
    public void setCover(String fileId, String url) {
        FileInfo newFileInfo = new FileInfo();
        newFileInfo.setFileId(fileId);
        newFileInfo.setFileCover(url);
        updateById(newFileInfo);
    }


    /**
     * @param addFolderDTO
     * @return void
     * @date 2023/6/1 21:08
     * @description 新建文件夹
     */
    @Override
    public void createFolder(AddFolderDTO addFolderDTO) {
        String userId = SecurityUtils.getUserId();
        String fileName = addFolderDTO.getFileName();
        String filePid = addFolderDTO.getFilePid();
        String folderName = renameFileName(userId, filePid, fileName, FileFolderTypeEnum.FOLDER.getTypeId());
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilePid(filePid);
        fileInfo.setUserId(userId);
        fileInfo.setFolderType(FileFolderTypeEnum.FOLDER.getTypeId());
        fileInfo.setFileId(RandomCodeGenerator.generateRandomCode(SystemConstants.FILE_ID_LENGTH));
        fileInfo.setFileName(folderName);
        fileInfo.setFileType(FileFolderTypeEnum.FOLDER.getTypeId());
        fileInfo.setFileCategory(FileCategoryEnum.ALL.getCategoryId());
        fileInfo.setStatus(FileStatusEnum.TANSFER_SUCCESS.getStatus());
        fileInfo.setDelFlag(SystemConstants.USING_FLAG);
        save(fileInfo);
    }

    /**
     * @param renameFileDto
     * @return void
     * @date 2023/6/2 8:54
     * @description 重命名文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rename(RenameFileDto renameFileDto) {
        String userId = SecurityUtils.getUserId();
        String fileId = renameFileDto.getFileId();
        String fileName = renameFileDto.getFileName();

        String newFileName;
        FileInfo oldFileInfo = getOne(Wrappers.<FileInfo>lambdaQuery()
                .select(FileInfo::getFileId,FileInfo::getFilePid,FileInfo::getFileName,FileInfo::getFolderType)
                .eq(FileInfo::getFileId, fileId)
                .eq(FileInfo::getUserId, userId).eq(FileInfo::getDelFlag, SystemConstants.USING_FLAG));
        if (Objects.isNull(oldFileInfo)) {
            throw new SystemException(HttpCodeEnum.NO_EXIST_FILE);
        }
        // 重命名
        if (oldFileInfo.getFolderType().equals(FileFolderTypeEnum.FILE.getTypeId())) {
            newFileName = renameFileName(userId, oldFileInfo.getFilePid(), fileName, FileFolderTypeEnum.FILE.getTypeId());
        } else {
            newFileName = renameFileName(userId, oldFileInfo.getFilePid(), fileName, FileFolderTypeEnum.FOLDER.getTypeId());
        }
        // 更改老文件的文件名
        oldFileInfo.setFileName(newFileName);
        updateById(oldFileInfo);
    }

    /**
     * @param filePid
     * @return List<FileListVo>
     * @date 2023/6/2 9:40
     * @description 列举出本目录下的所有文件夹
     */
    @Override
    public List<FileListVo> listFolder(String filePid) {
        if (Objects.isNull(filePid)) {
            filePid = SystemConstants.ROOT_PID;
        }
        String userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<FileInfo> fileLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fileLambdaQueryWrapper.eq(FileInfo::getFilePid, filePid).eq(FileInfo::getUserId, userId)
                .eq(FileInfo::getFolderType, FileFolderTypeEnum.FOLDER.getTypeId()).eq(FileInfo::getDelFlag,SystemConstants.USING_FLAG);
        fileLambdaQueryWrapper.select(FileInfo::getFileId,FileInfo::getFileName);
        List<FileInfo> list = list(fileLambdaQueryWrapper);
        List<FileListVo> fileListVos = BeanCopyUtils.copyBeanList(list, FileListVo.class);
        return fileListVos;
    }

    /**
     * @param moveFileDto
     * @return void
     * @date 2023/6/23 17:16
     * @description 移动文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveFile(MoveFileDto moveFileDto) {
        String[] fileIds = moveFileDto.getFileIds();
        String filePid = moveFileDto.getFilePid();
        if(fileIds.length==0){
            throw new SystemException(HttpCodeEnum.NO_SOURCE_FILE_ID);
        }
        String userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<FileInfo> fileLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fileLambdaQueryWrapper.eq(FileInfo::getUserId, userId).eq(FileInfo::getDelFlag,SystemConstants.USING_FLAG).in(FileInfo::getFileId, fileIds);
        List<FileInfo> list = list(fileLambdaQueryWrapper);
        for (FileInfo fileInfo : list) {
            fileInfo.setFileName(renameFileName(userId, filePid, fileInfo.getFileName(), fileInfo.getFolderType()));
            fileInfo.setFilePid(filePid);
        }
        updateBatchById(list);
    }



    /**
     * 检查用户是否还有空间进行存储
     *
     * @param fileSize
     * @return void （无空间）flase
     */
    public void checkUserSpace(String  userId, Long fileSize) {
        UserSpaceVo userSpaceVo = redisCache.getCacheObject(RedisKeyConstants.USER_SPACE_KEY+ "["+userId+"]");
        Boolean flag = false;
        if (Objects.nonNull(userSpaceVo)) {
            Long useSpace = userSpaceVo.getUseSpace();
            Long totalSpace = userSpaceVo.getTotalSpace();
            if ((fileSize + useSpace) <= totalSpace) {
                flag = true;
            }
        }
        if(!flag){
            throw new SystemException(HttpCodeEnum.NO_SPACE_TO_SAVE);
        }
    }

    /**
     * @param userId
     * @param pid
     * @param fileName
     * @return String
     * @date 2023/5/30 13:42
     * @description 重命名文件名
     */
    @Override
    public String renameFileName(String userId, String pid, String fileName, Integer folderType) {
        FileInfo fileInfo = getOne(Wrappers.<FileInfo>lambdaQuery().select(FileInfo::getFileName)
                .eq(FileInfo::getFilePid, pid).eq(FileInfo::getFileName, fileName)
                .eq(FileInfo::getUserId, userId).eq(FileInfo::getDelFlag,SystemConstants.USING_FLAG));
        if (Objects.nonNull(fileInfo)) {
            //普通文件在文件后加上序号
            if (folderType.equals(FileFolderTypeEnum.FILE.getTypeId())) {
                //查出所有可能的文件
                List<FileInfo> fileInfoList=list(Wrappers.<FileInfo>lambdaQuery().select(FileInfo::getFileName)
                        .eq(FileInfo::getFilePid, pid).eq(FileInfo::getUserId, userId).eq(FileInfo::getDelFlag,SystemConstants.USING_FLAG)
                        .likeRight(FileInfo::getFileName, FileUtils.getFilePrefix(fileName)).ne(FileInfo::getFileName,fileInfo.getFileName()));

                if(!fileInfoList.isEmpty()){
                    AtomicInteger atomicInteger = new AtomicInteger(1);
                    List<String> likeFileNameList = fileInfoList.stream().map(m -> m.getFileName()).collect(Collectors.toList());
                    String newName = FileUtils.renameFile(fileName,atomicInteger.intValue());
                    while(likeFileNameList.contains(newName)){
                        newName = FileUtils.renameFile(fileName, atomicInteger.getAndIncrement());
                    }
                    fileName=newName;
                }
            }
            else {
                //文件夹名后加上时间
                fileName = FileUtils.renameFolder(fileName);
            }
        }
        return fileName;
    }


    private String renameFileId(String userId, String fileId) {
        FileInfo fileInfo = getOne(Wrappers.<FileInfo>lambdaQuery().eq(FileInfo::getFileId, fileId).eq(FileInfo::getDelFlag,SystemConstants.USING_FLAG).eq(FileInfo::getUserId, userId));
        if (!Objects.isNull(fileInfo)) {
            fileId = fileId + RandomCodeGenerator.generateRandomCode(1);
        }
        return fileId;
    }


    /*回收站相关方法*/
    @Override
    public Result<?> getFolderNavigation(String path, String userId){
        if(!StringUtils.hasText(userId)){
            userId=SecurityUtils.getUserId();
        }
        String[] split = path.split("/");
        List<FileInfo> fileInfoList=this.getBaseMapper().
                getFolderNavigation(userId,FileFolderTypeEnum.FOLDER.getTypeId(), split);
        if(fileInfoList.size()!=split.length){
            throw new SystemException(HttpCodeEnum.NO_SUCH_FOLDER);
        }
        return Result.success(fileInfoList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFileByIdsToRecycle(List<String> fileIds) {
        String userId = SecurityUtils.getUserId();
        LocalDateTime now = LocalDateTime.now();
        // 查询该用户需删除的fileIds并且状态为使用中的文件
        List<FileInfo> fileInfoList = list(Wrappers.<FileInfo>lambdaQuery().eq(FileInfo::getUserId,userId)
                .eq(FileInfo::getDelFlag,SystemConstants.USING_FLAG).in(FileInfo::getFileId,fileIds));

        if (fileInfoList.isEmpty()) {
            throw new SystemException(HttpCodeEnum.NO_SUCH_FOLDER);
        }
        // 如果不为空
        List<FileInfo> delFileList = new ArrayList<>(fileInfoList);
        fileInfoList.stream()
                .filter(fileInfo ->
                        fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getTypeId()))
                .forEach(fileInfo ->
                        getSubFileIdList(delFileList, userId, fileInfo.getFileId(),SystemConstants.USING_FLAG));

        //将目录下的所有文件更新为已删除
        List<FileInfo> setDelFileList = delFileList.stream()
                    .map(m -> m.setRecycleTime(now).setExpireTime(now.plusDays(1))
                            .setDelFlag(SystemConstants.RECYCLE_FLAG))
                    .collect(Collectors.toList());

        Long allFileSize=0L;
        for (FileInfo fileInfo : delFileList) {
            if(Objects.nonNull(fileInfo.getFileSize())){
                allFileSize+=fileInfo.getFileSize();
            }
        }
        //更新文件状态
        updateBatchById(delFileList);
        //更新拥有的存储空间
        userService.decreaseUserSpace(userId,allFileSize);
    }

    /**
     * @param fileList delList
     * @param userId
     * @param fileId target FileId
     * @param delFlag
     * @return void
     * @date 2023/8/29 20:38
     * @description 递归查找子文件
     */
    @Override
    public void getSubFileIdList(List<FileInfo> fileList, String userId, String fileId, Integer delFlag){
        // 查找自己下面的所有的文件夹
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId,userId).eq(FileInfo::getFilePid,fileId).eq(FileInfo::getDelFlag,delFlag);
        // query.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        List<FileInfo> fileInfoList = list(wrapper);

        if(fileInfoList.isEmpty()){
            return;
        }
        for (FileInfo fileInfo : fileInfoList) {
            fileList.add(fileInfo);
            if(fileInfo.getFolderType().equals(FileFolderTypeEnum.FILE.getTypeId())){
                continue;
            }
            getSubFileIdList(fileList, userId, fileInfo.getFileId(), delFlag);
        }
    }

    @Override
    public void saveShare(String[] shareFileIds, String myFolderId, String shareUserId, String currentUserId) {
        //选择的文件
        List<FileInfo> targetFileList = list(Wrappers.<FileInfo>lambdaQuery().eq(FileInfo::getUserId, shareUserId)
                .in(FileInfo::getFileId, shareFileIds).eq(FileInfo::getDelFlag, SystemConstants.USING_FLAG));

        List<FileInfo> copyFileList = new ArrayList<>();
        //子文件加入copyFileList
        targetFileList.stream().filter(fileInfo -> fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getTypeId()))
                .forEach(fileInfo -> getSubFileIdList(copyFileList,shareUserId,fileInfo.getFileId(),SystemConstants.USING_FLAG));

        //保存时时父文件要判断是否重名
        targetFileList
                .forEach(m -> m.setFileName(renameFileName(currentUserId ,myFolderId,m.getFileName(),m.getFolderType()))
                );
        targetFileList.addAll(copyFileList);
        //将文件所有者更新为当前用户
        targetFileList.forEach(m->m.setUserId(currentUserId));
        saveBatch(targetFileList);
    }
}
