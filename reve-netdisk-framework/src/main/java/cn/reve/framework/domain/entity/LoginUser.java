package cn.reve.framework.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 简要描述
 * 实现userdetails接口，实现从数据库里查用户
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/23 15:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser implements UserDetails {
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserId();
    }

    /**
     * @param
     * @return boolean
     * @date 2023/4/23 15:39
     * @description 账号是否过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * @param 
     * @return boolean
     * @date 2023/4/23 15:40
     * @description 账号是否被锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * @param 
     * @return boolean
     * @date 2023/4/23 15:40
     * @description 密码是否过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * @param 
     * @return boolean
     * @date 2023/4/23 15:40
     * @description 账号是否可用
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
