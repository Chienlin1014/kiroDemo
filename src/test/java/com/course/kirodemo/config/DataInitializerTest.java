package com.course.kirodemo.config;

import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.repository.TodoItemRepository;
import com.course.kirodemo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataInitializer 測試類別
 * 測試資料初始化功能和密碼加密
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataInitializerTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TodoItemRepository todoItemRepository;
    
    @Autowired
    private DataInitializer dataInitializer;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Test
    @DisplayName("應用程式啟動時應該建立所有測試使用者")
    void test_run_whenApplicationStarts_then_shouldCreateAllTestUsers() throws Exception {
        // Given (給定) - 清空資料庫
        userRepository.deleteAll();
        todoItemRepository.deleteAll();
        
        // When (當) - 執行資料初始化
        dataInitializer.run();
        
        // Then (那麼) - 驗證使用者建立
        assertEquals(4, userRepository.count());
        
        // 驗證每個使用者都存在
        assertTrue(userRepository.existsByUsername("chienlin"));
        assertTrue(userRepository.existsByUsername("alice"));
        assertTrue(userRepository.existsByUsername("bob"));
        assertTrue(userRepository.existsByUsername("carol"));
    }
    
    @Test
    @DisplayName("所有使用者密碼應該使用 BCrypt 正確加密")
    void test_run_whenCreatingUsers_then_shouldEncryptPasswordsWithBCrypt() throws Exception {
        // Given (給定) - 清空資料庫
        userRepository.deleteAll();
        todoItemRepository.deleteAll();
        
        // When (當) - 執行資料初始化
        dataInitializer.run();
        
        // Then (那麼) - 驗證密碼加密
        Optional<User> chienlinOpt = userRepository.findByUsername("chienlin");
        assertTrue(chienlinOpt.isPresent());
        User chienlin = chienlinOpt.get();
        
        // 驗證密碼是 BCrypt 格式
        assertTrue(chienlin.getPassword().startsWith("$2a$"));
        
        // 驗證密碼可以正確驗證
        assertTrue(passwordEncoder.matches("1234", chienlin.getPassword()));
        
        // 驗證其他使用者的密碼
        Optional<User> aliceOpt = userRepository.findByUsername("alice");
        assertTrue(aliceOpt.isPresent());
        assertTrue(passwordEncoder.matches("password123", aliceOpt.get().getPassword()));
        
        Optional<User> bobOpt = userRepository.findByUsername("bob");
        assertTrue(bobOpt.isPresent());
        assertTrue(passwordEncoder.matches("mypass456", bobOpt.get().getPassword()));
        
        Optional<User> carolOpt = userRepository.findByUsername("carol");
        assertTrue(carolOpt.isPresent());
        assertTrue(passwordEncoder.matches("test789", carolOpt.get().getPassword()));
    }
    
    @Test
    @DisplayName("應該為每個使用者建立多筆測試待辦事項")
    void test_run_whenApplicationStarts_then_shouldCreateTodoItemsForAllUsers() throws Exception {
        // Given (給定) - 清空資料庫
        userRepository.deleteAll();
        todoItemRepository.deleteAll();
        
        // When (當) - 執行資料初始化
        dataInitializer.run();
        
        // Then (那麼) - 驗證待辦事項建立
        assertTrue(todoItemRepository.count() > 0);
        
        // 驗證每個使用者都有待辦事項
        User chienlin = userRepository.findByUsername("chienlin").orElseThrow();
        List<TodoItem> chienlinTodos = todoItemRepository.findByUserOrderByCreatedAtDesc(chienlin);
        assertTrue(chienlinTodos.size() > 5, "chienlin 應該有多筆待辦事項");
        
        User alice = userRepository.findByUsername("alice").orElseThrow();
        List<TodoItem> aliceTodos = todoItemRepository.findByUserOrderByCreatedAtDesc(alice);
        assertTrue(aliceTodos.size() > 3, "alice 應該有多筆待辦事項");
        
        User bob = userRepository.findByUsername("bob").orElseThrow();
        List<TodoItem> bobTodos = todoItemRepository.findByUserOrderByCreatedAtDesc(bob);
        assertTrue(bobTodos.size() > 3, "bob 應該有多筆待辦事項");
        
        User carol = userRepository.findByUsername("carol").orElseThrow();
        List<TodoItem> carolTodos = todoItemRepository.findByUserOrderByCreatedAtDesc(carol);
        assertTrue(carolTodos.size() > 3, "carol 應該有多筆待辦事項");
    }
    
    @Test
    @DisplayName("測試資料應該包含已完成和未完成的待辦事項")
    void test_run_whenCreatingTodos_then_shouldIncludeCompletedAndIncompleteTodos() throws Exception {
        // Given (給定) - 清空資料庫
        userRepository.deleteAll();
        todoItemRepository.deleteAll();
        
        // When (當) - 執行資料初始化
        dataInitializer.run();
        
        // Then (那麼) - 驗證待辦事項狀態
        List<TodoItem> allTodos = todoItemRepository.findAll();
        
        long completedCount = allTodos.stream().filter(TodoItem::isCompleted).count();
        long incompleteCount = allTodos.stream().filter(todo -> !todo.isCompleted()).count();
        
        assertTrue(completedCount > 0, "應該有已完成的待辦事項");
        assertTrue(incompleteCount > 0, "應該有未完成的待辦事項");
        
        // 驗證已完成的待辦事項有完成時間
        allTodos.stream()
            .filter(TodoItem::isCompleted)
            .forEach(todo -> assertNotNull(todo.getCompletedAt(), 
                "已完成的待辦事項應該有完成時間"));
        
        // 驗證未完成的待辦事項沒有完成時間
        allTodos.stream()
            .filter(todo -> !todo.isCompleted())
            .forEach(todo -> assertNull(todo.getCompletedAt(), 
                "未完成的待辦事項不應該有完成時間"));
    }
    
    @Test
    @DisplayName("如果資料庫已有資料應該跳過初始化")
    void test_run_whenDatabaseHasData_then_shouldSkipInitialization() throws Exception {
        // Given (給定) - 清空資料庫並建立一個使用者
        userRepository.deleteAll();
        todoItemRepository.deleteAll();
        
        User existingUser = new User("existing", "password");
        userRepository.save(existingUser);
        long initialUserCount = userRepository.count();
        
        // When (當) - 執行資料初始化
        dataInitializer.run();
        
        // Then (那麼) - 驗證沒有新增資料
        assertEquals(initialUserCount, userRepository.count());
        assertFalse(userRepository.existsByUsername("chienlin"));
        assertTrue(userRepository.existsByUsername("existing"));
    }
}