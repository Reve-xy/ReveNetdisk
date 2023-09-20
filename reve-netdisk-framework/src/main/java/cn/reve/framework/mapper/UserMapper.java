package cn.reve.framework.mapper;

import cn.reve.framework.domain.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author Rêve
 * @since 2023-05-24
 */
public interface UserMapper extends BaseMapper<User> {

    Integer updateUserSpace(@Param("userId") String userId,@Param("useSize") Long useSize);
}
