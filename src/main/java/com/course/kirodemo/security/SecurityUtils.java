package com.course.kirodemo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具類
 * 提供獲取當前認證使用者資訊的便利方法
 */
public class SecurityUtils {

    /**
     * 獲取當前認證的使用者名稱
     * 
     * @return 使用者名稱，如果未認證則返回 null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }

    /**
     * 獲取當前認證的使用者 ID
     * 
     * @return 使用者 ID，如果未認證或無法獲取則返回 null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) principal).getId();
        }
        
        return null;
    }

    /**
     * 檢查當前使用者是否已認證
     * 
     * @return true 如果已認證，否則 false
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 獲取當前認證物件
     * 
     * @return Authentication 物件，如果未認證則返回 null
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}