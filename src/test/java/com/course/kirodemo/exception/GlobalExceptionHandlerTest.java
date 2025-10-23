package com.course.kirodemo.exception;

import com.course.kirodemo.dto.ExtendTodoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * GlobalExceptionHandler 的單元測試
 * 測試全域異常處理器的各種異常處理邏輯
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 測試")
class GlobalExceptionHandlerTest {
    
    @Mock
    private Model model;
    
    @Mock
    private RedirectAttributes redirectAttributes;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private BindingResult bindingResult;
    
    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;
    
    @BeforeEach
    void setUp() {
        // 使用 lenient() 避免不必要的 stubbing 錯誤
        lenient().when(request.getRequestURI()).thenReturn("/test-path");
    }
    
    @Test
    @DisplayName("處理待辦事項不存在異常時應該回傳 404 錯誤頁面")
    void test_handleTodoNotFound_whenTodoNotFoundException_then_shouldReturn404ErrorPage() {
        // Given (給定) - 設定測試前置條件
        TodoNotFoundException exception = TodoNotFoundException.forId(1L);
        
        // When (當) - 執行被測試的行為
        String result = globalExceptionHandler.handleTodoNotFound(exception, model, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals("error/404", result);
        verify(model).addAttribute("error", exception.getMessage());
        verify(model).addAttribute("status", HttpStatus.NOT_FOUND.value());
    }
    
    @Test
    @DisplayName("處理使用者不存在異常時應該回傳 404 錯誤頁面")
    void test_handleUserNotFound_whenUserNotFoundException_then_shouldReturn404ErrorPage() {
        // Given (給定) - 設定測試前置條件
        UserNotFoundException exception = UserNotFoundException.forUsername("testuser");
        
        // When (當) - 執行被測試的行為
        String result = globalExceptionHandler.handleUserNotFound(exception, model, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals("error/404", result);
        verify(model).addAttribute("error", "找不到指定的使用者");
        verify(model).addAttribute("status", HttpStatus.NOT_FOUND.value());
    }
    
    @Test
    @DisplayName("處理未授權存取異常時應該回傳 403 錯誤頁面")
    void test_handleUnauthorizedAccess_whenUnauthorizedAccessException_then_shouldReturn403ErrorPage() {
        // Given (給定) - 設定測試前置條件
        UnauthorizedAccessException exception = new UnauthorizedAccessException("您沒有權限存取此資源");
        
        // When (當) - 執行被測試的行為
        String result = globalExceptionHandler.handleUnauthorizedAccess(exception, model, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals("error/403", result);
        verify(model).addAttribute("error", exception.getMessage());
        verify(model).addAttribute("status", HttpStatus.FORBIDDEN.value());
    }
    
    @Test
    @DisplayName("處理使用者已存在異常時應該重新導向到註冊頁面")
    void test_handleUserAlreadyExists_whenUserAlreadyExistsException_then_shouldRedirectToRegister() {
        // Given (給定) - 設定測試前置條件
        UserAlreadyExistsException exception = UserAlreadyExistsException.forUsername("testuser");
        
        // When (當) - 執行被測試的行為
        String result = globalExceptionHandler.handleUserAlreadyExists(exception, redirectAttributes);
        
        // Then (那麼) - 驗證預期結果
        assertEquals("redirect:/register", result);
        verify(redirectAttributes).addFlashAttribute("error", exception.getMessage());
    }
    
    @Test
    @DisplayName("處理表單驗證異常時應該回傳錯誤頁面並顯示驗證訊息")
    void test_handleValidationException_whenMethodArgumentNotValidException_then_shouldReturnErrorPageWithValidationMessages() {
        // Given (給定) - 設定測試前置條件
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        FieldError fieldError1 = new FieldError("todoItem", "title", "標題不能為空");
        FieldError fieldError2 = new FieldError("todoItem", "dueDate", "預計完成日不能為空");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));
        when(request.getRequestURI()).thenReturn("/todos/create");
        when(request.getHeader("X-Requested-With")).thenReturn(null);
        when(request.getHeader("Accept")).thenReturn("text/html");
        
        // When (當) - 執行被測試的行為
        Object result = globalExceptionHandler.handleValidationException(exception, model, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals("error/error", result);
        verify(model).addAttribute(eq("error"), contains("資料驗證失敗"));
        verify(model).addAttribute("status", HttpStatus.BAD_REQUEST.value());
    }
    
    @Test
    @DisplayName("處理一般驗證異常時應該重新導向到待辦事項頁面")
    void test_handleValidation_whenValidationExceptionFromTodos_then_shouldRedirectToTodos() {
        // Given (給定) - 設定測試前置條件
        ValidationException exception = new ValidationException("驗證失敗");
        when(request.getHeader("Referer")).thenReturn("http://localhost:8080/todos");
        
        // When (當) - 執行被測試的行為
        String result = globalExceptionHandler.handleValidation(exception, redirectAttributes, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals("redirect:/todos", result);
        verify(redirectAttributes).addFlashAttribute("error", exception.getMessage());
    }
    
    @Test
    @DisplayName("處理一般驗證異常時沒有 Referer 應該重新導向到首頁")
    void test_handleValidation_whenValidationExceptionWithoutReferer_then_shouldRedirectToHome() {
        // Given (給定) - 設定測試前置條件
        ValidationException exception = new ValidationException("驗證失敗");
        when(request.getHeader("Referer")).thenReturn(null);
        
        // When (當) - 執行被測試的行為
        String result = globalExceptionHandler.handleValidation(exception, redirectAttributes, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", exception.getMessage());
    }
    
    @Test
    @DisplayName("處理非法參數異常時應該重新導向到待辦事項頁面")
    void test_handleIllegalArgument_whenIllegalArgumentExceptionFromTodos_then_shouldRedirectToTodos() {
        // Given (給定) - 設定測試前置條件
        IllegalArgumentException exception = new IllegalArgumentException("參數無效");
        when(request.getHeader("Referer")).thenReturn("http://localhost:8080/todos/new");
        
        // When (當) - 執行被測試的行為
        String result = globalExceptionHandler.handleIllegalArgument(exception, redirectAttributes, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals("redirect:/todos", result);
        verify(redirectAttributes).addFlashAttribute("error", exception.getMessage());
    }
    
    @Test
    @DisplayName("處理非法參數異常時沒有 Referer 應該重新導向到首頁")
    void test_handleIllegalArgument_whenIllegalArgumentExceptionWithoutReferer_then_shouldRedirectToHome() {
        // Given (給定) - 設定測試前置條件
        IllegalArgumentException exception = new IllegalArgumentException("參數無效");
        when(request.getHeader("Referer")).thenReturn(null);
        
        // When (當) - 執行被測試的行為
        String result = globalExceptionHandler.handleIllegalArgument(exception, redirectAttributes, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals("redirect:/", result);
        verify(redirectAttributes).addFlashAttribute("error", exception.getMessage());
    }
    
    @Test
    @DisplayName("處理一般異常時應該回傳 500 錯誤頁面")
    void test_handleGenericException_whenGenericException_then_shouldReturn500ErrorPage() {
        // Given (給定) - 設定測試前置條件
        RuntimeException exception = new RuntimeException("未預期的錯誤");
        
        // When (當) - 執行被測試的行為
        String result = globalExceptionHandler.handleGenericException(exception, model, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals("error/error", result);
        verify(model).addAttribute("error", "系統發生未預期的錯誤，請稍後再試");
        verify(model).addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(model).addAttribute("message", exception.getMessage());
    }
    
    @Test
    @DisplayName("處理無效延期異常時應該回傳 JSON 錯誤回應")
    void test_handleInvalidExtension_whenInvalidExtensionException_then_shouldReturnJsonErrorResponse() {
        // Given (給定) - 設定測試前置條件
        InvalidExtensionException exception = InvalidExtensionException.forInvalidDays(-1);
        when(request.getRequestURI()).thenReturn("/todos/1/extend");
        
        // When (當) - 執行被測試的行為
        ResponseEntity<ExtendTodoResponse> result = globalExceptionHandler.handleInvalidExtension(exception, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
        assertTrue(result.getBody().getMessage().contains("延期天數無效"));
    }
    
    @Test
    @DisplayName("處理不允許延期異常時應該回傳 JSON 錯誤回應")
    void test_handleExtensionNotAllowed_whenExtensionNotAllowedException_then_shouldReturnJsonErrorResponse() {
        // Given (給定) - 設定測試前置條件
        ExtensionNotAllowedException exception = ExtensionNotAllowedException.forCompletedTodo(1L);
        when(request.getRequestURI()).thenReturn("/todos/1/extend");
        
        // When (當) - 執行被測試的行為
        ResponseEntity<ExtendTodoResponse> result = globalExceptionHandler.handleExtensionNotAllowed(exception, request);
        
        // Then (那麼) - 驗證預期結果
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
        assertEquals(Long.valueOf(1L), result.getBody().getTodoId());
        assertTrue(result.getBody().getMessage().contains("待辦事項已完成"));
    }
    
    @Test
    @DisplayName("處理延期相關的表單驗證異常時應該回傳 JSON 回應")
    void test_handleValidationException_whenExtensionAjaxRequest_then_shouldReturnJsonResponse() {
        // Given (給定) - 設定測試前置條件
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        FieldError fieldError = new FieldError("extendTodoRequest", "extensionDays", "延期天數必須為正數");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));
        when(request.getRequestURI()).thenReturn("/todos/1/extend");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getHeader("Accept")).thenReturn("application/json");
        
        // When (當) - 執行被測試的行為
        Object result = globalExceptionHandler.handleValidationException(exception, model, request);
        
        // Then (那麼) - 驗證預期結果
        assertTrue(result instanceof ResponseEntity);
        @SuppressWarnings("unchecked")
        ResponseEntity<ExtendTodoResponse> responseEntity = (ResponseEntity<ExtendTodoResponse>) result;
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertFalse(responseEntity.getBody().isSuccess());
        assertTrue(responseEntity.getBody().getMessage().contains("輸入驗證失敗"));
    }
}