package cn.reve.framework.service.impl;

import cn.reve.framework.constants.SystemConstants;
import cn.reve.framework.domain.entity.LoginUser;
import cn.reve.framework.domain.entity.User;
import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 简要描述
 * security
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/23 15:32
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    UserMapper userMapper;


    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user;
        if(userName.contains("@")){
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, userName));
        }else{
           user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userName));
        }
        if(Objects.isNull(user)){
            throw new SystemException(HttpCodeEnum.USER_NOT_FOUND);
        }else if(!SystemConstants.USER_STATUS_NORMAL.equals(user.getStatus())){
            throw new SystemException(HttpCodeEnum.USER_DISABLED);
        }
        return new LoginUser(user);
    }
}
