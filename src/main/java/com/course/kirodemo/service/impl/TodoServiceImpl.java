package com.course.kirodemo.service.impl;

import com.course.kirodemo.dto.CreateTodoRequest;
import com.course.kirodemo.dto.UpdateTodoRequest;
import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.TodoNotFoundException;
import com.course.kirodemo.exception.UnauthorizedAccessException;
import com.course.kirodemo.exception.UserNotFoundException;
import com.course.kirodemo.repository.TodoItemRepository;
import com.course.kirodemo.repository.UserRepository;
import com.course.kirodemo.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TodoService 實作類別
 * 處理待辦事項相關的業務邏輯，包含 CRUD 操作和權限檢查
 */
@Service
@Transactional
public class TodoServiceImpl implements TodoService {
    
    private static final Logger logger = LoggerFactory.getLogger(TodoServiceImpl.class);
    
    private final TodoItemRepository todoItemRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public TodoServiceImpl(TodoItemRepository todoItemRepository, UserRepository userRepository) {
        this.todoItemRepository = todoItemRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public TodoItem createTodo(CreateTodoRequest request, String username) {
        // 驗證請求
        if (request == null) {
            throw new IllegalArgumentException("建立待辦事項請求不能為空");
        }
        
        logger.info("使用者 {} 嘗試建立待辦事項: {}", username, request.getTitle());
        
        // 查詢使用者
        User user = getUserByUsername(username);
        
        // 建立待辦事項實體
        TodoItem todoItem = request.toEntity(user);
        
        // 儲存待辦事項
        TodoItem savedTodoItem = todoItemRepository.save(todoItem);
        logger.info("待辦事項建立成功，ID: {}, 使用者: {}", savedTodoItem.getId(), username);
        
        return savedTodoItem;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getUserTodos(String username, SortBy sortBy) {
        logger.debug("取得使用者 {} 的待辦事項，排序方式: {}", username, sortBy);
        
        User user = getUserByUsername(username);
        
        return switch (sortBy) {
            case CREATED_AT_DESC -> todoItemRepository.findByUserOrderByCreatedAtDesc(user);
            case CREATED_AT_ASC -> todoItemRepository.findByUserOrderByCreatedAtAsc(user);
            case DUE_DATE_ASC -> todoItemRepository.findByUserOrderByDueDateAsc(user);
            case DUE_DATE_DESC -> todoItemRepository.findByUserOrderByDueDateDesc(user);
        };
    }
    
    @Override
    public TodoItem updateTodo(Long todoId, UpdateTodoRequest request, String username) {
        logger.info("使用者 {} 嘗試更新待辦事項 ID: {}", username, todoId);
        
        // 驗證請求
        if (request == null || !request.isValid()) {
            throw new IllegalArgumentException("更新待辦事項請求無效");
        }
        
        // 查詢並驗證權限
        TodoItem todoItem = findAndValidateUserTodo(todoId, username);
        
        // 更新待辦事項
        request.updateEntity(todoItem);
        
        // 儲存更新
        TodoItem updatedTodoItem = todoItemRepository.save(todoItem);
        logger.info("待辦事項更新成功，ID: {}, 使用者: {}", todoId, username);
        
        return updatedTodoItem;
    }
    
    @Override
    public void deleteTodo(Long todoId, String username) {
        logger.info("使用者 {} 嘗試刪除待辦事項 ID: {}", username, todoId);
        
        // 查詢並驗證權限
        TodoItem todoItem = findAndValidateUserTodo(todoId, username);
        
        // 刪除待辦事項
        todoItemRepository.delete(todoItem);
        logger.info("待辦事項刪除成功，ID: {}, 使用者: {}", todoId, username);
    }
    
    @Override
    public TodoItem toggleComplete(Long todoId, String username) {
        logger.info("使用者 {} 嘗試切換待辦事項 {} 的完成狀態", username, todoId);
        
        // 查詢並驗證權限
        TodoItem todoItem = findAndValidateUserTodo(todoId, username);
        
        // 切換完成狀態
        todoItem.toggleCompleted();
        
        // 儲存更新
        TodoItem updatedTodoItem = todoItemRepository.save(todoItem);
        logger.info("待辦事項狀態切換成功，ID: {}, 新狀態: {}, 使用者: {}", 
                   todoId, updatedTodoItem.isCompleted(), username);
        
        return updatedTodoItem;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TodoItem> findUserTodo(Long todoId, String username) {
        User user = getUserByUsername(username);
        return todoItemRepository.findByIdAndUser(todoId, user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getCompletedTodos(String username) {
        User user = getUserByUsername(username);
        return todoItemRepository.findByUserAndCompletedOrderByCreatedAtDesc(user, true);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getIncompleteTodos(String username) {
        User user = getUserByUsername(username);
        return todoItemRepository.findByUserAndCompletedOrderByCreatedAtDesc(user, false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getOverdueTodos(String username) {
        User user = getUserByUsername(username);
        return todoItemRepository.findOverdueTodosByUser(user, LocalDate.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> getDueSoonTodos(String username) {
        User user = getUserByUsername(username);
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        return todoItemRepository.findDueSoonTodosByUser(user, today, threeDaysLater);
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
}