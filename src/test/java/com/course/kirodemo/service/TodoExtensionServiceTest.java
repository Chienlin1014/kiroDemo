package com.course.kirodemo.service;

import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.TodoNotFoundException;
import com.course.kirodemo.exception.UnauthorizedAccessException;
import com.course.kirodemo.exception.UserNotFoundException;
import com.course.kirodemo.repository.TodoItemRepository;
import com.course.kirodemo.repository.UserRepository;
import com.course.kirodemo.service.impl.TodoExtensionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TodoExtensionService 單元測試
 * 測試待辦事項延期功能的業務邏輯
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoExtensionService 單元測試")
class TodoExtensionServiceTest {
    
    @Mock
    private TodoItemRepository todoItemRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DateValidationService dateValidationService;
    
    @Mock
    private TodoService todoService;
    
    private TodoExtensionService extensionService;
    
    private User testUser;
    private TodoItem eligibleTodo;
    private TodoItem completedTodo;
    private TodoItem overdueTodo;
    
    @BeforeEach
    void setUp() {
        extensionService = new TodoExtensionServiceImpl(
            todoItemRepository, userRepository, dateValidationService, todoService);
        
        // 準備測試資料
        testUser = new User("testuser", "password");
        testUser.setId(1L);
        
        // 符合延期條件的待辦事項（未完成且三天內到期）
        eligibleTodo = new TodoItem("測試任務", "描述", LocalDate.now().plusDays(2));
        eligibleTodo.setId(1L);
        eligibleTodo.setUser(testUser);
        eligibleTodo.setCompleted(false);
        
        // 已完成的待辦事項
        completedTodo = new TodoItem("已完成任務", "描述", LocalDate.now().plusDays(1));
        completedTodo.setId(2L);
        completedTodo.setUser(testUser);
        completedTodo.setCompleted(true);
        completedTodo.setCompletedAt(LocalDateTime.now());
        
        // 逾期的待辦事項
        overdueTodo = new TodoItem("逾期任務", "描述", LocalDate.now().minusDays(1));
        overdueTodo.setId(3L);
        overdueTodo.setUser(testUser);
        overdueTodo.setCompleted(false);
    }
    
