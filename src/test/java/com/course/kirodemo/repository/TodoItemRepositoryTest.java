package com.course.kirodemo.repository;

import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TodoItemRepository 整合測試
 * 使用 @DataJpaTest 測試資料存取邏輯和自定義查詢方法
 */
@DataJpaTest
@DisplayName("TodoItemRepository 整合測試")
class TodoItemRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private TodoItemRepository todoItemRepository;
    
    private User testUser;
    private User anotherUser;
    private TodoItem todoItem1;
    private TodoItem todoItem2;
    private TodoItem todoItem3;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        testUser = new User("testuser", "password123");
        anotherUser = new User("anotheruser", "password456");
        
        // 儲存使用者
        testUser = entityManager.persistAndFlush(testUser);
        anotherUser = entityManager.persistAndFlush(anotherUser);
        
        // 準備待辦事項（不同的建立時間和預計完成日）
        todoItem1 = new TodoItem("第一個任務", "描述1", LocalDate.now().plusDays(1), testUser);
        todoItem2 = new TodoItem("第二個任務", "描述2", LocalDate.now().plusDays(3), testUser);
        todoItem3 = new TodoItem("第三個任務", "描述3", LocalDate.now().plusDays(2), testUser);
        
        // 設定不同的完成狀態
        todoItem2.setCompleted(true);
    }
    
    @Test
    @DisplayName("根據使用者查詢待辦事項並依建立時間降序排列時應該回傳正確順序")
    void test_findByUserOrderByCreatedAtDesc_whenMultipleTodos_then_shouldReturnInDescendingOrder() throws InterruptedException {
        // Given - 儲存待辦事項（有時間間隔以確保不同的建立時間）
        entityManager.persistAndFlush(todoItem1);
        entityManager.flush();
        entityManager.clear();
        
        // 等待 10 毫秒確保不同的時間戳記
        Thread.sleep(10);
        
        todoItem2 = new TodoItem("第二個任務", "描述2", LocalDate.now().plusDays(3), testUser);
        todoItem2.setCompleted(true);
        entityManager.persistAndFlush(todoItem2);
        entityManager.flush();
        entityManager.clear();
        
        // 等待 10 毫秒確保不同的時間戳記
        Thread.sleep(10);
        
        todoItem3 = new TodoItem("第三個任務", "描述3", LocalDate.now().plusDays(2), testUser);
        entityManager.persistAndFlush(todoItem3);
        
        // When - 查詢使用者的待辦事項，依建立時間降序排列
        List<TodoItem> todos = todoItemRepository.findByUserOrderByCreatedAtDesc(testUser);
        
        // Then - 驗證順序（最新的在前）
        assertThat(todos).hasSize(3);
        assertThat(todos.get(0).getTitle()).isEqualTo("第三個任務");
        assertThat(todos.get(1).getTitle()).isEqualTo("第二個任務");
        assertThat(todos.get(2).getTitle()).isEqualTo("第一個任務");
    }
    
    @Test
    @DisplayName("根據使用者查詢待辦事項並依建立時間升序排列時應該回傳正確順序")
    void test_findByUserOrderByCreatedAtAsc_whenMultipleTodos_then_shouldReturnInAscendingOrder() {
        // Given - 儲存待辦事項
        entityManager.persistAndFlush(todoItem1);
        entityManager.persistAndFlush(todoItem2);
        entityManager.persistAndFlush(todoItem3);
        
        // When - 查詢使用者的待辦事項，依建立時間升序排列
        List<TodoItem> todos = todoItemRepository.findByUserOrderByCreatedAtAsc(testUser);
        
        // Then - 驗證順序（最舊的在前）
        assertThat(todos).hasSize(3);
        // 由於是同時建立，順序可能會依據 ID 或其他因素
        assertThat(todos).extracting(TodoItem::getTitle)
                .containsExactly("第一個任務", "第二個任務", "第三個任務");
    }
    
    @Test
    @DisplayName("根據使用者查詢待辦事項並依預計完成日升序排列時應該回傳正確順序")
    void test_findByUserOrderByDueDateAsc_whenMultipleTodos_then_shouldReturnInDueDateOrder() {
        // Given - 儲存待辦事項
        entityManager.persistAndFlush(todoItem1); // +1 天
        entityManager.persistAndFlush(todoItem2); // +3 天
        entityManager.persistAndFlush(todoItem3); // +2 天
        
        // When - 查詢使用者的待辦事項，依預計完成日升序排列
        List<TodoItem> todos = todoItemRepository.findByUserOrderByDueDateAsc(testUser);
        
        // Then - 驗證順序（最近的預計完成日在前）
        assertThat(todos).hasSize(3);
        assertThat(todos.get(0).getTitle()).isEqualTo("第一個任務"); // +1 天
        assertThat(todos.get(1).getTitle()).isEqualTo("第三個任務"); // +2 天
        assertThat(todos.get(2).getTitle()).isEqualTo("第二個任務"); // +3 天
    }
    
    @Test
    @DisplayName("根據使用者查詢待辦事項並依預計完成日降序排列時應該回傳正確順序")
    void test_findByUserOrderByDueDateDesc_whenMultipleTodos_then_shouldReturnInReverseDueDateOrder() {
        // Given - 儲存待辦事項
        entityManager.persistAndFlush(todoItem1); // +1 天
        entityManager.persistAndFlush(todoItem2); // +3 天
        entityManager.persistAndFlush(todoItem3); // +2 天
        
        // When - 查詢使用者的待辦事項，依預計完成日降序排列
        List<TodoItem> todos = todoItemRepository.findByUserOrderByDueDateDesc(testUser);
        
        // Then - 驗證順序（最遠的預計完成日在前）
        assertThat(todos).hasSize(3);
        assertThat(todos.get(0).getTitle()).isEqualTo("第二個任務"); // +3 天
        assertThat(todos.get(1).getTitle()).isEqualTo("第三個任務"); // +2 天
        assertThat(todos.get(2).getTitle()).isEqualTo("第一個任務"); // +1 天
    }
    
    @Test
    @DisplayName("根據使用者和完成狀態查詢時應該只回傳符合條件的待辦事項")
    void test_findByUserAndCompleted_whenFilterByCompleted_then_shouldReturnOnlyCompletedTodos() {
        // Given - 儲存待辦事項（其中一個已完成）
        entityManager.persistAndFlush(todoItem1); // 未完成
        entityManager.persistAndFlush(todoItem2); // 已完成
        entityManager.persistAndFlush(todoItem3); // 未完成
        
        // When - 查詢已完成的待辦事項
        List<TodoItem> completedTodos = todoItemRepository.findByUserAndCompleted(testUser, true);
        List<TodoItem> incompleteTodos = todoItemRepository.findByUserAndCompleted(testUser, false);
        
        // Then - 驗證查詢結果
        assertThat(completedTodos).hasSize(1);
        assertThat(completedTodos.get(0).getTitle()).isEqualTo("第二個任務");
        assertThat(completedTodos.get(0).isCompleted()).isTrue();
        
        assertThat(incompleteTodos).hasSize(2);
        assertThat(incompleteTodos).extracting(TodoItem::getTitle)
                .containsExactlyInAnyOrder("第一個任務", "第三個任務");
    }
    
    @Test
    @DisplayName("根據使用者和完成狀態查詢並排序時應該回傳正確結果")
    void test_findByUserAndCompletedOrderByCreatedAtDesc_whenFilterAndSort_then_shouldReturnSortedResults() {
        // Given - 儲存待辦事項
        entityManager.persistAndFlush(todoItem1); // 未完成
        entityManager.persistAndFlush(todoItem2); // 已完成
        entityManager.persistAndFlush(todoItem3); // 未完成
        
        // When - 查詢未完成的待辦事項並依建立時間排序
        List<TodoItem> incompleteTodos = todoItemRepository.findByUserAndCompletedOrderByCreatedAtDesc(testUser, false);
        
        // Then - 驗證查詢結果
        assertThat(incompleteTodos).hasSize(2);
        assertThat(incompleteTodos).allMatch(todo -> !todo.isCompleted());
    }
    
    @Test
    @DisplayName("根據 ID 和使用者查詢待辦事項時應該回傳正確結果")
    void test_findByIdAndUser_whenTodoExists_then_shouldReturnTodo() {
        // Given - 儲存待辦事項
        TodoItem savedTodo = entityManager.persistAndFlush(todoItem1);
        
        // When - 根據 ID 和使用者查詢
        Optional<TodoItem> foundTodo = todoItemRepository.findByIdAndUser(savedTodo.getId(), testUser);
        
        // Then - 驗證查詢結果
        assertThat(foundTodo).isPresent();
        assertThat(foundTodo.get().getTitle()).isEqualTo("第一個任務");
        assertThat(foundTodo.get().getUser().getUsername()).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("查詢其他使用者的待辦事項時應該回傳空結果")
    void test_findByIdAndUser_whenWrongUser_then_shouldReturnEmpty() {
        // Given - 儲存待辦事項
        TodoItem savedTodo = entityManager.persistAndFlush(todoItem1);
        
        // When - 使用錯誤的使用者查詢
        Optional<TodoItem> foundTodo = todoItemRepository.findByIdAndUser(savedTodo.getId(), anotherUser);
        
        // Then - 驗證回傳空結果
        assertThat(foundTodo).isEmpty();
    }
    
    @Test
    @DisplayName("查詢逾期待辦事項時應該回傳正確結果")
    void test_findOverdueTodosByUser_whenOverdueTodosExist_then_shouldReturnOverdueTodos() {
        // Given - 建立逾期的待辦事項
        TodoItem overdueTodo1 = new TodoItem("逾期任務1", "描述", LocalDate.now().minusDays(2), testUser);
        TodoItem overdueTodo2 = new TodoItem("逾期任務2", "描述", LocalDate.now().minusDays(1), testUser);
        TodoItem futureTodo = new TodoItem("未來任務", "描述", LocalDate.now().plusDays(1), testUser);
        TodoItem completedOverdueTodo = new TodoItem("已完成逾期任務", "描述", LocalDate.now().minusDays(3), testUser);
        completedOverdueTodo.setCompleted(true);
        
        entityManager.persistAndFlush(overdueTodo1);
        entityManager.persistAndFlush(overdueTodo2);
        entityManager.persistAndFlush(futureTodo);
        entityManager.persistAndFlush(completedOverdueTodo);
        
        // When - 查詢逾期待辦事項
        List<TodoItem> overdueTodos = todoItemRepository.findOverdueTodosByUser(testUser, LocalDate.now());
        
        // Then - 驗證查詢結果（只包含未完成且逾期的）
        assertThat(overdueTodos).hasSize(2);
        assertThat(overdueTodos).extracting(TodoItem::getTitle)
                .containsExactly("逾期任務1", "逾期任務2"); // 依預計完成日升序排列
        assertThat(overdueTodos).allMatch(todo -> !todo.isCompleted());
    }
    
    @Test
    @DisplayName("查詢即將到期的待辦事項時應該回傳正確結果")
    void test_findDueSoonTodosByUser_whenDueSoonTodosExist_then_shouldReturnDueSoonTodos() {
        // Given - 建立即將到期的待辦事項
        TodoItem dueTodayTodo = new TodoItem("今天到期", "描述", LocalDate.now(), testUser);
        TodoItem dueTomorrowTodo = new TodoItem("明天到期", "描述", LocalDate.now().plusDays(1), testUser);
        TodoItem dueNextWeekTodo = new TodoItem("下週到期", "描述", LocalDate.now().plusDays(7), testUser);
        TodoItem completedDueSoonTodo = new TodoItem("已完成即將到期", "描述", LocalDate.now().plusDays(1), testUser);
        completedDueSoonTodo.setCompleted(true);
        
        entityManager.persistAndFlush(dueTodayTodo);
        entityManager.persistAndFlush(dueTomorrowTodo);
        entityManager.persistAndFlush(dueNextWeekTodo);
        entityManager.persistAndFlush(completedDueSoonTodo);
        
        // When - 查詢3天內到期的待辦事項
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(3);
        List<TodoItem> dueSoonTodos = todoItemRepository.findDueSoonTodosByUser(testUser, startDate, endDate);
        
        // Then - 驗證查詢結果
        assertThat(dueSoonTodos).hasSize(2);
        assertThat(dueSoonTodos).extracting(TodoItem::getTitle)
                .containsExactly("今天到期", "明天到期"); // 依預計完成日升序排列
        assertThat(dueSoonTodos).allMatch(todo -> !todo.isCompleted());
    }
    
    @Test
    @DisplayName("統計使用者待辦事項數量時應該回傳正確數量")
    void test_countByUser_whenMultipleTodos_then_shouldReturnCorrectCount() {
        // Given - 儲存待辦事項
        entityManager.persistAndFlush(todoItem1);
        entityManager.persistAndFlush(todoItem2);
        entityManager.persistAndFlush(todoItem3);
        
        // 為另一個使用者建立待辦事項
        TodoItem anotherUserTodo = new TodoItem("其他使用者任務", "描述", LocalDate.now().plusDays(1), anotherUser);
        entityManager.persistAndFlush(anotherUserTodo);
        
        // When - 統計使用者的待辦事項數量
        long testUserCount = todoItemRepository.countByUser(testUser);
        long anotherUserCount = todoItemRepository.countByUser(anotherUser);
        
        // Then - 驗證統計結果
        assertThat(testUserCount).isEqualTo(3);
        assertThat(anotherUserCount).isEqualTo(1);
    }
    
    @Test
    @DisplayName("統計使用者已完成待辦事項數量時應該回傳正確數量")
    void test_countByUserAndCompleted_whenMixedCompletionStatus_then_shouldReturnCorrectCount() {
        // Given - 儲存待辦事項（其中一個已完成）
        entityManager.persistAndFlush(todoItem1); // 未完成
        entityManager.persistAndFlush(todoItem2); // 已完成
        entityManager.persistAndFlush(todoItem3); // 未完成
        
        // When - 統計已完成和未完成的待辦事項數量
        long completedCount = todoItemRepository.countByUserAndCompleted(testUser, true);
        long incompleteCount = todoItemRepository.countByUserAndCompleted(testUser, false);
        
        // Then - 驗證統計結果
        assertThat(completedCount).isEqualTo(1);
        assertThat(incompleteCount).isEqualTo(2);
    }
    
    @Test
    @DisplayName("資料隔離測試：不同使用者的待辦事項應該互不影響")
    void test_dataIsolation_whenMultipleUsers_then_shouldIsolateData() {
        // Given - 為不同使用者建立待辦事項
        TodoItem testUserTodo = new TodoItem("測試使用者任務", "描述", LocalDate.now().plusDays(1), testUser);
        TodoItem anotherUserTodo = new TodoItem("其他使用者任務", "描述", LocalDate.now().plusDays(1), anotherUser);
        
        entityManager.persistAndFlush(testUserTodo);
        entityManager.persistAndFlush(anotherUserTodo);
        
        // When - 查詢各自的待辦事項
        List<TodoItem> testUserTodos = todoItemRepository.findByUserOrderByCreatedAtDesc(testUser);
        List<TodoItem> anotherUserTodos = todoItemRepository.findByUserOrderByCreatedAtDesc(anotherUser);
        
        // Then - 驗證資料隔離
        assertThat(testUserTodos).hasSize(1);
        assertThat(testUserTodos.get(0).getTitle()).isEqualTo("測試使用者任務");
        
        assertThat(anotherUserTodos).hasSize(1);
        assertThat(anotherUserTodos.get(0).getTitle()).isEqualTo("其他使用者任務");
        
        // 確保沒有交叉污染
        assertThat(testUserTodos.get(0).getUser()).isEqualTo(testUser);
        assertThat(anotherUserTodos.get(0).getUser()).isEqualTo(anotherUser);
    }
    
    @Test
    @DisplayName("查詢符合延期條件的待辦事項時應該回傳三天內到期且未完成的任務")
    void test_findEligibleForExtensionByUsername_whenEligibleTodosExist_then_shouldReturnEligibleTodos() {
        // Given - 建立不同條件的待辦事項
        TodoItem eligibleTodo1 = new TodoItem("今天到期未完成", "描述", LocalDate.now(), testUser);
        TodoItem eligibleTodo2 = new TodoItem("明天到期未完成", "描述", LocalDate.now().plusDays(1), testUser);
        TodoItem eligibleTodo3 = new TodoItem("三天後到期未完成", "描述", LocalDate.now().plusDays(3), testUser);
        TodoItem notEligibleTodo1 = new TodoItem("四天後到期", "描述", LocalDate.now().plusDays(4), testUser);
        TodoItem notEligibleTodo2 = new TodoItem("昨天到期", "描述", LocalDate.now().minusDays(1), testUser);
        TodoItem completedTodo = new TodoItem("明天到期已完成", "描述", LocalDate.now().plusDays(1), testUser);
        completedTodo.setCompleted(true);
        
        entityManager.persistAndFlush(eligibleTodo1);
        entityManager.persistAndFlush(eligibleTodo2);
        entityManager.persistAndFlush(eligibleTodo3);
        entityManager.persistAndFlush(notEligibleTodo1);
        entityManager.persistAndFlush(notEligibleTodo2);
        entityManager.persistAndFlush(completedTodo);
        
        // When - 查詢符合延期條件的待辦事項
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        List<TodoItem> eligibleTodos = todoItemRepository.findEligibleForExtensionByUsername("testuser", today, threeDaysLater);
        
        // Then - 驗證查詢結果（只包含三天內到期且未完成的）
        assertThat(eligibleTodos).hasSize(3);
        assertThat(eligibleTodos).extracting(TodoItem::getTitle)
                .containsExactly("今天到期未完成", "明天到期未完成", "三天後到期未完成"); // 依預計完成日升序排列
        assertThat(eligibleTodos).allMatch(todo -> !todo.isCompleted());
        assertThat(eligibleTodos).allMatch(todo -> 
            !todo.getDueDate().isBefore(LocalDate.now()) && 
            !todo.getDueDate().isAfter(LocalDate.now().plusDays(3)));
    }
    
    @Test
    @DisplayName("查詢符合延期條件的待辦事項時不同使用者應該回傳各自的結果")
    void test_findEligibleForExtensionByUsername_whenMultipleUsers_then_shouldReturnUserSpecificResults() {
        // Given - 為不同使用者建立符合條件的待辦事項
        TodoItem testUserTodo = new TodoItem("測試使用者符合條件", "描述", LocalDate.now().plusDays(1), testUser);
        TodoItem anotherUserTodo = new TodoItem("其他使用者符合條件", "描述", LocalDate.now().plusDays(2), anotherUser);
        
        entityManager.persistAndFlush(testUserTodo);
        entityManager.persistAndFlush(anotherUserTodo);
        
        // When - 查詢各自符合延期條件的待辦事項
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        List<TodoItem> testUserEligible = todoItemRepository.findEligibleForExtensionByUsername("testuser", today, threeDaysLater);
        List<TodoItem> anotherUserEligible = todoItemRepository.findEligibleForExtensionByUsername("anotheruser", today, threeDaysLater);
        
        // Then - 驗證資料隔離
        assertThat(testUserEligible).hasSize(1);
        assertThat(testUserEligible.get(0).getTitle()).isEqualTo("測試使用者符合條件");
        
        assertThat(anotherUserEligible).hasSize(1);
        assertThat(anotherUserEligible.get(0).getTitle()).isEqualTo("其他使用者符合條件");
    }
    
    @Test
    @DisplayName("查詢指定日期範圍內的待辦事項時應該回傳正確結果")
    void test_findByUserAndDueDateBetween_whenDateRangeSpecified_then_shouldReturnTodosInRange() {
        // Given - 建立不同日期的待辦事項
        TodoItem todoInRange1 = new TodoItem("範圍內任務1", "描述", LocalDate.now().plusDays(2), testUser);
        TodoItem todoInRange2 = new TodoItem("範圍內任務2", "描述", LocalDate.now().plusDays(4), testUser);
        TodoItem todoOutOfRange1 = new TodoItem("範圍外任務1", "描述", LocalDate.now().plusDays(1), testUser);
        TodoItem todoOutOfRange2 = new TodoItem("範圍外任務2", "描述", LocalDate.now().plusDays(6), testUser);
        
        entityManager.persistAndFlush(todoInRange1);
        entityManager.persistAndFlush(todoInRange2);
        entityManager.persistAndFlush(todoOutOfRange1);
        entityManager.persistAndFlush(todoOutOfRange2);
        
        // When - 查詢指定日期範圍內的待辦事項（2-5天後）
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(5);
        List<TodoItem> todosInRange = todoItemRepository.findByUserAndDueDateBetween("testuser", startDate, endDate);
        
        // Then - 驗證查詢結果
        assertThat(todosInRange).hasSize(2);
        assertThat(todosInRange).extracting(TodoItem::getTitle)
                .containsExactly("範圍內任務1", "範圍內任務2"); // 依預計完成日升序排列
        assertThat(todosInRange).allMatch(todo -> 
            !todo.getDueDate().isBefore(startDate) && 
            !todo.getDueDate().isAfter(endDate));
    }
    
    @Test
    @DisplayName("查詢日期範圍內的待辦事項時不同使用者應該回傳各自的結果")
    void test_findByUserAndDueDateBetween_whenMultipleUsers_then_shouldReturnUserSpecificResults() {
        // Given - 為不同使用者建立相同日期範圍的待辦事項
        LocalDate targetDate = LocalDate.now().plusDays(3);
        TodoItem testUserTodo = new TodoItem("測試使用者任務", "描述", targetDate, testUser);
        TodoItem anotherUserTodo = new TodoItem("其他使用者任務", "描述", targetDate, anotherUser);
        
        entityManager.persistAndFlush(testUserTodo);
        entityManager.persistAndFlush(anotherUserTodo);
        
        // When - 查詢各自在日期範圍內的待辦事項
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(4);
        List<TodoItem> testUserTodos = todoItemRepository.findByUserAndDueDateBetween("testuser", startDate, endDate);
        List<TodoItem> anotherUserTodos = todoItemRepository.findByUserAndDueDateBetween("anotheruser", startDate, endDate);
        
        // Then - 驗證資料隔離
        assertThat(testUserTodos).hasSize(1);
        assertThat(testUserTodos.get(0).getTitle()).isEqualTo("測試使用者任務");
        
        assertThat(anotherUserTodos).hasSize(1);
        assertThat(anotherUserTodos.get(0).getTitle()).isEqualTo("其他使用者任務");
    }
    
    @Test
    @DisplayName("統計使用者總延期次數時應該回傳正確數量")
    void test_countTotalExtensionsByUsername_whenExtensionsExist_then_shouldReturnCorrectCount() {
        // Given - 建立有延期記錄的待辦事項
        TodoItem todoWithExtensions1 = new TodoItem("延期任務1", "描述", LocalDate.now().plusDays(1), testUser);
        todoWithExtensions1.setExtensionCount(2); // 延期2次
        
        TodoItem todoWithExtensions2 = new TodoItem("延期任務2", "描述", LocalDate.now().plusDays(2), testUser);
        todoWithExtensions2.setExtensionCount(3); // 延期3次
        
        TodoItem todoWithoutExtensions = new TodoItem("無延期任務", "描述", LocalDate.now().plusDays(3), testUser);
        // extensionCount 預設為 0
        
        // 為另一個使用者建立有延期記錄的待辦事項
        TodoItem anotherUserTodo = new TodoItem("其他使用者延期任務", "描述", LocalDate.now().plusDays(1), anotherUser);
        anotherUserTodo.setExtensionCount(1);
        
        entityManager.persistAndFlush(todoWithExtensions1);
        entityManager.persistAndFlush(todoWithExtensions2);
        entityManager.persistAndFlush(todoWithoutExtensions);
        entityManager.persistAndFlush(anotherUserTodo);
        
        // When - 統計使用者的總延期次數
        Long testUserExtensions = todoItemRepository.countTotalExtensionsByUsername("testuser");
        Long anotherUserExtensions = todoItemRepository.countTotalExtensionsByUsername("anotheruser");
        
        // Then - 驗證統計結果
        assertThat(testUserExtensions).isEqualTo(5L); // 2 + 3 + 0 = 5
        assertThat(anotherUserExtensions).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("統計沒有延期記錄的使用者總延期次數時應該回傳0")
    void test_countTotalExtensionsByUsername_whenNoExtensions_then_shouldReturnZero() {
        // Given - 建立沒有延期記錄的待辦事項
        TodoItem todoWithoutExtensions1 = new TodoItem("無延期任務1", "描述", LocalDate.now().plusDays(1), testUser);
        TodoItem todoWithoutExtensions2 = new TodoItem("無延期任務2", "描述", LocalDate.now().plusDays(2), testUser);
        
        entityManager.persistAndFlush(todoWithoutExtensions1);
        entityManager.persistAndFlush(todoWithoutExtensions2);
        
        // When - 統計使用者的總延期次數
        Long totalExtensions = todoItemRepository.countTotalExtensionsByUsername("testuser");
        
        // Then - 驗證統計結果應該為0
        assertThat(totalExtensions).isEqualTo(0L);
    }
    
    @Test
    @DisplayName("統計不存在使用者的總延期次數時應該回傳0")
    void test_countTotalExtensionsByUsername_whenUserNotExists_then_shouldReturnZero() {
        // Given - 沒有為該使用者建立任何待辦事項
        
        // When - 統計不存在使用者的總延期次數
        Long totalExtensions = todoItemRepository.countTotalExtensionsByUsername("nonexistentuser");
        
        // Then - 驗證統計結果應該為0
        assertThat(totalExtensions).isEqualTo(0L);
    }
}