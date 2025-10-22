package com.course.kirodemo.dto;

import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DtoConverter 工具類別的單元測試
 * 測試 DTO 轉換邏輯和驗證方法
 */
@DisplayName("DtoConverter 工具類別測試")
class DtoConverterTest {
    
    private User testUser;
    private TodoItem testTodoItem;
    private CreateTodoRequest createRequest;
    private UpdateTodoRequest updateRequest;
    private UserRegistrationRequest registrationRequest;
    private LoginRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        testUser = new User("testuser", "password123");
        testUser.setId(1L);
        
        testTodoItem = new TodoItem("測試標題", "測試描述", LocalDate.now().plusDays(7));
        testTodoItem.setId(1L);
        testTodoItem.setUser(testUser);
        
        createRequest = new CreateTodoRequest("新待辦事項", "新描述", LocalDate.now().plusDays(5));
        updateRequest = new UpdateTodoRequest("更新標題", "更新描述", LocalDate.now().plusDays(10));
        registrationRequest = new UserRegistrationRequest("newuser", "newpass123", "newpass123");
        loginRequest = new LoginRequest("loginuser", "loginpass123");
    }
    
    @Test
    @DisplayName("轉換 CreateTodoRequest 為實體時應該建立正確的 TodoItem")
    void test_toEntity_whenValidCreateRequest_then_shouldCreateCorrectTodoItem() {
        // Given - 有效的 CreateTodoRequest 和使用者
        
        // When - 轉換為實體
        TodoItem result = DtoConverter.toEntity(createRequest, testUser);
        
        // Then - 驗證轉換結果
        assertNotNull(result);
        assertEquals(createRequest.getTitle(), result.getTitle());
        assertEquals(createRequest.getDescription(), result.getDescription());
        assertEquals(createRequest.getDueDate(), result.getDueDate());
        assertEquals(testUser, result.getUser());
    }
    
    @Test
    @DisplayName("轉換 null CreateTodoRequest 時應該回傳 null")
    void test_toEntity_whenNullCreateRequest_then_shouldReturnNull() {
        // Given - null 請求
        
        // When - 轉換為實體
        TodoItem result = DtoConverter.toEntity(null, testUser);
        
        // Then - 應該回傳 null
        assertNull(result);
    }
    
    @Test
    @DisplayName("轉換 TodoItem 為 CreateTodoRequest 時應該建立正確的 DTO")
    void test_toCreateRequest_whenValidTodoItem_then_shouldCreateCorrectDto() {
        // Given - 有效的 TodoItem
        
        // When - 轉換為 CreateTodoRequest
        CreateTodoRequest result = DtoConverter.toCreateRequest(testTodoItem);
        
        // Then - 驗證轉換結果
        assertNotNull(result);
        assertEquals(testTodoItem.getTitle(), result.getTitle());
        assertEquals(testTodoItem.getDescription(), result.getDescription());
        assertEquals(testTodoItem.getDueDate(), result.getDueDate());
    }
    
    @Test
    @DisplayName("轉換 null TodoItem 為 CreateTodoRequest 時應該回傳 null")
    void test_toCreateRequest_whenNullTodoItem_then_shouldReturnNull() {
        // Given - null TodoItem
        
        // When - 轉換為 CreateTodoRequest
        CreateTodoRequest result = DtoConverter.toCreateRequest(null);
        
        // Then - 應該回傳 null
        assertNull(result);
    }
    
    @Test
    @DisplayName("轉換 TodoItem 為 UpdateTodoRequest 時應該建立正確的 DTO")
    void test_toUpdateRequest_whenValidTodoItem_then_shouldCreateCorrectDto() {
        // Given - 有效的 TodoItem
        
        // When - 轉換為 UpdateTodoRequest
        UpdateTodoRequest result = DtoConverter.toUpdateRequest(testTodoItem);
        
        // Then - 驗證轉換結果
        assertNotNull(result);
        assertEquals(testTodoItem.getTitle(), result.getTitle());
        assertEquals(testTodoItem.getDescription(), result.getDescription());
        assertEquals(testTodoItem.getDueDate(), result.getDueDate());
    }
    
    @Test
    @DisplayName("使用 UpdateTodoRequest 更新實體時應該正確更新")
    void test_updateEntity_whenValidRequest_then_shouldUpdateCorrectly() {
        // Given - 有效的 UpdateTodoRequest 和 TodoItem
        String originalTitle = testTodoItem.getTitle();
        
        // When - 更新實體
        DtoConverter.updateEntity(updateRequest, testTodoItem);
        
        // Then - 驗證更新結果
        assertEquals(updateRequest.getTitle(), testTodoItem.getTitle());
        assertEquals(updateRequest.getDescription(), testTodoItem.getDescription());
        assertEquals(updateRequest.getDueDate(), testTodoItem.getDueDate());
        assertNotEquals(originalTitle, testTodoItem.getTitle());
    }
    
    @Test
    @DisplayName("使用 null 請求更新實體時不應該發生錯誤")
    void test_updateEntity_whenNullRequest_then_shouldNotThrowError() {
        // Given - null 請求和有效的 TodoItem
        String originalTitle = testTodoItem.getTitle();
        
        // When - 嘗試更新實體
        assertDoesNotThrow(() -> DtoConverter.updateEntity(null, testTodoItem));
        
        // Then - 實體應該保持不變
        assertEquals(originalTitle, testTodoItem.getTitle());
    }
    
    @Test
    @DisplayName("轉換 UserRegistrationRequest 為實體時應該建立正確的 User")
    void test_toEntity_whenValidRegistrationRequest_then_shouldCreateCorrectUser() {
        // Given - 有效的 UserRegistrationRequest
        
        // When - 轉換為實體
        User result = DtoConverter.toEntity(registrationRequest);
        
        // Then - 驗證轉換結果
        assertNotNull(result);
        assertEquals(registrationRequest.getUsername(), result.getUsername());
        assertEquals(registrationRequest.getPassword(), result.getPassword());
    }
    
    @Test
    @DisplayName("使用加密密碼轉換 UserRegistrationRequest 時應該使用提供的密碼")
    void test_toEntity_whenWithEncodedPassword_then_shouldUseProvidedPassword() {
        // Given - 有效的 UserRegistrationRequest 和加密密碼
        String encodedPassword = "$2a$10$encodedpassword";
        
        // When - 使用加密密碼轉換為實體
        User result = DtoConverter.toEntity(registrationRequest, encodedPassword);
        
        // Then - 驗證轉換結果
        assertNotNull(result);
        assertEquals(registrationRequest.getUsername(), result.getUsername());
        assertEquals(encodedPassword, result.getPassword());
    }
    
    @Test
    @DisplayName("驗證有效的 CreateTodoRequest 時應該回傳 true")
    void test_isValid_whenValidCreateRequest_then_shouldReturnTrue() {
        // Given - 有效的 CreateTodoRequest
        
        // When - 驗證請求
        boolean result = DtoConverter.isValid(createRequest);
        
        // Then - 應該回傳 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("驗證無效的 CreateTodoRequest 時應該回傳 false")
    void test_isValid_whenInvalidCreateRequest_then_shouldReturnFalse() {
        // Given - 無效的 CreateTodoRequest（空標題）
        createRequest.setTitle("");
        
        // When - 驗證請求
        boolean result = DtoConverter.isValid(createRequest);
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證 null CreateTodoRequest 時應該回傳 false")
    void test_isValid_whenNullCreateRequest_then_shouldReturnFalse() {
        // Given - null CreateTodoRequest
        
        // When - 驗證請求
        boolean result = DtoConverter.isValid((CreateTodoRequest) null);
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證有效的 UpdateTodoRequest 時應該回傳 true")
    void test_isValid_whenValidUpdateRequest_then_shouldReturnTrue() {
        // Given - 有效的 UpdateTodoRequest
        
        // When - 驗證請求
        boolean result = DtoConverter.isValid(updateRequest);
        
        // Then - 應該回傳 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("驗證有效的 UserRegistrationRequest 時應該回傳 true")
    void test_isValid_whenValidRegistrationRequest_then_shouldReturnTrue() {
        // Given - 有效的 UserRegistrationRequest
        
        // When - 驗證請求
        boolean result = DtoConverter.isValid(registrationRequest);
        
        // Then - 應該回傳 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("驗證有效的 LoginRequest 時應該回傳 true")
    void test_isValid_whenValidLoginRequest_then_shouldReturnTrue() {
        // Given - 有效的 LoginRequest
        
        // When - 驗證請求
        boolean result = DtoConverter.isValid(loginRequest);
        
        // Then - 應該回傳 true
        assertTrue(result);
    }
}