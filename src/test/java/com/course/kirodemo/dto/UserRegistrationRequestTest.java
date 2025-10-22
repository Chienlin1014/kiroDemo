package com.course.kirodemo.dto;

import com.course.kirodemo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRegistrationRequest DTO 的單元測試
 * 測試 DTO 的驗證邏輯和轉換方法
 */
@DisplayName("UserRegistrationRequest DTO 測試")
class UserRegistrationRequestTest {
    
    private UserRegistrationRequest validRequest;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        validRequest = new UserRegistrationRequest(
            "testuser",
            "password123",
            "password123"
        );
    }
    
    @Test
    @DisplayName("建立有效的 UserRegistrationRequest 時應該設定所有屬性")
    void test_constructor_whenValidData_then_shouldSetAllProperties() {
        // Given - 測試資料
        String expectedUsername = "newuser";
        String expectedPassword = "newpass123";
        String expectedConfirmPassword = "newpass123";
        
        // When - 建立 UserRegistrationRequest
        UserRegistrationRequest request = new UserRegistrationRequest(
            expectedUsername, expectedPassword, expectedConfirmPassword);
        
        // Then - 驗證所有屬性都正確設定
        assertEquals(expectedUsername, request.getUsername());
        assertEquals(expectedPassword, request.getPassword());
        assertEquals(expectedConfirmPassword, request.getConfirmPassword());
    }
    
    @Test
    @DisplayName("密碼一致時應該回傳 true")
    void test_isPasswordMatching_whenPasswordsMatch_then_shouldReturnTrue() {
        // Given - 密碼一致的請求
        
        // When - 檢查密碼是否一致
        boolean result = validRequest.isPasswordMatching();
        
        // Then - 應該回傳 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("密碼不一致時應該回傳 false")
    void test_isPasswordMatching_whenPasswordsDontMatch_then_shouldReturnFalse() {
        // Given - 密碼不一致的請求
        validRequest.setConfirmPassword("differentpassword");
        
        // When - 檢查密碼是否一致
        boolean result = validRequest.isPasswordMatching();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("null 密碼時應該回傳 false")
    void test_isPasswordMatching_whenPasswordIsNull_then_shouldReturnFalse() {
        // Given - null 密碼的請求
        validRequest.setPassword(null);
        
        // When - 檢查密碼是否一致
        boolean result = validRequest.isPasswordMatching();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("轉換為實體時應該建立正確的 User")
    void test_toEntity_whenValidRequest_then_shouldCreateCorrectUser() {
        // Given - 有效的請求
        
        // When - 轉換為實體
        User user = validRequest.toEntity();
        
        // Then - 驗證實體屬性正確設定
        assertNotNull(user);
        assertEquals(validRequest.getUsername(), user.getUsername());
        assertEquals(validRequest.getPassword(), user.getPassword());
    }
    
    @Test
    @DisplayName("使用加密密碼轉換為實體時應該使用提供的密碼")
    void test_toEntity_whenWithEncodedPassword_then_shouldUseProvidedPassword() {
        // Given - 有效的請求和加密密碼
        String encodedPassword = "$2a$10$encodedpassword";
        
        // When - 使用加密密碼轉換為實體
        User user = validRequest.toEntity(encodedPassword);
        
        // Then - 驗證實體使用加密密碼
        assertNotNull(user);
        assertEquals(validRequest.getUsername(), user.getUsername());
        assertEquals(encodedPassword, user.getPassword());
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
    @DisplayName("驗證過短使用者名稱請求時應該回傳 false")
    void test_isValid_whenUsernameTooShort_then_shouldReturnFalse() {
        // Given - 過短使用者名稱的請求
        validRequest.setUsername("ab"); // 少於 3 字元
        
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
    @DisplayName("驗證過短密碼請求時應該回傳 false")
    void test_isValid_whenPasswordTooShort_then_shouldReturnFalse() {
        // Given - 過短密碼的請求
        validRequest.setPassword("123"); // 少於 4 字元
        validRequest.setConfirmPassword("123");
        
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
        validRequest.setConfirmPassword(longPassword);
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("驗證密碼不一致請求時應該回傳 false")
    void test_isValid_whenPasswordsDontMatch_then_shouldReturnFalse() {
        // Given - 密碼不一致的請求
        validRequest.setConfirmPassword("differentpassword");
        
        // When - 驗證請求
        boolean result = validRequest.isValid();
        
        // Then - 應該回傳 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("toString 方法應該隱藏密碼資訊")
    void test_toString_whenCalled_then_shouldHidePasswordInformation() {
        // Given - 有效的請求
        
        // When - 呼叫 toString
        String result = validRequest.toString();
        
        // Then - 應該包含使用者名稱但隱藏密碼
        assertNotNull(result);
        assertTrue(result.contains("testuser"));
        assertTrue(result.contains("[PROTECTED]"));
        assertFalse(result.contains("password123"));
    }
    
    @Test
    @DisplayName("預設建構子應該建立空的 DTO")
    void test_defaultConstructor_whenCalled_then_shouldCreateEmptyDto() {
        // Given & When - 使用預設建構子
        UserRegistrationRequest request = new UserRegistrationRequest();
        
        // Then - 所有屬性應該為 null
        assertNull(request.getUsername());
        assertNull(request.getPassword());
        assertNull(request.getConfirmPassword());
    }
}