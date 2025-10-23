package com.course.kirodemo.exception;

/**
 * 不允許延期異常
 * 當待辦事項不符合延期條件時拋出
 */
public class ExtensionNotAllowedException extends RuntimeException {
    
    private final Long todoId;
    private final String reason;
    
    public ExtensionNotAllowedException(Long todoId, String reason) {
        super(String.format("待辦事項 %d 不允許延期: %s", todoId, reason));
        this.todoId = todoId;
        this.reason = reason;
    }
    
    public ExtensionNotAllowedException(String message) {
        super(message);
        this.todoId = null;
        this.reason = null;
    }
    
    public ExtensionNotAllowedException(String message, Throwable cause) {
        super(message, cause);
        this.todoId = null;
        this.reason = null;
    }
    
    public Long getTodoId() {
        return todoId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public static ExtensionNotAllowedException forCompletedTodo(Long todoId) {
        return new ExtensionNotAllowedException(todoId, "待辦事項已完成");
    }
    
    public static ExtensionNotAllowedException forNotDueSoon(Long todoId) {
        return new ExtensionNotAllowedException(todoId, "待辦事項不在三天內到期");
    }
    
    public static ExtensionNotAllowedException forUnauthorizedAccess(Long todoId) {
        return new ExtensionNotAllowedException(todoId, "無權限延期此待辦事項");
    }
}