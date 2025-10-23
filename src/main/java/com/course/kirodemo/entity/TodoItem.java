package com.course.kirodemo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * TodoItem 實體類別
 * 代表使用者的待辦事項，包含標題、描述、完成狀態等資訊
 */
@Entity
@Table(name = "todo_items", indexes = {
    @Index(name = "idx_todo_user_id", columnList = "user_id"),
    @Index(name = "idx_todo_created_at", columnList = "created_at"),
    @Index(name = "idx_todo_due_date", columnList = "due_date")
})
public class TodoItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "標題不能為空")
    @Size(max = 255, message = "標題長度不能超過255字元")
    @Column(nullable = false)
    private String title;
    
    @Size(max = 1000, message = "描述長度不能超過1000字元")
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private boolean completed = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @NotNull(message = "預計完成日不能為空")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * 延期次數記錄
     */
    @Column(name = "extension_count", nullable = false)
    private int extensionCount = 0;
    
    /**
     * 最後延期時間
     */
    @Column(name = "last_extended_at")
    private LocalDateTime lastExtendedAt;
    
    /**
     * 原始到期日（首次設定的到期日）
     */
    @Column(name = "original_due_date")
    private LocalDate originalDueDate;
    
    // 預設建構子
    public TodoItem() {}
    
    // 建構子
    public TodoItem(String title, String description, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }
    
    // 建構子（包含使用者）
    public TodoItem(String title, String description, LocalDate dueDate, User user) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.user = user;
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        // 當設定為完成時，自動設定完成時間
        if (completed && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!completed) {
            this.completedAt = null;
        }
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public int getExtensionCount() {
        return extensionCount;
    }
    
    public void setExtensionCount(int extensionCount) {
        this.extensionCount = extensionCount;
    }
    
    public LocalDateTime getLastExtendedAt() {
        return lastExtendedAt;
    }
    
    public void setLastExtendedAt(LocalDateTime lastExtendedAt) {
        this.lastExtendedAt = lastExtendedAt;
    }
    
    public LocalDate getOriginalDueDate() {
        return originalDueDate;
    }
    
    public void setOriginalDueDate(LocalDate originalDueDate) {
        this.originalDueDate = originalDueDate;
    }
    
    // 業務邏輯方法：標記為完成
    public void markAsCompleted() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }
    
    // 業務邏輯方法：標記為未完成
    public void markAsIncomplete() {
        this.completed = false;
        this.completedAt = null;
    }
    
    // 業務邏輯方法：切換完成狀態
    public void toggleCompleted() {
        if (this.completed) {
            markAsIncomplete();
        } else {
            markAsCompleted();
        }
    }
    
    // 業務邏輯方法：檢查是否逾期
    public boolean isOverdue() {
        if (completed) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }
    
    // 業務邏輯方法：檢查是否即將到期（3天內）
    public boolean isDueSoon() {
        if (completed) {
            return false;
        }
        LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);
        return dueDate.isBefore(threeDaysFromNow) || dueDate.isEqual(threeDaysFromNow);
    }
    
    /**
     * 檢查是否符合延期條件（未完成且三天內到期）
     * @return true 如果符合延期條件，false 否則
     */
    public boolean isEligibleForExtension() {
        return !this.completed && 
               this.dueDate != null && 
               this.dueDate.isAfter(LocalDate.now().minusDays(1)) && 
               this.dueDate.isBefore(LocalDate.now().plusDays(4));
    }
    
    /**
     * 執行延期操作
     * @param days 延期天數，必須為正數
     * @throws IllegalArgumentException 如果延期天數不是正數
     */
    public void extendDueDate(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("延期天數必須為正數");
        }
        
        // 首次延期時記錄原始到期日
        if (this.originalDueDate == null) {
            this.originalDueDate = this.dueDate;
        }
        
        this.dueDate = this.dueDate.plusDays(days);
        this.extensionCount++;
        this.lastExtendedAt = LocalDateTime.now();
    }
    
    /**
     * 取得總延期天數
     * @return 總延期天數，如果沒有延期過則返回0
     */
    public long getTotalExtensionDays() {
        if (originalDueDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(originalDueDate, dueDate);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TodoItem)) return false;
        TodoItem todoItem = (TodoItem) o;
        return id != null && id.equals(todoItem.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "TodoItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", createdAt=" + createdAt +
                ", dueDate=" + dueDate +
                ", completedAt=" + completedAt +
                '}';
    }
}