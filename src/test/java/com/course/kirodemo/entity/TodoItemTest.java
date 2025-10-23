package com.course.kirodemo.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TodoItem 實體類別的單元測試
 * 測試 TodoItem 實體的業務邏輯和約束條件
 */
@DisplayName("TodoItem 實體測試")
class TodoItemTest {

    private TodoItem todoItem;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "password123");
        todoItem = new TodoItem("測試任務", "測試描述", LocalDate.now().plusDays(1));
    }

    @Test
    @DisplayName("應該能夠建立 TodoItem 實體")
    void test_constructor_whenValidParameters_then_shouldCreateTodoItemEntity() {
        // Given & When
        TodoItem newTodoItem = new TodoItem("新任務", "新描述", LocalDate.now().plusDays(2));
        
        // Then
        assertNotNull(newTodoItem);
        assertEquals("新任務", newTodoItem.getTitle());
        assertEquals("新描述", newTodoItem.getDescription());
        assertEquals(LocalDate.now().plusDays(2), newTodoItem.getDueDate());
        assertFalse(newTodoItem.isCompleted());
        assertNull(newTodoItem.getCompletedAt());
    }

    @Test
    @DisplayName("應該能夠使用預設建構子建立 TodoItem")
    void test_defaultConstructor_whenCalled_then_shouldCreateTodoItemWithNullValues() {
        // Given & When
        TodoItem newTodoItem = new TodoItem();
        
        // Then
        assertNotNull(newTodoItem);
        assertNull(newTodoItem.getTitle());
        assertNull(newTodoItem.getDescription());
        assertNull(newTodoItem.getDueDate());
        assertFalse(newTodoItem.isCompleted());
        assertNull(newTodoItem.getCompletedAt());
    }

    @Test
    @DisplayName("應該能夠使用包含使用者的建構子建立 TodoItem")
    void test_constructorWithUser_whenValidParametersAndUser_then_shouldCreateTodoItemWithUser() {
        // Given & When
        TodoItem newTodoItem = new TodoItem("任務", "描述", LocalDate.now().plusDays(1), user);
        
        // Then
        assertNotNull(newTodoItem);
        assertEquals("任務", newTodoItem.getTitle());
        assertEquals("描述", newTodoItem.getDescription());
        assertEquals(LocalDate.now().plusDays(1), newTodoItem.getDueDate());
        assertEquals(user, newTodoItem.getUser());
        assertFalse(newTodoItem.isCompleted());
    }

    @Test
    @DisplayName("設定完成狀態為 true 時應該自動設定完成時間")
    void test_setCompleted_whenSetToTrue_then_shouldSetCompletedAtAutomatically() {
        // Given
        assertFalse(todoItem.isCompleted());
        assertNull(todoItem.getCompletedAt());
        
        // When
        todoItem.setCompleted(true);
        
        // Then
        assertTrue(todoItem.isCompleted());
        assertNotNull(todoItem.getCompletedAt());
        assertTrue(todoItem.getCompletedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("設定完成狀態為 false 時應該清除完成時間")
    void test_setCompleted_whenSetToFalse_then_shouldClearCompletedAt() {
        // Given
        todoItem.setCompleted(true);
        assertTrue(todoItem.isCompleted());
        assertNotNull(todoItem.getCompletedAt());
        
        // When
        todoItem.setCompleted(false);
        
        // Then
        assertFalse(todoItem.isCompleted());
        assertNull(todoItem.getCompletedAt());
    }

    @Test
    @DisplayName("重複設定完成狀態為 true 不應該更新完成時間")
    void test_setCompleted_whenAlreadyCompleted_then_shouldNotUpdateCompletedAt() {
        // Given
        todoItem.setCompleted(true);
        LocalDateTime originalCompletedAt = todoItem.getCompletedAt();
        
        // When
        todoItem.setCompleted(true);
        
        // Then
        assertTrue(todoItem.isCompleted());
        assertEquals(originalCompletedAt, todoItem.getCompletedAt());
    }

    @Test
    @DisplayName("markAsCompleted 方法應該正確標記為完成")
    void test_markAsCompleted_whenCalled_then_shouldSetCompletedTrueAndSetCompletedAt() {
        // Given
        assertFalse(todoItem.isCompleted());
        assertNull(todoItem.getCompletedAt());
        
        // When
        todoItem.markAsCompleted();
        
        // Then
        assertTrue(todoItem.isCompleted());
        assertNotNull(todoItem.getCompletedAt());
        assertTrue(todoItem.getCompletedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("markAsIncomplete 方法應該正確標記為未完成")
    void test_markAsIncomplete_whenCalled_then_shouldSetCompletedFalseAndClearCompletedAt() {
        // Given
        todoItem.markAsCompleted();
        assertTrue(todoItem.isCompleted());
        
        // When
        todoItem.markAsIncomplete();
        
        // Then
        assertFalse(todoItem.isCompleted());
        assertNull(todoItem.getCompletedAt());
    }

    @Test
    @DisplayName("toggleCompleted 方法應該正確切換完成狀態")
    void test_toggleCompleted_whenCalled_then_shouldToggleCompletedStatusCorrectly() {
        // Given
        assertFalse(todoItem.isCompleted());
        
        // When - 第一次切換
        todoItem.toggleCompleted();
        
        // Then
        assertTrue(todoItem.isCompleted());
        assertNotNull(todoItem.getCompletedAt());
        
        // When - 第二次切換
        todoItem.toggleCompleted();
        
        // Then
        assertFalse(todoItem.isCompleted());
        assertNull(todoItem.getCompletedAt());
    }

    @Test
    @DisplayName("isOverdue 方法應該正確判斷是否逾期")
    void test_isOverdue_whenDifferentDueDates_then_shouldReturnCorrectOverdueStatus() {
        // Given - 設定過期日期
        TodoItem overdueTodoItem = new TodoItem("逾期任務", "描述", LocalDate.now().minusDays(1));
        TodoItem futureTodoItem = new TodoItem("未來任務", "描述", LocalDate.now().plusDays(1));
        TodoItem todayTodoItem = new TodoItem("今日任務", "描述", LocalDate.now());
        
        // Then
        assertTrue(overdueTodoItem.isOverdue());
        assertFalse(futureTodoItem.isOverdue());
        assertFalse(todayTodoItem.isOverdue());
    }

    @Test
    @DisplayName("已完成的任務不應該被視為逾期")
    void test_isOverdue_whenTaskIsCompleted_then_shouldReturnFalse() {
        // Given
        TodoItem overdueTodoItem = new TodoItem("逾期任務", "描述", LocalDate.now().minusDays(1));
        overdueTodoItem.markAsCompleted();
        
        // Then
        assertFalse(overdueTodoItem.isOverdue());
    }

    @Test
    @DisplayName("isDueSoon 方法應該正確判斷是否即將到期")
    void test_isDueSoon_whenDifferentDueDates_then_shouldReturnCorrectDueSoonStatus() {
        // Given
        TodoItem dueTomorrowItem = new TodoItem("明天到期", "描述", LocalDate.now().plusDays(1));
        TodoItem dueInThreeDaysItem = new TodoItem("三天後到期", "描述", LocalDate.now().plusDays(3));
        TodoItem dueInFourDaysItem = new TodoItem("四天後到期", "描述", LocalDate.now().plusDays(4));
        TodoItem dueTodayItem = new TodoItem("今天到期", "描述", LocalDate.now());
        
        // Then
        assertTrue(dueTomorrowItem.isDueSoon());
        assertTrue(dueInThreeDaysItem.isDueSoon());
        assertFalse(dueInFourDaysItem.isDueSoon());
        assertTrue(dueTodayItem.isDueSoon());
    }

    @Test
    @DisplayName("已完成的任務不應該被視為即將到期")
    void test_isDueSoon_whenTaskIsCompleted_then_shouldReturnFalse() {
        // Given
        TodoItem dueSoonItem = new TodoItem("即將到期", "描述", LocalDate.now().plusDays(1));
        dueSoonItem.markAsCompleted();
        
        // Then
        assertFalse(dueSoonItem.isDueSoon());
    }

    @Test
    @DisplayName("應該正確實作 equals 方法")
    void test_equals_whenComparingTodoItems_then_shouldReturnCorrectEquality() {
        // Given
        TodoItem todoItem1 = new TodoItem("任務1", "描述1", LocalDate.now().plusDays(1));
        TodoItem todoItem2 = new TodoItem("任務2", "描述2", LocalDate.now().plusDays(2));
        TodoItem todoItem3 = new TodoItem("任務3", "描述3", LocalDate.now().plusDays(3));
        
        todoItem1.setId(1L);
        todoItem2.setId(2L);
        todoItem3.setId(1L);
        
        // Then
        assertEquals(todoItem1, todoItem3); // 相同 ID
        assertNotEquals(todoItem1, todoItem2); // 不同 ID
        assertNotEquals(todoItem1, null);
        assertNotEquals(todoItem1, "not a todo item");
        assertEquals(todoItem1, todoItem1); // 自己等於自己
    }

    @Test
    @DisplayName("沒有 ID 的 TodoItem 不應該相等")
    void test_equals_whenIdIsNull_then_shouldNotBeEqual() {
        // Given
        TodoItem todoItem1 = new TodoItem("任務", "描述", LocalDate.now().plusDays(1));
        TodoItem todoItem2 = new TodoItem("任務", "描述", LocalDate.now().plusDays(1));
        
        // Then (兩個都沒有 ID)
        assertNotEquals(todoItem1, todoItem2);
    }

    @Test
    @DisplayName("應該正確實作 hashCode 方法")
    void test_hashCode_whenCalledOnDifferentTodoItems_then_shouldReturnSameValue() {
        // Given
        TodoItem todoItem1 = new TodoItem("任務1", "描述1", LocalDate.now().plusDays(1));
        TodoItem todoItem2 = new TodoItem("任務2", "描述2", LocalDate.now().plusDays(2));
        
        // Then
        assertEquals(todoItem1.hashCode(), todoItem2.hashCode()); // 使用 getClass().hashCode()
    }

    @Test
    @DisplayName("應該正確實作 toString 方法")
    void test_toString_whenCalled_then_shouldContainAllRequiredFields() {
        // Given
        todoItem.setId(1L);
        LocalDateTime now = LocalDateTime.now();
        todoItem.setCreatedAt(now);
        todoItem.markAsCompleted();
        
        // When
        String result = todoItem.toString();
        
        // Then
        assertTrue(result.contains("TodoItem{"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("title='測試任務'"));
        assertTrue(result.contains("completed=true"));
        assertTrue(result.contains("createdAt=" + now));
        assertTrue(result.contains("dueDate=" + todoItem.getDueDate()));
        assertTrue(result.contains("completedAt=" + todoItem.getCompletedAt()));
    }

    @Test
    @DisplayName("應該能夠設定和取得所有屬性")
    void test_settersAndGetters_whenCalledWithValidValues_then_shouldSetAndReturnCorrectValues() {
        // Given
        Long id = 1L;
        String title = "新標題";
        String description = "新描述";
        boolean completed = true;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDate dueDate = LocalDate.now().plusDays(5);
        LocalDateTime completedAt = LocalDateTime.now();
        
        // When
        todoItem.setId(id);
        todoItem.setTitle(title);
        todoItem.setDescription(description);
        todoItem.setCompleted(completed);
        todoItem.setCreatedAt(createdAt);
        todoItem.setDueDate(dueDate);
        todoItem.setCompletedAt(completedAt);
        todoItem.setUser(user);
        
        // Then
        assertEquals(id, todoItem.getId());
        assertEquals(title, todoItem.getTitle());
        assertEquals(description, todoItem.getDescription());
        assertTrue(todoItem.isCompleted());
        assertEquals(createdAt, todoItem.getCreatedAt());
        assertEquals(dueDate, todoItem.getDueDate());
        assertEquals(completedAt, todoItem.getCompletedAt());
        assertEquals(user, todoItem.getUser());
    }

    @Test
    @DisplayName("應該能夠處理空描述")
    void test_constructor_whenDescriptionIsNull_then_shouldHandleNullDescription() {
        // Given & When
        TodoItem todoItemWithNullDescription = new TodoItem("標題", null, LocalDate.now().plusDays(1));
        
        // Then
        assertEquals("標題", todoItemWithNullDescription.getTitle());
        assertNull(todoItemWithNullDescription.getDescription());
        assertEquals(LocalDate.now().plusDays(1), todoItemWithNullDescription.getDueDate());
    }

    @Test
    @DisplayName("應該能夠處理空字串描述")
    void test_constructor_whenDescriptionIsEmpty_then_shouldHandleEmptyDescription() {
        // Given & When
        TodoItem todoItemWithEmptyDescription = new TodoItem("標題", "", LocalDate.now().plusDays(1));
        
        // Then
        assertEquals("標題", todoItemWithEmptyDescription.getTitle());
        assertEquals("", todoItemWithEmptyDescription.getDescription());
    }

    // 延期功能測試

    @Test
    @DisplayName("未完成且三天內到期的待辦事項應該符合延期條件")
    void test_isEligibleForExtension_whenIncompleteAndDueWithinThreeDays_then_shouldReturnTrue() {
        // Given
        TodoItem dueTomorrowItem = new TodoItem("明天到期", "描述", LocalDate.now().plusDays(1));
        TodoItem dueInTwoDaysItem = new TodoItem("後天到期", "描述", LocalDate.now().plusDays(2));
        TodoItem dueInThreeDaysItem = new TodoItem("三天後到期", "描述", LocalDate.now().plusDays(3));
        TodoItem dueTodayItem = new TodoItem("今天到期", "描述", LocalDate.now());
        
        // When & Then
        assertTrue(dueTomorrowItem.isEligibleForExtension());
        assertTrue(dueInTwoDaysItem.isEligibleForExtension());
        assertTrue(dueInThreeDaysItem.isEligibleForExtension());
        assertTrue(dueTodayItem.isEligibleForExtension());
    }

    @Test
    @DisplayName("已完成的待辦事項不應該符合延期條件")
    void test_isEligibleForExtension_whenCompleted_then_shouldReturnFalse() {
        // Given
        TodoItem completedItem = new TodoItem("已完成任務", "描述", LocalDate.now().plusDays(1));
        completedItem.markAsCompleted();
        
        // When
        boolean result = completedItem.isEligibleForExtension();
        
        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("超過三天後到期的待辦事項不應該符合延期條件")
    void test_isEligibleForExtension_whenDueAfterThreeDays_then_shouldReturnFalse() {
        // Given
        TodoItem dueInFourDaysItem = new TodoItem("四天後到期", "描述", LocalDate.now().plusDays(4));
        TodoItem dueInWeekItem = new TodoItem("一週後到期", "描述", LocalDate.now().plusDays(7));
        
        // When & Then
        assertFalse(dueInFourDaysItem.isEligibleForExtension());
        assertFalse(dueInWeekItem.isEligibleForExtension());
    }

    @Test
    @DisplayName("昨天到期的待辦事項不應該符合延期條件")
    void test_isEligibleForExtension_whenOverdue_then_shouldReturnFalse() {
        // Given
        TodoItem overdueItem = new TodoItem("逾期任務", "描述", LocalDate.now().minusDays(1));
        
        // When
        boolean result = overdueItem.isEligibleForExtension();
        
        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("延期操作應該正確更新到期日和延期記錄")
    void test_extendDueDate_whenValidDays_then_shouldUpdateDueDateAndExtensionRecord() {
        // Given
        LocalDate originalDueDate = LocalDate.now().plusDays(1);
        TodoItem todoItem = new TodoItem("測試任務", "描述", originalDueDate);
        int extensionDays = 3;
        
        // When
        todoItem.extendDueDate(extensionDays);
        
        // Then
        assertEquals(originalDueDate.plusDays(extensionDays), todoItem.getDueDate());
        assertEquals(originalDueDate, todoItem.getOriginalDueDate());
        assertEquals(1, todoItem.getExtensionCount());
        assertNotNull(todoItem.getLastExtendedAt());
        assertTrue(todoItem.getLastExtendedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("延期天數為負數時應該拋出異常")
    void test_extendDueDate_whenNegativeDays_then_shouldThrowException() {
        // Given
        TodoItem todoItem = new TodoItem("測試任務", "描述", LocalDate.now().plusDays(1));
        int negativeDays = -1;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> todoItem.extendDueDate(negativeDays));
        assertEquals("延期天數必須為正數", exception.getMessage());
    }

    @Test
    @DisplayName("延期天數為零時應該拋出異常")
    void test_extendDueDate_whenZeroDays_then_shouldThrowException() {
        // Given
        TodoItem todoItem = new TodoItem("測試任務", "描述", LocalDate.now().plusDays(1));
        int zeroDays = 0;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> todoItem.extendDueDate(zeroDays));
        assertEquals("延期天數必須為正數", exception.getMessage());
    }

    @Test
    @DisplayName("多次延期應該累計延期次數和總天數")
    void test_multipleExtensions_whenExtendedMultipleTimes_then_shouldAccumulateCountAndDays() {
        // Given
        LocalDate originalDueDate = LocalDate.now().plusDays(1);
        TodoItem todoItem = new TodoItem("測試任務", "描述", originalDueDate);
        
        // When
        todoItem.extendDueDate(2); // 第一次延期2天
        LocalDateTime firstExtensionTime = todoItem.getLastExtendedAt();
        
        // 稍微等待以確保時間戳不同
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        todoItem.extendDueDate(3); // 第二次延期3天
        
        // Then
        assertEquals(originalDueDate.plusDays(5), todoItem.getDueDate()); // 總共延期5天
        assertEquals(originalDueDate, todoItem.getOriginalDueDate()); // 原始日期不變
        assertEquals(2, todoItem.getExtensionCount()); // 延期次數為2
        assertTrue(todoItem.getLastExtendedAt().isAfter(firstExtensionTime)); // 最後延期時間更新
        assertEquals(5, todoItem.getTotalExtensionDays()); // 總延期天數為5
    }

    @Test
    @DisplayName("沒有延期過的待辦事項總延期天數應該為0")
    void test_getTotalExtensionDays_whenNoExtension_then_shouldReturnZero() {
        // Given
        TodoItem todoItem = new TodoItem("測試任務", "描述", LocalDate.now().plusDays(1));
        
        // When
        long totalDays = todoItem.getTotalExtensionDays();
        
        // Then
        assertEquals(0, totalDays);
        assertNull(todoItem.getOriginalDueDate());
        assertEquals(0, todoItem.getExtensionCount());
    }

    @Test
    @DisplayName("應該能夠設定和取得延期相關屬性")
    void test_extensionSettersAndGetters_whenCalledWithValidValues_then_shouldSetAndReturnCorrectValues() {
        // Given
        int extensionCount = 3;
        LocalDateTime lastExtendedAt = LocalDateTime.now();
        LocalDate originalDueDate = LocalDate.now().plusDays(1);
        
        // When
        todoItem.setExtensionCount(extensionCount);
        todoItem.setLastExtendedAt(lastExtendedAt);
        todoItem.setOriginalDueDate(originalDueDate);
        
        // Then
        assertEquals(extensionCount, todoItem.getExtensionCount());
        assertEquals(lastExtendedAt, todoItem.getLastExtendedAt());
        assertEquals(originalDueDate, todoItem.getOriginalDueDate());
    }

    @Test
    @DisplayName("延期後的待辦事項如果不再符合三天內到期條件應該返回false")
    void test_isEligibleForExtension_whenExtendedBeyondThreeDays_then_shouldReturnFalse() {
        // Given
        TodoItem todoItem = new TodoItem("測試任務", "描述", LocalDate.now().plusDays(2));
        assertTrue(todoItem.isEligibleForExtension()); // 延期前符合條件
        
        // When
        todoItem.extendDueDate(5); // 延期5天，現在是7天後到期
        
        // Then
        assertFalse(todoItem.isEligibleForExtension()); // 延期後不符合條件
    }
}