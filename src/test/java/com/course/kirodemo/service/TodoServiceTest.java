package com.course.kirodemo.service;

import com.course.kirodemo.dto.CreateTodoRequest;
import com.course.kirodemo.dto.UpdateTodoRequest;
import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.TodoNotFoundException;
import com.course.kirodemo.exception.UnauthorizedAccessException;
import com.course.kirodemo.exception.UserNotFoundException;
import com.course.kirodemo.repository.TodoItemRepository;
import com.course.kirodemo.repository.UserRepository;
import com.course.kirodemo.service.impl.TodoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * TodoService 單元測試
 * 使用 Mockito 模擬依賴，測試業務邏輯和權限檢查
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService 單元測試")
class TodoServiceTest {
    
    @Mock
    private TodoItemRepository todoItemRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private TodoServiceImpl todoService;
    
    private User mockUser;
    private TodoItem mockTodoItem;
    private CreateTodoRequest createRequest;
    private UpdateTodoRequest updateRequest;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        mockUser.setCreatedAt(LocalDateTime.now());
        
        mockTodoItem = new TodoItem();
        mockTodoItem.setId(1L);
        mockTodoItem.setTitle("測試待辦事項");
        mockTodoItem.setDescription("測試描述");
        mockTodoItem.setDueDate(LocalDate.now().plusDays(7));
        mockTodoItem.setCompleted(false);
        mockTodoItem.setUser(mockUser);
        mockTodoItem.setCreatedAt(LocalDateTime.now());
        
