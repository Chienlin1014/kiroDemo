package com.course.kirodemo.dto;

import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UpdateTodoRequest DTO 的單元測試
 * 測試 DTO 的驗證邏輯和更新方法
 */
@DisplayName("UpdateTodoRequest DTO 測試")
class UpdateTodoRequestTest {
    
    private UpdateTodoRequest validRequest;
    private TodoItem testTodoItem;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        validRequest = new UpdateTodoRequest(
            "更新後的標題",
            "更新後的描述",
            LocalDate.now().plusDays(10)
        );
        
        testUser = new User("testuser", "password123");
        testUser.setId(1L);
        
        testTodoItem = new TodoItem("原始標題", "原始描述", LocalDate.now().plusDays(5));
        testTodoItem.setId(1L);
        testTodoItem.setUser(testUser);
    }
    
    @Test
    @DisplayName("建立有效的 UpdateTodoRequest 時應該設定所有屬性")
    void test_constructor_whenValidData_then_shouldSetAllProperties() {
        // Given - 測試資料
        String expectedTitle = "更新標題";
        String expectedDescription = "更新描述";
        LocalDate expectedDueDate = LocalDate.now().plusDays(3);
        
        // When - 建立 UpdateTodoRequest
        UpdateTodoRequest request = new UpdateTodoRequest(expectedTitle, expectedDescription, expectedDueDate);
        
        // Then - 驗證所有屬性都正確設定
        assertEquals(expectedTitle, request.getTitle());
        assertEquals(expectedDescription, request.getDescription());
        assertEquals(expectedDueDate, request.getDueDate());
    }
    
    @Test
    @DisplayName("更新實體時應該只更新允許的欄位")
    void test_updateEntity_whenValidRequest_then_shouldUpdateOnlyAllowedFields() {
        // Given - 原始實體資料
        Long originalId = testTodoItem.getId();
        User originalUser = testTodoItem.getUser();
        boolean originalCompleted = testTodoItem.isCompleted();
        LocalDateTime originalCreatedAt = testTodoItem.getCreatedAt();
        LocalDateTime originalCompletedAt = testTodoItem.getCompletedAt();
        
        // When - 更新實體
        validRequest.updateEntity(testTodoItem);
        
        // Then - 驗證只有允許的欄位被更新
        assertEquals(validRequest.getTitle(), testTodoItem.getTitle());
        assertEquals(validRequest.getDescription(), testTodoItem.getDescription());
        assertEquals(validRequest.getDueDate(), testTodoItem.getDueDate());
        
        // 驗證不應該被更新的欄位保持不變
        assertEquals(originalId, testTodoItem.getId());
        assertEquals(originalUser, testTodoItem.getUser());
        assertEquals(originalCompleted, testTodoItem.isCompleted());
        assertEquals(originalCreatedAt, testTodoItem.getCreatedAt());
        assertEquals(originalCompletedAt, testTodoItem.getCompletedAt());
    }
    
    @Test
    @DisplayName("從實體建立 DTO 時應該複製正確的屬性")
    void test_fromEntity_whenValidTodoItem_then_shouldCreateCorrectRequest() {
        // Given - 測試用的 TodoItem
        
        // When - 從實體建立 DTO
        UpdateTodoRequest request = UpdateTodoRequest.fromEntity(testTodoItem);
        
        // Then - 驗證 DTO 屬性正確複製
        assertNotNull(request);
        assertEquals(testTodoItem.getTitle(), request.getTitle());
        assertEquals(testTodoItem.getDescription(), request.getDescription());
        assertEquals(testTodoItem.getDueDate(), request.getDueDate());
    }
    
    @Test
    @DisplayName("驗證有效請求時應該回傳 true")
    void test_isValid_whenValidRequest_then_shouldReturnTrue() {
        // Given - 有效的請求
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("驗證空標題請求時應該回傳 false")
    void test_isValid_whenEmptyTitle_then_shouldReturnFalse() {
        // Given - 空標題的請求
        validRequest.setTitle("");
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證 null 標題請求時應該回傳 false")
    void test_isValid_whenNullTitle_then_shouldReturnFalse() {
        // Given - null 標題的請求
        validRequest.setTitle(null);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證 null 預計完成日請求時應該回傳 false")
    void test_isValid_whenNullDueDate_then_shouldReturnFalse() {
        // Given - null 預計完成日的請求
        validRequest.setDueDate(null);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證過長標題請求時應該回傳 false")
    void test_isValid_whenTitleTooLong_then_shouldReturnFalse() {
        // Given - 過長標題的請求
        String longTitle = "a".repeat(256); // 超過 255 字元
        validRequest.setTitle(longTitle);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證過長描述請求時應該回傳 false")
    void test_isValid_whenDescriptionTooLong_then_shouldReturnFalse() {
        // Given - 過長描述的請求
        String longDescription = "a".repeat(1001); // 超過 1000 字元
        validRequest.setDescription(longDescription);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證 null 描述請求時應該回傳 true")
    void test_isValid_whenNullDescription_then_shouldReturnTrue() {
        // Given - null 描述的請求
        validRequest.setDescription(null);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 true（描述是可選的）
        assertTrue(result);
    }
    
    @Test
    @DisplayName("toString 方法應該回傳包含所有屬性的字串")
    void test_toString_whenCalled_then_shouldReturnStringWithAllProperties() {
        // Given - 有效的請求
        
        // When - 呼叫 toString
        String result = validRequest.toString();
        
        // Then - 應該包含所有屬性
        assertNotNull(result);
        assertTrue(result.contains("更新後的標題"));
        assertTrue(result.contains("更新後的描述"));
        assertTrue(result.contains(validRequest.getDueDate().toString()));
    }
    
    @Test
    @DisplayName("預設建構子應該建立空的 DTO")
    void test_defaultConstructor_whenCalled_then_shouldCreateEmptyDto() {
        // Given & When - 使用預設建構子
        UpdateTodoRequest request = new UpdateTodoRequest();
        
        // Then - 所有屬性應該為 null
        assertNull(request.getTitle());
        assertNull(request.getDescription());
        assertNull(request.getDueDate());
    }
}