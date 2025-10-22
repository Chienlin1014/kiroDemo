package com.course.kirodemo.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User 實體類別的單元測試
 * 測試 User 實體的驗證規則和關聯關係
 */
@DisplayName("User 實體測試")
class UserTest {

    private User user;
    private TodoItem todoItem1;
    private TodoItem todoItem2;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "password123");
        todoItem1 = new TodoItem("測試任務1", "測試描述1", LocalDate.now().plusDays(1));
        todoItem2 = new TodoItem("測試任務2", "測試描述2", LocalDate.now().plusDays(2));
    }

    @Test
    @DisplayName("應該能夠建立 User 實體")
    void test_constructor_whenValidUsernameAndPassword_then_shouldCreateUserEntity() {
        // Given & When
        User newUser = new User("chienlin", "1234");
        
        // Then
        assertNotNull(newUser);
        assertEquals("chienlin", newUser.getUsername());
        assertEquals("1234", newUser.getPassword());
        assertNotNull(newUser.getTodoItems());
        assertTrue(newUser.getTodoItems().isEmpty());
    }

    @Test
    @DisplayName("應該能夠使用預設建構子建立 User")
    void test_defaultConstructor_whenCalled_then_shouldCreateUserWithNullValues() {
        // Given & When
        User newUser = new User();
        
        // Then
        assertNotNull(newUser);
        assertNull(newUser.getUsername());
        assertNull(newUser.getPassword());
        assertNotNull(newUser.getTodoItems());
        assertTrue(newUser.getTodoItems().isEmpty());
    }

    @Test
    @DisplayName("應該能夠新增待辦事項到使用者")
    void test_addTodoItem_whenValidTodoItem_then_shouldAddToListAndSetUser() {
        // Given & When
        user.addTodoItem(todoItem1);
        
        // Then
        assertEquals(1, user.getTodoItems().size());
        assertTrue(user.getTodoItems().contains(todoItem1));
        assertEquals(user, todoItem1.getUser());
    }

    @Test
    @DisplayName("應該能夠新增多個待辦事項到使用者")
    void test_addTodoItem_whenMultipleTodoItems_then_shouldAddAllToListAndSetUser() {
        // Given & When
        user.addTodoItem(todoItem1);
        user.addTodoItem(todoItem2);
        
        // Then
        assertEquals(2, user.getTodoItems().size());
        assertTrue(user.getTodoItems().contains(todoItem1));
        assertTrue(user.getTodoItems().contains(todoItem2));
        assertEquals(user, todoItem1.getUser());
        assertEquals(user, todoItem2.getUser());
    }

    @Test
    @DisplayName("應該能夠從使用者移除待辦事項")
    void test_removeTodoItem_whenExistingTodoItem_then_shouldRemoveFromListAndClearUser() {
        // Given
        user.addTodoItem(todoItem1);
        user.addTodoItem(todoItem2);
        
        // When
        user.removeTodoItem(todoItem1);
        
        // Then
        assertEquals(1, user.getTodoItems().size());
        assertFalse(user.getTodoItems().contains(todoItem1));
        assertTrue(user.getTodoItems().contains(todoItem2));
        assertNull(todoItem1.getUser());
        assertEquals(user, todoItem2.getUser());
    }

    @Test
    @DisplayName("移除不存在的待辦事項應該不會影響列表")
    void test_removeTodoItem_whenNonExistentTodoItem_then_shouldNotAffectList() {
        // Given
        user.addTodoItem(todoItem1);
        int originalSize = user.getTodoItems().size();
        
        // When
        user.removeTodoItem(todoItem2);
        
        // Then
        assertEquals(originalSize, user.getTodoItems().size());
        assertTrue(user.getTodoItems().contains(todoItem1));
    }

    @Test
    @DisplayName("應該正確實作 equals 方法")
    void test_equals_whenComparingUsers_then_shouldReturnCorrectEquality() {
        // Given
        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");
        User user3 = new User("user1", "pass1");
        
        user1.setId(1L);
        user2.setId(2L);
        user3.setId(1L);
        
        // Then
        assertEquals(user1, user3); // 相同 ID
        assertNotEquals(user1, user2); // 不同 ID
        assertNotEquals(user1, null);
        assertNotEquals(user1, "not a user");
        assertEquals(user1, user1); // 自己等於自己
    }

    @Test
    @DisplayName("沒有 ID 的 User 不應該相等")
    void test_equals_whenIdIsNull_then_shouldNotBeEqual() {
        // Given
        User user1 = new User("user1", "pass1");
        User user2 = new User("user1", "pass1");
        
        // Then (兩個都沒有 ID)
        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("應該正確實作 hashCode 方法")
    void test_hashCode_whenCalledOnDifferentUsers_then_shouldReturnSameValue() {
        // Given
        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");
        
        // Then
        assertEquals(user1.hashCode(), user2.hashCode()); // 使用 getClass().hashCode()
    }

    @Test
    @DisplayName("應該正確實作 toString 方法")
    void test_toString_whenCalled_then_shouldContainAllRequiredFields() {
        // Given
        user.setId(1L);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        
        // When
        String result = user.toString();
        
        // Then
        assertTrue(result.contains("User{"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("createdAt=" + now));
    }

    @Test
    @DisplayName("應該能夠設定和取得所有屬性")
    void test_settersAndGetters_whenCalledWithValidValues_then_shouldSetAndReturnCorrectValues() {
        // Given
        Long id = 1L;
        String username = "newuser";
        String password = "newpass";
        LocalDateTime createdAt = LocalDateTime.now();
        
        // When
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setCreatedAt(createdAt);
        
        // Then
        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    @DisplayName("應該能夠設定待辦事項列表")
    void test_setTodoItems_whenValidList_then_shouldSetTodoItemsList() {
        // Given
        java.util.List<TodoItem> todoItems = java.util.Arrays.asList(todoItem1, todoItem2);
        
        // When
        user.setTodoItems(todoItems);
        
        // Then
        assertEquals(2, user.getTodoItems().size());
        assertTrue(user.getTodoItems().contains(todoItem1));
        assertTrue(user.getTodoItems().contains(todoItem2));
    }
}