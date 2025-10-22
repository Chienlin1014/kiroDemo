package com.course.kirodemo.repository;

import com.course.kirodemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository 介面
 * 提供 User 實體的資料存取操作
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根據使用者名稱查詢使用者
     * @param username 使用者名稱
     * @return 使用者實體的 Optional 包裝
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 檢查使用者名稱是否已存在
     * @param username 使用者名稱
     * @return 如果存在則回傳 true，否則回傳 false
     */
    boolean existsByUsername(String username);
}