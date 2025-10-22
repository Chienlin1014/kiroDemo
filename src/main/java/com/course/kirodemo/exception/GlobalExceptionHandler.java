package com.course.kirodemo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;

/**
 * 全域異常處理器
 * 統一處理應用程式中的各種異常，提供友善的錯誤頁面和訊息
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 處理待辦事項不存在異常
     */
    @ExceptionHandler(TodoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleTodoNotFound(TodoNotFoundException ex, Model model, HttpServletRequest request) {
        logger.warn("待辦事項不存在異常: {} - 請求路徑: {}", ex.getMessage(), request.getRequestURI());
        
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        return "error/404";
    }
    
    /**
     * 處理使用者不存在異常
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFound(UserNotFoundException ex, Model model, HttpServletRequest request) {
        logger.warn("使用者不存在異常: {} - 請求路徑: {}", ex.getMessage(), request.getRequestURI());
        
        model.addAttribute("error", "找不到指定的使用者");
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        return "error/404";
    }
    
    /**
     * 處理未授權存取異常
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedAccess(UnauthorizedAccessException ex, Model model, HttpServletRequest request) {
        logger.warn("未授權存取異常: {} - 請求路徑: {}", ex.getMessage(), request.getRequestURI());
        
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
        return "error/403";
    }
    
    /**
     * 處理使用者已存在異常
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleUserAlreadyExists(UserAlreadyExistsException ex, RedirectAttributes redirectAttributes) {
        logger.warn("使用者已存在異常: {}", ex.getMessage());
        
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/register";
    }
    
    /**
     * 處理表單驗證異常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(MethodArgumentNotValidException ex, Model model, HttpServletRequest request) {
        logger.warn("表單驗證異常 - 請求路徑: {}", request.getRequestURI());
        
        StringBuilder errorMessage = new StringBuilder("資料驗證失敗：");
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errorMessage.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ")
        );
        
        model.addAttribute("error", errorMessage.toString());
        model.addAttribute("status", HttpStatus.BAD_REQUEST.value());
        return "error/error";
    }
    
    /**
     * 處理一般驗證異常
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidation(ValidationException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        logger.warn("驗證異常: {} - 請求路徑: {}", ex.getMessage(), request.getRequestURI());
        
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        
        // 根據請求路徑決定重新導向位置
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/todos")) {
            return "redirect:/todos";
        }
        return "redirect:/";
    }
    
    /**
     * 處理非法參數異常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        logger.warn("非法參數異常: {} - 請求路徑: {}", ex.getMessage(), request.getRequestURI());
        
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        
        // 根據請求路徑決定重新導向位置
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/todos")) {
            return "redirect:/todos";
        }
        return "redirect:/";
    }
    
    /**
     * 處理所有其他未預期的異常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model, HttpServletRequest request) {
        logger.error("未預期的系統異常 - 請求路徑: {} - 異常類型: {}", 
                    request.getRequestURI(), ex.getClass().getSimpleName(), ex);
        
        model.addAttribute("error", "系統發生未預期的錯誤，請稍後再試");
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("message", ex.getMessage());
        return "error/error";
    }
}