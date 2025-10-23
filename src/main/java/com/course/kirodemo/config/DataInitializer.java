package com.course.kirodemo.config;

import com.course.kirodemo.entity.TodoItem;
import com.course.kirodemo.entity.User;
import com.course.kirodemo.repository.TodoItemRepository;
import com.course.kirodemo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DataInitializer
 * 應用程式啟動時初始化測試資料的 CommandLineRunner
 * 確保所有密碼使用 BCrypt 正確加密
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    private final UserRepository userRepository;
    private final TodoItemRepository todoItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public DataInitializer(UserRepository userRepository, 
                          TodoItemRepository todoItemRepository,
                          PasswordEncoder passwordEncoder,
                          JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.todoItemRepository = todoItemRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("開始初始化測試資料...");
        
        // 初始化 Spring Session 資料表
        initializeSpringSessionTables();
        
        // 檢查是否需要初始化資料
        if (userRepository.count() > 0) {
            logger.info("資料庫已有資料，跳過初始化");
            return;
        }
        
        // 建立測試使用者
        createTestUsers();
        
        // 建立測試待辦事項
        createTestTodoItems();
        
        logger.info("測試資料初始化完成");
    }
    
    /**
     * 初始化 Spring Session 資料表
     */
    private void initializeSpringSessionTables() {
        try {
            logger.info("初始化 Spring Session 資料表...");
            
            // 建立 SPRING_SESSION 資料表
            String createSessionTable = """
                CREATE TABLE IF NOT EXISTS SPRING_SESSION (
                    PRIMARY_ID CHAR(36) NOT NULL,
                    SESSION_ID CHAR(36) NOT NULL,
                    CREATION_TIME BIGINT NOT NULL,
                    LAST_ACCESS_TIME BIGINT NOT NULL,
                    MAX_INACTIVE_INTERVAL INT NOT NULL,
                    EXPIRY_TIME BIGINT NOT NULL,
                    PRINCIPAL_NAME VARCHAR(100),
                    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
                )
                """;
            
            // 建立 SPRING_SESSION_ATTRIBUTES 資料表
            String createSessionAttributesTable = """
                CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
                    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
                    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
                    ATTRIBUTE_BYTES BLOB NOT NULL,
                    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
                    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
                )
                """;
            
            // 執行 SQL
            jdbcTemplate.execute(createSessionTable);
            jdbcTemplate.execute(createSessionAttributesTable);
            
            // 建立索引
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME)");
            
            logger.info("Spring Session 資料表初始化完成");
            
        } catch (Exception e) {
            logger.warn("Spring Session 資料表可能已存在，跳過初始化: {}", e.getMessage());
        }
    }

    /**
     * 建立測試使用者
     */
    private void createTestUsers() {
        logger.info("建立測試使用者...");
        
        // 建立四個測試使用者，密碼使用 BCrypt 加密
        User chienlin = createUser("chienlin", "1234", 
            LocalDateTime.of(2025, 10, 15, 8, 0, 0));
        User alice = createUser("alice", "password123", 
            LocalDateTime.of(2025, 10, 16, 9, 30, 0));
        User bob = createUser("bob", "mypass456", 
            LocalDateTime.of(2025, 10, 17, 10, 15, 0));
        User carol = createUser("carol", "test789", 
            LocalDateTime.of(2025, 10, 18, 11, 45, 0));
        
        logger.info("成功建立 {} 個測試使用者", 4);
    }
    
    /**
     * 建立使用者
     */
    private User createUser(String username, String rawPassword, LocalDateTime createdAt) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, encodedPassword);
        user.setCreatedAt(createdAt);
        User savedUser = userRepository.save(user);
        
        logger.debug("建立使用者: {} (ID: {})", username, savedUser.getId());
        return savedUser;
    }
    
    /**
     * 建立測試待辦事項
     */
    private void createTestTodoItems() {
        logger.info("建立測試待辦事項...");
        
        // 取得使用者
        User chienlin = userRepository.findByUsername("chienlin").orElseThrow();
        User alice = userRepository.findByUsername("alice").orElseThrow();
        User bob = userRepository.findByUsername("bob").orElseThrow();
        User carol = userRepository.findByUsername("carol").orElseThrow();
        
        // chienlin 的待辦事項
        createChienlinTodos(chienlin);
        
        // alice 的待辦事項
        createAliceTodos(alice);
        
        // bob 的待辦事項
        createBobTodos(bob);
        
        // carol 的待辦事項
        createCarolTodos(carol);
        
        long totalTodos = todoItemRepository.count();
        logger.info("成功建立 {} 個測試待辦事項", totalTodos);
    }
    
    /**
     * 建立 chienlin 的待辦事項
     */
    private void createChienlinTodos(User user) {
        // 未完成的待辦事項
        createTodoItem(user, "完成專案提案", "準備下週一的專案提案簡報，包含需求分析和時程規劃", 
            false, LocalDateTime.of(2025, 10, 20, 9, 0, 0), 
            LocalDate.of(2025, 10, 28), null);
            
        createTodoItem(user, "購買生活用品", "到超市購買洗髮精、牙膏、衛生紙等日用品", 
            false, LocalDateTime.of(2025, 10, 21, 14, 30, 0), 
            LocalDate.of(2025, 10, 25), null);
            
        createTodoItem(user, "健康檢查預約", "預約年度健康檢查，包含血液檢查和X光檢查", 
            false, LocalDateTime.of(2025, 10, 19, 11, 20, 0), 
            LocalDate.of(2025, 11, 15), null);
            
        createTodoItem(user, "準備面試", "準備技術面試的常見問題和專案經驗分享", 
            false, LocalDateTime.of(2025, 10, 22, 13, 45, 0), 
            LocalDate.of(2025, 10, 30), null);
            
        createTodoItem(user, "閱讀技術書籍", "閱讀《Clean Code》和《設計模式》兩本書", 
            false, LocalDateTime.of(2025, 10, 21, 20, 0, 0), 
            LocalDate.of(2025, 12, 15), null);
            
        createTodoItem(user, "運動計畫", "每週至少運動三次，每次30分鐘以上", 
            false, LocalDateTime.of(2025, 10, 19, 7, 0, 0), 
            LocalDate.of(2025, 12, 31), null);
            
        createTodoItem(user, "今日購物清單", "買牛奶、麵包、雞蛋和蔬菜", 
            false, LocalDateTime.of(2025, 10, 23, 8, 0, 0), 
            LocalDate.of(2025, 10, 23), null);
            
        createTodoItem(user, "週末計畫", "安排這個週末的休閒活動和朋友聚會", 
            false, LocalDateTime.of(2025, 10, 23, 10, 30, 0), 
            LocalDate.of(2025, 10, 26), null);
        
        // 已完成的待辦事項
        createTodoItem(user, "學習 Spring Boot", "完成 Spring Boot 官方教學文件的閱讀", 
            true, LocalDateTime.of(2025, 10, 18, 10, 15, 0), 
            LocalDate.of(2025, 10, 22), LocalDateTime.of(2025, 10, 22, 16, 45, 0));
            
        createTodoItem(user, "整理書房", "整理書房的書籍和文件，丟棄不需要的資料", 
            true, LocalDateTime.of(2025, 10, 17, 8, 0, 0), 
            LocalDate.of(2025, 10, 20), LocalDateTime.of(2025, 10, 20, 15, 30, 0));
    }
    
    /**
     * 建立 alice 的待辦事項
     */
    private void createAliceTodos(User user) {
        // 未完成的待辦事項
        createTodoItem(user, "完成月報告", "整理本月的工作成果和下月計畫", 
            false, LocalDateTime.of(2025, 10, 20, 10, 30, 0), 
            LocalDate.of(2025, 10, 31), null);
            
        createTodoItem(user, "學習 React", "完成 React 官方教學和建立一個小專案", 
            false, LocalDateTime.of(2025, 10, 18, 14, 0, 0), 
            LocalDate.of(2025, 11, 30), null);
            
        createTodoItem(user, "買生日禮物", "為媽媽挑選生日禮物", 
            false, LocalDateTime.of(2025, 10, 21, 16, 20, 0), 
            LocalDate.of(2025, 10, 29), null);
            
        createTodoItem(user, "預約牙醫", "預約牙齒檢查和洗牙", 
            false, LocalDateTime.of(2025, 10, 19, 12, 45, 0), 
            LocalDate.of(2025, 10, 27), null);
            
        createTodoItem(user, "準備簡報", "為下週的客戶會議準備產品展示簡報", 
            false, LocalDateTime.of(2025, 10, 23, 9, 15, 0), 
            LocalDate.of(2025, 10, 25), null);
            
        createTodoItem(user, "學習新技術", "研究 AI 相關技術和應用案例", 
            false, LocalDateTime.of(2025, 10, 22, 16, 0, 0), 
            LocalDate.of(2025, 11, 15), null);
        
        // 已完成的待辦事項
        createTodoItem(user, "整理照片", "整理手機和電腦中的照片，備份到雲端", 
            true, LocalDateTime.of(2025, 10, 17, 19, 30, 0), 
            LocalDate.of(2025, 10, 21), LocalDateTime.of(2025, 10, 21, 22, 15, 0));
    }
    
    /**
     * 建立 bob 的待辦事項
     */
    private void createBobTodos(User user) {
        // 未完成的待辦事項
        createTodoItem(user, "修理腳踏車", "腳踏車鏈條需要更換，輪胎需要打氣", 
            false, LocalDateTime.of(2025, 10, 20, 15, 10, 0), 
            LocalDate.of(2025, 10, 26), null);
            
        createTodoItem(user, "學習 Docker", "完成 Docker 基礎教學和容器化應用程式", 
            false, LocalDateTime.of(2025, 10, 19, 9, 20, 0), 
            LocalDate.of(2025, 11, 20), null);
            
        createTodoItem(user, "規劃週末旅行", "安排下週末的台中一日遊行程", 
            false, LocalDateTime.of(2025, 10, 21, 18, 45, 0), 
            LocalDate.of(2025, 10, 25), null);
            
        createTodoItem(user, "更新履歷", "更新個人履歷和作品集網站", 
            false, LocalDateTime.of(2025, 10, 20, 21, 30, 0), 
            LocalDate.of(2025, 11, 10), null);
            
        createTodoItem(user, "學習吉他", "練習新的吉他和弦和歌曲", 
            false, LocalDateTime.of(2025, 10, 17, 20, 15, 0), 
            LocalDate.of(2025, 12, 31), null);
            
        createTodoItem(user, "今天運動", "到健身房進行重量訓練和有氧運動", 
            false, LocalDateTime.of(2025, 10, 23, 7, 30, 0), 
            LocalDate.of(2025, 10, 23), null);
            
        createTodoItem(user, "程式碼重構", "重構舊專案的程式碼，提升可讀性", 
            false, LocalDateTime.of(2025, 10, 23, 14, 0, 0), 
            LocalDate.of(2025, 10, 28), null);
        
        // 已完成的待辦事項
        createTodoItem(user, "繳交水電費", "繳交本月的水費和電費帳單", 
            true, LocalDateTime.of(2025, 10, 18, 11, 0, 0), 
            LocalDate.of(2025, 10, 22), LocalDateTime.of(2025, 10, 22, 14, 30, 0));
    }
    
    /**
     * 建立 carol 的待辦事項
     */
    private void createCarolTodos(User user) {
        // 未完成的待辦事項
        createTodoItem(user, "準備考試", "準備下個月的證照考試，複習相關資料", 
            false, LocalDateTime.of(2025, 10, 20, 8, 30, 0), 
            LocalDate.of(2025, 11, 25), null);
            
        createTodoItem(user, "學習日文", "完成日文 N3 級別的學習課程", 
            false, LocalDateTime.of(2025, 10, 19, 19, 0, 0), 
            LocalDate.of(2025, 12, 31), null);
            
        createTodoItem(user, "買菜做飯", "採購本週的食材，準備健康餐點", 
            false, LocalDateTime.of(2025, 10, 21, 17, 30, 0), 
            LocalDate.of(2025, 10, 24), null);
            
        createTodoItem(user, "閱讀小說", "完成這個月的閱讀目標，讀完兩本小說", 
            false, LocalDateTime.of(2025, 10, 22, 20, 0, 0), 
            LocalDate.of(2025, 10, 31), null);
            
        createTodoItem(user, "今日瑜伽", "進行30分鐘的瑜伽練習", 
            false, LocalDateTime.of(2025, 10, 23, 6, 0, 0), 
            LocalDate.of(2025, 10, 23), null);
        
        // 已完成的待辦事項
        createTodoItem(user, "整理衣櫃", "整理換季衣物，捐贈不需要的衣服", 
            true, LocalDateTime.of(2025, 10, 18, 15, 45, 0), 
            LocalDate.of(2025, 10, 21), LocalDateTime.of(2025, 10, 21, 17, 20, 0));
    }
    
    /**
     * 建立待辦事項
     */
    private TodoItem createTodoItem(User user, String title, String description, 
                                   boolean completed, LocalDateTime createdAt, 
                                   LocalDate dueDate, LocalDateTime completedAt) {
        TodoItem todoItem = new TodoItem(title, description, dueDate, user);
        todoItem.setCreatedAt(createdAt);
        todoItem.setCompleted(completed);
        todoItem.setCompletedAt(completedAt);
        
        // 初始化延期相關欄位
        todoItem.setExtensionCount(0);
        todoItem.setLastExtendedAt(null);
        todoItem.setOriginalDueDate(null); // 將在首次延期時設定
        
        return todoItemRepository.save(todoItem);
    }
}