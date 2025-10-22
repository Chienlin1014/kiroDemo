package com.course.kirodemo.repository;

import com.course.kirodemo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository 整合測試
 * 使用 @DataJpaTest 測試資料存取邏輯
 */
@DataJpaTest
@DisplayName("UserRepository 整合測試")
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Given - 準備測試資料
        testUser = new User("testuser", "password123");
    }
    
    @Test
    @DisplayName("根據使用者名稱查詢使用者時應該回傳正確的使用者")
    void test_findByUsername_whenUserExists_then_shouldReturnUser() {
        // Given - 儲存測試使用者到資料庫
        entityManager.persistAndFlush(testUser);
        
        // When - 根據使用者名稱查詢
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        
        // Then - 驗證查詢結果
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getPassword()).isEqualTo("password123");
        assertThat(foundUser.get().getId()).isNotNull();
        assertThat(foundUser.get().getCreatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("查詢不存在的使用者名稱時應該回傳空的 Optional")
    void test_findByUsername_whenUserNotExists_then_shouldReturnEmpty() {
        // Given - 資料庫中沒有使用者
        
        // When - 查詢不存在的使用者名稱
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");
        
        // Then - 驗證回傳空的 Optional
        assertThat(foundUser).isEmpty();
    }
    
    @Test
    @DisplayName("檢查存在的使用者名稱時應該回傳 true")
    void test_existsByUsername_whenUserExists_then_shouldReturnTrue() {
        // Given - 儲存測試使用者到資料庫
        entityManager.persistAndFlush(testUser);
        
        // When - 檢查使用者名稱是否存在
        boolean exists = userRepository.existsByUsername("testuser");
        
        // Then - 驗證回傳 true
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("檢查不存在的使用者名稱時應該回傳 false")
    void test_existsByUsername_whenUserNotExists_then_shouldReturnFalse() {
        // Given - 資料庫中沒有使用者
        
        // When - 檢查不存在的使用者名稱
        boolean exists = userRepository.existsByUsername("nonexistent");
        
        // Then - 驗證回傳 false
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("儲存使用者時應該自動產生 ID 和建立時間")
    void test_save_whenValidUser_then_shouldGenerateIdAndCreatedAt() {
        // Given - 準備新使用者
        User newUser = new User("newuser", "newpassword");
        
        // When - 儲存使用者
        User savedUser = userRepository.save(newUser);
        
        // Then - 驗證自動產生的欄位
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("newuser");
        assertThat(savedUser.getPassword()).isEqualTo("newpassword");
    }
    
    @Test
    @DisplayName("查詢所有使用者時應該回傳正確的數量")
    void test_findAll_whenMultipleUsers_then_shouldReturnCorrectCount() {
        // Given - 儲存多個使用者
        User user1 = new User("user1", "password1");
        User user2 = new User("user2", "password2");
        User user3 = new User("user3", "password3");
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);
        
        // When - 查詢所有使用者
        long count = userRepository.count();
        
        // Then - 驗證使用者數量
        assertThat(count).isEqualTo(3);
    }
    
    @Test
    @DisplayName("刪除使用者時應該從資料庫中移除")
    void test_delete_whenUserExists_then_shouldRemoveFromDatabase() {
        // Given - 儲存測試使用者
        User savedUser = entityManager.persistAndFlush(testUser);
        Long userId = savedUser.getId();
        
        // When - 刪除使用者
        userRepository.delete(savedUser);
        entityManager.flush();
        
        // Then - 驗證使用者已被刪除
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }
}