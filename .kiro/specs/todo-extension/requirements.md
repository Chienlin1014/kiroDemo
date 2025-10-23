# 需求文件

## 簡介

待辦事項延期功能是對現有 Todo List 應用程式的擴展，允許使用者對即將到期的未完成待辦事項進行延期操作。此功能專門針對三天內到期（不含超過三天）的待辦事項，提供彈性的延期管理機制。

## 詞彙表

- **Todo_Extension_System**: 待辦事項延期管理系統
- **Extension_Button**: 延期按鈕，顯示在符合條件的待辦事項上
- **Extension_Days**: 使用者指定的延期天數
- **Due_Date**: 待辦事項的預計完成日期
- **Extension_Service**: 處理延期邏輯的服務
- **Date_Validation_Service**: 處理日期驗證和計算的服務
- **Incomplete_Todo**: 尚未完成的待辦事項
- **Near_Due_Todo**: 三天內到期（不含超過三天）的待辦事項

## 需求

### 需求 1

**使用者故事:** 身為一個已登入的使用者，我想要看到即將到期的未完成待辦事項上有延期按鈕，這樣我就能識別哪些任務可以延期。

#### 驗收標準

1. WHEN 待辦事項的預計完成日期在三天內（包含今天、明天、後天），THE Todo_Extension_System SHALL 顯示延期按鈕
2. WHILE 待辦事項狀態為未完成，THE Extension_Button SHALL 在待辦事項列表中可見
3. WHILE 待辦事項狀態為已完成，THE Extension_Button SHALL 不顯示
4. WHEN 待辦事項的預計完成日期超過三天，THE Extension_Button SHALL 不顯示
5. THE Todo_Extension_System SHALL 每次載入待辦事項列表時重新計算延期按鈕的顯示條件

### 需求 2

**使用者故事:** 身為一個已登入的使用者，我想要點擊延期按鈕後能輸入或選擇延期天數，這樣我就能靈活調整待辦事項的到期日。

#### 驗收標準

1. WHEN 使用者點擊延期按鈕，THE Todo_Extension_System SHALL 顯示延期天數輸入介面
2. THE Todo_Extension_System SHALL 提供數字輸入欄位供使用者輸入延期天數
3. THE Todo_Extension_System SHALL 提供常用延期天數的快速選擇選項（如 1天、3天、7天、14天）
4. THE Todo_Extension_System SHALL 顯示當前到期日和計算後的新到期日預覽
5. THE Todo_Extension_System SHALL 提供確認和取消按鈕

### 需求 3

**使用者故事:** 身為一個已登入的使用者，我想要系統驗證我輸入的延期天數，這樣我就能確保輸入的是有效的正數。

#### 驗收標準

1. WHEN 使用者輸入負數作為延期天數，THE Date_Validation_Service SHALL 拒絕該輸入並顯示錯誤訊息
2. WHEN 使用者輸入零作為延期天數，THE Date_Validation_Service SHALL 拒絕該輸入並顯示錯誤訊息
3. WHEN 使用者輸入非數字內容，THE Date_Validation_Service SHALL 拒絕該輸入並顯示錯誤訊息
4. THE Date_Validation_Service SHALL 在前端和後端都進行輸入驗證
5. WHEN 輸入驗證失敗，THE Todo_Extension_System SHALL 保持延期介面開啟並顯示具體的錯誤訊息

### 需求 4

**使用者故事:** 身為一個已登入的使用者，我想要系統正確計算跨月的新到期日，這樣我就能確保延期後的日期是準確的。

#### 驗收標準

1. WHEN 延期後的新日期跨越月份邊界，THE Date_Validation_Service SHALL 正確處理月份和年份的變更
2. WHEN 延期涉及不同月份的天數差異（如2月28日延期到3月），THE Date_Validation_Service SHALL 正確計算實際日期
3. WHEN 延期涉及閏年的2月29日，THE Date_Validation_Service SHALL 正確處理閏年邏輯
4. THE Date_Validation_Service SHALL 使用 Java LocalDate API 確保日期計算的準確性
5. WHEN 計算新到期日，THE Extension_Service SHALL 驗證結果日期的有效性

### 需求 5

**使用者故事:** 身為一個已登入的使用者，我想要成功延期後看到更新的到期日，這樣我就能確認延期操作已生效。

#### 驗收標準

1. WHEN 使用者確認延期操作，THE Extension_Service SHALL 更新待辦事項的預計完成日期
2. WHEN 延期操作成功，THE Todo_Extension_System SHALL 關閉延期介面並刷新待辦事項列表
3. THE Extension_Service SHALL 記錄延期操作的時間戳記和延期天數
4. WHEN 延期後的待辦事項不再符合三天內到期條件，THE Extension_Button SHALL 不再顯示
5. THE Todo_Extension_System SHALL 顯示成功訊息確認延期操作已完成

### 需求 6

**使用者故事:** 身為一個已登入的使用者，我想要系統確保我只能延期自己的待辦事項，這樣我就能保護我的資料安全。

#### 驗收標準

1. THE Extension_Service SHALL 驗證當前登入使用者擁有待辦事項的修改權限
2. WHEN 使用者嘗試延期不屬於自己的待辦事項，THE Extension_Service SHALL 拒絕操作並返回未授權錯誤
3. THE Todo_Extension_System SHALL 在前端隱藏其他使用者待辦事項的延期按鈕
4. THE Extension_Service SHALL 使用 Spring Security 進行權限驗證
5. WHEN 權限驗證失敗，THE Todo_Extension_System SHALL 記錄安全事件並通知使用者

### 需求 7

**使用者故事:** 身為一個開發者，我想要延期功能有完整的單元測試覆蓋，這樣我就能確保功能的可靠性和維護性。

#### 驗收標準

1. THE Extension_Service SHALL 有涵蓋所有業務邏輯的單元測試
2. THE Date_Validation_Service SHALL 有涵蓋各種日期計算情境的單元測試
3. THE Todo_Extension_System SHALL 有涵蓋前端互動邏輯的整合測試
4. THE Extension_Service SHALL 有涵蓋錯誤處理和邊界條件的測試案例
5. THE Todo_Extension_System SHALL 達到至少 80% 的測試覆蓋率