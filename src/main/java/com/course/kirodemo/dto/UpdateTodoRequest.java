package com.course.kirodemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.course.kirodemo.entity.TodoItem;

import java.time.LocalDate;

/**
 * 更新待辦事項的請求 DTO
 * 用於接收前端編輯表單資料並進行驗證
 */
public class UpdateTodoRequest {
    
    @NotBlank(message = "標題不能為空")
    @Size(max = 255, message = "標題長度不能超過255字元")
    private String title;
    
    @Size(max = 1000, message = "描述長度不能超過1000字元")
    private String description;
    
    @NotNull(message = "預計完成日不能為空")
    private LocalDate dueDate;
    
    // 預設建構子
    public UpdateTodoRequest() {}
    
    // 建構子
    public UpdateTodoRequest(String title, String description, LocalDate dueDate) {
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
     * 更新 TodoItem 實體的資料
     * 注意：不更新 id、createdAt、completed、completedAt、user 等欄位
     * @param todoItem 要更新的 TodoItem 實體
     */
    public void updateEntity(TodoItem todoItem) {
        todoItem.setTitle(this.title);
        todoItem.setDescription(this.description);
        todoItem.setDueDate(this.dueDate);
    }
    
    /**
     * 從 TodoItem 實體建立 DTO（用於編輯表單預填）
     * @param todoItem TodoItem 實體
     * @return UpdateTodoRequest DTO
     */
    public static UpdateTodoRequest fromEntity(TodoItem todoItem) {
        UpdateTodoRequest request = new UpdateTodoRequest();
        request.setTitle(todoItem.getTitle());
        request.setDescription(todoItem.getDescription());
        request.setDueDate(todoItem.getDueDate());
        return request;
    }
    
    /**
     * 驗證更新請求是否有效
     * @return 如果所有必填欄位都有值則回傳 true
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() && 
               dueDate != null &&
               (description == null || description.length() <= 1000) &&
               title.length() <= 255;
    }
    
    @Override
    public String toString() {
        return "UpdateTodoRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                '}';
    }
}