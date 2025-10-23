# 專案開發標準與原則

## 開發方法論

### 測試驅動開發 (TDD)
- **必須先撰寫測試，再實作功能**
  - 紅燈 → 綠燈 → 重構的循環
  - 每個功能都必須有對應的單元測試
  - 測試覆蓋率應達到 80% 以上

### 行為驅動開發 (BDD) 測試方法論
- **Given-When-Then 模式**: 所有測試必須遵循此結構
  - **Given (給定)**: 設定測試的前置條件和初始狀態
  - **When (當)**: 執行被測試的行為或動作
  - **Then (那麼)**: 驗證預期的結果和副作用

#### 測試方法命名規範
```
test_<method>_when_<situation>_then_<test_result>
```

**命名規則說明：**
- `<method>`: 被測試的方法名稱
- `<situation>`: 測試的情境或條件
- `<test_result>`: 預期的測試結果

#### BDD 測試結構範例
```java
@Test
@DisplayName("設定完成狀態為 true 時應該自動設定完成時間")
void test_setCompleted_whenSetToTrue_then_shouldSetCompletedAtAutomatically() {
    // Given (給定) - 設定測試前置條件
    TodoItem todoItem = new TodoItem("測試任務", "描述", LocalDate.now().plusDays(1));
    assertFalse(todoItem.isCompleted());
    assertNull(todoItem.getCompletedAt());
    
    // When (當) - 執行被測試的行為
    todoItem.setCompleted(true);
    
    // Then (那麼) - 驗證預期結果
    assertTrue(todoItem.isCompleted());
    assertNotNull(todoItem.getCompletedAt());
    assertTrue(todoItem.getCompletedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
}
```

#### BDD 最佳實踐
- **清晰的測試意圖**: 每個測試應該只驗證一個行為
- **獨立性**: 測試之間不應該有依賴關係
- **可讀性**: 測試程式碼應該像規格文件一樣易讀
- **完整性**: Given-When-Then 三個部分都必須明確存在
- **註解標示**: 使用註解清楚標示 Given、When、Then 區塊

#### 不同測試類型的 BDD 應用

**實體測試 (Entity Tests):**
```java
// Given: 建立實體和設定初始狀態
// When: 呼叫實體方法
// Then: 驗證實體狀態變化
```

**服務測試 (Service Tests):**
```java
// Given: 模擬依賴物件的行為
// When: 呼叫服務方法
// Then: 驗證回傳值和依賴物件的互動
```

**控制器測試 (Controller Tests):**
```java
// Given: 設定 Mock 服務和請求參數
// When: 執行 HTTP 請求
// Then: 驗證回應狀態、內容和視圖
```

#### 測試資料準備原則
- **使用 @BeforeEach**: 準備每個測試共用的基礎資料
- **測試內準備**: 特定測試需要的資料在 Given 區塊中準備
- **避免魔術數字**: 使用有意義的常數或變數名稱
- **最小化資料**: 只準備測試所需的最少資料

## 設計原則

### SOLID 原則
- **單一職責原則 (SRP)**: 每個類別只負責一個職責
- **開放封閉原則 (OCP)**: 對擴展開放，對修改封閉
- **里氏替換原則 (LSP)**: 子類別應該能夠替換父類別
- **介面隔離原則 (ISP)**: 不應該強迫客戶端依賴它們不使用的介面
- **依賴反轉原則 (DIP)**: 高層模組不應該依賴低層模組，兩者都應該依賴抽象

### KISS 原則
- **保持簡單愚蠢 (Keep It Simple, Stupid)**
- 優先選擇簡單的解決方案
- 避免過度設計和不必要的複雜性
- 程式碼應該易於理解和維護

### Clean Architecture
- **分層架構**: 明確區分 Domain、Application、Infrastructure、Presentation 層
- **依賴規則**: 內層不依賴外層，依賴方向由外向內
- **實體 (Entities)**: 封裝企業級業務規則
- **用例 (Use Cases)**: 封裝應用程式特定的業務規則
- **介面適配器**: 轉換資料格式
- **框架與驅動程式**: 外層細節

## 程式碼品質要求
- 使用有意義的變數和方法命名
- 保持方法簡短，單一方法不超過 20 行
- 適當的註解和文件
- 遵循專案的程式碼風格指南

## 溝通語言
- 聊天室對話、規劃文件、註解等使用繁體中文(台灣)
- 程式碼中的專有名詞、變數名稱、方法名稱保持英文
- 錯誤訊息和日誌可使用繁體中文

## 版本控制
- 每次提交都應該包含測試
- 提交訊息使用繁體中文描述
- 功能分支開發，完成後合併到主分支