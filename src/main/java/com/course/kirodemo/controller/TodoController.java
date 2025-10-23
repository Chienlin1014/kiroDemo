package com.course.kirodemo.controller;

import com.course.kirodemo.dto.CreateTodoRequest;
import com.course.kirodemo.dto.ExtendTodoRequest;
import com.course.kirodemo.dto.ExtendTodoResponse;
import com.course.kirodemo.dto.UpdateTodoRequest;
import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.exception.TodoNotFoundException;
import com.course.kirodemo.exception.UnauthorizedAccessException;
import com.course.kirodemo.security.SecurityUtils;
import com.course.kirodemo.service.DateValidationService;
import com.course.kirodemo.service.TodoExtensionService;
import com.course.kirodemo.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 待辦事項控制器
 * 處理待辦事項相關的 HTTP 請求
 */
@Controller
@RequestMapping("/todos")
public class TodoController {

    private final TodoService todoService;
    private final TodoExtensionService extensionService;
    private final DateValidationService dateValidationService;

    @Autowired
    public TodoController(TodoService todoService, 
                         TodoExtensionService extensionService,
                         DateValidationService dateValidationService) {
        this.todoService = todoService;
        this.extensionService = extensionService;
        this.dateValidationService = dateValidationService;
    }

    /**
     * 取得當前登入使用者名稱
     */
    private String getCurrentUsername() {
        return SecurityUtils.getCurrentUsername();
    }

    /**
     * 顯示待辦事項列表
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String getTodos(@RequestParam(defaultValue = "CREATED_AT_DESC") String sortBy,
                          Model model) {
        
        String username = getCurrentUsername();
        
        // 解析排序參數
        TodoService.SortBy sort;
        try {
            sort = TodoService.SortBy.valueOf(sortBy);
        } catch (IllegalArgumentException e) {
            sort = TodoService.SortBy.CREATED_AT_DESC;
        }

        // 取得待辦事項列表
        List<TodoItem> todos = todoService.getUserTodos(username, sort);
        
        model.addAttribute("todos", todos);
        model.addAttribute("sortBy", sort.name());
        
        return "todos/list";
    }

    /**
     * 顯示新增待辦事項表單
     */
    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String getNewTodoForm(Model model) {
        model.addAttribute("todoRequest", new CreateTodoRequest());
        model.addAttribute("isEdit", false);
        
        return "todos/form";
    }

    /**
     * 處理新增待辦事項請求
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public String createTodo(@Valid @ModelAttribute("todoRequest") CreateTodoRequest todoRequest,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        // 驗證表單資料
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "todos/form";
        }

        String username = getCurrentUsername();
        
        try {
            // 建立待辦事項
            TodoItem createdTodo = todoService.createTodo(todoRequest, username);
            redirectAttributes.addFlashAttribute("successMessage", "待辦事項建立成功");
            return "redirect:/todos";
            
        } catch (Exception e) {
            model.addAttribute("error", "建立待辦事項時發生錯誤：" + e.getMessage());
            model.addAttribute("isEdit", false);
            return "todos/form";
        }
    }

    /**
     * 顯示編輯待辦事項表單
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String getEditTodoForm(@PathVariable Long id,
                                Model model) {
        
        String username = getCurrentUsername();
        
        // 查詢待辦事項
        Optional<TodoItem> todoOpt = todoService.findUserTodo(id, username);
        
        if (todoOpt.isEmpty()) {
            throw new TodoNotFoundException("找不到指定的待辦事項");
        }

        TodoItem todo = todoOpt.get();
        UpdateTodoRequest updateRequest = UpdateTodoRequest.fromEntity(todo);
        
        model.addAttribute("todoRequest", updateRequest);
        model.addAttribute("isEdit", true);
        model.addAttribute("todoId", id);
        
        return "todos/form";
    }

    /**
     * 處理更新待辦事項請求
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public String updateTodo(@PathVariable Long id,
                           @Valid @ModelAttribute("todoRequest") UpdateTodoRequest updateRequest,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        // 驗證表單資料
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("todoId", id);
            return "todos/form";
        }

        String username = getCurrentUsername();
        
        try {
            // 更新待辦事項
            TodoItem updatedTodo = todoService.updateTodo(id, updateRequest, username);
            redirectAttributes.addFlashAttribute("successMessage", "待辦事項更新成功");
            return "redirect:/todos";
            
        } catch (TodoNotFoundException | UnauthorizedAccessException e) {
            // 讓全域異常處理器處理
            throw e;
            
        } catch (Exception e) {
            model.addAttribute("error", "更新待辦事項時發生錯誤：" + e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("todoId", id);
            return "todos/form";
        }
    }

    /**
     * 處理刪除待辦事項請求
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteTodo(@PathVariable Long id,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        String username = getCurrentUsername();
        
        try {
            // 刪除待辦事項
            todoService.deleteTodo(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "待辦事項刪除成功");
            return "redirect:/todos";
            
        } catch (TodoNotFoundException | UnauthorizedAccessException e) {
            // 讓全域異常處理器處理
            throw e;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "刪除待辦事項時發生錯誤：" + e.getMessage());
            return "redirect:/todos";
        }
    }

    /**
     * 處理切換待辦事項完成狀態請求
     */
    @PostMapping("/{id}/toggle")
    @PreAuthorize("isAuthenticated()")
    public String toggleTodoComplete(@PathVariable Long id,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        
        String username = getCurrentUsername();
        
        try {
            // 切換完成狀態
            TodoItem toggledTodo = todoService.toggleComplete(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "待辦事項狀態更新成功");
            return "redirect:/todos";
            
        } catch (TodoNotFoundException | UnauthorizedAccessException e) {
            // 讓全域異常處理器處理
            throw e;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新待辦事項狀態時發生錯誤：" + e.getMessage());
            return "redirect:/todos";
        }
    }

