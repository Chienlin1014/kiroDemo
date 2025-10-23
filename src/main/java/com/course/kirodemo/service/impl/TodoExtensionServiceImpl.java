package com.course.kirodemo.service.impl;

import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.TodoNotFoundException;
import com.course.kirodemo.exception.UnauthorizedAccessException;
import com.course.kirodemo.exception.UserNotFoundException;
import com.course.kirodemo.repository.TodoItemRepository;
import com.course.kirodemo.repository.UserRepository;
import com.course.kirodemo.service.DateValidationService;
import com.course.kirodemo.service.TodoExtensionService;
import com.course.kirodemo.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 待辦事項延期服務實作類別
 * 實作延期功能相關的業務邏輯，包含延期條件檢查、延期操作和資料驗證
 */
@Service
@Transactional
public class TodoExtensionServiceImpl implements TodoExtensionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TodoExtensionServiceImpl.class);
    
    private final TodoItemRepository todoItemRepository;
    private final UserRepository userRepository;
    private final DateValidationService dateValidationService;
    private final TodoService todoService;
    
    @Autowired
    public TodoExtensionServiceImpl(
            TodoItemRepository todoItemRepository,
            UserRepository userRepository,
            DateValidationService dateValidationService,
            TodoService todoService) {
        this.todoItemRepository = todoItemRepository;
        this.userRepository = userRepository;
        this.dateValidationService = dateValidationService;
        this.todoService = todoService;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isEligibleForExtension(TodoItem todoItem) {
        if (todoItem == null) {
            return false;
        }
        
        // 使用 TodoItem 實體中的業務邏輯方法
        return todoItem.isEligibleForExtension();
    }
    
    @Override
    public TodoItem extendTodo(Long todoId, int extensionDays, String username) {
        logger.info("使用者 {} 嘗試延期待辦事項 ID: {}，延期天數: {}", username, todoId, extensionDays);
        
        // 驗證延期天數
        validateExtensionDays(extensionDays);
        
        // 查詢並驗證使用者權限
        TodoItem todoItem = findAndValidateUserTodo(todoId, username);
        
        // 檢查是否符合延期條件
        if (!isEligibleForExtension(todoItem)) {
            String reason = determineIneligibilityReason(todoItem);
            logger.warn("待辦事項 {} 不符合延期條件: {}", todoId, reason);
            throw new IllegalStateException("此待辦事項不符合延期條件: " + reason);
        }
        
        // 執行延期操作
        todoItem.extendDueDate(extensionDays);
        
        // 儲存更新
        TodoItem extendedTodoItem = todoItemRepository.save(todoItem);
        logger.info("待辦事項延期成功，ID: {}，新到期日: {}，使用者: {}", 
                   todoId, extendedTodoItem.getDueDate(), username);
        
        return extendedTodoItem;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getEligibleTodosForUser(String username) {
        logger.debug("取得使用者 {} 符合延期條件的待辦事項", username);
        
        // 查詢使用者
        User user = getUserByUsername(username);
        
        // 取得使用者所有未完成的待辦事項
        List<TodoItem> incompleteTodos = todoItemRepository.findByUserAndCompleted(user, false);
        
        // 篩選符合延期條件的待辦事項
        List<TodoItem> eligibleTodos = incompleteTodos.stream()
                .filter(this::isEligibleForExtension)
                .collect(Collectors.toList());
        
        logger.debug("使用者 {} 有 {} 個待辦事項符合延期條件", username, eligibleTodos.size());
        
        return eligibleTodos;
    }
    
    @Override
    public void validateExtensionDays(int extensionDays) {
        if (!dateValidationService.isValidExtensionDays(extensionDays)) {
            throw new IllegalArgumentException("延期天數必須為正數，實際輸入: " + extensionDays);
        }
        
        // 額外的業務規則：限制最大延期天數為365天
        if (extensionDays > 365) {
            throw new IllegalArgumentException("延期天數不能超過365天，實際輸入: " + extensionDays);
        }
    }
    
    /**
     * 根據使用者名稱查詢使用者，如果不存在則拋出異常
     * @param username 使用者名稱
     * @return 使用者實體
     * @throws UserNotFoundException 如果使用者不存在
     */
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.forUsername(username));
    }
    
    /**
     * 查詢待辦事項並驗證使用者權限
     * @param todoId 待辦事項 ID
     * @param username 使用者名稱
     * @return 待辦事項實體
     * @throws TodoNotFoundException 如果待辦事項不存在
     * @throws UnauthorizedAccessException 如果使用者無權限存取
     */
    private TodoItem findAndValidateUserTodo(Long todoId, String username) {
        User user = getUserByUsername(username);
        
        Optional<TodoItem> todoOptional = todoItemRepository.findByIdAndUser(todoId, user);
        if (todoOptional.isEmpty()) {
            // 檢查待辦事項是否存在（但不屬於該使用者）
            if (todoItemRepository.existsById(todoId)) {
                logger.warn("使用者 {} 嘗試存取不屬於自己的待辦事項 ID: {}", username, todoId);
                throw new UnauthorizedAccessException("您沒有權限存取此待辦事項");
            } else {
                logger.warn("待辦事項不存在，ID: {}", todoId);
                throw TodoNotFoundException.forId(todoId);
            }
        }
        
        return todoOptional.get();
    }
    
    /**
     * 判斷待辦事項不符合延期條件的原因
     * @param todoItem 待辦事項實體
     * @return 不符合條件的原因描述
     */
    private String determineIneligibilityReason(TodoItem todoItem) {
        if (todoItem.isCompleted()) {
            return "待辦事項已完成";
        }
        
        if (todoItem.getDueDate() == null) {
            return "待辦事項沒有設定到期日";
        }
        
        LocalDate today = LocalDate.now();
        LocalDate dueDate = todoItem.getDueDate();
        
        if (dueDate.isBefore(today)) {
            return "待辦事項已逾期";
        }
        
        if (dueDate.isAfter(today.plusDays(3))) {
            return "待辦事項到期日超過三天";
        }
        
        return "未知原因";
    }
}