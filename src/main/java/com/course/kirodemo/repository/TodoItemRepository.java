package com.course.kirodemo.repository;

import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TodoItemRepository 介面
 * 提供 TodoItem 實體的資料存取操作，包含自定義查詢方法
 */
@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
    
    /**
     * 根據使用者查詢所有待辦事項，依建立時間排序（最新的在前）
     * @param user 使用者實體
     * @return 待辦事項列表
     */
    List<TodoItem> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 根據使用者查詢所有待辦事項，依建立時間排序（最舊的在前）
     * @param user 使用者實體
     * @return 待辦事項列表
     */
    List<TodoItem> findByUserOrderByCreatedAtAsc(User user);
    
    /**
     * 根據使用者查詢所有待辦事項，依預計完成日排序（最近的在前）
     * @param user 使用者實體
     * @return 待辦事項列表
     */
    List<TodoItem> findByUserOrderByDueDateAsc(User user);
    
    /**
     * 根據使用者查詢所有待辦事項，依預計完成日排序（最遠的在前）
     * @param user 使用者實體
     * @return 待辦事項列表
     */
    List<TodoItem> findByUserOrderByDueDateDesc(User user);
    
    /**
     * 根據使用者和完成狀態查詢待辦事項
     * @param user 使用者實體
     * @param completed 完成狀態
     * @return 待辦事項列表
     */
    List<TodoItem> findByUserAndCompleted(User user, boolean completed);
    
    /**
     * 根據使用者和完成狀態查詢待辦事項，依建立時間排序
     * @param user 使用者實體
     * @param completed 完成狀態
     * @return 待辦事項列表
     */
    List<TodoItem> findByUserAndCompletedOrderByCreatedAtDesc(User user, boolean completed);
    
    /**
     * 根據使用者查詢特定的待辦事項
     * @param id 待辦事項 ID
     * @param user 使用者實體
     * @return 待辦事項的 Optional 包裝
     */
    Optional<TodoItem> findByIdAndUser(Long id, User user);
    
    /**
     * 查詢使用者的逾期待辦事項（未完成且預計完成日已過）
     * @param user 使用者實體
     * @param currentDate 當前日期
     * @return 逾期待辦事項列表
     */
    @Query("SELECT t FROM TodoItem t WHERE t.user = :user AND t.completed = false AND t.dueDate < :currentDate ORDER BY t.dueDate ASC")
    List<TodoItem> findOverdueTodosByUser(@Param("user") User user, @Param("currentDate") LocalDate currentDate);
    
    /**
     * 查詢使用者即將到期的待辦事項（未完成且預計完成日在指定天數內）
     * @param user 使用者實體
     * @param startDate 開始日期（通常是今天）
     * @param endDate 結束日期（通常是今天加上指定天數）
     * @return 即將到期的待辦事項列表
     */
    @Query("SELECT t FROM TodoItem t WHERE t.user = :user AND t.completed = false AND t.dueDate BETWEEN :startDate AND :endDate ORDER BY t.dueDate ASC")
    List<TodoItem> findDueSoonTodosByUser(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 統計使用者的待辦事項數量
     * @param user 使用者實體
     * @return 待辦事項總數
     */
    long countByUser(User user);
    
    /**
     * 統計使用者已完成的待辦事項數量
     * @param user 使用者實體
     * @return 已完成的待辦事項數量
     */
    long countByUserAndCompleted(User user, boolean completed);
}