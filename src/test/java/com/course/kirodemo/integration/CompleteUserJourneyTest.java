package com.course.kirodemo.integration;

import com.course.kirodemo.dto.CreateTodoRequest;
import com.course.kirodemo.dto.UpdateTodoRequest;
import com.course.kirodemo.dto.UserRegistrationRequest;
import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
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

/**
 * 完整使用者旅程測試
 * 測試從使用者註冊到待辦事項管理的完整業務流程
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CompleteUserJourneyTest {

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

    @BeforeEach
    void setUp() {
        // Given: 清理測試資料
        todoItemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("完整使用者旅程測試 - 從註冊到管理待辦事項")
    void test_completeUserJourney_fromRegistrationToTodoManagement_then_shouldWorkCorrectly() {
        String username = "journeyuser";
        String password = "journeypass";
        
        // Step 1: 使用者註冊
        // Given: 準備註冊資料
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername(username);
        registrationRequest.setPassword(password);
        registrationRequest.setConfirmPassword(password);

        // When: 註冊使用者
        User createdUser = userService.registerUser(registrationRequest);

        // Then: 驗證使用者已建立
        assertThat(createdUser.getUsername()).isEqualTo(username);
        assertThat(passwordEncoder.matches(password, createdUser.getPassword())).isTrue();

        // Step 2: 驗證使用者可以被找到
        // When: 查詢使用者
        Optional<User> foundUser = userService.findByUsername(username);

        // Then: 驗證查詢結果
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(username);

        // Step 3: 驗證密碼驗證功能
        // When: 驗證正確密碼
        boolean validPassword = userService.validatePassword(username, password);

        // Then: 應該驗證成功
        assertThat(validPassword).isTrue();

        // When: 驗證錯誤密碼
        boolean invalidPassword = userService.validatePassword(username, "wrongpassword");

        // Then: 應該驗證失敗
        assertThat(invalidPassword).isFalse();

        // Step 4: 建立第一個待辦事項
        // Given: 準備待辦事項資料
        CreateTodoRequest todo1Request = new CreateTodoRequest();
        todo1Request.setTitle("學習 Spring Boot");
        todo1Request.setDescription("完成 Spring Boot 教學課程");
        todo1Request.setDueDate(LocalDate.now().plusDays(7));

        // When: 建立待辦事項
        TodoItem todo1 = todoService.createTodo(todo1Request, username);

        // Then: 驗證待辦事項已建立
        assertThat(todo1.getTitle()).isEqualTo("學習 Spring Boot");
        assertThat(todo1.getDescription()).isEqualTo("完成 Spring Boot 教學課程");
        assertThat(todo1.isCompleted()).isFalse();

        // Step 5: 建立第二個待辦事項
        // Given: 準備第二個待辦事項資料
        CreateTodoRequest todo2Request = new CreateTodoRequest();
        todo2Request.setTitle("準備面試");
        todo2Request.setDescription("準備技術面試問題");
        todo2Request.setDueDate(LocalDate.now().plusDays(3));

        // When: 建立第二個待辦事項
        TodoItem todo2 = todoService.createTodo(todo2Request, username);

        // Then: 驗證第二個待辦事項已建立
        assertThat(todo2.getTitle()).isEqualTo("準備面試");
        assertThat(todo2.getDueDate()).isEqualTo(LocalDate.now().plusDays(3));

        // Step 6: 驗證待辦事項列表
        // When: 查詢使用者的所有待辦事項
        List<TodoItem> userTodos = todoService.getUserTodos(username, TodoService.SortBy.CREATED_AT_DESC);

        // Then: 應該有兩個待辦事項
        assertThat(userTodos).hasSize(2);
        assertThat(userTodos.get(0).getTitle()).isEqualTo("準備面試"); // 最新建立的
        assertThat(userTodos.get(1).getTitle()).isEqualTo("學習 Spring Boot");

        // Step 7: 測試排序功能
        // When: 依預計完成日排序
        List<TodoItem> sortedByDueDate = todoService.getUserTodos(username, TodoService.SortBy.DUE_DATE_ASC);

        // Then: 應該按預計完成日排序
        assertThat(sortedByDueDate).hasSize(2);
        assertThat(sortedByDueDate.get(0).getTitle()).isEqualTo("準備面試"); // 3天後到期
        assertThat(sortedByDueDate.get(1).getTitle()).isEqualTo("學習 Spring Boot"); // 7天後到期

        // Step 8: 標記第一個待辦事項為完成
        // When: 切換完成狀態
        TodoItem completedTodo = todoService.toggleComplete(todo1.getId(), username);

        // Then: 驗證狀態已更新
        assertThat(completedTodo.isCompleted()).isTrue();
        assertThat(completedTodo.getCompletedAt()).isNotNull();

        // Step 9: 編輯第二個待辦事項
        // Given: 準備更新資料
        UpdateTodoRequest updateRequest = new UpdateTodoRequest();
        updateRequest.setTitle("準備技術面試");
        updateRequest.setDescription("準備 Java 和 Spring 相關問題");
        updateRequest.setDueDate(LocalDate.now().plusDays(5));

        // When: 更新待辦事項
        TodoItem updatedTodo = todoService.updateTodo(todo2.getId(), updateRequest, username);

        // Then: 驗證更新成功
        assertThat(updatedTodo.getTitle()).isEqualTo("準備技術面試");
        assertThat(updatedTodo.getDescription()).isEqualTo("準備 Java 和 Spring 相關問題");
        assertThat(updatedTodo.getDueDate()).isEqualTo(LocalDate.now().plusDays(5));

        // Step 10: 建立第三個待辦事項用於刪除測試
        // Given: 準備第三個待辦事項
        CreateTodoRequest todo3Request = new CreateTodoRequest();
        todo3Request.setTitle("要刪除的待辦事項");
        todo3Request.setDescription("這個待辦事項將被刪除");
        todo3Request.setDueDate(LocalDate.now().plusDays(1));

        // When: 建立第三個待辦事項
        TodoItem todo3 = todoService.createTodo(todo3Request, username);

        // Then: 驗證已建立
        List<TodoItem> allTodos = todoService.getUserTodos(username, TodoService.SortBy.CREATED_AT_DESC);
        assertThat(allTodos).hasSize(3);

        // Step 11: 刪除第三個待辦事項
        // When: 刪除待辦事項
        todoService.deleteTodo(todo3.getId(), username);

        // Then: 驗證刪除成功
        List<TodoItem> remainingTodos = todoService.getUserTodos(username, TodoService.SortBy.CREATED_AT_DESC);
        assertThat(remainingTodos).hasSize(2);

        // Step 12: 最終驗證 - 檢查完整狀態
        // Then: 驗證最終狀態
        List<TodoItem> finalTodos = todoService.getUserTodos(username, TodoService.SortBy.CREATED_AT_DESC);
        assertThat(finalTodos).hasSize(2);
        
        // 驗證第一個待辦事項（已完成）
        TodoItem finalTodo1 = finalTodos.stream()
                .filter(todo -> todo.getTitle().equals("學習 Spring Boot"))
                .findFirst()
                .orElseThrow();
        assertThat(finalTodo1.isCompleted()).isTrue();
        assertThat(finalTodo1.getCompletedAt()).isNotNull();
        
        // 驗證第二個待辦事項（已更新）
        TodoItem finalTodo2 = finalTodos.stream()
                .filter(todo -> todo.getTitle().equals("準備技術面試"))
                .findFirst()
                .orElseThrow();
        assertThat(finalTodo2.isCompleted()).isFalse();
        assertThat(finalTodo2.getDescription()).isEqualTo("準備 Java 和 Spring 相關問題");
        assertThat(finalTodo2.getDueDate()).isEqualTo(LocalDate.now().plusDays(5));
    }

    @Test
    @DisplayName("多使用者資料隔離端到端測試")
    void test_multiUserDataIsolation_endToEnd_then_shouldMaintainSeparation() {
        // Step 1: 註冊第一個使用者
        String user1Name = "user1";
        String user1Pass = "pass1";
        
        UserRegistrationRequest user1Request = new UserRegistrationRequest();
        user1Request.setUsername(user1Name);
        user1Request.setPassword(user1Pass);
        user1Request.setConfirmPassword(user1Pass);
        
        User user1 = userService.registerUser(user1Request);

        // Step 2: 註冊第二個使用者
        String user2Name = "user2";
        String user2Pass = "pass2";
        
        UserRegistrationRequest user2Request = new UserRegistrationRequest();
        user2Request.setUsername(user2Name);
        user2Request.setPassword(user2Pass);
        user2Request.setConfirmPassword(user2Pass);
        
        User user2 = userService.registerUser(user2Request);

        // Step 3: 使用者1建立待辦事項
        CreateTodoRequest user1TodoRequest = new CreateTodoRequest();
        user1TodoRequest.setTitle("使用者1的待辦事項");
        user1TodoRequest.setDescription("只有使用者1能看到");
        user1TodoRequest.setDueDate(LocalDate.now().plusDays(1));
        
        TodoItem user1Todo = todoService.createTodo(user1TodoRequest, user1Name);

        // Step 4: 使用者2建立待辦事項
        CreateTodoRequest user2TodoRequest = new CreateTodoRequest();
        user2TodoRequest.setTitle("使用者2的待辦事項");
        user2TodoRequest.setDescription("只有使用者2能看到");
        user2TodoRequest.setDueDate(LocalDate.now().plusDays(2));
        
        TodoItem user2Todo = todoService.createTodo(user2TodoRequest, user2Name);

        // Step 5: 驗證資料隔離
        List<TodoItem> user1Todos = todoService.getUserTodos(user1Name, TodoService.SortBy.CREATED_AT_DESC);
        List<TodoItem> user2Todos = todoService.getUserTodos(user2Name, TodoService.SortBy.CREATED_AT_DESC);

        assertThat(user1Todos).hasSize(1);
        assertThat(user1Todos.get(0).getTitle()).isEqualTo("使用者1的待辦事項");
        assertThat(user1Todos.get(0).getUser().getUsername()).isEqualTo(user1Name);

        assertThat(user2Todos).hasSize(1);
        assertThat(user2Todos.get(0).getTitle()).isEqualTo("使用者2的待辦事項");
        assertThat(user2Todos.get(0).getUser().getUsername()).isEqualTo(user2Name);

        // Step 6: 驗證使用者無法存取其他使用者的待辦事項
        Optional<TodoItem> user1CannotAccessUser2Todo = todoService.findUserTodo(user2Todo.getId(), user1Name);
        Optional<TodoItem> user2CannotAccessUser1Todo = todoService.findUserTodo(user1Todo.getId(), user2Name);

        assertThat(user1CannotAccessUser2Todo).isEmpty();
        assertThat(user2CannotAccessUser1Todo).isEmpty();
    }

    @Test
    @DisplayName("業務規則驗證測試 - 完整的驗證流程")
    void test_businessRuleValidation_completeFlow_then_shouldEnforceRules() {
        // Step 1: 註冊使用者
        String username = "validationuser";
        String password = "validationpass";
        
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername(username);
        registrationRequest.setPassword(password);
        registrationRequest.setConfirmPassword(password);
        
        User user = userService.registerUser(registrationRequest);

        // Step 2: 建立待辦事項並測試業務規則
        CreateTodoRequest todoRequest = new CreateTodoRequest();
        todoRequest.setTitle("驗證測試待辦事項");
        todoRequest.setDescription("測試各種業務規則");
        todoRequest.setDueDate(LocalDate.now().plusDays(1));
        
        TodoItem todo = todoService.createTodo(todoRequest, username);

        // Step 3: 驗證待辦事項初始狀態
        assertThat(todo.isCompleted()).isFalse();
        assertThat(todo.getCompletedAt()).isNull();
        assertThat(todo.getCreatedAt()).isNotNull();

        // Step 4: 測試狀態切換規則
        // 切換為完成
        TodoItem completedTodo = todoService.toggleComplete(todo.getId(), username);
        assertThat(completedTodo.isCompleted()).isTrue();
        assertThat(completedTodo.getCompletedAt()).isNotNull();

        // 再次切換為未完成
        TodoItem uncompletedTodo = todoService.toggleComplete(todo.getId(), username);
        assertThat(uncompletedTodo.isCompleted()).isFalse();
        assertThat(uncompletedTodo.getCompletedAt()).isNull();

        // Step 5: 測試更新規則
        UpdateTodoRequest updateRequest = new UpdateTodoRequest();
        updateRequest.setTitle("更新後的標題");
        updateRequest.setDescription("更新後的描述");
        updateRequest.setDueDate(LocalDate.now().plusDays(10));

        TodoItem updatedTodo = todoService.updateTodo(todo.getId(), updateRequest, username);
        
        // 驗證更新不會影響建立時間和完成狀態
        assertThat(updatedTodo.getCreatedAt()).isEqualTo(todo.getCreatedAt());
        assertThat(updatedTodo.isCompleted()).isFalse(); // 保持未完成狀態
        assertThat(updatedTodo.getTitle()).isEqualTo("更新後的標題");

        // Step 6: 驗證刪除規則
        Long todoId = updatedTodo.getId();
        todoService.deleteTodo(todoId, username);
        
        // 驗證待辦事項已被刪除
        Optional<TodoItem> deletedTodo = todoService.findUserTodo(todoId, username);
        assertThat(deletedTodo).isEmpty();
    }
}