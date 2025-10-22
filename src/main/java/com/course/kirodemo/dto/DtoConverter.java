package com.course.kirodemo.dto;

import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;

/**
 * DTO 轉換工具類別
 * 提供實體與 DTO 之間的轉換方法
 */
public class DtoConverter {
    
    /**
     * 將 CreateTodoRequest 轉換為 TodoItem 實體
     * @param request CreateTodoRequest DTO
     * @param user 關聯的使用者
     * @return TodoItem 實體
     */
    public static TodoItem toEntity(CreateTodoRequest request, User user) {
        if (request == null) {
            return null;
        }
        return request.toEntity(user);
    }
    
    /**
     * 將 TodoItem 實體轉換為 CreateTodoRequest DTO
     * @param todoItem TodoItem 實體
     * @return CreateTodoRequest DTO
     */
    public static CreateTodoRequest toCreateRequest(TodoItem todoItem) {
        if (todoItem == null) {
            return null;
        }
        return CreateTodoRequest.fromEntity(todoItem);
    }
    
    /**
     * 將 TodoItem 實體轉換為 UpdateTodoRequest DTO
     * @param todoItem TodoItem 實體
     * @return UpdateTodoRequest DTO
     */
    public static UpdateTodoRequest toUpdateRequest(TodoItem todoItem) {
        if (todoItem == null) {
            return null;
        }
        return UpdateTodoRequest.fromEntity(todoItem);
    }
    
    /**
     * 使用 UpdateTodoRequest 更新 TodoItem 實體
     * @param request UpdateTodoRequest DTO
     * @param todoItem 要更新的 TodoItem 實體
     */
    public static void updateEntity(UpdateTodoRequest request, TodoItem todoItem) {
        if (request != null && todoItem != null) {
            request.updateEntity(todoItem);
        }
    }
    
    /**
     * 將 UserRegistrationRequest 轉換為 User 實體
     * @param request UserRegistrationRequest DTO
     * @return User 實體（密碼未加密）
     */
    public static User toEntity(UserRegistrationRequest request) {
        if (request == null) {
            return null;
        }
        return request.toEntity();
    }
    
    /**
     * 將 UserRegistrationRequest 轉換為 User 實體（使用加密密碼）
     * @param request UserRegistrationRequest DTO
     * @param encodedPassword 已加密的密碼
     * @return User 實體（密碼已加密）
     */
    public static User toEntity(UserRegistrationRequest request, String encodedPassword) {
        if (request == null) {
            return null;
        }
        return request.toEntity(encodedPassword);
    }
    
    /**
     * 驗證 CreateTodoRequest 是否有效
     * @param request CreateTodoRequest DTO
     * @return 驗證結果
     */
    public static boolean isValid(CreateTodoRequest request) {
        return request != null && 
               request.getTitle() != null && !request.getTitle().trim().isEmpty() &&
               request.getDueDate() != null &&
               request.getTitle().length() <= 255 &&
               (request.getDescription() == null || request.getDescription().length() <= 1000);
    }
    
    /**
     * 驗證 UpdateTodoRequest 是否有效
     * @param request UpdateTodoRequest DTO
     * @return 驗證結果
     */
    public static boolean isValid(UpdateTodoRequest request) {
        return request != null && request.isValid();
    }
    
    /**
     * 驗證 UserRegistrationRequest 是否有效
     * @param request UserRegistrationRequest DTO
     * @return 驗證結果
     */
    public static boolean isValid(UserRegistrationRequest request) {
        return request != null && request.isValid();
    }
    
    /**
     * 驗證 LoginRequest 是否有效
     * @param request LoginRequest DTO
     * @return 驗證結果
     */
    public static boolean isValid(LoginRequest request) {
        return request != null && request.isValid();
    }
}