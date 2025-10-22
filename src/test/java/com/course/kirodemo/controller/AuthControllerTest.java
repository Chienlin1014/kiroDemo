package com.course.kirodemo.controller;

import com.course.kirodemo.dto.UserRegistrationRequest;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.UserAlreadyExistsException;
import com.course.kirodemo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 整合測試
 * 測試認證相關的 HTTP 請求處理
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("AuthController 整合測試")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserService userService;

    private MockMvc mockMvc;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Given - 準備測試資料
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        mockUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET / 當使用者未認證時應該重新導向到登入頁面")
    void test_redirectToTodos_whenNotAuthenticated_then_shouldRedirectToLogin() throws Exception {
        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET / 當使用者已認證時應該重新導向到待辦事項頁面")
    void test_redirectToTodos_whenAuthenticated_then_shouldRedirectToTodos() throws Exception {
        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));
    }

    @Test
    @DisplayName("GET /login 應該回傳登入頁面")
    void test_getLoginPage_whenAccessed_then_shouldReturnLoginView() throws Exception {
        // When & Then
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /login 當有錯誤參數時應該顯示錯誤訊息")
    void test_getLoginPage_whenErrorParam_then_shouldShowErrorMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("error", "使用者名稱或密碼錯誤"));
    }

    @Test
    @DisplayName("GET /login 當有登出參數時應該顯示登出訊息")
    void test_getLoginPage_whenLogoutParam_then_shouldShowLogoutMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("message", "您已成功登出"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /login 當使用者已認證時應該重新導向到待辦事項頁面")
    void test_getLoginPage_whenAuthenticated_then_shouldRedirectToTodos() throws Exception {
        // When & Then
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));
    }

    @Test
    @DisplayName("GET /register 應該回傳註冊頁面")
    void test_getRegisterPage_whenAccessed_then_shouldReturnRegisterView() throws Exception {
        // When & Then
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registrationRequest"));
    }

    @Test
    @DisplayName("POST /register 當資料有效時應該註冊使用者並重新導向到登入頁面")
    void test_processRegistration_whenValidData_then_shouldRegisterAndRedirect() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/register")
                .param("username", "newuser")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /register 當資料無效時應該回傳註冊表單並顯示錯誤")
    void test_processRegistration_whenInvalidData_then_shouldReturnFormWithErrors() throws Exception {
        // When & Then - 測試空白使用者名稱
        mockMvc.perform(post("/register")
                .param("username", "")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registrationRequest"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("POST /register 當密碼不一致時應該回傳註冊表單並顯示錯誤")
    void test_processRegistration_whenPasswordMismatch_then_shouldReturnFormWithError() throws Exception {
        // When & Then
        mockMvc.perform(post("/register")
                .param("username", "newuser")
                .param("password", "password123")
                .param("confirmPassword", "differentpassword")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("error", "密碼與確認密碼不一致"))
                .andExpect(model().attributeExists("registrationRequest"));

        verify(userService, never()).registerUser(any());
    }

    @Test
    @DisplayName("POST /register 當使用者已存在時應該回傳註冊表單並顯示錯誤")
    void test_processRegistration_whenUserExists_then_shouldReturnFormWithError() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("使用者名稱已存在"));

        // When & Then
        mockMvc.perform(post("/register")
                .param("username", "existinguser")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("error", "使用者名稱已存在"))
                .andExpect(model().attributeExists("registrationRequest"));

        verify(userService).registerUser(any(UserRegistrationRequest.class));
    }
}