package com.course.kirodemo.service;

import com.course.kirodemo.entity.TodoItem;

import java.util.List;

/**
 * 待辦事項延期服務介面
 * 提供待辦事項延期功能相關的業務邏輯操作
 */
public interface TodoExtensionService {
    
    /**
     * 檢查待辦事項是否符合延期條件（三天內到期且未完成）
     * 
     * @param todoItem 待辦事項實體
     * @return true 如果符合延期條件，false 否則
     */
    boolean isEligibleForExtension(TodoItem todoItem);
    
    /**
     * 延期待辦事項
     * 
     * @param todoId 待辦事項 ID
     * @param extensionDays 延期天數，必須為正數
     * @param username 使用者名稱
     * @return 延期後的待辦事項實體
     * @throws com.course.kirodemo.exception.TodoNotFoundException 如果待辦事項不存在
     * @throws com.course.kirodemo.exception.UnauthorizedAccessException 如果使用者無權限存取
     * @throws com.course.kirodemo.exception.InvalidExtensionException 如果延期請求無效
     * @throws com.course.kirodemo.exception.ExtensionNotAllowedException 如果不允許延期
     */
    TodoItem extendTodo(Long todoId, int extensionDays, String username);
    
    /**
     * 取得使用者所有符合延期條件的待辦事項
     * 
     * @param username 使用者名稱
     * @return 符合延期條件的待辦事項列表
     * @throws com.course.kirodemo.exception.UserNotFoundException 如果使用者不存在
     */
    List<TodoItem> getEligibleTodosForUser(String username);
    
    /**
     * 驗證延期天數的有效性
     * 
     * @param extensionDays 延期天數
     * @throws com.course.kirodemo.exception.InvalidExtensionException 如果延期天數無效
     */
    void validateExtensionDays(int extensionDays);
}