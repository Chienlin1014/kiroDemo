package com.course.kirodemo.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoginRequest DTO 的單元測試
 * 測試 DTO 的驗證邏輯和功能方法
 */
@DisplayName("LoginRequest DTO 測試")
class LoginRequestTest {
    
    private LoginRequest validRequest;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        validRequest = new LoginRequest("testuser", "password123");
    }
    
    @Test
    @DisplayName("建立有效的 LoginRequest 時應該設定所有屬性")
    void test_constructor_whenValidData_then_shouldSetAllProperties() {
        // Given - 測試資料
        String expectedUsername = "newuser";
        String expectedPassword = "newpass123";
        
        // When - 建立 LoginRequest
        LoginRequest request = new LoginRequest(expectedUsername, expectedPassword);
        
        // Then - 驗證所有屬性都正確設定
        assertEquals(expectedUsername, request.getUsername());
        assertEquals(expectedPassword, request.getPassword());
        assertFalse(request.isRememberMe()); // 預設為 false
    }
    
    @Test
    @DisplayName("建立包含記住我選項的 LoginRequest 時應該設定所有屬性")
    void test_constructor_whenWithRememberMe_then_shouldSetAllProperties() {
        // Given - 測試資料
        String expectedUsername = "newuser";
        String expectedPassword = "newpass123";
        boolean expectedRememberMe = true;
        
        // When - 建立包含記住我選項的 LoginRequest
        LoginRequest request = new LoginRequest(expectedUsername, expectedPassword, expectedRememberMe);
        
        // Then - 驗證所有屬性都正確設定
        assertEquals(expectedUsername, request.getUsername());
        assertEquals(expectedPassword, request.getPassword());
        assertEquals(expectedRememberMe, request.isRememberMe());
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
    @DisplayName("驗證空使用者名稱請求時應該回傳 false")
    void test_isValid_whenEmptyUsername_then_shouldReturnFalse() {
        // Given - 空使用者名稱的請求
        validRequest.setUsername("");
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證 null 使用者名稱請求時應該回傳 false")
    void test_isValid_whenNullUsername_then_shouldReturnFalse() {
        // Given - null 使用者名稱的請求
        validRequest.setUsername(null);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證空密碼請求時應該回傳 false")
    void test_isValid_whenEmptyPassword_then_shouldReturnFalse() {
        // Given - 空密碼的請求
        validRequest.setPassword("");
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證 null 密碼請求時應該回傳 false")
    void test_isValid_whenNullPassword_then_shouldReturnFalse() {
        // Given - null 密碼的請求
        validRequest.setPassword(null);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證過長使用者名稱請求時應該回傳 false")
    void test_isValid_whenUsernameTooLong_then_shouldReturnFalse() {
        // Given - 過長使用者名稱的請求
        String longUsername = "a".repeat(51); // 超過 50 字元
        validRequest.setUsername(longUsername);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證過長密碼請求時應該回傳 false")
    void test_isValid_whenPasswordTooLong_then_shouldReturnFalse() {
        // Given - 過長密碼的請求
        String longPassword = "a".repeat(101); // 超過 100 字元
        validRequest.setPassword(longPassword);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("設定記住我選項時應該正確設定")
    void test_setRememberMe_whenSetToTrue_then_shouldSetCorrectly() {
        // Given - 有效的請求
        
        // When - 設定記住我選項為 true
        validRequest.setRememberMe(true);
        
        // Then - 應該正確設定
        assertTrue(validRequest.isRememberMe());
    }
    
    @Test
    @DisplayName("清除敏感資料時應該將密碼設為 null")
    void test_clearSensitiveData_whenCalled_then_shouldSetPasswordToNull() {
        // Given - 有效的請求
        assertNotNull(validRequest.getPassword());
        
        // When - 清除敏感資料
        validRequest.clearSensitiveData();
        
        // Then - 密碼應該被設為 null
        assertNull(validRequest.getPassword());
        // 使用者名稱應該保持不變
        assertEquals("testuser", validRequest.getUsername());
    }
    
    @Test
    @DisplayName("toString 方法應該隱藏密碼資訊")
    void test_toString_whenCalled_then_shouldHidePasswordInformation() {
        // Given - 有效的請求
        validRequest.setRememberMe(true);
        
        // When - 呼叫 toString
        String result = validRequest.toString();
        
        // Then - 應該包含使用者名稱和記住我選項但隱藏密碼
        assertNotNull(result);
        assertTrue(result.contains("testuser"));
        assertTrue(result.contains("rememberMe=true"));
        assertTrue(result.contains("[PROTECTED]"));
        assertFalse(result.contains("password123"));
    }
    
    @Test
    @DisplayName("預設建構子應該建立空的 DTO")
    void test_defaultConstructor_whenCalled_then_shouldCreateEmptyDto() {
        // Given & When - 使用預設建構子
        LoginRequest request = new LoginRequest();
        
        // Then - 所有屬性應該為預設值
        assertNull(request.getUsername());
        assertNull(request.getPassword());
        assertFalse(request.isRememberMe());
    }
}