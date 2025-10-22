package com.course.kirodemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.course.kirodemo.entity.User;

/**
 * 使用者註冊請求 DTO
 * 用於接收前端註冊表單資料並進行驗證
 */
public class UserRegistrationRequest {
    
    @NotBlank(message = "使用者名稱不能為空")
    @Size(min = 3, max = 50, message = "使用者名稱長度必須在3到50字元之間")
    private String username;
    
    @NotBlank(message = "密碼不能為空")
    @Size(min = 4, max = 100, message = "密碼長度必須在4到100字元之間")
    private String password;
    
    @NotBlank(message = "確認密碼不能為空")
    private String confirmPassword;
    
    // 預設建構子
    public UserRegistrationRequest() {}
    
    // 建構子
    public UserRegistrationRequest(String username, String password, String confirmPassword) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
    
    // Getter 和 Setter 方法
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    /**
     * 驗證密碼是否一致
     * @return 如果密碼和確認密碼一致則回傳 true
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
    
    /**
     * 轉換為 User 實體
     * 注意：密碼需要在服務層進行加密處理
     * @return User 實體（密碼未加密）
     */
    public User toEntity() {
        User user = new User();
        user.setUsername(this.username);
        user.setPassword(this.password); // 密碼將在服務層加密
        return user;
    }
    
    /**
     * 轉換為 User 實體（使用加密密碼）
     * @param encodedPassword 已加密的密碼
     * @return User 實體（密碼已加密）
     */
    public User toEntity(String encodedPassword) {
        User user = new User();
        user.setUsername(this.username);
        user.setPassword(encodedPassword);
        return user;
    }
    
    /**
     * 驗證註冊請求是否有效
     * @return 如果所有驗證都通過則回傳 true
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() && 
               username.length() >= 3 && username.length() <= 50 &&
               password != null && !password.trim().isEmpty() && 
               password.length() >= 4 && password.length() <= 100 &&
               isPasswordMatching();
    }
    
    @Override
    public String toString() {
        return "UserRegistrationRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                '}';
    }
}