package com.course.kirodemo.exception;

/**
 * 無效延期異常
 * 當延期請求包含無效參數時拋出
 */
public class InvalidExtensionException extends RuntimeException {
    
    public InvalidExtensionException(String message) {
        super(message);
    }
    
    public InvalidExtensionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static InvalidExtensionException forInvalidDays(int days) {
        return new InvalidExtensionException("延期天數無效: " + days + "，延期天數必須為正數");
    }
    
    public static InvalidExtensionException forExcessiveDays(int days, int maxDays) {
        return new InvalidExtensionException("延期天數過多: " + days + "，最大允許延期天數為 " + maxDays);
    }
}