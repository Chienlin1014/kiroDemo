package com.course.kirodemo.exception;

/**
 * 使用者已存在異常
 * 當嘗試註冊已存在的使用者名稱時拋出
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static UserAlreadyExistsException forUsername(String username) {
        return new UserAlreadyExistsException("使用者名稱 '" + username + "' 已存在");
    }
}