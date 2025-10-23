package com.course.kirodemo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 延期待辦事項的請求 DTO
 * 用於接收前端延期表單資料並進行驗證
 */
public class ExtendTodoRequest {
    
    @NotNull(message = "待辦事項ID不能為空")
    private Long todoId;
    
    @NotNull(message = "延期天數不能為空")
    @Min(value = 1, message = "延期天數必須為正數")
    @Max(value = 365, message = "延期天數不能超過365天")
    private Integer extensionDays;
    
    // 預設建構子
    public ExtendTodoRequest() {}
    
    // 建構子
    public ExtendTodoRequest(Long todoId, Integer extensionDays) {
        this.todoId = todoId;
        this.extensionDays = extensionDays;
    }
    
    // Getter 和 Setter 方法
    public Long getTodoId() {
        return todoId;
    }
    
    public void setTodoId(Long todoId) {
        this.todoId = todoId;
    }
    
    public Integer getExtensionDays() {
        return extensionDays;
    }
    
    public void setExtensionDays(Integer extensionDays) {
        this.extensionDays = extensionDays;
    }
    
    /**
     * 驗證延期請求是否有效
     * @return 如果所有必填欄位都有效則回傳 true
     */
    public boolean isValid() {
        return todoId != null && 
               extensionDays != null && 
               extensionDays > 0 && 
               extensionDays <= 365;
    }
    
    @Override
    public String toString() {
        return "ExtendTodoRequest{" +
                "todoId=" + todoId +
                ", extensionDays=" + extensionDays +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ExtendTodoRequest that = (ExtendTodoRequest) o;
        
        if (!todoId.equals(that.todoId)) return false;
        return extensionDays.equals(that.extensionDays);
    }
    
    @Override
    public int hashCode() {
        int result = todoId.hashCode();
        result = 31 * result + extensionDays.hashCode();
        return result;
    }
}