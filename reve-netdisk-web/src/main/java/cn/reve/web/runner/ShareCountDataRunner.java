package cn.reve.web.runner;

import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.entity.ShareInfo;
import cn.reve.framework.mapper.ShareMapper;
import cn.reve.framework.utils.RedisCache;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 简要描述
 * 项目启动获取
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/9/4 16:38
 */
public class ShareCountDataRunner implements CommandLineRunner {
    @Resource
    ShareMapper shareMapper;

    @Resource
    RedisCache redisCache;

    /**
     * @param args
     * @return void
     * @date 2023/9/4 17:04
     * @description 将访问量等数据存入Redis
     */
    @Override
    public void run(String... args) throws Exception {
        List<ShareInfo> shareInfos = shareMapper.selectList(Wrappers.<ShareInfo>lambdaQuery().
                eq(ShareInfo::getDelFlag, SystemConstants.USING_FLAG));
        redisCache.setCacheMap(RedisKeyConstants.SHARE_VIEW_COUNT_KEY,
                shareInfos.stream().collect(Collectors.toMap(ShareInfo::getShareId,ShareInfo::getViewCount))
                );
        redisCache.setCacheMap(RedisKeyConstants.SHARE_SAVE_COUNT_KEY,
                shareInfos.stream().collect(Collectors.toMap(ShareInfo::getShareId,ShareInfo::getSaveCount))
        );
        redisCache.setCacheMap(RedisKeyConstants.SHARE_DOWNLOAD_COUNT_KEY,
                shareInfos.stream().collect(Collectors.toMap(ShareInfo::getShareId,ShareInfo::getDownloadCount))
        );
    }

    private Map<String,Long> objectToMap(Object obj){
        if(obj==null) {
            return null;
        }
        Map<String, Long> hashMap = new HashMap<>();
        try {
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                hashMap.put(field.getName(), (Long) field.get(obj));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return hashMap;
    }
}
