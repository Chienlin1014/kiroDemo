package com.course.kirodemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;

import java.time.LocalDate;

/**
 * 建立待辦事項的請求 DTO
 * 用於接收前端表單資料並進行驗證
 */
public class CreateTodoRequest {
    
    @NotBlank(message = "標題不能為空")
    @Size(max = 255, message = "標題長度不能超過255字元")
    private String title;
    
    @Size(max = 1000, message = "描述長度不能超過1000字元")
    private String description;
    
    @NotNull(message = "預計完成日不能為空")
    private LocalDate dueDate;
    
    // 預設建構子
    public CreateTodoRequest() {}
    
    // 建構子
    public CreateTodoRequest(String title, String description, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }
    
    // Getter 和 Setter 方法
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
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    /**
     * 轉換為 TodoItem 實體
     * @param user 關聯的使用者
     * @return TodoItem 實體
     */
    public TodoItem toEntity(User user) {
        TodoItem todoItem = new TodoItem();
        todoItem.setTitle(this.title);
        todoItem.setDescription(this.description);
        todoItem.setDueDate(this.dueDate);
        todoItem.setUser(user);
        return todoItem;
    }
    
    /**
     * 從 TodoItem 實體建立 DTO（用於編輯表單預填）
     * @param todoItem TodoItem 實體
     * @return CreateTodoRequest DTO
     */
    public static CreateTodoRequest fromEntity(TodoItem todoItem) {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle(todoItem.getTitle());
        request.setDescription(todoItem.getDescription());
        request.setDueDate(todoItem.getDueDate());
        return request;
    }
    
    @Override
    public String toString() {
        return "CreateTodoRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                '}';
    }
}