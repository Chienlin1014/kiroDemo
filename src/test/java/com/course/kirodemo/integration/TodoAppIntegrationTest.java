package com.course.kirodemo.integration;

import com.course.kirodemo.dto.CreateTodoRequest;
import com.course.kirodemo.dto.UpdateTodoRequest;
import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.exception.TodoNotFoundException;
import com.course.kirodemo.exception.UnauthorizedAccessException;
import com.course.kirodemo.repository.TodoItemRepository;
import com.course.kirodemo.repository.UserRepository;
import com.course.kirodemo.service.TodoService;
import com.course.kirodemo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Todo 應用程式整合測試
 * 測試完整的服務層整合和資料存取層整合
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TodoAppIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private TodoService todoService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Given: 清理測試資料並建立測試使用者
        todoItemRepository.deleteAll();
        userRepository.deleteAll();

        // 建立測試使用者 1
        testUser1 = new User();
        testUser1.setUsername("testuser1");
        testUser1.setPassword(passwordEncoder.encode("password"));
        testUser1 = userRepository.save(testUser1);

        // 建立測試使用者 2
        testUser2 = new User();
        testUser2.setUsername("testuser2");
        testUser2.setPassword(passwordEncoder.encode("password123"));
        testUser2 = userRepository.save(testUser2);
    }

    @Test
    @DisplayName("待辦事項服務整合測試 - 建立待辦事項")
    void test_todoService_integration_whenCreateTodo_then_shouldSaveCorrectly() {
        // Given: 準備待辦事項資料
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("測試待辦事項");
        request.setDescription("這是一個測試描述");
        request.setDueDate(LocalDate.now().plusDays(7));

        // When: 建立待辦事項
        TodoItem createdTodo = todoService.createTodo(request, testUser1.getUsername());

        // Then: 驗證待辦事項已建立
        assertThat(createdTodo.getId()).isNotNull();
        assertThat(createdTodo.getTitle()).isEqualTo("測試待辦事項");
        assertThat(createdTodo.getDescription()).isEqualTo("這是一個測試描述");
        assertThat(createdTodo.getDueDate()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(createdTodo.isCompleted()).isFalse();
        assertThat(createdTodo.getUser().getUsername()).isEqualTo(testUser1.getUsername());

        // Then: 驗證資料庫中的資料
        List<TodoItem> todos = todoItemRepository.findByUserOrderByCreatedAtDesc(testUser1);
        assertThat(todos).hasSize(1);
        assertThat(todos.get(0).getTitle()).isEqualTo("測試待辦事項");
    }

    @Test
    @DisplayName("資料隔離測試 - 使用者只能存取自己的待辦事項")
    void test_dataIsolation_whenMultipleUsers_then_shouldOnlyAccessOwnTodos() {
        // Given: 為不同使用者建立待辦事項
        TodoItem user1Todo = createTestTodo("使用者1的待辦事項", "描述1", testUser1, false);
        TodoItem user2Todo = createTestTodo("使用者2的待辦事項", "描述2", testUser2, false);

        // When: 使用者1查詢待辦事項
        List<TodoItem> user1Todos = todoService.getUserTodos(testUser1.getUsername(), TodoService.SortBy.CREATED_AT_DESC);

        // Then: 只能看到自己的待辦事項
        assertThat(user1Todos).hasSize(1);
        assertThat(user1Todos.get(0).getTitle()).isEqualTo("使用者1的待辦事項");

        // When: 使用者2查詢待辦事項
        List<TodoItem> user2Todos = todoService.getUserTodos(testUser2.getUsername(), TodoService.SortBy.CREATED_AT_DESC);

        // Then: 只能看到自己的待辦事項
        assertThat(user2Todos).hasSize(1);
        assertThat(user2Todos.get(0).getTitle()).isEqualTo("使用者2的待辦事項");
    }

    @Test
    @DisplayName("排序功能測試 - 依建立時間排序")
    void test_sorting_whenSortByCreatedAt_then_shouldReturnCorrectOrder() throws InterruptedException {
        // Given: 建立多個待辦事項（時間順序不同）
        TodoItem todo1 = createTestTodo("第一個待辦事項", "描述1", testUser1, false);
        Thread.sleep(10); // 確保時間差異
        TodoItem todo2 = createTestTodo("第二個待辦事項", "描述2", testUser1, false);
        Thread.sleep(10);
        TodoItem todo3 = createTestTodo("第三個待辦事項", "描述3", testUser1, false);

        // When: 使用建立時間降序排序
        List<TodoItem> todos = todoService.getUserTodos(testUser1.getUsername(), TodoService.SortBy.CREATED_AT_DESC);

        // Then: 驗證排序結果
        assertThat(todos).hasSize(3);
        assertThat(todos.get(0).getTitle()).isEqualTo("第三個待辦事項");
        assertThat(todos.get(1).getTitle()).isEqualTo("第二個待辦事項");
        assertThat(todos.get(2).getTitle()).isEqualTo("第一個待辦事項");
    }

    @Test
    @DisplayName("排序功能測試 - 依預計完成日排序")
    void test_sorting_whenSortByDueDate_then_shouldReturnCorrectOrder() {
        // Given: 建立不同預計完成日的待辦事項
        TodoItem todo1 = createTestTodoWithDueDate("待辦事項1", testUser1, LocalDate.now().plusDays(3));
        TodoItem todo2 = createTestTodoWithDueDate("待辦事項2", testUser1, LocalDate.now().plusDays(1));
        TodoItem todo3 = createTestTodoWithDueDate("待辦事項3", testUser1, LocalDate.now().plusDays(2));

        // When: 使用預計完成日升序排序
        List<TodoItem> todos = todoService.getUserTodos(testUser1.getUsername(), TodoService.SortBy.DUE_DATE_ASC);

        // Then: 驗證排序結果
        assertThat(todos).hasSize(3);
        assertThat(todos.get(0).getTitle()).isEqualTo("待辦事項2"); // +1 day
        assertThat(todos.get(1).getTitle()).isEqualTo("待辦事項3"); // +2 days
        assertThat(todos.get(2).getTitle()).isEqualTo("待辦事項1"); // +3 days
    }

    @Test
    @DisplayName("切換完成狀態測試")
    void test_toggleComplete_whenCalled_then_shouldUpdateStatus() {
        // Given: 建立未完成的測試待辦事項
        TodoItem todo = createTestTodo("測試待辦事項", "測試描述", testUser1, false);
        assertThat(todo.isCompleted()).isFalse();

        // When: 切換完成狀態
        TodoItem toggledTodo = todoService.toggleComplete(todo.getId(), testUser1.getUsername());

        // Then: 驗證狀態已切換
        assertThat(toggledTodo.isCompleted()).isTrue();
        assertThat(toggledTodo.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("未授權存取測試 - 使用者不能存取其他使用者的待辦事項")
    void test_unauthorizedAccess_whenAccessingOtherUserTodo_then_shouldReturnEmpty() {
        // Given: 為使用者2建立待辦事項
        TodoItem user2Todo = createTestTodo("使用者2的待辦事項", "描述", testUser2, false);

        // When: 使用者1嘗試存取使用者2的待辦事項
        Optional<TodoItem> result = todoService.findUserTodo(user2Todo.getId(), testUser1.getUsername());

        // Then: 應該回傳空的 Optional
        assertThat(result).isEmpty();

        // When & Then: 使用者1嘗試更新使用者2的待辦事項應該拋出異常
        UpdateTodoRequest updateRequest = new UpdateTodoRequest();
        updateRequest.setTitle("嘗試更新");
        updateRequest.setDescription("不應該成功");
        updateRequest.setDueDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> todoService.updateTodo(user2Todo.getId(), updateRequest, testUser1.getUsername()))
                .isInstanceOf(UnauthorizedAccessException.class);

        // When & Then: 使用者1嘗試刪除使用者2的待辦事項應該拋出異常
        assertThatThrownBy(() -> todoService.deleteTodo(user2Todo.getId(), testUser1.getUsername()))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    /**
     * 建立測試待辦事項的輔助方法
     */
    private TodoItem createTestTodo(String title, String description, User user, boolean completed) {
        TodoItem todo = new TodoItem();
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setDueDate(LocalDate.now().plusDays(7));
        todo.setUser(user);
        todo.setCompleted(completed);
        if (completed) {
            todo.setCompletedAt(java.time.LocalDateTime.now());
        }
        return todoItemRepository.save(todo);
    }

    /**
     * 建立指定預計完成日的測試待辦事項
     */
    private TodoItem createTestTodoWithDueDate(String title, User user, LocalDate dueDate) {
        TodoItem todo = new TodoItem();
        todo.setTitle(title);
        todo.setDescription("測試描述");
        todo.setDueDate(dueDate);
        todo.setUser(user);
        todo.setCompleted(false);
        return todoItemRepository.save(todo);
    }
}