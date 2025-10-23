package com.course.kirodemo.service.impl;

import com.course.kirodemo.service.DateValidationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 日期驗證服務實作類別
 * 實作延期功能相關的日期驗證和計算邏輯
 */
@Service
public class DateValidationServiceImpl implements DateValidationService {
    
    @Override
    public boolean isValidExtensionDays(int days) {
        return days > 0;
    }
    
    @Override
    public LocalDate calculateNewDueDate(LocalDate currentDueDate, int extensionDays) {
        if (currentDueDate == null) {
            throw new IllegalArgumentException("當前到期日不能為空");
        }
        
        if (!isValidExtensionDays(extensionDays)) {
            throw new IllegalArgumentException("延期天數必須為正數");
        }
        
        return currentDueDate.plusDays(extensionDays);
    }
    
    @Override
    public boolean isDueWithinDays(LocalDate dueDate, int days) {
        if (dueDate == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(days);
        
        // 檢查日期是否在今天到指定天數後的範圍內（包含邊界）
        return !dueDate.isBefore(today) && !dueDate.isAfter(targetDate);
    }
    
    @Override
    public boolean validateCrossMonthCalculation(LocalDate originalDate, LocalDate newDate, int expectedDays) {
        if (originalDate == null || newDate == null) {
            return false;
        }
        
        // 計算實際的天數差異
        long actualDays = ChronoUnit.DAYS.between(originalDate, newDate);
        
        return actualDays == expectedDays;
    }
}