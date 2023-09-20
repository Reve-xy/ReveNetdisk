package cn.reve.framework.service.impl;

import cn.reve.framework.annotation.CacheFind;
import cn.reve.framework.constants.RedisKeyConstants;
import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.entity.User;
import cn.reve.framework.domain.vo.UserInfoVo;
import cn.reve.framework.domain.vo.UserSpaceVo;
import cn.reve.framework.mapper.UserMapper;
import cn.reve.framework.service.UserService;
import cn.reve.framework.utils.BeanCopyUtils;
import cn.reve.framework.utils.RedisCache;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    @CacheFind(key= RedisKeyConstants.USER_INFO_KEY)
    public UserInfoVo getUserInfo(String userId) {
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getUserId, userId).eq(User::getDelFlag, SystemConstants.USING_FLAG));
        UserInfoVo userInfoVo = BeanCopyUtils.copyBean(user, UserInfoVo.class);
        return userInfoVo;
    }

    @Override
    @CacheFind(key= RedisKeyConstants.USER_SPACE_KEY)
    public UserSpaceVo getUserSpace(String userId) {
        User user = getOne(Wrappers.<User>lambdaQuery()
                .eq(User::getDelFlag, SystemConstants.USING_FLAG).eq(User::getUserId, userId).select(User::getUserId,User::getUseSpace,User::getTotalSpace));
        UserSpaceVo userSpaceVo = BeanCopyUtils.copyBean(user, UserSpaceVo.class);
        return userSpaceVo;
    }


    @Resource
    RedisCache redisCache;
    @Override
    public void increaseUserSpace(String userId, Long fileSize) {
        UserSpaceVo userSpaceVo = redisCache.getCacheObject(RedisKeyConstants.USER_SPACE_KEY +"["+ userId+"]");
        Long totalSize=userSpaceVo.getUseSpace()+fileSize;
        userSpaceVo.setUseSpace(totalSize);
        this.baseMapper.updateUserSpace(userId,totalSize);
        redisCache.setCacheObject(RedisKeyConstants.USER_SPACE_KEY+"["+userId+"]",userSpaceVo);
    }

    @Override
    public void decreaseUserSpace(String userId, Long fileSize) {
        UserSpaceVo userSpaceVo = redisCache.getCacheObject(RedisKeyConstants.USER_SPACE_KEY +"["+ userId+"]");
        Long totalSize=userSpaceVo.getUseSpace()-fileSize;
        userSpaceVo.setUseSpace(totalSize);
        this.baseMapper.updateUserSpace(userId,totalSize);
        redisCache.setCacheObject(RedisKeyConstants.USER_SPACE_KEY+"["+userId+"]",userSpaceVo);
    }


}
