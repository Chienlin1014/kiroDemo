package com.course.kirodemo.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Security 配置測試
 * 測試認證和授權規則是否正確配置
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("未認證使用者存取受保護頁面時應該重新導向到登入頁面")
    void test_accessProtectedPage_whenNotAuthenticated_then_shouldRedirectToLogin() throws Exception {
        setUp();
        
        // Given & When & Then
        mockMvc.perform(get("/todos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("未認證使用者可以存取登入頁面")
    void test_accessLoginPage_whenNotAuthenticated_then_shouldReturnLoginPage() throws Exception {
        setUp();
        
        // Given & When & Then
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("未認證使用者可以存取註冊頁面")
    void test_accessRegisterPage_whenNotAuthenticated_then_shouldReturnRegisterPage() throws Exception {
        setUp();
        
        // Given & When & Then
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("已認證使用者存取待辦事項頁面時應該處理使用者不存在的情況")
    void test_accessTodosPage_whenAuthenticated_then_shouldHandleUserNotFound() throws Exception {
        setUp();
        
        // Given & When & Then
        // 由於測試使用者不存在於資料庫中，會拋出異常並由全域異常處理器處理為 404
        mockMvc.perform(get("/todos"))
                .andExpect(status().isNotFound()); // 預期會有 404 錯誤
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("已認證使用者可以登出")
    void test_logout_whenAuthenticated_then_shouldRedirectToLogin() throws Exception {
        setUp();
        
        // Given & When & Then
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    @DisplayName("CSRF 保護應該啟用，沒有 CSRF token 的 POST 請求會被重新導向")
    void test_csrfProtection_whenPostWithoutToken_then_shouldRedirectToInvalidSession() throws Exception {
        setUp();
        
        // Given & When & Then
        // Spring Security 會重新導向到 /login?invalid=true 而不是直接回傳 403
        mockMvc.perform(post("/login")
                .param("username", "testuser")
                .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?invalid=true"));
    }
}