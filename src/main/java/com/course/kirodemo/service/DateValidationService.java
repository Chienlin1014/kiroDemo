package com.course.kirodemo.service;

import java.time.LocalDate;

/**
 * 日期驗證服務介面
 * 提供延期功能相關的日期驗證和計算方法
 */
public interface DateValidationService {
    
    /**
     * 驗證延期天數（必須為正數）
     * 
     * @param days 延期天數
     * @return true 如果天數有效（正數），false 否則
     */
    boolean isValidExtensionDays(int days);
    
    /**
     * 計算新的到期日
     * 
     * @param currentDueDate 當前到期日
     * @param extensionDays 延期天數
     * @return 計算後的新到期日
     * @throws IllegalArgumentException 如果延期天數無效
     */
    LocalDate calculateNewDueDate(LocalDate currentDueDate, int extensionDays);
    
    /**
     * 檢查日期是否在指定天數內
     * 
     * @param dueDate 要檢查的日期
     * @param days 天數範圍
     * @return true 如果日期在指定天數內，false 否則
     */
    boolean isDueWithinDays(LocalDate dueDate, int days);
    
    /**
     * 驗證跨月日期計算的正確性
     * 
     * @param originalDate 原始日期
     * @param newDate 新日期
     * @param expectedDays 預期的天數差異
     * @return true 如果計算正確，false 否則
     */
    boolean validateCrossMonthCalculation(LocalDate originalDate, LocalDate newDate, int expectedDays);
}