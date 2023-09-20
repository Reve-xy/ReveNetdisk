package cn.reve.web.job;

import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.domain.entity.ShareInfo;
import cn.reve.framework.service.ShareService;
import cn.reve.framework.utils.RedisCache;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 简要描述
 * 定时转存数据库
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/9/4 17:05
 */
@Component
public class UpdateShareCountDataTask {
    @Resource
    ShareService shareService;
    @Resource
    RedisCache redisCache;

    public void updateShareDataCount(){
        Map<String, Long> viewCountMap = redisCache.getCacheMap(RedisKeyConstants.SHARE_VIEW_COUNT_KEY);
        Map<String, Long> saveCountMap = redisCache.getCacheMap(RedisKeyConstants.SHARE_SAVE_COUNT_KEY);
        Map<String, Long> downloadCountMap = redisCache.getCacheMap(RedisKeyConstants.SHARE_DOWNLOAD_COUNT_KEY);

        List<ShareInfo> viewCountList = viewCountMap.entrySet().stream().map(s -> new ShareInfo().setShareId(s.getKey())
                .setViewCount(s.getValue())).collect(Collectors.toList());
        List<ShareInfo> saveCountList = saveCountMap.entrySet().stream().map(s -> new ShareInfo().setShareId(s.getKey())
                .setViewCount(s.getValue())).collect(Collectors.toList());
        List<ShareInfo> downloadCountList = downloadCountMap.entrySet().stream().map(s -> new ShareInfo().setShareId(s.getKey())
                .setViewCount(s.getValue())).collect(Collectors.toList());

        //通过主键批量更新
        shareService.updateBatchById(viewCountList);
        shareService.updateBatchById(saveCountList);
        shareService.updateBatchById(downloadCountList);
    }
}
