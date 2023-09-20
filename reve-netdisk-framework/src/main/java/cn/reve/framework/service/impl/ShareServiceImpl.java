package cn.reve.framework.service.impl;

import cn.reve.framework.constants.RedisExprieConstants;
import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.dto.share.CheckShareCodeDto;
import cn.reve.framework.domain.dto.share.QueryShareDto;
import cn.reve.framework.domain.dto.share.ShareFileDto;
import cn.reve.framework.domain.dto.share.ShareRedisValidDto;
import cn.reve.framework.domain.entity.FileInfo;
import cn.reve.framework.domain.entity.ShareInfo;
import cn.reve.framework.domain.vo.PageVo;
import cn.reve.framework.domain.vo.ShareFileVo;
import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.enums.ShareValidTypeEnums;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.mapper.ShareMapper;
import cn.reve.framework.service.FileService;
import cn.reve.framework.service.ShareService;
import cn.reve.framework.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 分享列表 服务实现类
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, ShareInfo> implements ShareService {

    @Override
    public Result<?> listShareFile(QueryShareDto queryShareDto) {
        Integer pageNum = queryShareDto.getPageNum();
        Integer pageSize = queryShareDto.getPageSize();
        if(Objects.isNull(pageNum)||Objects.isNull(pageSize)){
            throw new SystemException(HttpCodeEnum.PAGE_NULL);
        }
        String userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<ShareInfo> shareLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shareLambdaQueryWrapper.eq(ShareInfo::getUserId,userId).eq(ShareInfo::getDelFlag, SystemConstants.USING_FLAG);
        PageHelper.startPage(pageNum,pageSize);
        List<ShareInfo> list = list(shareLambdaQueryWrapper);
        list.forEach(s->s.setTimeLeft(covertSecondToDay(s.getExpireType(),s.getCreateTime(),LocalDateTime.now())));
        return Result.success(new PageVo<ShareInfo>(list, (long) list.size()));
    }

    @Resource
    FileService fileService;
    @Override
    public Result<?> saveShareFile(ShareFileDto shareFileDto) {
        ShareValidTypeEnums shareType = ShareValidTypeEnums.getByType(shareFileDto.getExpireType());
        if(Objects.isNull(shareType)){
            throw new SystemException(HttpCodeEnum.SHARE_TYPE_NULL);
        }
        if(shareFileDto.getFileIds()==null){
            throw new SystemException(HttpCodeEnum.NO_SELECT_SHARE_FILE);
        }
        Boolean isDiy = shareFileDto.getIsDiy();
        String code=shareFileDto.getCode();
        if(!isDiy){
            code= RandomCodeGenerator.generateRandomCode(SystemConstants.CODE_LENGTH);
        }
        //多选对fileId进行处理
        String fileId=shareFileDto.getFileIds()[0];
        if(shareFileDto.getFileIds().length>1){
            //分割Id
            fileId= String.join(",",shareFileDto.getFileIds());
        }
        FileInfo fileInfo = fileService.getOne(Wrappers.<FileInfo>lambdaQuery()
                .select(FileInfo::getFileType, FileInfo::getFolderType, FileInfo::getFileCover)
                .eq(FileInfo::getFileId, shareFileDto.getFileIds()[0])
                .eq(FileInfo::getUserId, SecurityUtils.getUserId())
                .eq(FileInfo::getDelFlag, SystemConstants.USING_FLAG));

        //保存信息
        LocalDateTime now = LocalDateTime.now();
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setUserId(SecurityUtils.getUserId());
        shareInfo.setFileId(fileId);
        shareInfo.setShareId(RandomCodeGenerator.generateRandomCode(SystemConstants.SHARE_ID_LENGTH));
        shareInfo.setFileName(shareFileDto.getFileName()).setFileType(fileInfo.getFileType())
                .setFolderType(fileInfo.getFolderType()).setFileCover(fileInfo.getFileCover());
        shareInfo.setCode(code).setViewCount(0L).setSaveCount(0L).setDownloadCount(0L).setCreateTime(now);
        shareInfo.setExpireType(shareType.getDays()).setExpireTime(now.plusDays(shareType.getDays())).setDelFlag(SystemConstants.USING_FLAG);
        save(shareInfo);

        ShareFileVo shareFileVo = BeanCopyUtils.copyBean(shareInfo, ShareFileVo.class);
        return Result.success(shareFileVo);
    }

    @Resource
    RedisCache redisCache;
    @Override
    public Result<?> delShareFile(List<String> shareIds) {
        if(shareIds.isEmpty()){
            throw new SystemException(HttpCodeEnum.NO_SELECT_DATA);
        }
        /*String userId = SecurityUtils.getUserId();
        List<ShareInfo> list = list(Wrappers.<ShareInfo>lambdaQuery().select(ShareInfo::getShareId).
                eq(ShareInfo::getUserId, userId).in(ShareInfo::getShareId, shareIds));*/
        /*list.forEach(s->{
            s.setDelFlag(SystemConstants.DEL_FLAG);
        });*/

        String[] hkeys = shareIds.toArray(new String[shareIds.size()]);

        //移除后删除指定的redis hash
        redisCache.delCacheMapValue(RedisKeyConstants.SHARE_VIEW_COUNT_KEY,hkeys);
        redisCache.delCacheMapValue(RedisKeyConstants.SHARE_SAVE_COUNT_KEY,hkeys);
        redisCache.delCacheMapValue(RedisKeyConstants.SHARE_DOWNLOAD_COUNT_KEY,hkeys);

        // updateBatchById(list);
        removeByIds(shareIds);//直接删除
        return Result.success();

    }

    @Resource
    JwtUtils jwtUtils;
    @Override
    public Result<?> checkShareCode(CheckShareCodeDto checkShareCodeDto) {
        String shareId = checkShareCodeDto.getShareId();
        String code = checkShareCodeDto.getCode();
        ShareInfo shareInfo = getById(shareId);
        if(Objects.isNull(shareInfo)){
            throw new SystemException(HttpCodeEnum.NO_SUCH_SHARE_LINK);
        }
        if(shareInfo.getExpireTime().isBefore(LocalDateTime.now())){
            throw new SystemException(HttpCodeEnum.SHARE_LINK_EXPIRED);
        }
        if(!shareInfo.getCode().equalsIgnoreCase(code)){
            throw new SystemException(HttpCodeEnum.SHARE_CODE_ERR);
        }else{
            //分享提取码一个小时内有效
            String shareToken = jwtUtils.createJWT(shareId, RedisExprieConstants.SHARE_TOKEN_EXPIRE);
            ShareRedisValidDto shareRedisValidDto = BeanCopyUtils.copyBean(shareInfo, ShareRedisValidDto.class);
            redisCache.setCacheObject(RedisKeyConstants.SHARE_CODE_VALID_PASS+shareToken
                    , shareRedisValidDto
                    ,RedisExprieConstants.SHARE_TOKEN_EXPIRE, TimeUnit.HOURS);

            return Result.success(shareToken);
        }
    }


    @Override
    public Long covertSecondToDay(Long validDate,LocalDateTime createTime, LocalDateTime now) {
        if(validDate.equals(ShareValidTypeEnums.FOREVER.getDays())){
            return validDate;
        }
        return validDate- ChronoUnit.DAYS.between(createTime,now);
    }
}
