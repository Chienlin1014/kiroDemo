package com.course.kirodemo.security;

import com.course.kirodemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * 自定義認證提供者
 * 整合應用程式的使用者驗證邏輯與 Spring Security
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public CustomAuthenticationProvider(UserService userService, 
                                      CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 執行認證
     * 
     * @param authentication 認證物件
     * @return Authentication 認證成功的認證物件
     * @throws AuthenticationException 認證失敗時拋出
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            // 載入使用者詳細資訊
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 驗證密碼
            boolean isValidPassword = userService.validatePassword(username, password);
            
            if (!isValidPassword) {
                throw new BadCredentialsException("使用者名稱或密碼錯誤");
            }

            // 認證成功，建立認證物件
            return new UsernamePasswordAuthenticationToken(
                userDetails, 
                password, 
                userDetails.getAuthorities()
            );
            
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("使用者名稱或密碼錯誤");
        }
    }

    /**
     * 檢查是否支援指定的認證類型
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}