package com.course.kirodemo.exception;

/**
 * 使用者不存在異常
 * 當查詢不存在的使用者時拋出
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static UserNotFoundException forUsername(String username) {
        return new UserNotFoundException("找不到使用者: " + username);
    }
}