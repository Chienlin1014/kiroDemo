package com.course.kirodemo.controller;

import com.course.kirodemo.dto.CreateTodoRequest;
import com.course.kirodemo.dto.ExtendTodoRequest;
import com.course.kirodemo.dto.UpdateTodoRequest;
import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.TodoNotFoundException;
import com.course.kirodemo.exception.UnauthorizedAccessException;
import com.course.kirodemo.service.DateValidationService;
import com.course.kirodemo.service.TodoExtensionService;
import com.course.kirodemo.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TodoController 整合測試
 * 使用 @WebMvcTest 和 @MockBean 測試 HTTP 請求處理
 */
@WebMvcTest(controllers = TodoController.class)
@DisplayName("TodoController 整合測試")
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;
    
    @MockBean
    private TodoExtensionService extensionService;
    
    @MockBean
    private DateValidationService dateValidationService;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    private TodoItem mockTodoItem;
    private List<TodoItem> mockTodoList;
    private CreateTodoRequest validCreateRequest;
    private UpdateTodoRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        mockUser.setCreatedAt(LocalDateTime.now());

        mockTodoItem = new TodoItem();
        mockTodoItem.setId(1L);
        mockTodoItem.setTitle("測試待辦事項");
        mockTodoItem.setDescription("測試描述");
        mockTodoItem.setDueDate(LocalDate.now().plusDays(7));
        mockTodoItem.setCompleted(false);
        mockTodoItem.setCreatedAt(LocalDateTime.now());
        mockTodoItem.setUser(mockUser);

        TodoItem completedTodo = new TodoItem();
        completedTodo.setId(2L);
        completedTodo.setTitle("已完成的待辦事項");
        completedTodo.setDescription("已完成描述");
        completedTodo.setDueDate(LocalDate.now().plusDays(3));
        completedTodo.setCompleted(true);
        completedTodo.setCompletedAt(LocalDateTime.now());
        completedTodo.setCreatedAt(LocalDateTime.now().minusDays(1));
        completedTodo.setUser(mockUser);

        mockTodoList = Arrays.asList(mockTodoItem, completedTodo);

        validCreateRequest = new CreateTodoRequest("新待辦事項", "新描述", LocalDate.now().plusDays(5));
        validUpdateRequest = new UpdateTodoRequest("更新的標題", "更新的描述", LocalDate.now().plusDays(10));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos 當使用者已認證時應該回傳待辦事項列表頁面")
    void test_getTodos_whenUserAuthenticated_then_shouldReturnTodoListView() throws Exception {
        // Given
        when(todoService.getUserTodos("testuser", TodoService.SortBy.CREATED_AT_DESC))
                .thenReturn(mockTodoList);

        // When & Then
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(view().name("todos/list"))
                .andExpect(model().attributeExists("todos"))
                .andExpect(model().attribute("todos", mockTodoList))
                .andExpect(model().attributeExists("sortBy"))
                .andExpect(model().attribute("sortBy", "CREATED_AT_DESC"));

        verify(todoService).getUserTodos("testuser", TodoService.SortBy.CREATED_AT_DESC);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos 當指定排序方式時應該使用指定的排序")
    void test_getTodos_whenSortBySpecified_then_shouldUseSortBy() throws Exception {
        // Given
        when(todoService.getUserTodos("testuser", TodoService.SortBy.DUE_DATE_ASC))
                .thenReturn(mockTodoList);

        // When & Then
        mockMvc.perform(get("/todos")
                .param("sortBy", "DUE_DATE_ASC"))
                .andExpect(status().isOk())
                .andExpect(view().name("todos/list"))
                .andExpect(model().attributeExists("todos"))
                .andExpect(model().attribute("sortBy", "DUE_DATE_ASC"));

        verify(todoService).getUserTodos("testuser", TodoService.SortBy.DUE_DATE_ASC);
    }

    @Test
    @DisplayName("GET /todos 當使用者未認證時應該回傳 401 未授權")
    void test_getTodos_whenUserNotAuthenticated_then_shouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(get("/todos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos/new 應該回傳新增待辦事項表單頁面")
    void test_getNewTodoForm_whenAccessed_then_shouldReturnFormView() throws Exception {
        // When & Then
        mockMvc.perform(get("/todos/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("todos/form"))
                .andExpect(model().attributeExists("todoRequest"))
                .andExpect(model().attribute("isEdit", false));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /todos 當資料有效時應該建立待辦事項並重新導向")
    void test_createTodo_whenValidData_then_shouldCreateTodoAndRedirect() throws Exception {
        // Given
        when(todoService.createTodo(any(CreateTodoRequest.class), eq("testuser")))
                .thenReturn(mockTodoItem);

        // When & Then
        mockMvc.perform(post("/todos")
                .param("title", "新待辦事項")
                .param("description", "新描述")
                .param("dueDate", "2024-10-30")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(todoService).createTodo(any(CreateTodoRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /todos 當資料無效時應該回傳表單並顯示錯誤")
    void test_createTodo_whenInvalidData_then_shouldReturnFormWithErrors() throws Exception {
        // When & Then - 測試空白標題
        mockMvc.perform(post("/todos")
                .param("title", "")
                .param("description", "描述")
                .param("dueDate", "2024-10-30")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("todos/form"))
                .andExpect(model().attributeExists("todoRequest"))
                .andExpect(model().attribute("isEdit", false));

        verify(todoService, never()).createTodo(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos/{id}/edit 當待辦事項存在時應該回傳編輯表單")
    void test_getEditTodoForm_whenTodoExists_then_shouldReturnEditForm() throws Exception {
        // Given
        when(todoService.findUserTodo(1L, "testuser")).thenReturn(Optional.of(mockTodoItem));

        // When & Then
        mockMvc.perform(get("/todos/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("todos/form"))
                .andExpect(model().attributeExists("todoRequest"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attribute("todoId", 1L));

        verify(todoService).findUserTodo(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos/{id}/edit 當待辦事項不存在時應該回傳404錯誤")
    void test_getEditTodoForm_whenTodoNotExists_then_shouldReturn404() throws Exception {
        // Given
        when(todoService.findUserTodo(999L, "testuser")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/todos/999/edit"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));

        verify(todoService).findUserTodo(999L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /todos/{id} 當資料有效時應該更新待辦事項並重新導向")
    void test_updateTodo_whenValidData_then_shouldUpdateTodoAndRedirect() throws Exception {
        // Given
        when(todoService.updateTodo(eq(1L), any(UpdateTodoRequest.class), eq("testuser")))
                .thenReturn(mockTodoItem);

        // When & Then
        mockMvc.perform(put("/todos/1")
                .param("title", "更新的標題")
                .param("description", "更新的描述")
                .param("dueDate", "2024-11-05")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(todoService).updateTodo(eq(1L), any(UpdateTodoRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /todos/{id} 當待辦事項不存在時應該回傳404錯誤")
    void test_updateTodo_whenTodoNotExists_then_shouldReturn404() throws Exception {
        // Given
        when(todoService.updateTodo(eq(999L), any(UpdateTodoRequest.class), eq("testuser")))
                .thenThrow(new TodoNotFoundException("待辦事項不存在"));

        // When & Then
        mockMvc.perform(put("/todos/999")
                .param("title", "更新的標題")
                .param("description", "更新的描述")
                .param("dueDate", "2024-11-05")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));

        verify(todoService).updateTodo(eq(999L), any(UpdateTodoRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /todos/{id} 當使用者無權限時應該回傳403錯誤")
    void test_updateTodo_whenUnauthorized_then_shouldReturn403() throws Exception {
        // Given
        when(todoService.updateTodo(eq(1L), any(UpdateTodoRequest.class), eq("testuser")))
                .thenThrow(new UnauthorizedAccessException("無權限存取"));

        // When & Then
        mockMvc.perform(put("/todos/1")
                .param("title", "更新的標題")
                .param("description", "更新的描述")
                .param("dueDate", "2024-11-05")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));

        verify(todoService).updateTodo(eq(1L), any(UpdateTodoRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /todos/{id} 當待辦事項存在時應該刪除並重新導向")
    void test_deleteTodo_whenTodoExists_then_shouldDeleteAndRedirect() throws Exception {
        // Given
        doNothing().when(todoService).deleteTodo(1L, "testuser");

        // When & Then
        mockMvc.perform(delete("/todos/1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(todoService).deleteTodo(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /todos/{id} 當待辦事項不存在時應該回傳404錯誤")
    void test_deleteTodo_whenTodoNotExists_then_shouldReturn404() throws Exception {
        // Given
        doThrow(new TodoNotFoundException("待辦事項不存在"))
                .when(todoService).deleteTodo(999L, "testuser");

        // When & Then
        mockMvc.perform(delete("/todos/999")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));

        verify(todoService).deleteTodo(999L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /todos/{id}/toggle 當待辦事項存在時應該切換狀態並重新導向")
    void test_toggleTodoComplete_whenTodoExists_then_shouldToggleAndRedirect() throws Exception {
        // Given
        TodoItem toggledTodo = new TodoItem();
        toggledTodo.setId(1L);
        toggledTodo.setCompleted(true);
        when(todoService.toggleComplete(1L, "testuser")).thenReturn(toggledTodo);

        // When & Then
        mockMvc.perform(post("/todos/1/toggle")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(todoService).toggleComplete(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /todos/{id}/toggle 當待辦事項不存在時應該回傳404錯誤")
    void test_toggleTodoComplete_whenTodoNotExists_then_shouldReturn404() throws Exception {
        // Given
        when(todoService.toggleComplete(999L, "testuser"))
                .thenThrow(new TodoNotFoundException("待辦事項不存在"));

        // When & Then
        mockMvc.perform(post("/todos/999/toggle")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));

        verify(todoService).toggleComplete(999L, "testuser");
    }

    @Test
    @DisplayName("所有操作當使用者未認證時應該回傳 401 未授權")
    void test_allOperations_whenUserNotAuthenticated_then_shouldReturn401() throws Exception {
        // When & Then - 測試各種操作都需要認證
        mockMvc.perform(get("/todos/new"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/todos").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/todos/1/edit"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/todos/1").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/todos/1").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/todos/1/toggle").with(csrf()))
                .andExpect(status().isUnauthorized());
                
        // 延期相關端點也需要認證
        mockMvc.perform(get("/todos/1/extend"))
                .andExpect(status().isUnauthorized());
                
        mockMvc.perform(post("/todos/1/extend").with(csrf()))
                .andExpect(status().isUnauthorized());
                
        mockMvc.perform(get("/todos/1/extend/preview"))
                .andExpect(status().isUnauthorized());
    }

    // === 延期功能測試 ===

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos/{id}/extend 當待辦事項符合延期條件時應該回傳延期表單資料")
    void test_getExtensionForm_whenTodoEligibleForExtension_then_shouldReturnFormData() throws Exception {
        // Given
        TodoItem eligibleTodo = new TodoItem();
        eligibleTodo.setId(1L);
        eligibleTodo.setTitle("即將到期的任務");
        eligibleTodo.setDueDate(LocalDate.now().plusDays(2));
        eligibleTodo.setCompleted(false);
        
        when(todoService.findUserTodo(1L, "testuser")).thenReturn(Optional.of(eligibleTodo));
        when(extensionService.isEligibleForExtension(eligibleTodo)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/todos/1/extend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todoId").value(1L))
                .andExpect(jsonPath("$.title").value("即將到期的任務"))
                .andExpect(jsonPath("$.currentDueDate").exists())
                .andExpect(jsonPath("$.maxExtensionDays").value(365));

        verify(todoService).findUserTodo(1L, "testuser");
        verify(extensionService).isEligibleForExtension(eligibleTodo);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos/{id}/extend 當待辦事項不符合延期條件時應該回傳錯誤")
    void test_getExtensionForm_whenTodoNotEligibleForExtension_then_shouldReturnError() throws Exception {
        // Given
        TodoItem ineligibleTodo = new TodoItem();
        ineligibleTodo.setId(1L);
        ineligibleTodo.setCompleted(true); // 已完成
        
        when(todoService.findUserTodo(1L, "testuser")).thenReturn(Optional.of(ineligibleTodo));
        when(extensionService.isEligibleForExtension(ineligibleTodo)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/todos/1/extend"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("此待辦事項不符合延期條件"));

        verify(todoService).findUserTodo(1L, "testuser");
        verify(extensionService).isEligibleForExtension(ineligibleTodo);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos/{id}/extend 當待辦事項不存在時應該回傳錯誤")
    void test_getExtensionForm_whenTodoNotExists_then_shouldReturnError() throws Exception {
        // Given
        when(todoService.findUserTodo(999L, "testuser")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/todos/999/extend"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("待辦事項不存在"));

        verify(todoService).findUserTodo(999L, "testuser");
        verify(extensionService, never()).isEligibleForExtension(any());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /todos/{id}/extend 當延期請求有效時應該成功延期")
    void test_extendTodo_whenValidRequest_then_shouldExtendSuccessfully() throws Exception {
        // Given
        ExtendTodoRequest request = new ExtendTodoRequest(1L, 3);
        
        TodoItem extendedTodo = new TodoItem();
        extendedTodo.setId(1L);
        extendedTodo.setTitle("測試任務");
        extendedTodo.setDueDate(LocalDate.now().plusDays(5)); // 延期後的日期
        extendedTodo.setOriginalDueDate(LocalDate.now().plusDays(2)); // 原始日期
        extendedTodo.setExtensionCount(1);
        
        when(extensionService.extendTodo(1L, 3, "testuser")).thenReturn(extendedTodo);

        // When & Then
        mockMvc.perform(post("/todos/1/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("延期成功"))
                .andExpect(jsonPath("$.todoId").value(1L))
                .andExpect(jsonPath("$.newDueDate").exists())
                .andExpect(jsonPath("$.originalDueDate").exists())
                .andExpect(jsonPath("$.totalExtensionDays").value(3));

        verify(extensionService).extendTodo(1L, 3, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /todos/{id}/extend 當延期天數無效時應該回傳驗證錯誤")
    void test_extendTodo_whenInvalidExtensionDays_then_shouldReturnValidationError() throws Exception {
        // Given
        ExtendTodoRequest request = new ExtendTodoRequest(1L, -1); // 無效的負數

        // When & Then
        mockMvc.perform(post("/todos/1/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("輸入驗證失敗")));

        verify(extensionService, never()).extendTodo(anyLong(), anyInt(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /todos/{id}/extend 當路徑ID與請求ID不一致時應該回傳錯誤")
    void test_extendTodo_whenPathIdMismatch_then_shouldReturnError() throws Exception {
        // Given
        ExtendTodoRequest request = new ExtendTodoRequest(2L, 3); // 請求ID為2，但路徑ID為1

        // When & Then
        mockMvc.perform(post("/todos/1/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("請求參數不一致"));

        verify(extensionService, never()).extendTodo(anyLong(), anyInt(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /todos/{id}/extend 當待辦事項不存在時應該回傳錯誤")
    void test_extendTodo_whenTodoNotExists_then_shouldReturnError() throws Exception {
        // Given
        ExtendTodoRequest request = new ExtendTodoRequest(999L, 3);
        
        when(extensionService.extendTodo(999L, 3, "testuser"))
                .thenThrow(new TodoNotFoundException("待辦事項不存在"));

        // When & Then
        mockMvc.perform(post("/todos/999/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("待辦事項不存在"));

        verify(extensionService).extendTodo(999L, 3, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /todos/{id}/extend 當使用者無權限時應該回傳403錯誤")
    void test_extendTodo_whenUnauthorized_then_shouldReturn403() throws Exception {
        // Given
        ExtendTodoRequest request = new ExtendTodoRequest(1L, 3);
        
        when(extensionService.extendTodo(1L, 3, "testuser"))
                .thenThrow(new UnauthorizedAccessException("無權限存取此待辦事項"));

        // When & Then
        mockMvc.perform(post("/todos/1/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("無權限存取此待辦事項"));

        verify(extensionService).extendTodo(1L, 3, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos/{id}/extend/preview 當延期天數有效時應該回傳預覽資料")
    void test_previewExtension_whenValidDays_then_shouldReturnPreviewData() throws Exception {
        // Given
        TodoItem todo = new TodoItem();
        todo.setId(1L);
        todo.setDueDate(LocalDate.now().plusDays(2));
        
        LocalDate newDueDate = LocalDate.now().plusDays(5);
        
        when(todoService.findUserTodo(1L, "testuser")).thenReturn(Optional.of(todo));
        doNothing().when(extensionService).validateExtensionDays(3);
        when(dateValidationService.calculateNewDueDate(todo.getDueDate(), 3)).thenReturn(newDueDate);

        // When & Then
        mockMvc.perform(get("/todos/1/extend/preview")
                .param("days", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentDueDate").exists())
                .andExpect(jsonPath("$.newDueDate").exists())
                .andExpect(jsonPath("$.extensionDays").value(3));

        verify(todoService).findUserTodo(1L, "testuser");
        verify(extensionService).validateExtensionDays(3);
        verify(dateValidationService).calculateNewDueDate(todo.getDueDate(), 3);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos/{id}/extend/preview 當延期天數無效時應該回傳錯誤")
    void test_previewExtension_whenInvalidDays_then_shouldReturnError() throws Exception {
        // Given
        TodoItem todo = new TodoItem();
        todo.setId(1L);
        todo.setDueDate(LocalDate.now().plusDays(2));
        
        when(todoService.findUserTodo(1L, "testuser")).thenReturn(Optional.of(todo));
        doThrow(new IllegalArgumentException("延期天數必須為正數"))
                .when(extensionService).validateExtensionDays(-1);

        // When & Then
        mockMvc.perform(get("/todos/1/extend/preview")
                .param("days", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("延期天數必須為正數"));

        verify(todoService).findUserTodo(1L, "testuser");
        verify(extensionService).validateExtensionDays(-1);
        verify(dateValidationService, never()).calculateNewDueDate(any(), anyInt());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /todos/{id}/extend/preview 當待辦事項不存在時應該回傳錯誤")
    void test_previewExtension_whenTodoNotExists_then_shouldReturnError() throws Exception {
        // Given
        when(todoService.findUserTodo(999L, "testuser")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/todos/999/extend/preview")
                .param("days", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("待辦事項不存在"));

        verify(todoService).findUserTodo(999L, "testuser");
        verify(extensionService, never()).validateExtensionDays(anyInt());
        verify(dateValidationService, never()).calculateNewDueDate(any(), anyInt());
    }
}