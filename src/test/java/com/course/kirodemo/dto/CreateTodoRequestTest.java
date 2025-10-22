package com.course.kirodemo.dto;

import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CreateTodoRequest DTO 的單元測試
 * 測試 DTO 的驗證邏輯和轉換方法
 */
@DisplayName("CreateTodoRequest DTO 測試")
class CreateTodoRequestTest {
    
    private CreateTodoRequest validRequest;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        validRequest = new CreateTodoRequest(
            "測試待辦事項",
            "這是一個測試描述",
            LocalDate.now().plusDays(7)
        );
        
        testUser = new User("testuser", "password123");
        testUser.setId(1L);
    }
    
    @Test
    @DisplayName("建立有效的 CreateTodoRequest 時應該設定所有屬性")
    void test_constructor_whenValidData_then_shouldSetAllProperties() {
        // Given - 測試資料在 setUp 中準備
        String expectedTitle = "測試待辦事項";
        String expectedDescription = "這是一個測試描述";
        LocalDate expectedDueDate = LocalDate.now().plusDays(7);
        
        // When - 建立 CreateTodoRequest
        CreateTodoRequest request = new CreateTodoRequest(expectedTitle, expectedDescription, expectedDueDate);
        
        // Then - 驗證所有屬性都正確設定
        assertEquals(expectedTitle, request.getTitle());
        assertEquals(expectedDescription, request.getDescription());
        assertEquals(expectedDueDate, request.getDueDate());
    }
    
    @Test
    @DisplayName("轉換為實體時應該建立正確的 TodoItem")
    void test_toEntity_whenValidRequest_then_shouldCreateCorrectTodoItem() {
        // Given - 有效的請求和使用者
        
        // When - 轉換為實體
        TodoItem todoItem = validRequest.toEntity(testUser);
        
        // Then - 驗證實體屬性正確設定
        assertNotNull(todoItem);
        assertEquals(validRequest.getTitle(), todoItem.getTitle());
        assertEquals(validRequest.getDescription(), todoItem.getDescription());
        assertEquals(validRequest.getDueDate(), todoItem.getDueDate());
        assertEquals(testUser, todoItem.getUser());
        assertFalse(todoItem.isCompleted());
        assertNull(todoItem.getCompletedAt());
    }
    
    @Test
    @DisplayName("從實體建立 DTO 時應該複製正確的屬性")
    void test_fromEntity_whenValidTodoItem_then_shouldCreateCorrectRequest() {
        // Given - 建立測試用的 TodoItem
        TodoItem todoItem = new TodoItem("原始標題", "原始描述", LocalDate.now().plusDays(5));
        todoItem.setUser(testUser);
        
        // When - 從實體建立 DTO
        CreateTodoRequest request = CreateTodoRequest.fromEntity(todoItem);
        
        // Then - 驗證 DTO 屬性正確複製
        assertNotNull(request);
        assertEquals(todoItem.getTitle(), request.getTitle());
        assertEquals(todoItem.getDescription(), request.getDescription());
        assertEquals(todoItem.getDueDate(), request.getDueDate());
    }
    
    @Test
    @DisplayName("設定空標題時應該允許設定")
    void test_setTitle_whenEmptyTitle_then_shouldAllowSetting() {
        // Given - 有效的請求
        
        // When - 設定空標題
        validRequest.setTitle("");
        
        // Then - 應該允許設定（驗證由 Bean Validation 處理）
        assertEquals("", validRequest.getTitle());
    }
    
    @Test
    @DisplayName("設定 null 描述時應該允許設定")
    void test_setDescription_whenNullDescription_then_shouldAllowSetting() {
        // Given - 有效的請求
        
        // When - 設定 null 描述
        validRequest.setDescription(null);
        
        // Then - 應該允許設定
        assertNull(validRequest.getDescription());
    }
    
    @Test
    @DisplayName("設定過去日期時應該允許設定")
    void test_setDueDate_whenPastDate_then_shouldAllowSetting() {
        // Given - 有效的請求
        LocalDate pastDate = LocalDate.now().minusDays(1);
        
        // When - 設定過去日期
        validRequest.setDueDate(pastDate);
        
        // Then - 應該允許設定（驗證由 Bean Validation 處理）
        assertEquals(pastDate, validRequest.getDueDate());
    }
    
    @Test
    @DisplayName("toString 方法應該回傳包含所有屬性的字串")
    void test_toString_whenCalled_then_shouldReturnStringWithAllProperties() {
        // Given - 有效的請求
        
        // When - 呼叫 toString
        String result = validRequest.toString();
        
        // Then - 應該包含所有屬性
        assertNotNull(result);
        assertTrue(result.contains("測試待辦事項"));
        assertTrue(result.contains("這是一個測試描述"));
        assertTrue(result.contains(validRequest.getDueDate().toString()));
    }
    
    @Test
    @DisplayName("預設建構子應該建立空的 DTO")
    void test_defaultConstructor_whenCalled_then_shouldCreateEmptyDto() {
        // Given & When - 使用預設建構子
        CreateTodoRequest request = new CreateTodoRequest();
        
        // Then - 所有屬性應該為 null
        assertNull(request.getTitle());
        assertNull(request.getDescription());
        assertNull(request.getDueDate());
    }
}