package cn.reve.framework.service.impl;

import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.dto.file.QueryRecycleFileDto;
import cn.reve.framework.domain.entity.FileInfo;
import cn.reve.framework.domain.vo.FileListVo;
import cn.reve.framework.domain.vo.PageVo;
import cn.reve.framework.enums.file.FileFolderTypeEnum;
import cn.reve.framework.service.FileService;
import cn.reve.framework.service.RecycleService;
import cn.reve.framework.service.UserService;
import cn.reve.framework.utils.BeanCopyUtils;
import cn.reve.framework.utils.Result;
import cn.reve.framework.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/8/29 21:00
 */
@Service
public class RecycleServiceImpl implements RecycleService {

    @Resource
    FileService fileService;

    @Override
    public Result<?> loadRecycleList(QueryRecycleFileDto queryRecycleFileDto) {
            String userId = SecurityUtils.getUserId();
            LocalDateTime now = LocalDateTime.now();

        //获取
            LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileInfo::getUserId,userId).eq(FileInfo::getDelFlag, SystemConstants.RECYCLE_FLAG);
            wrapper.gt(FileInfo::getExpireTime,now);
            PageHelper.startPage(queryRecycleFileDto.getPageNum(),queryRecycleFileDto.getPageSize());
            List<FileInfo> recycleList = fileService.list(wrapper);

            List<FileListVo> recycleFileListVo = BeanCopyUtils.copyBeanList(recycleList, FileListVo.class);
            //计算时间（天）
            recycleFileListVo.forEach(m->m.setTimeLeft(covertSecondToDay(m.getRecycleTime(),now)));
            Integer size = recycleFileListVo.size();
            Long total=size.longValue();
            return Result.success(new PageVo<FileListVo>(recycleFileListVo,total));
    }

    @Resource
    UserService userService;
    @Override
    public Result<?> revertFile(List<String> fids) {
        String userId = SecurityUtils.getUserId();
        List<FileInfo> fileInfoList = fileService.list(Wrappers.<FileInfo>lambdaQuery().eq(FileInfo::getUserId,userId)
                .eq(FileInfo::getDelFlag, SystemConstants.RECYCLE_FLAG).in(FileInfo::getFileId, fids));

        List<FileInfo> recoverList=new ArrayList<>();

        //遍历出所有子目录或子文件
        //子文件是不会重名的
        fileInfoList.stream()
                .filter(fileInfo ->
                        fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getTypeId()))
                .forEach(fileInfo ->
                        fileService.getSubFileIdList(recoverList, userId, fileInfo.getFileId(),SystemConstants.RECYCLE_FLAG));

        //还原时父文件要判断是否重名
        fileInfoList
                .forEach(m -> m.setDelFlag(SystemConstants.USING_FLAG)
                        .setFileName(fileService.renameFileName(m.getUserId() ,m.getFilePid(),m.getFileName(),m.getFolderType()))
                );
        //子文件加入父文件
        fileInfoList.addAll(recoverList);
        Long allFileSize=0L;
        for (FileInfo fileInfo : fileInfoList) {
            if(Objects.nonNull(fileInfo.getFileSize())){
                allFileSize+=fileInfo.getFileSize();
            }
        }
        fileService.updateBatchById(fileInfoList);
        userService.increaseUserSpace(userId,allFileSize);
        return Result.success();
    }

    private Long covertSecondToDay(LocalDateTime recycleTime, LocalDateTime now) {
        return SystemConstants.RECYCLE_TIME-ChronoUnit.DAYS.between(recycleTime,now);
    }
}
