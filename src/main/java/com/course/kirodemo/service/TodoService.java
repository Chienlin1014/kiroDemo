package com.course.kirodemo.service;

import com.course.kirodemo.dto.CreateTodoRequest;
import com.course.kirodemo.dto.UpdateTodoRequest;
import com.course.kirodemo.entity.TodoItem;

import java.util.List;
import java.util.Optional;

/**
 * TodoService 介面
 * 定義待辦事項相關的業務邏輯操作
 */
public interface TodoService {
    
    /**
     * 排序方式枚舉
     */
    enum SortBy {
        CREATED_AT_DESC,    // 依建立時間排序（最新的在前）
        CREATED_AT_ASC,     // 依建立時間排序（最舊的在前）
        DUE_DATE_ASC,       // 依預計完成日排序（最近的在前）
        DUE_DATE_DESC       // 依預計完成日排序（最遠的在前）
    }
    
    /**
     * 建立新的待辦事項
     * @param request 建立待辦事項請求
     * @param username 使用者名稱
     * @return 建立的待辦事項實體
     * @throws UserNotFoundException 如果使用者不存在
     * @throws IllegalArgumentException 如果請求資料無效
     */
    TodoItem createTodo(CreateTodoRequest request, String username);
    
    /**
     * 取得使用者的所有待辦事項
     * @param username 使用者名稱
     * @param sortBy 排序方式
     * @return 待辦事項列表
     * @throws UserNotFoundException 如果使用者不存在
     */
    List<TodoItem> getUserTodos(String username, SortBy sortBy);
    
    /**
     * 更新待辦事項
     * @param todoId 待辦事項 ID
     * @param request 更新請求
     * @param username 使用者名稱
     * @return 更新後的待辦事項實體
     * @throws TodoNotFoundException 如果待辦事項不存在
     * @throws UnauthorizedAccessException 如果使用者無權限存取
     * @throws IllegalArgumentException 如果請求資料無效
     */
    TodoItem updateTodo(Long todoId, UpdateTodoRequest request, String username);
    
    /**
     * 刪除待辦事項
     * @param todoId 待辦事項 ID
     * @param username 使用者名稱
     * @throws TodoNotFoundException 如果待辦事項不存在
     * @throws UnauthorizedAccessException 如果使用者無權限存取
     */
    void deleteTodo(Long todoId, String username);
    
    /**
     * 切換待辦事項的完成狀態
     * @param todoId 待辦事項 ID
     * @param username 使用者名稱
     * @return 更新後的待辦事項實體
     * @throws TodoNotFoundException 如果待辦事項不存在
     * @throws UnauthorizedAccessException 如果使用者無權限存取
     */
    TodoItem toggleComplete(Long todoId, String username);
    
    /**
     * 查詢使用者的特定待辦事項
     * @param todoId 待辦事項 ID
     * @param username 使用者名稱
     * @return 待辦事項實體的 Optional 包裝
     * @throws UserNotFoundException 如果使用者不存在
     */
    Optional<TodoItem> findUserTodo(Long todoId, String username);
    
    /**
     * 取得使用者已完成的待辦事項
     * @param username 使用者名稱
     * @return 已完成的待辦事項列表
     * @throws UserNotFoundException 如果使用者不存在
     */
    List<TodoItem> getCompletedTodos(String username);
    
    /**
     * 取得使用者未完成的待辦事項
     * @param username 使用者名稱
     * @return 未完成的待辦事項列表
     * @throws UserNotFoundException 如果使用者不存在
     */
    List<TodoItem> getIncompleteTodos(String username);
    
    /**
     * 取得使用者逾期的待辦事項
     * @param username 使用者名稱
     * @return 逾期的待辦事項列表
     * @throws UserNotFoundException 如果使用者不存在
     */
    List<TodoItem> getOverdueTodos(String username);
    
    /**
     * 取得使用者即將到期的待辦事項（3天內）
     * @param username 使用者名稱
     * @return 即將到期的待辦事項列表
     * @throws UserNotFoundException 如果使用者不存在
     */
    List<TodoItem> getDueSoonTodos(String username);
}