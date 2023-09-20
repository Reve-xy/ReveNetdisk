package cn.reve.web.controller;

import cn.reve.framework.annotation.SystemLog;
import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.dto.share.CheckShareCodeDto;
import cn.reve.framework.domain.dto.share.SaveShareDto;
import cn.reve.framework.domain.dto.share.ShareRedisValidDto;
import cn.reve.framework.domain.entity.FileInfo;
import cn.reve.framework.domain.entity.LoginUser;
import cn.reve.framework.domain.entity.ShareInfo;
import cn.reve.framework.domain.entity.User;
import cn.reve.framework.domain.vo.PageVo;
import cn.reve.framework.domain.vo.ShareInfoVO;
import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.service.FileService;
import cn.reve.framework.service.ShareService;
import cn.reve.framework.service.UserService;
import cn.reve.framework.utils.BeanCopyUtils;
import cn.reve.framework.utils.RedisCache;
import cn.reve.framework.utils.Result;
import cn.reve.framework.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 简要描述
 * 外链分享控制器
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/9/5 9:21
 */
@RestController
@RequestMapping("/share")
public class WebShareController {
    @Resource
    UserService userService;
    @Resource
    ShareService shareService;
    @Resource
    FileService fileService;
    @Resource
    RedisCache redisCache;



