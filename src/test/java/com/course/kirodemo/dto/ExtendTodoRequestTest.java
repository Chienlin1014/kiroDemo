package com.course.kirodemo.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExtendTodoRequest DTO 的單元測試
 * 測試 DTO 的驗證邏輯和基本功能
 */
@DisplayName("ExtendTodoRequest DTO 測試")
class ExtendTodoRequestTest {
    
    private ExtendTodoRequest validRequest;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        validRequest = new ExtendTodoRequest(1L, 7);
    }
    
    @Test
    @DisplayName("建立有效的 ExtendTodoRequest 時應該設定所有屬性")
    void test_constructor_whenValidData_then_shouldSetAllProperties() {
        // Given - 測試資料
        Long expectedTodoId = 1L;
        Integer expectedExtensionDays = 7;
        
        // When - 建立 ExtendTodoRequest
        ExtendTodoRequest request = new ExtendTodoRequest(expectedTodoId, expectedExtensionDays);
        
        // Then - 驗證所有屬性都正確設定
        assertEquals(expectedTodoId, request.getTodoId());
        assertEquals(expectedExtensionDays, request.getExtensionDays());
    }
    
    @Test
    @DisplayName("預設建構子應該建立空的 DTO")
    void test_defaultConstructor_whenCalled_then_shouldCreateEmptyDto() {
        // Given & When - 使用預設建構子
        ExtendTodoRequest request = new ExtendTodoRequest();
        
        // Then - 所有屬性應該為 null
        assertNull(request.getTodoId());
        assertNull(request.getExtensionDays());
    }
    
    @Test
    @DisplayName("有效的請求應該通過驗證")
    void test_isValid_whenValidRequest_then_shouldReturnTrue() {
        // Given - 有效的請求
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該通過驗證
        assertTrue(result);
    }
    
    @Test
    @DisplayName("todoId 為 null 時應該不通過驗證")
    void test_isValid_whenTodoIdIsNull_then_shouldReturnFalse() {
        // Given - todoId 為 null 的請求
        validRequest.setTodoId(null);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該不通過驗證
        assertFalse(result);
    }
    
    @Test
    @DisplayName("extensionDays 為 null 時應該不通過驗證")
    void test_isValid_whenExtensionDaysIsNull_then_shouldReturnFalse() {
        // Given - extensionDays 為 null 的請求
        validRequest.setExtensionDays(null);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該不通過驗證
        assertFalse(result);
    }
    
    @Test
    @DisplayName("extensionDays 為零時應該不通過驗證")
    void test_isValid_whenExtensionDaysIsZero_then_shouldReturnFalse() {
        // Given - extensionDays 為零的請求
        validRequest.setExtensionDays(0);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該不通過驗證
        assertFalse(result);
    }
    
    @Test
    @DisplayName("extensionDays 為負數時應該不通過驗證")
    void test_isValid_whenExtensionDaysIsNegative_then_shouldReturnFalse() {
        // Given - extensionDays 為負數的請求
        validRequest.setExtensionDays(-1);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該不通過驗證
        assertFalse(result);
    }
    
    @Test
    @DisplayName("extensionDays 超過365天時應該不通過驗證")
    void test_isValid_whenExtensionDaysExceedsLimit_then_shouldReturnFalse() {
        // Given - extensionDays 超過365天的請求
        validRequest.setExtensionDays(366);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該不通過驗證
        assertFalse(result);
    }
    
    @Test
    @DisplayName("extensionDays 等於365天時應該通過驗證")
    void test_isValid_whenExtensionDaysEquals365_then_shouldReturnTrue() {
        // Given - extensionDays 等於365天的請求
        validRequest.setExtensionDays(365);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該通過驗證
        assertTrue(result);
    }
    
    @Test
    @DisplayName("extensionDays 等於1天時應該通過驗證")
    void test_isValid_whenExtensionDaysEquals1_then_shouldReturnTrue() {
        // Given - extensionDays 等於1天的請求
        validRequest.setExtensionDays(1);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該通過驗證
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
        assertTrue(result.contains("todoId=1"));
        assertTrue(result.contains("extensionDays=7"));
    }
    
    @Test
    @DisplayName("相同內容的請求應該相等")
    void test_equals_whenSameContent_then_shouldReturnTrue() {
        // Given - 兩個相同內容的請求
        ExtendTodoRequest request1 = new ExtendTodoRequest(1L, 7);
        ExtendTodoRequest request2 = new ExtendTodoRequest(1L, 7);
        
        // When & Then - 應該相等
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    @DisplayName("不同內容的請求應該不相等")
    void test_equals_whenDifferentContent_then_shouldReturnFalse() {
        // Given - 兩個不同內容的請求
        ExtendTodoRequest request1 = new ExtendTodoRequest(1L, 7);
        ExtendTodoRequest request2 = new ExtendTodoRequest(2L, 7);
        
        // When & Then - 應該不相等
        assertNotEquals(request1, request2);
    }
    
    @Test
    @DisplayName("與 null 比較時應該不相等")
    void test_equals_whenComparedWithNull_then_shouldReturnFalse() {
        // Given - 有效的請求
        
        // When & Then - 與 null 比較應該不相等
        assertNotEquals(validRequest, null);
    }
    
    @Test
    @DisplayName("與自己比較時應該相等")
    void test_equals_whenComparedWithSelf_then_shouldReturnTrue() {
        // Given - 有效的請求
        
        // When & Then - 與自己比較應該相等
        assertEquals(validRequest, validRequest);
    }
}