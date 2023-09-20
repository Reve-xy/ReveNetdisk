package cn.reve.framework.service;

import cn.reve.framework.domain.entity.User;
import cn.reve.framework.domain.vo.UserInfoVo;
import cn.reve.framework.domain.vo.UserSpaceVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
public interface UserService extends IService<User> {

    UserInfoVo getUserInfo(String userId);

    UserSpaceVo getUserSpace(String userId);

    void increaseUserSpace(String userId, Long useSize);
    void decreaseUserSpace(String userId, Long fileSize);
}