        createRequest = new CreateTodoRequest("新待辦事項", "新描述", LocalDate.now().plusDays(3));
        updateRequest = new UpdateTodoRequest("更新標題", "更新描述", LocalDate.now().plusDays(5));
    }
    
    @Test
    @DisplayName("有效請求建立待辦事項時應該成功建立")
    void test_createTodo_whenValidRequest_then_shouldCreateTodo() {
        // Given - 設定 Mock 行為
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.save(any(TodoItem.class))).thenReturn(mockTodoItem);
        
        // When - 執行被測試的方法
        TodoItem result = todoService.createTodo(createRequest, "testuser");
        
        // Then - 驗證結果
        assertNotNull(result);
        assertEquals(mockTodoItem.getId(), result.getId());
        assertEquals(mockTodoItem.getTitle(), result.getTitle());
        
        // 驗證 Repository 方法被正確呼叫
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).save(any(TodoItem.class));
    }
    
    @Test
    @DisplayName("使用者不存在時建立待辦事項應該拋出 UserNotFoundException")
    void test_createTodo_whenUserNotExists_then_shouldThrowUserNotFoundException() {
        // Given - 設定使用者不存在
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then - 執行並驗證異常
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> todoService.createTodo(createRequest, "nonexistent")
        );
        
        assertTrue(exception.getMessage().contains("nonexistent"));
        
        // 驗證 save 方法沒有被呼叫
        verify(todoItemRepository, never()).save(any(TodoItem.class));
    }
    
    @Test
    @DisplayName("請求為 null 時應該拋出 IllegalArgumentException")
    void test_createTodo_whenRequestIsNull_then_shouldThrowIllegalArgumentException() {
        // When & Then - 執行並驗證異常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> todoService.createTodo(null, "testuser")
        );
        
        assertEquals("建立待辦事項請求不能為空", exception.getMessage());
        
        // 驗證 Repository 方法沒有被呼叫
        verify(userRepository, never()).findByUsername(anyString());
        verify(todoItemRepository, never()).save(any(TodoItem.class));
    }
    
    @Test
    @DisplayName("取得使用者待辦事項時應該依指定方式排序")
    void test_getUserTodos_whenValidUser_then_shouldReturnSortedTodos() {
        // Given - 準備待辦事項列表
        List<TodoItem> mockTodos = Arrays.asList(mockTodoItem);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByUserOrderByCreatedAtDesc(mockUser)).thenReturn(mockTodos);
        
        // When - 執行被測試的方法
        List<TodoItem> result = todoService.getUserTodos("testuser", TodoService.SortBy.CREATED_AT_DESC);
        
        // Then - 驗證結果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockTodoItem.getId(), result.get(0).getId());
        
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).findByUserOrderByCreatedAtDesc(mockUser);
    }
    
    @Test
    @DisplayName("使用者不存在時取得待辦事項應該拋出 UserNotFoundException")
    void test_getUserTodos_whenUserNotExists_then_shouldThrowUserNotFoundException() {
        // Given - 設定使用者不存在
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then - 執行並驗證異常
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> todoService.getUserTodos("nonexistent", TodoService.SortBy.CREATED_AT_DESC)
        );
        
        assertTrue(exception.getMessage().contains("nonexistent"));
    }
    
    @Test
    @DisplayName("有效請求更新待辦事項時應該成功更新")
    void test_updateTodo_whenValidRequest_then_shouldUpdateTodo() {
        // Given - 設定 Mock 行為
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(mockTodoItem));
        when(todoItemRepository.save(mockTodoItem)).thenReturn(mockTodoItem);
        
        // When - 執行被測試的方法
        TodoItem result = todoService.updateTodo(1L, updateRequest, "testuser");
        
        // Then - 驗證結果
        assertNotNull(result);
        assertEquals(mockTodoItem.getId(), result.getId());
        
        // 驗證 Repository 方法被正確呼叫
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).findByIdAndUser(1L, mockUser);
        verify(todoItemRepository).save(mockTodoItem);
    }
    
    @Test
    @DisplayName("待辦事項不存在時更新應該拋出 TodoNotFoundException")
    void test_updateTodo_whenTodoNotExists_then_shouldThrowTodoNotFoundException() {
        // Given - 設定待辦事項不存在
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByIdAndUser(999L, mockUser)).thenReturn(Optional.empty());
        when(todoItemRepository.existsById(999L)).thenReturn(false);
        
        // When & Then - 執行並驗證異常
        TodoNotFoundException exception = assertThrows(
            TodoNotFoundException.class,
            () -> todoService.updateTodo(999L, updateRequest, "testuser")
        );
        
        assertTrue(exception.getMessage().contains("999"));
        
        // 驗證 save 方法沒有被呼叫
        verify(todoItemRepository, never()).save(any(TodoItem.class));
    }
    
    @Test
    @DisplayName("待辦事項屬於其他使用者時更新應該拋出 UnauthorizedAccessException")
    void test_updateTodo_whenTodoNotBelongsToUser_then_shouldThrowUnauthorizedAccessException() {
        // Given - 設定待辦事項存在但不屬於該使用者
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.empty());
        when(todoItemRepository.existsById(1L)).thenReturn(true);
        
        // When & Then - 執行並驗證異常
        UnauthorizedAccessException exception = assertThrows(
            UnauthorizedAccessException.class,
            () -> todoService.updateTodo(1L, updateRequest, "testuser")
        );
        
        assertTrue(exception.getMessage().contains("權限"));
        
        // 驗證 save 方法沒有被呼叫
        verify(todoItemRepository, never()).save(any(TodoItem.class));
    }
    
    @Test
    @DisplayName("更新請求無效時應該拋出 IllegalArgumentException")
    void test_updateTodo_whenRequestIsInvalid_then_shouldThrowIllegalArgumentException() {
        // Given - 建立無效的更新請求
        UpdateTodoRequest invalidRequest = new UpdateTodoRequest("", "描述", LocalDate.now());
        
        // When & Then - 執行並驗證異常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> todoService.updateTodo(1L, invalidRequest, "testuser")
        );
        
        assertEquals("更新待辦事項請求無效", exception.getMessage());
    }
    
    @Test
    @DisplayName("有效請求刪除待辦事項時應該成功刪除")
    void test_deleteTodo_whenValidRequest_then_shouldDeleteTodo() {
        // Given - 設定 Mock 行為
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(mockTodoItem));
        
        // When - 執行被測試的方法
        todoService.deleteTodo(1L, "testuser");
        
        // Then - 驗證 Repository 方法被正確呼叫
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).findByIdAndUser(1L, mockUser);
        verify(todoItemRepository).delete(mockTodoItem);
    }
    
    @Test
    @DisplayName("待辦事項不存在時刪除應該拋出 TodoNotFoundException")
    void test_deleteTodo_whenTodoNotExists_then_shouldThrowTodoNotFoundException() {
        // Given - 設定待辦事項不存在
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByIdAndUser(999L, mockUser)).thenReturn(Optional.empty());
        when(todoItemRepository.existsById(999L)).thenReturn(false);
        
        // When & Then - 執行並驗證異常
        TodoNotFoundException exception = assertThrows(
            TodoNotFoundException.class,
            () -> todoService.deleteTodo(999L, "testuser")
        );
        
        assertTrue(exception.getMessage().contains("999"));
        
        // 驗證 delete 方法沒有被呼叫
        verify(todoItemRepository, never()).delete(any(TodoItem.class));
    }
    
    @Test
    @DisplayName("切換完成狀態時應該成功更新")
    void test_toggleComplete_whenValidRequest_then_shouldToggleStatus() {
        // Given - 設定 Mock 行為
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(mockTodoItem));
        when(todoItemRepository.save(mockTodoItem)).thenReturn(mockTodoItem);
        
        // When - 執行被測試的方法
        TodoItem result = todoService.toggleComplete(1L, "testuser");
        
        // Then - 驗證結果
        assertNotNull(result);
        assertTrue(result.isCompleted()); // 原本是 false，切換後應該是 true
        
        // 驗證 Repository 方法被正確呼叫
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).findByIdAndUser(1L, mockUser);
        verify(todoItemRepository).save(mockTodoItem);
    }
    
    @Test
    @DisplayName("查詢使用者特定待辦事項時應該回傳正確結果")
    void test_findUserTodo_whenTodoExists_then_shouldReturnTodo() {
        // Given - 設定 Mock 行為
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(mockTodoItem));
        
        // When - 執行被測試的方法
        Optional<TodoItem> result = todoService.findUserTodo(1L, "testuser");
        
        // Then - 驗證結果
        assertTrue(result.isPresent());
        assertEquals(mockTodoItem.getId(), result.get().getId());
        
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).findByIdAndUser(1L, mockUser);
    }
    
    @Test
    @DisplayName("查詢不存在的待辦事項時應該回傳空的 Optional")
    void test_findUserTodo_whenTodoNotExists_then_shouldReturnEmpty() {
        // Given - 設定 Mock 行為
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByIdAndUser(999L, mockUser)).thenReturn(Optional.empty());
        
        // When - 執行被測試的方法
        Optional<TodoItem> result = todoService.findUserTodo(999L, "testuser");
        
        // Then - 驗證結果
        assertFalse(result.isPresent());
        
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).findByIdAndUser(999L, mockUser);
    }
    
    @Test
    @DisplayName("取得已完成待辦事項時應該回傳正確列表")
    void test_getCompletedTodos_whenValidUser_then_shouldReturnCompletedTodos() {
        // Given - 準備已完成的待辦事項列表
        List<TodoItem> completedTodos = Arrays.asList(mockTodoItem);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByUserAndCompletedOrderByCreatedAtDesc(mockUser, true)).thenReturn(completedTodos);
        
        // When - 執行被測試的方法
        List<TodoItem> result = todoService.getCompletedTodos("testuser");
        
        // Then - 驗證結果
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).findByUserAndCompletedOrderByCreatedAtDesc(mockUser, true);
    }
    
    @Test
    @DisplayName("取得未完成待辦事項時應該回傳正確列表")
    void test_getIncompleteTodos_whenValidUser_then_shouldReturnIncompleteTodos() {
        // Given - 準備未完成的待辦事項列表
        List<TodoItem> incompleteTodos = Arrays.asList(mockTodoItem);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findByUserAndCompletedOrderByCreatedAtDesc(mockUser, false)).thenReturn(incompleteTodos);
        
        // When - 執行被測試的方法
        List<TodoItem> result = todoService.getIncompleteTodos("testuser");
        
        // Then - 驗證結果
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).findByUserAndCompletedOrderByCreatedAtDesc(mockUser, false);
    }
    
    @Test
    @DisplayName("取得逾期待辦事項時應該回傳正確列表")
    void test_getOverdueTodos_whenValidUser_then_shouldReturnOverdueTodos() {
        // Given - 準備逾期的待辦事項列表
        List<TodoItem> overdueTodos = Arrays.asList(mockTodoItem);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findOverdueTodosByUser(eq(mockUser), any(LocalDate.class))).thenReturn(overdueTodos);
        
        // When - 執行被測試的方法
        List<TodoItem> result = todoService.getOverdueTodos("testuser");
        
        // Then - 驗證結果
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).findOverdueTodosByUser(eq(mockUser), any(LocalDate.class));
    }
    
    @Test
    @DisplayName("取得即將到期待辦事項時應該回傳正確列表")
    void test_getDueSoonTodos_whenValidUser_then_shouldReturnDueSoonTodos() {
        // Given - 準備即將到期的待辦事項列表
        List<TodoItem> dueSoonTodos = Arrays.asList(mockTodoItem);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoItemRepository.findDueSoonTodosByUser(eq(mockUser), any(LocalDate.class), any(LocalDate.class))).thenReturn(dueSoonTodos);
        
        // When - 執行被測試的方法
        List<TodoItem> result = todoService.getDueSoonTodos("testuser");
        
        // Then - 驗證結果
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(userRepository).findByUsername("testuser");
        verify(todoItemRepository).findDueSoonTodosByUser(eq(mockUser), any(LocalDate.class), any(LocalDate.class));
    }
}