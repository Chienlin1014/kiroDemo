package com.course.kirodemo.service;

import com.course.kirodemo.service.impl.DateValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DateValidationService 單元測試
 * 測試日期驗證和計算邏輯
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DateValidationService 單元測試")
class DateValidationServiceTest {
    
    private DateValidationService dateValidationService;
    
    @BeforeEach
    void setUp() {
        dateValidationService = new DateValidationServiceImpl();
    }
    
    @Test
    @DisplayName("正數延期天數應該通過驗證")
    void test_isValidExtensionDays_whenPositiveDays_then_shouldReturnTrue() {
        // Given (給定) - 設定正數延期天數
        int positiveDays = 5;
        
        // When (當) - 執行驗證
        boolean result = dateValidationService.isValidExtensionDays(positiveDays);
        
        // Then (那麼) - 驗證結果為 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("零延期天數應該不通過驗證")
    void test_isValidExtensionDays_whenZeroDays_then_shouldReturnFalse() {
        // Given (給定) - 設定零延期天數
        int zeroDays = 0;
        
        // When (當) - 執行驗證
        boolean result = dateValidationService.isValidExtensionDays(zeroDays);
        
        // Then (那麼) - 驗證結果為 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("負數延期天數應該不通過驗證")
    void test_isValidExtensionDays_whenNegativeDays_then_shouldReturnFalse() {
        // Given (給定) - 設定負數延期天數
        int negativeDays = -1;
        
        // When (當) - 執行驗證
        boolean result = dateValidationService.isValidExtensionDays(negativeDays);
        
        // Then (那麼) - 驗證結果為 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("計算新到期日應該正確加上延期天數")
    void test_calculateNewDueDate_whenValidInput_then_shouldCalculateCorrectly() {
        // Given (給定) - 設定當前日期和延期天數
        LocalDate currentDate = LocalDate.of(2024, 1, 15);
        int extensionDays = 5;
        
        // When (當) - 計算新到期日
        LocalDate newDate = dateValidationService.calculateNewDueDate(currentDate, extensionDays);
        
        // Then (那麼) - 驗證新日期正確
        LocalDate expectedDate = LocalDate.of(2024, 1, 20);
        assertEquals(expectedDate, newDate);
    }
    
    @Test
    @DisplayName("跨月延期應該正確計算新日期")
    void test_calculateNewDueDate_whenCrossMonth_then_shouldCalculateCorrectly() {
        // Given (給定) - 設定1月30日和5天延期
        LocalDate currentDate = LocalDate.of(2024, 1, 30);
        int extensionDays = 5;
        
        // When (當) - 計算新到期日
        LocalDate newDate = dateValidationService.calculateNewDueDate(currentDate, extensionDays);
        
        // Then (那麼) - 驗證跨月計算正確
        LocalDate expectedDate = LocalDate.of(2024, 2, 4);
        assertEquals(expectedDate, newDate);
    }
    
    @Test
    @DisplayName("閏年2月29日延期應該正確處理")
    void test_calculateNewDueDate_whenLeapYearFebruary29_then_shouldHandleCorrectly() {
        // Given (給定) - 設定閏年2月29日和1天延期
        LocalDate leapYearDate = LocalDate.of(2024, 2, 29); // 2024年是閏年
        int extensionDays = 1;
        
        // When (當) - 計算新到期日
        LocalDate newDate = dateValidationService.calculateNewDueDate(leapYearDate, extensionDays);
        
        // Then (那麼) - 驗證閏年處理正確
        LocalDate expectedDate = LocalDate.of(2024, 3, 1);
        assertEquals(expectedDate, newDate);
    }
    
    @Test
    @DisplayName("當前到期日為空時應該拋出異常")
    void test_calculateNewDueDate_whenCurrentDueDateIsNull_then_shouldThrowException() {
        // Given (給定) - 設定空的當前日期
        LocalDate currentDate = null;
        int extensionDays = 5;
        
        // When & Then (當且那麼) - 驗證拋出異常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> dateValidationService.calculateNewDueDate(currentDate, extensionDays));
        
        assertEquals("當前到期日不能為空", exception.getMessage());
    }
    
    @Test
    @DisplayName("延期天數為負數時應該拋出異常")
    void test_calculateNewDueDate_whenNegativeExtensionDays_then_shouldThrowException() {
        // Given (給定) - 設定有效日期和負數延期天數
        LocalDate currentDate = LocalDate.of(2024, 1, 15);
        int negativeDays = -1;
        
        // When & Then (當且那麼) - 驗證拋出異常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> dateValidationService.calculateNewDueDate(currentDate, negativeDays));
        
        assertEquals("延期天數必須為正數", exception.getMessage());
    }
    
    @Test
    @DisplayName("今天到期的待辦事項應該在三天內")
    void test_isDueWithinDays_whenDueToday_then_shouldReturnTrue() {
        // Given (給定) - 設定今天的日期
        LocalDate today = LocalDate.now();
        int days = 3;
        
        // When (當) - 檢查是否在三天內
        boolean result = dateValidationService.isDueWithinDays(today, days);
        
        // Then (那麼) - 驗證結果為 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("明天到期的待辦事項應該在三天內")
    void test_isDueWithinDays_whenDueTomorrow_then_shouldReturnTrue() {
        // Given (給定) - 設定明天的日期
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        int days = 3;
        
        // When (當) - 檢查是否在三天內
        boolean result = dateValidationService.isDueWithinDays(tomorrow, days);
        
        // Then (那麼) - 驗證結果為 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("三天後到期的待辦事項應該在三天內")
    void test_isDueWithinDays_whenDueInThreeDays_then_shouldReturnTrue() {
        // Given (給定) - 設定三天後的日期
        LocalDate threeDaysLater = LocalDate.now().plusDays(3);
        int days = 3;
        
        // When (當) - 檢查是否在三天內
        boolean result = dateValidationService.isDueWithinDays(threeDaysLater, days);
        
        // Then (那麼) - 驗證結果為 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("四天後到期的待辦事項不應該在三天內")
    void test_isDueWithinDays_whenDueInFourDays_then_shouldReturnFalse() {
        // Given (給定) - 設定四天後的日期
        LocalDate fourDaysLater = LocalDate.now().plusDays(4);
        int days = 3;
        
        // When (當) - 檢查是否在三天內
        boolean result = dateValidationService.isDueWithinDays(fourDaysLater, days);
        
        // Then (那麼) - 驗證結果為 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("昨天到期的待辦事項不應該在三天內")
    void test_isDueWithinDays_whenDueYesterday_then_shouldReturnFalse() {
        // Given (給定) - 設定昨天的日期
        LocalDate yesterday = LocalDate.now().minusDays(1);
        int days = 3;
        
        // When (當) - 檢查是否在三天內
        boolean result = dateValidationService.isDueWithinDays(yesterday, days);
        
        // Then (那麼) - 驗證結果為 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("到期日為空時應該返回 false")
    void test_isDueWithinDays_whenDueDateIsNull_then_shouldReturnFalse() {
        // Given (給定) - 設定空的到期日
        LocalDate dueDate = null;
        int days = 3;
        
        // When (當) - 檢查是否在三天內
        boolean result = dateValidationService.isDueWithinDays(dueDate, days);
        
        // Then (那麼) - 驗證結果為 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("跨月日期計算驗證應該正確")
    void test_validateCrossMonthCalculation_whenValidCalculation_then_shouldReturnTrue() {
        // Given (給定) - 設定原始日期、新日期和預期天數
        LocalDate originalDate = LocalDate.of(2024, 1, 30);
        LocalDate newDate = LocalDate.of(2024, 2, 4);
        int expectedDays = 5;
        
        // When (當) - 驗證跨月計算
        boolean result = dateValidationService.validateCrossMonthCalculation(originalDate, newDate, expectedDays);
        
        // Then (那麼) - 驗證結果為 true
        assertTrue(result);
    }
    
    @Test
    @DisplayName("跨月日期計算驗證錯誤時應該返回 false")
    void test_validateCrossMonthCalculation_whenInvalidCalculation_then_shouldReturnFalse() {
        // Given (給定) - 設定原始日期、新日期和錯誤的預期天數
        LocalDate originalDate = LocalDate.of(2024, 1, 30);
        LocalDate newDate = LocalDate.of(2024, 2, 4);
        int wrongExpectedDays = 3; // 實際是5天，但預期3天
        
        // When (當) - 驗證跨月計算
        boolean result = dateValidationService.validateCrossMonthCalculation(originalDate, newDate, wrongExpectedDays);
        
        // Then (那麼) - 驗證結果為 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("原始日期為空時驗證應該返回 false")
    void test_validateCrossMonthCalculation_whenOriginalDateIsNull_then_shouldReturnFalse() {
        // Given (給定) - 設定空的原始日期
        LocalDate originalDate = null;
        LocalDate newDate = LocalDate.of(2024, 2, 4);
        int expectedDays = 5;
        
        // When (當) - 驗證跨月計算
        boolean result = dateValidationService.validateCrossMonthCalculation(originalDate, newDate, expectedDays);
        
        // Then (那麼) - 驗證結果為 false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("新日期為空時驗證應該返回 false")
    void test_validateCrossMonthCalculation_whenNewDateIsNull_then_shouldReturnFalse() {
        // Given (給定) - 設定空的新日期
        LocalDate originalDate = LocalDate.of(2024, 1, 30);
        LocalDate newDate = null;
        int expectedDays = 5;
        
        // When (當) - 驗證跨月計算
        boolean result = dateValidationService.validateCrossMonthCalculation(originalDate, newDate, expectedDays);
        
        // Then (那麼) - 驗證結果為 false
        assertFalse(result);
    }
}