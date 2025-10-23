# 實作計畫

- [x] 1. 設定專案依賴和基礎配置





  - 更新 pom.xml 添加必要的 Spring Boot 依賴（Thymeleaf、Spring Session、Security、Validation）
  - 建立 application.yml 配置檔案，設定 H2 資料庫和 Spring Session
  - 配置 H2 Console 和 JPA 設定
  - _需求: 7.1, 7.4, 7.5_

- [x] 2. 建立領域實體和資料模型





  - 建立 User 實體類別，包含 JPA 註解和關聯關係
  - 建立 TodoItem 實體類別，包含驗證規則和時間戳記
  - 定義實體間的雙向關聯關係
  - _需求: 1.3, 2.1, 2.2, 2.4_

- [x] 2.1 撰寫實體類別的單元測試






  - 測試 User 實體的驗證規則和關聯關係（純 POJO 測試，不使用 Mock）
  - 測試 TodoItem 實體的業務邏輯和約束條件（純 POJO 測試，不使用 Mock）
  - 專注於實體內部的業務邏輯方法和驗證規則
  - _需求: 2.1, 2.2_

- [x] 3. 實作資料存取層





  - 建立 UserRepository 介面，繼承 JpaRepository
  - 建立 TodoItemRepository 介面，包含自定義查詢方法
  - 實作依使用者和排序條件查詢待辦事項的方法
  - _需求: 3.1, 3.2, 3.3_

- [x] 3.1 撰寫 Repository 層的整合測試



  - 使用 @DataJpaTest 測試資料存取邏輯
  - 測試自定義查詢方法和排序功能
  - _需求: 3.2, 3.3_

- [x] 4. 建立 DTO 和表單物件





  - 建立 CreateTodoRequest DTO，包含驗證註解
  - 建立 UpdateTodoRequest DTO，包含更新邏輯
  - 建立 UserRegistrationRequest 和 LoginRequest DTO
  - 實作 DTO 與實體間的轉換方法
  - _需求: 2.1, 2.5, 4.1, 4.2_



- [x] 5. 實作服務層業務邏輯



  - 建立 UserService，處理使用者註冊、登入驗證和密碼加密
  - 建立 TodoService，實作 CRUD 操作和業務規則
  - 實作使用者權限檢查，確保資料隔離
  - 加入 @Transactional 註解管理交易
  - _需求: 1.1, 1.2, 1.3, 2.1, 2.4, 4.2, 4.5, 5.1, 5.3, 5.5, 6.3_

- [x] 5.1 撰寫服務層的單元測試



  - 使用 Mockito @Mock 和 @InjectMocks 測試 UserService 的註冊和認證邏輯
  - 使用 Mockito 模擬 Repository 依賴，測試 TodoService 的 CRUD 操作和權限檢查
  - 使用 @ExtendWith(MockitoExtension.class) 進行測試設定
  - 專注於業務邏輯測試，隔離外部依賴
  - _需求: 1.1, 2.1, 4.2, 5.1, 6.3_
-

- [x] 6. 實作 Web 控制器



  - 建立 AuthController，處理登入、註冊和登出請求
  - 建立 TodoController，處理待辦事項的 CRUD 操作
  - 實作會話管理和使用者認證檢查
  - 加入表單驗證和錯誤處理
  - _需求: 1.2, 1.4, 2.5, 3.1, 3.2, 3.3, 4.1, 4.4, 5.2, 5.4, 6.1, 6.4_

- [x] 6.1 撰寫控制器的整合測試



  - 使用 @WebMvcTest 和 @MockBean 測試 HTTP 請求處理
  - 使用 Mockito 模擬服務層依賴，測試表單提交和重新導向邏輯
  - 使用 MockMvc 測試認證和授權流程
  - 專注於 Web 層邏輯測試，隔離服務層依賴
  - _需求: 1.2, 2.5, 4.1, 5.2, 6.1_

- [x] 7. 建立 Thymeleaf 模板頁面





  - 建立登入頁面 (login.html) 和註冊頁面 (register.html)
  - 建立待辦事項列表頁面 (todos.html)，包含排序和狀態切換功能
  - 建立新增/編輯待辦事項表單頁面 (todo-form.html)
  - 建立共用的頁面佈局和導航選單
  - _需求: 1.1, 2.5, 3.1, 3.2, 3.4, 4.1, 5.2_

- [x] 8. 實作全域異常處理和錯誤頁面





  - 建立 GlobalExceptionHandler，處理常見異常
  - 建立自定義異常類別 (TodoNotFoundException, UnauthorizedAccessException 等)
  - 建立錯誤頁面模板 (404.html, 403.html, error.html)
  - _需求: 4.5, 5.5, 6.3_

- [x] 9. 設定 Spring Security 和會話管理





  - 配置 Spring Security，設定登入和登出端點
  - 實作自定義認證邏輯和會話管理
  - 設定 CSRF 保護和安全標頭
  - 配置會話超時和清理機制
  - _需求: 1.2, 1.4, 1.5_

- [x] 10. 建立初始資料和資料庫設定





  - 建立 data.sql 檔案，包含 4 個預設使用者（chienlin/1234, alice/password123, bob/mypass456, carol/test789）
  - 為每個使用者建立多筆測試待辦事項，涵蓋已完成和未完成狀態
  - 實作 DataInitializer CommandLineRunner，確保所有密碼使用 BCrypt 正確加密
  - 設定 H2 資料庫 Console 存取和資料庫初始化配置
  - 確保應用程式啟動時自動載入所有假資料
  - _需求: 7.2, 7.3_

- [x] 11. 整合測試和端到端驗證





  - 建立完整的應用程式整合測試
  - 測試使用者註冊、登入和待辦事項管理的完整流程
  - 驗證資料隔離和安全性功能
  - 測試排序和狀態切換功能
  - _需求: 1.1, 1.2, 1.3, 2.1, 3.2, 3.3, 4.2, 5.1, 6.1_

- [ ]* 11.1 效能測試和優化
  - 測試資料庫查詢效能
  - 驗證會話管理效率
  - 檢查記憶體使用情況
  - _需求: 3.2, 3.3_

- [ ] 12. 最終整合和部署準備
  - 整合所有功能模組，確保系統正常運作
  - 驗證所有 4 個預設使用者（chienlin, alice, bob, carol）的資料載入和登入流程
  - 測試每個使用者的待辦事項資料隔離和 CRUD 操作
  - 驗證排序功能在多筆資料下的正確性
  - 確認 H2 Console 可正常存取並檢視載入的假資料
  - 測試應用程式重啟後資料持久性（如使用檔案模式）
  - _需求: 1.1, 1.2, 2.1, 3.1, 3.2, 3.3, 4.1, 5.1, 6.1, 7.2, 7.3, 7.4_