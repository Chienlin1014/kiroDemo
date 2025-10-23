# 專案結構

## 整體架構
採用標準的 Spring Boot 分層架構，遵循 Clean Architecture 原則，確保關注點分離和依賴反轉。

## 目錄結構

### 根目錄
```
├── .kiro/                    # Kiro AI 助手配置
│   ├── steering/            # 專案指導文件
│   └── specs/               # 功能規格文件
├── src/                     # 原始碼目錄
├── target/                  # Maven 建置輸出
├── pom.xml                  # Maven 專案配置
├── development-standards.md # 開發標準文件
└── mvnw, mvnw.cmd          # Maven Wrapper
```

### 原始碼結構 (src/main/java)
```
com.course.kirodemo/
├── KiroDemoApplication.java     # Spring Boot 主程式
├── ServletInitializer.java     # WAR 部署初始化
├── config/                      # 配置類別
│   ├── DataInitializer.java    # 資料初始化
│   ├── SecurityConfig.java     # Spring Security 配置
│   └── SessionConfig.java      # 會話管理配置
├── controller/                  # 控制器層 (Presentation Layer)
│   ├── AuthController.java     # 認證相關控制器
│   └── TodoController.java     # 待辦事項控制器
├── dto/                        # 資料傳輸物件
│   ├── CreateTodoRequest.java  # 建立待辦事項請求
│   ├── UpdateTodoRequest.java  # 更新待辦事項請求
│   ├── UserRegistrationRequest.java # 使用者註冊請求
│   ├── LoginRequest.java       # 登入請求
│   └── DtoConverter.java       # DTO 轉換工具
├── entity/                     # 實體層 (Domain Layer)
│   ├── TodoItem.java          # 待辦事項實體
│   └── User.java              # 使用者實體
├── exception/                  # 例外處理
│   ├── GlobalExceptionHandler.java # 全域例外處理器
│   ├── TodoNotFoundException.java
│   ├── UserNotFoundException.java
│   ├── UserAlreadyExistsException.java
│   └── UnauthorizedAccessException.java
├── repository/                 # 資料存取層 (Infrastructure Layer)
│   ├── TodoItemRepository.java # 待辦事項資料庫存取
│   └── UserRepository.java     # 使用者資料庫存取
├── security/                   # 安全相關
│   ├── CustomAuthenticationProvider.java
│   ├── CustomUserDetailsService.java
│   ├── CustomUserPrincipal.java
│   └── SecurityUtils.java
└── service/                    # 業務邏輯層 (Application Layer)
    ├── TodoService.java        # 待辦事項業務邏輯
    ├── UserService.java        # 使用者業務邏輯
    └── impl/                   # 服務實作
```

### 資源目錄 (src/main/resources)
```
├── application.yml             # 主要配置檔
├── data.sql                   # 初始資料 SQL
├── static/                    # 靜態資源 (CSS, JS, 圖片)
└── templates/                 # Thymeleaf 模板
```

### 測試結構 (src/test/java)
```
com.course.kirodemo/
├── integration/               # 整合測試
│   ├── CompleteUserJourneyTest.java # 完整使用者旅程測試
│   └── TodoAppIntegrationTest.java  # 應用程式整合測試
├── unit/                     # 單元測試 (依照主程式結構)
└── resources/
    └── application-test.yml  # 測試環境配置
```

## 分層架構說明

### 1. Presentation Layer (控制器層)
- **職責**: 處理 HTTP 請求、回應格式化、輸入驗證
- **包含**: Controller 類別
- **依賴**: Service 層

### 2. Application Layer (應用服務層)
- **職責**: 業務流程協調、用例實作、事務管理
- **包含**: Service 介面和實作
- **依賴**: Domain 層、Infrastructure 層

### 3. Domain Layer (領域層)
- **職責**: 核心業務邏輯、實體定義、業務規則
- **包含**: Entity 類別、Domain Service
- **依賴**: 無外部依賴

### 4. Infrastructure Layer (基礎設施層)
- **職責**: 資料持久化、外部服務整合、技術實作
- **包含**: Repository 實作、配置類別
- **依賴**: Domain 層

## 命名慣例

### 類別命名
- **Entity**: 名詞，如 `User`, `TodoItem`
- **Controller**: `{Entity}Controller`, 如 `TodoController`
- **Service**: `{Entity}Service`, 如 `UserService`
- **Repository**: `{Entity}Repository`, 如 `TodoItemRepository`
- **DTO**: `{Action}{Entity}Request/Response`, 如 `CreateTodoRequest`
- **Exception**: `{Condition}Exception`, 如 `TodoNotFoundException`

### 套件命名
- 使用小寫字母
- 按功能分組，不按技術分組
- 遵循 `com.course.kirodemo.{layer}` 模式

## 檔案組織原則
1. **按層級分離**: 不同層級的類別放在對應的套件中
2. **單一職責**: 每個類別只負責一個明確的職責
3. **依賴方向**: 依賴關係由外層指向內層
4. **測試對應**: 測試結構鏡像主程式結構