    @Test
    @DisplayName("符合條件的待辦事項應該可以延期")
    void test_isEligibleForExtension_whenTodoIsEligible_then_shouldReturnTrue() {
        // Given (給定) - 設定符合延期條件的待辦事項
        TodoItem todo = new TodoItem("測試任務", "描述", LocalDate.now().plusDays(2));
        todo.setCompleted(false);
        
        // When (當) - 檢查是否符合延期條件
        boolean result = extensionService.isEligibleForExtension(todo);
        
        // Then (那麼) - 驗證結果為 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("已完成的待辦事項不應該可以延期")
    void test_isEligibleForExtension_whenTodoIsCompleted_then_shouldReturnFalse() {
        // Given (給定) - 設定已完成的待辦事項
        TodoItem todo = new TodoItem("已完成任務", "描述", LocalDate.now().plusDays(1));
        todo.setCompleted(true);
        
        // When (當) - 檢查是否符合延期條件
        boolean result = extensionService.isEligibleForExtension(todo);
        
        // Then (那麼) - 驗證結果為 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("空的待辦事項不應該可以延期")
    void test_isEligibleForExtension_whenTodoIsNull_then_shouldReturnFalse() {
        // Given (給定) - 設定空的待辦事項
        TodoItem todo = null;
        
        // When (當) - 檢查是否符合延期條件
        boolean result = extensionService.isEligibleForExtension(todo);
        
        // Then (那麼) - 驗證結果為 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("成功延期應該更新到期日和延期記錄")
    void test_extendTodo_whenValidRequest_then_shouldUpdateDueDateAndExtensionRecord() {
        // Given (給定) - 設定有效的延期請求
        Long todoId = 1L;
        int extensionDays = 3;
        String username = "testuser";
        
        when(dateValidationService.isValidExtensionDays(extensionDays)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(todoItemRepository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.of(eligibleTodo));
        when(todoItemRepository.save(any(TodoItem.class))).thenReturn(eligibleTodo);
        
        LocalDate originalDueDate = eligibleTodo.getDueDate();
        
        // When (當) - 執行延期操作
        TodoItem result = extensionService.extendTodo(todoId, extensionDays, username);
        
        // Then (那麼) - 驗證延期結果
        assertNotNull(result);
        assertEquals(originalDueDate.plusDays(extensionDays), result.getDueDate());
        assertEquals(1, result.getExtensionCount());
        assertNotNull(result.getLastExtendedAt());
        assertEquals(originalDueDate, result.getOriginalDueDate());
        
        verify(todoItemRepository).save(eligibleTodo);
    }
    
    @Test
    @DisplayName("延期天數為負數時應該拋出異常")
    void test_extendTodo_whenNegativeExtensionDays_then_shouldThrowException() {
        // Given (給定) - 設定負數延期天數
        Long todoId = 1L;
        int negativeDays = -1;
        String username = "testuser";
        
        when(dateValidationService.isValidExtensionDays(negativeDays)).thenReturn(false);
        
        // When & Then (當且那麼) - 驗證拋出異常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> extensionService.extendTodo(todoId, negativeDays, username));
        
        assertTrue(exception.getMessage().contains("延期天數必須為正數"));
    }
    
    @Test
    @DisplayName("延期不存在的待辦事項應該拋出異常")
    void test_extendTodo_whenTodoNotFound_then_shouldThrowException() {
        // Given (給定) - 設定不存在的待辦事項ID
        Long nonExistentTodoId = 999L;
        int extensionDays = 3;
        String username = "testuser";
        
        when(dateValidationService.isValidExtensionDays(extensionDays)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(todoItemRepository.findByIdAndUser(nonExistentTodoId, testUser)).thenReturn(Optional.empty());
        when(todoItemRepository.existsById(nonExistentTodoId)).thenReturn(false);
        
        // When & Then (當且那麼) - 驗證拋出異常
        assertThrows(TodoNotFoundException.class,
            () -> extensionService.extendTodo(nonExistentTodoId, extensionDays, username));
    }
    
    @Test
    @DisplayName("延期不屬於自己的待辦事項應該拋出異常")
    void test_extendTodo_whenUnauthorizedAccess_then_shouldThrowException() {
        // Given (給定) - 設定不屬於使用者的待辦事項
        Long todoId = 1L;
        int extensionDays = 3;
        String username = "testuser";
        
        when(dateValidationService.isValidExtensionDays(extensionDays)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(todoItemRepository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.empty());
        when(todoItemRepository.existsById(todoId)).thenReturn(true);
        
        // When & Then (當且那麼) - 驗證拋出異常
        assertThrows(UnauthorizedAccessException.class,
            () -> extensionService.extendTodo(todoId, extensionDays, username));
    }
    
    @Test
    @DisplayName("延期已完成的待辦事項應該拋出異常")
    void test_extendTodo_whenTodoIsCompleted_then_shouldThrowException() {
        // Given (給定) - 設定已完成的待辦事項
        Long todoId = 2L;
        int extensionDays = 3;
        String username = "testuser";
        
        when(dateValidationService.isValidExtensionDays(extensionDays)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(todoItemRepository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.of(completedTodo));
        
        // When & Then (當且那麼) - 驗證拋出異常
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> extensionService.extendTodo(todoId, extensionDays, username));
        
        assertTrue(exception.getMessage().contains("不符合延期條件"));
        assertTrue(exception.getMessage().contains("待辦事項已完成"));
    }
    
    @Test
    @DisplayName("取得使用者符合延期條件的待辦事項")
    void test_getEligibleTodosForUser_whenUserHasEligibleTodos_then_shouldReturnFilteredList() {
        // Given (給定) - 設定使用者的待辦事項列表
        String username = "testuser";
        List<TodoItem> incompleteTodos = Arrays.asList(eligibleTodo, overdueTodo);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(todoItemRepository.findByUserAndCompleted(testUser, false)).thenReturn(incompleteTodos);
        
        // When (當) - 取得符合延期條件的待辦事項
        List<TodoItem> result = extensionService.getEligibleTodosForUser(username);
        
        // Then (那麼) - 驗證只返回符合條件的待辦事項
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(eligibleTodo.getId(), result.get(0).getId());
        assertTrue(result.get(0).isEligibleForExtension());
    }
    
    @Test
    @DisplayName("不存在的使用者應該拋出異常")
    void test_getEligibleTodosForUser_whenUserNotFound_then_shouldThrowException() {
        // Given (給定) - 設定不存在的使用者
        String nonExistentUsername = "nonexistent";
        
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());
        
        // When & Then (當且那麼) - 驗證拋出異常
        assertThrows(UserNotFoundException.class,
            () -> extensionService.getEligibleTodosForUser(nonExistentUsername));
    }
    
    @Test
    @DisplayName("正數延期天數應該通過驗證")
    void test_validateExtensionDays_whenPositiveDays_then_shouldNotThrowException() {
        // Given (給定) - 設定正數延期天數
        int positiveDays = 5;
        
        when(dateValidationService.isValidExtensionDays(positiveDays)).thenReturn(true);
        
        // When & Then (當且那麼) - 驗證不拋出異常
        assertDoesNotThrow(() -> extensionService.validateExtensionDays(positiveDays));
    }
    
    @Test
    @DisplayName("零延期天數應該拋出異常")
    void test_validateExtensionDays_whenZeroDays_then_shouldThrowException() {
        // Given (給定) - 設定零延期天數
        int zeroDays = 0;
        
        when(dateValidationService.isValidExtensionDays(zeroDays)).thenReturn(false);
        
        // When & Then (當且那麼) - 驗證拋出異常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> extensionService.validateExtensionDays(zeroDays));
        
        assertTrue(exception.getMessage().contains("延期天數必須為正數"));
    }
    
    @Test
    @DisplayName("超過365天的延期天數應該拋出異常")
    void test_validateExtensionDays_whenExceedsMaxDays_then_shouldThrowException() {
        // Given (給定) - 設定超過365天的延期天數
        int excessiveDays = 400;
        
        when(dateValidationService.isValidExtensionDays(excessiveDays)).thenReturn(true);
        
        // When & Then (當且那麼) - 驗證拋出異常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> extensionService.validateExtensionDays(excessiveDays));
        
        assertTrue(exception.getMessage().contains("延期天數不能超過365天"));
    }
    
    @Test
    @DisplayName("365天的延期天數應該通過驗證")
    void test_validateExtensionDays_whenExactly365Days_then_shouldNotThrowException() {
        // Given (給定) - 設定正好365天的延期天數
        int maxDays = 365;
        
        when(dateValidationService.isValidExtensionDays(maxDays)).thenReturn(true);
        
        // When & Then (當且那麼) - 驗證不拋出異常
        assertDoesNotThrow(() -> extensionService.validateExtensionDays(maxDays));
    }
}