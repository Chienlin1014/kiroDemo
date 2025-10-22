package com.course.kirodemo.service;

import com.course.kirodemo.dto.UserRegistrationRequest;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.UserAlreadyExistsException;
import com.course.kirodemo.repository.UserRepository;
import com.course.kirodemo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService 單元測試
 * 使用 Mockito 模擬依賴，測試業務邏輯
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 單元測試")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private UserRegistrationRequest validRegistrationRequest;
    private User mockUser;
    private BCryptPasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        validRegistrationRequest = new UserRegistrationRequest("testuser", "password123", "password123");
        
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("$2a$10$encodedPassword");
        mockUser.setCreatedAt(LocalDateTime.now());
        
        passwordEncoder = new BCryptPasswordEncoder();
    }
    
    @Test
    @DisplayName("有效註冊請求時應該成功建立使用者")
    void test_registerUser_whenValidRequest_then_shouldCreateUser() {
        // Given - 設定 Mock 行為
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        
        // When - 執行被測試的方法
        User result = userService.registerUser(validRegistrationRequest);
        
        // Then - 驗證結果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(1L, result.getId());
        
        // 驗證 Repository 方法被正確呼叫
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("使用者名稱已存在時應該拋出 UserAlreadyExistsException")
    void test_registerUser_whenUsernameExists_then_shouldThrowUserAlreadyExistsException() {
        // Given - 設定使用者名稱已存在
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When & Then - 執行並驗證異常
        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> userService.registerUser(validRegistrationRequest)
        );
        
        assertTrue(exception.getMessage().contains("testuser"));
        
        // 驗證 save 方法沒有被呼叫
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("註冊請求為 null 時應該拋出 IllegalArgumentException")
    void test_registerUser_whenRequestIsNull_then_shouldThrowIllegalArgumentException() {
        // When & Then - 執行並驗證異常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.registerUser(null)
        );
        
        assertEquals("註冊資料無效", exception.getMessage());
        
        // 驗證 Repository 方法沒有被呼叫
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("註冊請求無效時應該拋出 IllegalArgumentException")
    void test_registerUser_whenRequestIsInvalid_then_shouldThrowIllegalArgumentException() {
        // Given - 建立無效的註冊請求（密碼不一致）
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest("testuser", "password123", "differentPassword");
        
        // When & Then - 執行並驗證異常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.registerUser(invalidRequest)
        );
        
        assertEquals("註冊資料無效", exception.getMessage());
    }
    
    @Test
    @DisplayName("根據使用者名稱查詢存在的使用者時應該回傳使用者")
    void test_findByUsername_whenUserExists_then_shouldReturnUser() {
        // Given - 設定 Mock 行為
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        
        // When - 執行被測試的方法
        Optional<User> result = userService.findByUsername("testuser");
        
        // Then - 驗證結果
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("根據使用者名稱查詢不存在的使用者時應該回傳空的 Optional")
    void test_findByUsername_whenUserNotExists_then_shouldReturnEmpty() {
        // Given - 設定 Mock 行為
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When - 執行被測試的方法
        Optional<User> result = userService.findByUsername("nonexistent");
        
        // Then - 驗證結果
        assertFalse(result.isPresent());
        
        verify(userRepository).findByUsername("nonexistent");
    }
    
    @Test
    @DisplayName("使用者名稱為 null 時應該回傳空的 Optional")
    void test_findByUsername_whenUsernameIsNull_then_shouldReturnEmpty() {
        // When - 執行被測試的方法
        Optional<User> result = userService.findByUsername(null);
        
        // Then - 驗證結果
        assertFalse(result.isPresent());
        
        // 驗證 Repository 方法沒有被呼叫
        verify(userRepository, never()).findByUsername(anyString());
    }
    
    @Test
    @DisplayName("使用者名稱為空字串時應該回傳空的 Optional")
    void test_findByUsername_whenUsernameIsEmpty_then_shouldReturnEmpty() {
        // When - 執行被測試的方法
        Optional<User> result = userService.findByUsername("   ");
        
        // Then - 驗證結果
        assertFalse(result.isPresent());
        
        // 驗證 Repository 方法沒有被呼叫
        verify(userRepository, never()).findByUsername(anyString());
    }
    
    @Test
    @DisplayName("有效憑證驗證時應該回傳 true")
    void test_validatePassword_whenValidCredentials_then_shouldReturnTrue() {
        // Given - 設定 Mock 行為和加密密碼
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        mockUser.setPassword(encodedPassword);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        
        // When - 執行被測試的方法
        boolean result = userService.validatePassword("testuser", rawPassword);
        
        // Then - 驗證結果
        assertTrue(result);
        
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("無效密碼驗證時應該回傳 false")
    void test_validatePassword_whenInvalidPassword_then_shouldReturnFalse() {
        // Given - 設定 Mock 行為
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        String encodedPassword = passwordEncoder.encode(correctPassword);
        mockUser.setPassword(encodedPassword);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        
        // When - 執行被測試的方法
        boolean result = userService.validatePassword("testuser", wrongPassword);
        
        // Then - 驗證結果
        assertFalse(result);
        
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("使用者不存在時密碼驗證應該回傳 false")
    void test_validatePassword_whenUserNotExists_then_shouldReturnFalse() {
        // Given - 設定 Mock 行為
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When - 執行被測試的方法
        boolean result = userService.validatePassword("nonexistent", "password123");
        
        // Then - 驗證結果
        assertFalse(result);
        
        verify(userRepository).findByUsername("nonexistent");
    }
    
    @Test
    @DisplayName("使用者名稱或密碼為 null 時應該回傳 false")
    void test_validatePassword_whenUsernameOrPasswordIsNull_then_shouldReturnFalse() {
        // When & Then - 測試使用者名稱為 null
        boolean result1 = userService.validatePassword(null, "password123");
        assertFalse(result1);
        
        // When & Then - 測試密碼為 null
        boolean result2 = userService.validatePassword("testuser", null);
        assertFalse(result2);
        
        // 驗證 Repository 方法沒有被呼叫
        verify(userRepository, never()).findByUsername(anyString());
    }
    
    @Test
    @DisplayName("檢查存在的使用者名稱時應該回傳 true")
    void test_existsByUsername_whenUsernameExists_then_shouldReturnTrue() {
        // Given - 設定 Mock 行為
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When - 執行被測試的方法
        boolean result = userService.existsByUsername("testuser");
        
        // Then - 驗證結果
        assertTrue(result);
        
        verify(userRepository).existsByUsername("testuser");
    }
    
    @Test
    @DisplayName("檢查不存在的使用者名稱時應該回傳 false")
    void test_existsByUsername_whenUsernameNotExists_then_shouldReturnFalse() {
        // Given - 設定 Mock 行為
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);
        
        // When - 執行被測試的方法
        boolean result = userService.existsByUsername("nonexistent");
        
        // Then - 驗證結果
        assertFalse(result);
        
        verify(userRepository).existsByUsername("nonexistent");
    }
    
    @Test
    @DisplayName("使用者名稱為 null 或空字串時應該回傳 false")
    void test_existsByUsername_whenUsernameIsNullOrEmpty_then_shouldReturnFalse() {
        // When & Then - 測試 null
        boolean result1 = userService.existsByUsername(null);
        assertFalse(result1);
        
        // When & Then - 測試空字串
        boolean result2 = userService.existsByUsername("   ");
        assertFalse(result2);
        
        // 驗證 Repository 方法沒有被呼叫
        verify(userRepository, never()).existsByUsername(anyString());
    }
    
    @Test
    @DisplayName("加密密碼時應該回傳加密後的密碼")
    void test_encodePassword_whenValidPassword_then_shouldReturnEncodedPassword() {
        // Given - 準備原始密碼
        String rawPassword = "password123";
        
        // When - 執行被測試的方法
        String encodedPassword = userService.encodePassword(rawPassword);
        
        // Then - 驗證結果
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$"));
        
        // 驗證加密後的密碼可以正確驗證
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }
    
    @Test
    @DisplayName("密碼為 null 時應該拋出 IllegalArgumentException")
    void test_encodePassword_whenPasswordIsNull_then_shouldThrowIllegalArgumentException() {
        // When & Then - 執行並驗證異常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.encodePassword(null)
        );
        
        assertEquals("密碼不能為空", exception.getMessage());
    }
}