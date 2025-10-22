package com.course.kirodemo.controller;

import com.course.kirodemo.dto.UserRegistrationRequest;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.UserAlreadyExistsException;
import com.course.kirodemo.security.SecurityUtils;
import com.course.kirodemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 認證控制器
 * 處理使用者登入、註冊和登出相關的 HTTP 請求
 */
@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 根路徑重新導向
     */
    @GetMapping("/")
    public String redirectToTodos() {
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/todos";
        }
        return "redirect:/login";
    }

    /**
     * 顯示登入頁面
     */
    @GetMapping("/login")
    public String getLoginPage(@RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "logout", required = false) String logout,
                              @RequestParam(value = "expired", required = false) String expired,
                              @RequestParam(value = "invalid", required = false) String invalid,
                              Model model) {
        
        // 如果已經登入，重新導向到待辦事項頁面
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/todos";
        }
        
        // 處理各種登入狀態訊息
        if (error != null) {
            model.addAttribute("error", "使用者名稱或密碼錯誤");
        }
        if (logout != null) {
            model.addAttribute("message", "您已成功登出");
        }
        if (expired != null) {
            model.addAttribute("error", "您的會話已過期，請重新登入");
        }
        if (invalid != null) {
            model.addAttribute("error", "會話無效，請重新登入");
        }
        
        return "auth/login";
    }

    /**
     * 顯示註冊頁面
     */
    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("registrationRequest", new UserRegistrationRequest());
        return "auth/register";
    }

    /**
     * 處理註冊請求
     */
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registrationRequest") UserRegistrationRequest registrationRequest,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        
        // 驗證表單資料
        if (bindingResult.hasErrors()) {
            model.addAttribute("registrationRequest", registrationRequest);
            return "auth/register";
        }

        // 檢查密碼是否一致
        if (!registrationRequest.isPasswordMatching()) {
            model.addAttribute("error", "密碼與確認密碼不一致");
            model.addAttribute("registrationRequest", registrationRequest);
            return "auth/register";
        }

        try {
            // 註冊使用者
            User user = userService.registerUser(registrationRequest);
            redirectAttributes.addFlashAttribute("successMessage", "註冊成功！請登入您的帳號。");
            return "redirect:/login";
            
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registrationRequest", registrationRequest);
            return "auth/register";
        }
    }

    /**
     * 登出處理由 Spring Security 自動處理
     * 此方法僅作為文件說明，實際登出由 SecurityConfig 中的配置處理
     */
}