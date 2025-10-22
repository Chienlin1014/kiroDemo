package com.course.kirodemo.service;

import com.course.kirodemo.dto.UserRegistrationRequest;
import com.course.kirodemo.entity.User;

import java.util.Optional;

/**
 * UserService 介面
 * 定義使用者相關的業務邏輯操作
 */
public interface UserService {
    
    /**
     * 註冊新使用者
     * @param registrationRequest 註冊請求資料
     * @return 建立的使用者實體
     * @throws UserAlreadyExistsException 如果使用者名稱已存在
     * @throws IllegalArgumentException 如果註冊資料無效
     */
    User registerUser(UserRegistrationRequest registrationRequest);
    
    /**
     * 根據使用者名稱查詢使用者
     * @param username 使用者名稱
     * @return 使用者實體的 Optional 包裝
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 驗證使用者登入憑證
     * @param username 使用者名稱
     * @param rawPassword 原始密碼（未加密）
     * @return 如果憑證有效則回傳 true
     */
    boolean validatePassword(String username, String rawPassword);
    
    /**
     * 檢查使用者名稱是否已存在
     * @param username 使用者名稱
     * @return 如果存在則回傳 true
     */
    boolean existsByUsername(String username);
    
    /**
     * 加密密碼
     * @param rawPassword 原始密碼
     * @return 加密後的密碼
     */
    String encodePassword(String rawPassword);
}