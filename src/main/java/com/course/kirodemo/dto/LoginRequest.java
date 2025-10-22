package com.course.kirodemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 使用者登入請求 DTO
 * 用於接收前端登入表單資料並進行驗證
 */
public class LoginRequest {
    
    @NotBlank(message = "使用者名稱不能為空")
    @Size(max = 50, message = "使用者名稱長度不能超過50字元")
    private String username;
    
    @NotBlank(message = "密碼不能為空")
    @Size(max = 100, message = "密碼長度不能超過100字元")
    private String password;
    
    // 記住我選項（可選）
    private boolean rememberMe = false;
    
    // 預設建構子
    public LoginRequest() {}
    
    // 建構子
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // 建構子（包含記住我選項）
    public LoginRequest(String username, String password, boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
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
    
    public boolean isRememberMe() {
        return rememberMe;
    }
    
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    /**
     * 驗證登入請求是否有效
     * @return 如果使用者名稱和密碼都不為空則回傳 true
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() && 
               password != null && !password.trim().isEmpty() &&
               username.length() <= 50 && password.length() <= 100;
    }
    
    /**
     * 清除敏感資料（密碼）
     * 用於記錄日誌或除錯時保護密碼資訊
     */
    public void clearSensitiveData() {
        this.password = null;
    }
    
    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", rememberMe=" + rememberMe +
                '}';
    }
}