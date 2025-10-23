# 技術規格

## 技術棧

### 後端框架
- **Spring Boot 3.5.6** - 主要應用框架
- **Java 17** - 程式語言版本
- **Maven** - 建置工具和依賴管理

### 核心依賴
- **Spring Boot Starter Web** - REST API 和 Web 功能
- **Spring Boot Starter Data JPA** - 資料持久化
- **Spring Boot Starter Security** - 認證和授權
- **Spring Boot Starter Thymeleaf** - 模板引擎
- **Spring Boot Starter Validation** - 資料驗證
- **Spring Session JDBC** - 會話管理
- **Thymeleaf Extras Spring Security 6** - 模板安全整合

### 資料庫
- **H2 Database** - 記憶體資料庫（開發和測試）
- **JPA/Hibernate** - ORM 框架

### 安全性
- **Spring Security** - 認證和授權框架
- **BCrypt** - 密碼加密
- **CSRF 保護** - 跨站請求偽造防護
- **Session Management** - 會話安全管理

### 測試框架
- **Spring Boot Test** - 整合測試
- **JUnit 5** - 單元測試框架
- **AssertJ** - 斷言庫

## 常用指令

### 建置和執行
```bash
# 編譯專案
./mvnw compile

# 執行測試
./mvnw test

# 啟動應用程式
./mvnw spring-boot:run

# 打包應用程式
./mvnw package

# 清理建置檔案
./mvnw clean
```

### 開發工具
```bash
# 啟動應用程式（開發模式，支援熱重載）
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 執行特定測試類別
./mvnw test -Dtest=CompleteUserJourneyTest

# 跳過測試執行打包
./mvnw package -DskipTests
```

### 資料庫存取
- **H2 Console**: http://localhost:8080/h2-console
- **JDBC URL**: jdbc:h2:mem:todoapp_new
- **使用者名稱**: sa
- **密碼**: (空白)

## 應用程式配置

### 預設埠號
- **應用程式**: 8080
- **H2 Console**: 8080/h2-console

### 環境設定檔
- `application.yml` - 主要配置
- `application-test.yml` - 測試環境配置

### 重要配置項目
- **會話超時**: 30分鐘
- **密碼編碼**: BCrypt
- **JPA DDL**: create-drop（開發環境）
- **日誌等級**: INFO