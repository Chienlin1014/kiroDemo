package com.course.kirodemo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User 實體類別
 * 代表系統中的使用者，包含基本資訊和與待辦事項的關聯關係
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TodoItem> todoItems = new ArrayList<>();
    
    // 預設建構子
    public User() {}
    
    // 建構子
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<TodoItem> getTodoItems() {
        return todoItems;
    }
    
    public void setTodoItems(List<TodoItem> todoItems) {
        this.todoItems = todoItems;
    }
    
    // 便利方法：新增待辦事項
    public void addTodoItem(TodoItem todoItem) {
        todoItems.add(todoItem);
        todoItem.setUser(this);
    }
    
    // 便利方法：移除待辦事項
    public void removeTodoItem(TodoItem todoItem) {
        todoItems.remove(todoItem);
        todoItem.setUser(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}