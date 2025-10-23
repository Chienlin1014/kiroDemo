package com.course.kirodemo.dto;

import java.time.LocalDate;

/**
 * 延期待辦事項的回應 DTO
 * 用於回傳延期操作的結果資訊
 */
public class ExtendTodoResponse {
    
    private boolean success;
    private String message;
    private LocalDate newDueDate;
    private LocalDate originalDueDate;
    private Integer totalExtensionDays;
    private Long todoId;
    
    // 預設建構子
    public ExtendTodoResponse() {}
    
    // 建構子 - 僅包含基本回應資訊
    public ExtendTodoResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // 建構子 - 包含完整延期資訊
    public ExtendTodoResponse(boolean success, String message, 
                            LocalDate newDueDate, LocalDate originalDueDate, 
                            Integer totalExtensionDays) {
        this.success = success;
        this.message = message;
        this.newDueDate = newDueDate;
        this.originalDueDate = originalDueDate;
        this.totalExtensionDays = totalExtensionDays;
    }
    
    // 建構子 - 包含所有資訊
    public ExtendTodoResponse(boolean success, String message, Long todoId,
                            LocalDate newDueDate, LocalDate originalDueDate, 
                            Integer totalExtensionDays) {
        this.success = success;
        this.message = message;
        this.todoId = todoId;
        this.newDueDate = newDueDate;
        this.originalDueDate = originalDueDate;
        this.totalExtensionDays = totalExtensionDays;
    }
    
    // Getter 和 Setter 方法
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDate getNewDueDate() {
        return newDueDate;
    }
    
    public void setNewDueDate(LocalDate newDueDate) {
        this.newDueDate = newDueDate;
    }
    
    public LocalDate getOriginalDueDate() {
        return originalDueDate;
    }
    
    public void setOriginalDueDate(LocalDate originalDueDate) {
        this.originalDueDate = originalDueDate;
    }
    
    public Integer getTotalExtensionDays() {
        return totalExtensionDays;
    }
    
    public void setTotalExtensionDays(Integer totalExtensionDays) {
        this.totalExtensionDays = totalExtensionDays;
    }
    
    public Long getTodoId() {
        return todoId;
    }
    
    public void setTodoId(Long todoId) {
        this.todoId = todoId;
    }
    
    /**
     * 建立成功回應的靜態工廠方法
     * @param message 成功訊息
     * @param todoId 待辦事項ID
     * @param newDueDate 新到期日
     * @param originalDueDate 原始到期日
     * @param totalExtensionDays 總延期天數
     * @return 成功的 ExtendTodoResponse
     */
    public static ExtendTodoResponse success(String message, Long todoId,
                                           LocalDate newDueDate, LocalDate originalDueDate, 
                                           Integer totalExtensionDays) {
        return new ExtendTodoResponse(true, message, todoId, newDueDate, originalDueDate, totalExtensionDays);
    }
    
    /**
     * 建立失敗回應的靜態工廠方法
     * @param message 錯誤訊息
     * @return 失敗的 ExtendTodoResponse
     */
    public static ExtendTodoResponse failure(String message) {
        return new ExtendTodoResponse(false, message);
    }
    
    /**
     * 建立失敗回應的靜態工廠方法（包含待辦事項ID）
     * @param message 錯誤訊息
     * @param todoId 待辦事項ID
     * @return 失敗的 ExtendTodoResponse
     */
    public static ExtendTodoResponse failure(String message, Long todoId) {
        ExtendTodoResponse response = new ExtendTodoResponse(false, message);
        response.setTodoId(todoId);
        return response;
    }
    
    @Override
    public String toString() {
        return "ExtendTodoResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", todoId=" + todoId +
                ", newDueDate=" + newDueDate +
                ", originalDueDate=" + originalDueDate +
                ", totalExtensionDays=" + totalExtensionDays +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ExtendTodoResponse that = (ExtendTodoResponse) o;
        
        if (success != that.success) return false;
        if (!message.equals(that.message)) return false;
        if (todoId != null ? !todoId.equals(that.todoId) : that.todoId != null) return false;
        if (newDueDate != null ? !newDueDate.equals(that.newDueDate) : that.newDueDate != null) return false;
        if (originalDueDate != null ? !originalDueDate.equals(that.originalDueDate) : that.originalDueDate != null) return false;
        return totalExtensionDays != null ? totalExtensionDays.equals(that.totalExtensionDays) : that.totalExtensionDays == null;
    }
    
    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + message.hashCode();
        result = 31 * result + (todoId != null ? todoId.hashCode() : 0);
        result = 31 * result + (newDueDate != null ? newDueDate.hashCode() : 0);
        result = 31 * result + (originalDueDate != null ? originalDueDate.hashCode() : 0);
        result = 31 * result + (totalExtensionDays != null ? totalExtensionDays.hashCode() : 0);
        return result;
    }
}