    /**
     * 顯示延期表單（AJAX 請求）
     * GET /todos/{id}/extend
     */
    @GetMapping("/{id}/extend")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExtensionForm(@PathVariable Long id) {
        
        String username = getCurrentUsername();
        
        try {
            // 查詢待辦事項
            Optional<TodoItem> todoOpt = todoService.findUserTodo(id, username);
            
            if (todoOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "待辦事項不存在"));
            }
            
            TodoItem todo = todoOpt.get();
            
            // 檢查是否符合延期條件
            if (!extensionService.isEligibleForExtension(todo)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "此待辦事項不符合延期條件"));
            }
            
            // 準備回應資料
            Map<String, Object> response = new HashMap<>();
            response.put("todoId", todo.getId());
            response.put("title", todo.getTitle());
            response.put("currentDueDate", todo.getDueDate());
            response.put("maxExtensionDays", 365);
            
            return ResponseEntity.ok(response);
            
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "無權限存取此待辦事項"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "系統錯誤，請稍後再試"));
        }
    }
    
    /**
     * 處理延期請求
     * POST /todos/{id}/extend
     */
    @PostMapping("/{id}/extend")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<ExtendTodoResponse> extendTodo(@PathVariable Long id,
                                                        @Valid @RequestBody ExtendTodoRequest request,
                                                        BindingResult bindingResult) {
        
        String username = getCurrentUsername();
        
        // 驗證表單資料
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest()
                .body(ExtendTodoResponse.failure("輸入驗證失敗: " + errorMessage, id));
        }
        
        try {
            // 驗證請求參數一致性
            if (!id.equals(request.getTodoId())) {
                return ResponseEntity.badRequest()
                    .body(ExtendTodoResponse.failure("請求參數不一致", id));
            }
            
            // 執行延期操作
            TodoItem extendedTodo = extensionService.extendTodo(
                id, request.getExtensionDays(), username);
            
            // 建立成功回應
            ExtendTodoResponse response = ExtendTodoResponse.success(
                "延期成功",
                extendedTodo.getId(),
                extendedTodo.getDueDate(),
                extendedTodo.getOriginalDueDate(),
                (int) extendedTodo.getTotalExtensionDays()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (TodoNotFoundException e) {
            return ResponseEntity.badRequest()
                .body(ExtendTodoResponse.failure("待辦事項不存在", id));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ExtendTodoResponse.failure("無權限存取此待辦事項", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ExtendTodoResponse.failure(e.getMessage(), id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExtendTodoResponse.failure("系統錯誤，請稍後再試", id));
        }
    }
    
    /**
     * 取得延期預覽（計算新日期但不儲存）
     * GET /todos/{id}/extend/preview
     */
    @GetMapping("/{id}/extend/preview")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> previewExtension(@PathVariable Long id,
                                                               @RequestParam int days) {
        
        String username = getCurrentUsername();
        
        try {
            // 查詢待辦事項
            Optional<TodoItem> todoOpt = todoService.findUserTodo(id, username);
            
            if (todoOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "待辦事項不存在"));
            }
            
            TodoItem todo = todoOpt.get();
            
            // 驗證延期天數
            extensionService.validateExtensionDays(days);
            
            // 計算新到期日
            LocalDate newDueDate = dateValidationService.calculateNewDueDate(
                todo.getDueDate(), days);
            
            // 準備預覽資料
            Map<String, Object> response = new HashMap<>();
            response.put("currentDueDate", todo.getDueDate());
            response.put("newDueDate", newDueDate);
            response.put("extensionDays", days);
            
            return ResponseEntity.ok(response);
            
        } catch (TodoNotFoundException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "待辦事項不存在"));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "無權限存取此待辦事項"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "系統錯誤，請稍後再試"));
        }
    }

    /**
     * 全域異常處理 - 處理 TodoNotFoundException
     */
    @ExceptionHandler(TodoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleTodoNotFound(TodoNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/404";
    }

    /**
     * 全域異常處理 - 處理 UnauthorizedAccessException
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedAccess(UnauthorizedAccessException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/403";
    }
}