    /**
     * @param shareId
     * @return Result<?>
     * @date 2023/9/9 14:19
     * @description 获取外链基本信息
     */
    @GetMapping("/getShareInfo")
    @SystemLog("获取外链分享信息")
    public Result<?> getShareInfo(String shareId,String shareToken) {
        //发过来有token，就先认证token
        if(StringUtils.hasText(shareToken)){
            validShareToken(shareToken);
            return Result.success();
        }
        // 解析shareId并验证
        ShareInfo shareInfo = checkDbShare(shareId);

        // FileShare映射为ShareInfoVO
        ShareInfoVO shareInfoVO = BeanCopyUtils.copyBean(shareInfo, ShareInfoVO.class);
        System.out.println(shareInfoVO);
        User userInfo = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getUserId, shareInfo.getUserId())
                        .eq(User::getDelFlag,SystemConstants.USING_FLAG));
        shareInfoVO.setNickName(userInfo.getNickName());
        shareInfoVO.setAvatar(userInfo.getAvatar());
        shareInfoVO.setQqAvatar(userInfo.getQqAvatar());
        shareInfoVO.setUserId(userInfo.getUserId());
        shareInfoVO.setSignature(userInfo.getSignature());

        return Result.success(shareInfoVO);
    }

    /**
     * @param checkShareCodeDto
     * @return Result
     * @date 2023/9/5 10:50
     * @description 按钮第一次请求认证，认证成功返回shareToken，
     */
    @PostMapping("/checkShareCode")
    @SystemLog("验证提取码")
    public Result checkShareCode(@RequestBody CheckShareCodeDto checkShareCodeDto) {
        return shareService.checkShareCode(checkShareCodeDto);
    }

    /**
     * @param shareId
     * @param shareToken
     * @return Result
     * @date 2023/9/9 14:09
     * @description 分享信息页面，首先进行认证
     */
    @GetMapping("/getShareValidInfo")
    @SystemLog("获取分享链接认证信息")
    public Result getShareValidInfo(String shareId,String shareToken) {
        ShareInfo shareInfo = checkDbShare(shareId);
        shareInfo.setTimeLeft(shareService.covertSecondToDay(shareInfo.getExpireType(),shareInfo.getCreateTime(),LocalDateTime.now()));
        LoginUser loginUser = SecurityUtils.getAuthentication().getPrincipal() instanceof LoginUser ? ((LoginUser) SecurityUtils.getAuthentication().getPrincipal()) : null;
        if(Objects.nonNull(loginUser)&&shareInfo.getUserId().equals(loginUser.getUser().getUserId())){
            shareInfo.setIsCurrentUser(true);
            return Result.success(shareInfo);
        }
        validShareToken(shareToken);
        //是否认证过
        return Result.success(shareInfo);
    }

    /**
     * @param shareId
     * @return Result<?>
     * @date 2023/9/9 13:42
     * @description 初始化加载分享文件列表
     */
    @GetMapping("/loadShareFileList")
    @SystemLog("加载分享文件列表")
    public Result<?> loadFileList(Integer pageNum,Integer pageSize,String shareToken,String shareId,String filePid) {
        if(Objects.isNull(pageNum)||Objects.isNull(pageSize)){
            return Result.failure();
        }
        ShareInfo shareInfo = checkDbShare(shareId);
        LoginUser loginUser = SecurityUtils.getAuthentication().getPrincipal() instanceof LoginUser ? ((LoginUser) SecurityUtils.getAuthentication().getPrincipal()) : null;
        if((Objects.isNull(loginUser)||!shareInfo.getUserId().equals(loginUser.getUser().getUserId()) )){
            // 查询出对应链接下的token
            validShareToken(shareToken);
        }
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        if(Objects.nonNull(filePid)&&filePid.equals(SystemConstants.ROOT_PID)){
            String fileId = shareInfo.getFileId();
            String[] fileIds = fileId.split(",");
            queryWrapper.in(FileInfo::getFileId, Arrays.asList(fileIds));
        }else{
            queryWrapper.eq(FileInfo::getFilePid,filePid);
        }
        queryWrapper.eq(FileInfo::getUserId, shareInfo.getUserId()).eq(FileInfo::getDelFlag, SystemConstants.USING_FLAG);
        PageHelper.startPage(pageNum,pageSize);
        List<FileInfo> fileInfos = fileService.list(queryWrapper);
        //更新浏览次数
        redisCache.incrementCacheMapValue(RedisKeyConstants.SHARE_VIEW_COUNT_KEY,shareId,1);
        return Result.success(new PageVo<FileInfo>(fileInfos, (long) fileInfos.size()));
    }

    @GetMapping("/getShareFolderInfo")
    @SystemLog("查看文件夹内容")
    public Result<?> getShareFolderInfo(Integer pageNum,Integer pageSize,String shareId,String shareToken,String filePid) {
        if(Objects.isNull(pageNum)||Objects.isNull(pageSize)){
            return Result.failure();
        }
        ShareInfo shareInfo = checkDbShare(shareId);
        // 查询出对应链接下的token
        validShareToken(shareToken);
        PageHelper.startPage(pageNum,pageSize);
        List<FileInfo> fileInfos = fileService.list(Wrappers.<FileInfo>lambdaQuery().eq(FileInfo::getUserId, shareInfo.getUserId())
                .eq(FileInfo::getFilePid, filePid).eq(FileInfo::getDelFlag, SystemConstants.USING_FLAG));
        return Result.success(new PageVo<FileInfo>(fileInfos, (long) fileInfos.size()));
    }

    @PostMapping ("/saveShare")
    @SystemLog("保存到网盘")
    public Result<?> saveShare(@RequestBody SaveShareDto saveShareDto) {
        ShareInfo shareInfo = checkDbShare(saveShareDto.getShareId());
        // 查询出对应链接下的token
        validShareToken(saveShareDto.getShareToken());
        // 校验登陆用户和分享用户
        String userId = SecurityUtils.getUserId();
        String shareUserId = shareInfo.getUserId();
        if (Objects.nonNull(shareUserId)&&shareUserId.equals(userId)) {
            throw new SystemException(HttpCodeEnum.NO_SAVE_BY_MYSERLF);
        }
        fileService.saveShare(saveShareDto.getShareFileIds(),saveShareDto.getMyFolderId(),shareUserId,userId);
        return Result.success();
    }

    /**
     * @param shareId
     * @return void
     * @date 2023/9/9 14:24
     * @description 判断是否存在或者是否过期
     */
    private ShareInfo checkDbShare(String shareId){
        // 根据shareId获得FileShare
        ShareInfo shareInfo = shareService.getOne(Wrappers.<ShareInfo>lambdaQuery().eq(ShareInfo::getShareId, shareId)
                .eq(ShareInfo::getDelFlag, SystemConstants.USING_FLAG));
        // 如果FileShare为空或者已经过期
        if(Objects.isNull(shareInfo)){
            throw new SystemException(HttpCodeEnum.NO_SUCH_SHARE_LINK);
        }
        LocalDateTime expireTime = shareInfo.getExpireTime();
        if(Objects.nonNull(expireTime)&&expireTime.isBefore(LocalDateTime.now())){
            throw new SystemException(HttpCodeEnum.SHARE_LINK_EXPIRED);
        }
        return shareInfo;
    }
    
    /**
     * @param shareToken
     * @return ShareRedisValidDto
     * @date 2023/9/9 14:28
     * @description 判断token是否失效
     */
    private ShareRedisValidDto validShareToken(String shareToken) {
        ShareRedisValidDto shareRedisValidDto = redisCache.getCacheObject(RedisKeyConstants.SHARE_CODE_VALID_PASS + shareToken);
        if (Objects.isNull(shareRedisValidDto)) {
            throw new SystemException(HttpCodeEnum.SHARE_TOKEN_VALID_EXPIRED);
        }
        return shareRedisValidDto;
    }

}
