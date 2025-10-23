package com.course.kirodemo.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExtendTodoResponse DTO 的單元測試
 * 測試 DTO 的建構子和靜態工廠方法
 */
@DisplayName("ExtendTodoResponse DTO 測試")
class ExtendTodoResponseTest {
    
    private LocalDate testNewDueDate;
    private LocalDate testOriginalDueDate;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        testOriginalDueDate = LocalDate.now().plusDays(1);
        testNewDueDate = LocalDate.now().plusDays(8);
    }
    
    @Test
    @DisplayName("預設建構子應該建立空的 DTO")
    void test_defaultConstructor_whenCalled_then_shouldCreateEmptyDto() {
        // Given & When - 使用預設建構子
        ExtendTodoResponse response = new ExtendTodoResponse();
        
        // Then - 所有屬性應該為預設值
        assertFalse(response.isSuccess());
        assertNull(response.getMessage());
        assertNull(response.getNewDueDate());
        assertNull(response.getOriginalDueDate());
        assertNull(response.getTotalExtensionDays());
        assertNull(response.getTodoId());
    }
    
    @Test
    @DisplayName("基本建構子應該設定成功狀態和訊息")
    void test_basicConstructor_whenValidData_then_shouldSetSuccessAndMessage() {
        // Given - 測試資料
        boolean expectedSuccess = true;
        String expectedMessage = "延期成功";
        
        // When - 建立 ExtendTodoResponse
        ExtendTodoResponse response = new ExtendTodoResponse(expectedSuccess, expectedMessage);
        
        // Then - 驗證基本屬性設定正確
        assertEquals(expectedSuccess, response.isSuccess());
        assertEquals(expectedMessage, response.getMessage());
        assertNull(response.getNewDueDate());
        assertNull(response.getOriginalDueDate());
        assertNull(response.getTotalExtensionDays());
        assertNull(response.getTodoId());
    }
    
    @Test
    @DisplayName("完整建構子應該設定所有延期資訊")
    void test_fullConstructor_whenValidData_then_shouldSetAllExtensionInfo() {
        // Given - 測試資料
        boolean expectedSuccess = true;
        String expectedMessage = "延期成功";
        Integer expectedTotalDays = 7;
        
        // When - 建立 ExtendTodoResponse
        ExtendTodoResponse response = new ExtendTodoResponse(
            expectedSuccess, expectedMessage, testNewDueDate, testOriginalDueDate, expectedTotalDays);
        
        // Then - 驗證所有屬性設定正確
        assertEquals(expectedSuccess, response.isSuccess());
        assertEquals(expectedMessage, response.getMessage());
        assertEquals(testNewDueDate, response.getNewDueDate());
        assertEquals(testOriginalDueDate, response.getOriginalDueDate());
        assertEquals(expectedTotalDays, response.getTotalExtensionDays());
        assertNull(response.getTodoId());
    }
    
    @Test
    @DisplayName("完整建構子（包含ID）應該設定所有屬性")
    void test_fullConstructorWithId_whenValidData_then_shouldSetAllProperties() {
        // Given - 測試資料
        boolean expectedSuccess = true;
        String expectedMessage = "延期成功";
        Long expectedTodoId = 1L;
        Integer expectedTotalDays = 7;
        
        // When - 建立 ExtendTodoResponse
        ExtendTodoResponse response = new ExtendTodoResponse(
            expectedSuccess, expectedMessage, expectedTodoId, testNewDueDate, testOriginalDueDate, expectedTotalDays);
        
        // Then - 驗證所有屬性設定正確
        assertEquals(expectedSuccess, response.isSuccess());
        assertEquals(expectedMessage, response.getMessage());
        assertEquals(expectedTodoId, response.getTodoId());
        assertEquals(testNewDueDate, response.getNewDueDate());
        assertEquals(testOriginalDueDate, response.getOriginalDueDate());
        assertEquals(expectedTotalDays, response.getTotalExtensionDays());
    }
    
    @Test
    @DisplayName("成功靜態工廠方法應該建立成功回應")
    void test_success_whenValidData_then_shouldCreateSuccessResponse() {
        // Given - 測試資料
        String expectedMessage = "延期成功";
        Long expectedTodoId = 1L;
        Integer expectedTotalDays = 7;
        
        // When - 使用靜態工廠方法建立成功回應
        ExtendTodoResponse response = ExtendTodoResponse.success(
            expectedMessage, expectedTodoId, testNewDueDate, testOriginalDueDate, expectedTotalDays);
        
        // Then - 驗證成功回應屬性
        assertTrue(response.isSuccess());
        assertEquals(expectedMessage, response.getMessage());
        assertEquals(expectedTodoId, response.getTodoId());
        assertEquals(testNewDueDate, response.getNewDueDate());
        assertEquals(testOriginalDueDate, response.getOriginalDueDate());
        assertEquals(expectedTotalDays, response.getTotalExtensionDays());
    }
    
    @Test
    @DisplayName("失敗靜態工廠方法應該建立失敗回應")
    void test_failure_whenErrorMessage_then_shouldCreateFailureResponse() {
        // Given - 錯誤訊息
        String expectedMessage = "延期失敗：無效的延期天數";
        
        // When - 使用靜態工廠方法建立失敗回應
        ExtendTodoResponse response = ExtendTodoResponse.failure(expectedMessage);
        
        // Then - 驗證失敗回應屬性
        assertFalse(response.isSuccess());
        assertEquals(expectedMessage, response.getMessage());
        assertNull(response.getTodoId());
        assertNull(response.getNewDueDate());
        assertNull(response.getOriginalDueDate());
        assertNull(response.getTotalExtensionDays());
    }
    
    @Test
    @DisplayName("失敗靜態工廠方法（包含ID）應該建立失敗回應")
    void test_failureWithId_whenErrorMessageAndId_then_shouldCreateFailureResponseWithId() {
        // Given - 錯誤訊息和待辦事項ID
        String expectedMessage = "延期失敗：待辦事項不存在";
        Long expectedTodoId = 1L;
        
        // When - 使用靜態工廠方法建立失敗回應
        ExtendTodoResponse response = ExtendTodoResponse.failure(expectedMessage, expectedTodoId);
        
        // Then - 驗證失敗回應屬性
        assertFalse(response.isSuccess());
        assertEquals(expectedMessage, response.getMessage());
        assertEquals(expectedTodoId, response.getTodoId());
        assertNull(response.getNewDueDate());
        assertNull(response.getOriginalDueDate());
        assertNull(response.getTotalExtensionDays());
    }
    
    @Test
    @DisplayName("toString 方法應該回傳包含所有屬性的字串")
    void test_toString_whenCalled_then_shouldReturnStringWithAllProperties() {
        // Given - 完整的回應
        ExtendTodoResponse response = ExtendTodoResponse.success(
            "延期成功", 1L, testNewDueDate, testOriginalDueDate, 7);
        
        // When - 呼叫 toString
        String result = response.toString();
        
        // Then - 應該包含所有屬性
        assertNotNull(result);
        assertTrue(result.contains("success=true"));
        assertTrue(result.contains("延期成功"));
        assertTrue(result.contains("todoId=1"));
        assertTrue(result.contains("totalExtensionDays=7"));
    }
    
    @Test
    @DisplayName("相同內容的回應應該相等")
    void test_equals_whenSameContent_then_shouldReturnTrue() {
        // Given - 兩個相同內容的回應
        ExtendTodoResponse response1 = ExtendTodoResponse.success(
            "延期成功", 1L, testNewDueDate, testOriginalDueDate, 7);
        ExtendTodoResponse response2 = ExtendTodoResponse.success(
            "延期成功", 1L, testNewDueDate, testOriginalDueDate, 7);
        
        // When & Then - 應該相等
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
    
    @Test
    @DisplayName("不同內容的回應應該不相等")
    void test_equals_whenDifferentContent_then_shouldReturnFalse() {
        // Given - 兩個不同內容的回應
        ExtendTodoResponse response1 = ExtendTodoResponse.success(
            "延期成功", 1L, testNewDueDate, testOriginalDueDate, 7);
        ExtendTodoResponse response2 = ExtendTodoResponse.failure("延期失敗");
        
        // When & Then - 應該不相等
        assertNotEquals(response1, response2);
    }
    
    @Test
    @DisplayName("與 null 比較時應該不相等")
    void test_equals_whenComparedWithNull_then_shouldReturnFalse() {
        // Given - 有效的回應
        ExtendTodoResponse response = ExtendTodoResponse.success(
            "延期成功", 1L, testNewDueDate, testOriginalDueDate, 7);
        
        // When & Then - 與 null 比較應該不相等
        assertNotEquals(response, null);
    }
    
    @Test
    @DisplayName("與自己比較時應該相等")
    void test_equals_whenComparedWithSelf_then_shouldReturnTrue() {
        // Given - 有效的回應
        ExtendTodoResponse response = ExtendTodoResponse.success(
            "延期成功", 1L, testNewDueDate, testOriginalDueDate, 7);
        
        // When & Then - 與自己比較應該相等
        assertEquals(response, response);
    }
    
    @Test
    @DisplayName("設定屬性後應該正確更新")
    void test_setters_whenCalled_then_shouldUpdateProperties() {
        // Given - 空的回應
        ExtendTodoResponse response = new ExtendTodoResponse();
        
        // When - 設定所有屬性
        response.setSuccess(true);
        response.setMessage("測試訊息");
        response.setTodoId(1L);
        response.setNewDueDate(testNewDueDate);
        response.setOriginalDueDate(testOriginalDueDate);
        response.setTotalExtensionDays(7);
        
        // Then - 驗證所有屬性都正確設定
        assertTrue(response.isSuccess());
        assertEquals("測試訊息", response.getMessage());
        assertEquals(1L, response.getTodoId());
        assertEquals(testNewDueDate, response.getNewDueDate());
        assertEquals(testOriginalDueDate, response.getOriginalDueDate());
        assertEquals(7, response.getTotalExtensionDays());
    }
}