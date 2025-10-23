# 實作計畫

- [x] 1. 擴展 TodoItem 實體以支援延期功能





  - 在 TodoItem 實體中新增延期相關欄位（extension_count, last_extended_at, original_due_date）
  - 實作 isEligibleForExtension() 業務邏輯方法，檢查待辦事項是否符合延期條件（未完成且三天內到期）
  - 實作 extendDueDate(int days) 方法，處理延期操作並更新相關欄位
  - 實作 getTotalExtensionDays() 方法，計算總延期天數
  - 新增適當的驗證邏輯，確保延期天數為正數
  - _需求: 1.1, 1.2, 2.1, 4.1, 5.1_

- [x] 2. 建立日期驗證服務





  - [x] 2.1 建立 DateValidationService 介面和實作類別


    - 定義 isValidExtensionDays(int days) 方法驗證延期天數
    - 定義 calculateNewDueDate(LocalDate currentDueDate, int extensionDays) 方法計算新到期日
    - 定義 isDueWithinDays(LocalDate dueDate, int days) 方法檢查日期範圍
    - 實作跨月份和閏年的日期計算邏輯
    - _需求: 3.1, 3.2, 4.1, 4.2, 4.3, 4.4, 4.5_

  - [x] 2.2 為 DateValidationService 撰寫單元測試



    - 測試正數延期天數驗證
    - 測試零和負數延期天數的拒絕邏輯
    - 測試跨月份日期計算的正確性
    - 測試閏年 2 月 29 日的特殊情況
    - 測試三天內到期的判斷邏輯
    - _需求: 7.2_
-

- [x] 3. 建立待辦事項延期服務



  - [x] 3.1 建立 TodoExtensionService 介面和實作類別


    - 實作 isEligibleForExtension(TodoItem todoItem) 方法
    - 實作 extendTodo(Long todoId, int extensionDays, String username) 方法
    - 實作 getEligibleTodosForUser(String username) 方法
    - 實作 validateExtensionDays(int extensionDays) 方法
    - 整合 DateValidationService 進行日期計算
    - 整合現有的 TodoService 和 Repository 進行資料操作
    - _需求: 2.1, 2.2, 3.1, 5.1, 5.2, 6.1, 6.2_

  - [x] 3.2 為 TodoExtensionService 撰寫單元測試


    - 測試符合延期條件的待辦事項識別
    - 測試已完成待辦事項的延期拒絕
    - 測試負數延期天數的異常處理
    - 測試成功延期後的資料更新
    - 測試使用者權限驗證
    - 使用 Mockito 模擬依賴服務
    - _需求: 7.1_

- [x] 4. 擴展資料存取層







  - [x] 4.1 更新 TodoItemRepository 介面


    - 新增 findEligibleForExtensionByUsername 查詢方法
    - 新增 findByUserAndDueDateBetween 查詢方法
    - 新增 countTotalExtensionsByUsername 統計方法
    - 使用 @Query 註解實作自定義 JPA 查詢
    - _需求: 1.1, 1.4_


  - [x] 4.2 為 Repository 查詢方法撰寫測試




    - 測試符合延期條件的待辦事項查詢
    - 測試日期範圍查詢的正確性
    - 測試延期次數統計功能
    - 使用 @DataJpaTest 進行資料庫整合測試
    - _需求: 7.3_
-

- [x] 5. 建立資料傳輸物件




  - 建立 ExtendTodoRequest DTO 類別，包含 todoId 和 extensionDays 欄位
  - 建立 ExtendTodoResponse DTO 類別，包含成功狀態、訊息和新到期日資訊
  - 新增適當的 Bean Validation 註解進行輸入驗證
  - 實作建構子、getter 和 setter 方法
  - _需求: 2.1, 2.2, 3.1, 3.2, 3.3_

- [x] 6. 擴展控制器層





  - [x] 6.1 在 TodoController 中新增延期相關端點


    - 新增 GET /todos/{id}/extend 端點取得延期表單資料
    - 新增 POST /todos/{id}/extend 端點處理延期請求
    - 新增 GET /todos/{id}/extend/preview 端點提供延期預覽
    - 實作適當的錯誤處理和回應格式
    - 整合 Spring Security 進行使用者認證
    - _需求: 2.1, 2.2, 5.1, 5.2, 6.1, 6.2_

  - [x] 6.2 為控制器端點撰寫測試



    - 測試成功延期請求的處理
    - 測試無效輸入的錯誤回應
    - 測試未授權存取的拒絕
    - 測試延期預覽功能
    - 使用 @WebMvcTest 和 MockMvc 進行測試
    - _需求: 7.3_

- [x] 7. 建立自定義異常類別




  - 建立 InvalidExtensionException 異常類別處理無效延期請求
  - 建立 ExtensionNotAllowedException 異常類別處理不允許的延期操作
  - 在 GlobalExceptionHandler 中新增異常處理方法
  - 實作統一的錯誤回應格式
  - _需求: 3.3, 3.4, 6.2_

- [ ] 8. 更新資料庫 Schema
  - 建立資料庫遷移腳本，新增延期相關欄位到 todo_items 表
  - 新增適當的索引以優化查詢效能
  - 更新現有資料，設定 original_due_date 為當前的 due_date
  - 確保資料庫約束和預設值的正確設定
  - _需求: 5.3_

- [x] 9. 實作前端延期介面





  - [x] 9.1 更新待辦事項列表模板


    - 在 todos/list.html 中新增延期按鈕
    - 實作按鈕顯示邏輯，只在符合條件的未完成待辦事項上顯示
    - 新增延期模態框的 HTML 結構
    - 包含延期天數輸入欄位和快速選擇按鈕
    - 新增日期預覽和錯誤訊息顯示區域
    - _需求: 1.1, 1.2, 1.3, 2.1, 2.2_


  - [x] 9.2 實作 JavaScript 延期功能

    - 建立 TodoExtensionManager 類別處理前端邏輯
    - 實作延期按鈕點擊事件處理
    - 實作快速選擇按鈕功能
    - 實作延期天數輸入的即時預覽
    - 實作 AJAX 延期請求和回應處理
    - 新增輸入驗證和錯誤訊息顯示
    - _需求: 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 3.3_


  - [x] 9.3 新增 CSS 樣式

    - 設計延期按鈕的視覺樣式
    - 實作快速選擇按鈕群組的樣式
    - 新增日期顯示和錯誤訊息的樣式
    - 優化模態框的視覺呈現
    - _需求: 1.1, 2.1_

- [ ] 10. 整合測試和端到端測試
  - [ ]* 10.1 建立完整的延期功能整合測試
    - 測試從前端請求到後端處理的完整流程
    - 測試資料庫資料的正確更新
    - 測試錯誤情況的處理
    - 使用 @SpringBootTest 進行完整的應用程式測試
    - _需求: 7.3_

  - [ ]* 10.2 更新現有的使用者旅程測試
    - 在 CompleteUserJourneyTest 中新增延期功能的測試案例
    - 測試使用者從登入到成功延期待辦事項的完整流程
    - 驗證延期後的資料正確性和頁面更新
    - _需求: 7.3_

- [ ] 11. 文件更新和部署準備
  - 更新 API 文件，記錄新增的延期端點
  - 更新使用者手冊，說明延期功能的使用方法
  - 檢查並更新應用程式配置檔案
  - 準備部署腳本和資料庫遷移檔案
  - _需求: 5.4, 5.5_