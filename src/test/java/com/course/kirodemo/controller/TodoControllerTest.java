package com.course.kirodemo.controller;

import com.course.kirodemo.dto.CreateTodoRequest;
import com.course.kirodemo.dto.UpdateTodoRequest;
import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.TodoNotFoundException;
import com.course.kirodemo.exception.UnauthorizedAccessException;
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
    }
}