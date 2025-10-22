package com.course.kirodemo.security;

import com.course.kirodemo.entity.User;
import com.course.kirodemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * 自定義使用者詳細資訊服務
 * 整合 Spring Security 與應用程式的使用者管理系統
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    /**
     * 根據使用者名稱載入使用者詳細資訊
     * 
     * @param username 使用者名稱
     * @return UserDetails 使用者詳細資訊
     * @throws UsernameNotFoundException 當使用者不存在時拋出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("使用者不存在: " + username);
        }
        
        User user = userOpt.get();
        
        return new CustomUserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            getAuthorities(),
            true, // enabled
            true, // accountNonExpired
            true, // credentialsNonExpired
            true  // accountNonLocked
        );
    }

    /**
     * 獲取使用者權限
     * 目前所有使用者都有 USER 角色
     */
    private Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}