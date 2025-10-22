package com.course.kirodemo.exception;

/**
 * 待辦事項不存在異常
 * 當查詢不存在的待辦事項時拋出
 */
public class TodoNotFoundException extends RuntimeException {
    
    public TodoNotFoundException(String message) {
        super(message);
    }
    
    public TodoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static TodoNotFoundException forId(Long todoId) {
        return new TodoNotFoundException("找不到待辦事項，ID: " + todoId);
    }
}