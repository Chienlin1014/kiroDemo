# 需求文件

## 簡介

Todo List 應用程式是一個基於 Spring Boot 的網頁應用程式，使用 Thymeleaf 作為前端模板引擎，H2 資料庫作為資料儲存，並實作會員系統讓使用者管理個人的待辦事項。系統採用測試驅動開發 (TDD) 方法論進行開發。

## 詞彙表

- **Todo_System**: 整個待辦事項管理系統
- **User**: 註冊並登入系統的使用者
- **Todo_Item**: 個別的待辦事項，包含標題、描述、建立時間、預計完成日等資訊
- **Authentication_Service**: 處理使用者登入登出的服務
- **Todo_Service**: 處理待辦事項 CRUD 操作的服務
- **H2_Database**: 嵌入式資料庫，用於儲存應用程式資料
- **Spring_Session**: 用於管理使用者會話狀態的機制

## 需求

### 需求 1

**使用者故事:** 身為一個使用者，我想要註冊並登入系統，這樣我就能管理我個人的待辦事項。

#### 驗收標準

1. THE Todo_System SHALL 提供使用者註冊功能，包含使用者名稱和密碼欄位
2. WHEN 使用者提供有效的登入憑證，THE Authentication_Service SHALL 建立使用者會話
3. WHILE 使用者已登入，THE Todo_System SHALL 只顯示該使用者的待辦事項
4. WHEN 使用者選擇登出，THE Authentication_Service SHALL 終止使用者會話
5. THE Todo_System SHALL 使用 Spring Session 管理使用者會話狀態

### 需求 2

**使用者故事:** 身為一個已登入的使用者，我想要建立新的待辦事項，這樣我就能記錄需要完成的任務。

#### 驗收標準

1. WHEN 已登入使用者提交新待辦事項表單，THE Todo_Service SHALL 建立包含標題、描述、建立時間和預計完成日的待辦事項
2. THE Todo_System SHALL 自動設定待辦事項的建立時間為當前時間
3. THE Todo_System SHALL 要求使用者提供預計完成日期
4. THE Todo_Service SHALL 將新建立的待辦事項與當前登入使用者關聯
5. WHEN 待辦事項成功建立，THE Todo_System SHALL 重新導向至待辦事項列表頁面

### 需求 3

**使用者故事:** 身為一個已登入的使用者，我想要檢視我的待辦事項列表，這樣我就能了解我需要完成的任務。

#### 驗收標準

1. WHEN 已登入使用者存取待辦事項列表頁面，THE Todo_System SHALL 顯示該使用者的所有待辦事項
2. THE Todo_System SHALL 提供依建立時間排序的選項
3. THE Todo_System SHALL 提供依預計完成日排序的選項
4. THE Todo_System SHALL 顯示每個待辦事項的標題、描述、建立時間、預計完成日和完成狀態
5. THE Todo_System SHALL 使用 Thymeleaf 模板引擎渲染前端頁面

### 需求 4

**使用者故事:** 身為一個已登入的使用者，我想要修改我的待辦事項，這樣我就能更新任務的詳細資訊。

#### 驗收標準

1. WHEN 已登入使用者選擇編輯待辦事項，THE Todo_System SHALL 顯示預填入現有資料的編輯表單
2. THE Todo_Service SHALL 允許修改待辦事項的標題、描述和預計完成日
3. THE Todo_Service SHALL 保持原始建立時間不變
4. WHEN 使用者提交修改後的資料，THE Todo_Service SHALL 更新對應的待辦事項
5. THE Todo_System SHALL 驗證使用者只能修改自己的待辦事項

### 需求 5

**使用者故事:** 身為一個已登入的使用者，我想要標記待辦事項為已完成，這樣我就能追蹤我的進度。

#### 驗收標準

1. WHEN 已登入使用者點擊完成按鈕，THE Todo_Service SHALL 將待辦事項狀態更新為已完成
2. THE Todo_System SHALL 在待辦事項列表中視覺化區分已完成和未完成的項目
3. THE Todo_Service SHALL 記錄待辦事項的完成時間
4. THE Todo_System SHALL 允許使用者將已完成的待辦事項重新標記為未完成
5. THE Todo_System SHALL 驗證使用者只能修改自己的待辦事項狀態

### 需求 6

**使用者故事:** 身為一個已登入的使用者，我想要刪除不需要的待辦事項，這樣我就能保持列表的整潔。

#### 驗收標準

1. WHEN 已登入使用者選擇刪除待辦事項，THE Todo_System SHALL 顯示確認對話框
2. WHEN 使用者確認刪除操作，THE Todo_Service SHALL 從資料庫中永久移除該待辦事項
3. THE Todo_System SHALL 驗證使用者只能刪除自己的待辦事項
4. WHEN 刪除操作完成，THE Todo_System SHALL 更新待辦事項列表頁面
5. THE Todo_System SHALL 提供取消刪除操作的選項

### 需求 7

**使用者故事:** 身為一個開發者，我想要系統在啟動時載入測試資料，這樣我就能進行開發和測試。

#### 驗收標準

1. THE Todo_System SHALL 使用 H2 嵌入式資料庫作為資料儲存
2. WHEN 應用程式啟動，THE H2_Database SHALL 自動建立必要的資料表結構
3. THE Todo_System SHALL 在啟動時載入預設的測試使用者和待辦事項資料
4. THE H2_Database SHALL 設定為開發模式，允許透過 H2 Console 存取
5. THE Todo_System SHALL 提供資料庫連線設定，支援記憶體模式和檔案模式