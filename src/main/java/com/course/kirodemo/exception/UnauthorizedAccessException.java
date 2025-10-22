package com.course.kirodemo.exception;

/**
 * 未授權存取異常
 * 當使用者嘗試存取不屬於自己的資源時拋出
 */
public class UnauthorizedAccessException extends RuntimeException {
    
    public UnauthorizedAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UnauthorizedAccessException() {
        super("您沒有權限存取此資源");
    }
}