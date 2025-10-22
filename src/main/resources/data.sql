-- 預設使用者資料（密碼使用 BCrypt 加密）
-- chienlin/1234: $2a$10$N.zmdr9k7uOsxVQDApz2lOIhK6s.L0LGst1/pjYzpwRwHgYqxkHvC
-- alice/password123: $2a$10$8K1p/wf5YE29/Mpep.Vije.Ry/8Nc/YvGLLR/.fOn8Us5ssVdtlRO
-- bob/mypass456: $2a$10$Xl0yhvzLIuyg1yI6k2uAuOUmieWyafgq5/OjqiQab7CoQk6eOqSRy
-- carol/test789: $2a$10$5/Kw8/8F.nqy6B8KvQf5..QJ9FbNx/8qVQy5B8KvQf5..QJ9FbNx8

INSERT INTO users (username, password, created_at) VALUES 
('chienlin', '$2a$10$N.zmdr9k7uOsxVQDApz2lOIhK6s.L0LGst1/pjYzpwRwHgYqxkHvC', '2025-10-15 08:00:00'),
('alice', '$2a$10$8K1p/wf5YE29/Mpep.Vije.Ry/8Nc/YvGLLR/.fOn8Us5ssVdtlRO', '2025-10-16 09:30:00'),
('bob', '$2a$10$Xl0yhvzLIuyg1yI6k2uAuOUmieWyafgq5/OjqiQab7CoQk6eOqSRy', '2025-10-17 10:15:00'),
('carol', '$2a$10$5/Kw8/8F.nqy6B8KvQf5..QJ9FbNx/8qVQy5B8KvQf5..QJ9FbNx8', '2025-10-18 11:45:00');

-- chienlin 的待辦事項（涵蓋不同狀態和日期）
INSERT INTO todo_items (title, description, completed, created_at, due_date, completed_at, user_id) VALUES 
('完成專案提案', '準備下週一的專案提案簡報，包含需求分析和時程規劃', false, '2025-10-20 09:00:00', '2025-10-28', null, 1),
('購買生活用品', '到超市購買洗髮精、牙膏、衛生紙等日用品', false, '2025-10-21 14:30:00', '2025-10-25', null, 1),
('學習 Spring Boot', '完成 Spring Boot 官方教學文件的閱讀', true, '2025-10-18 10:15:00', '2025-10-22', '2025-10-22 16:45:00', 1),
('健康檢查預約', '預約年度健康檢查，包含血液檢查和X光檢查', false, '2025-10-19 11:20:00', '2025-11-15', null, 1),
('整理書房', '整理書房的書籍和文件，丟棄不需要的資料', true, '2025-10-17 08:00:00', '2025-10-20', '2025-10-20 15:30:00', 1),
('準備面試', '準備技術面試的常見問題和專案經驗分享', false, '2025-10-22 13:45:00', '2025-10-30', null, 1),
('閱讀技術書籍', '閱讀《Clean Code》和《設計模式》兩本書', false, '2025-10-21 20:00:00', '2025-12-15', null, 1),
('運動計畫', '每週至少運動三次，每次30分鐘以上', false, '2025-10-19 07:00:00', '2025-12-31', null, 1),
('今日購物清單', '買牛奶、麵包、雞蛋和蔬菜', false, '2025-10-23 08:00:00', '2025-10-23', null, 1),
('週末計畫', '安排這個週末的休閒活動和朋友聚會', false, '2025-10-23 10:30:00', '2025-10-26', null, 1);

-- alice 的待辦事項
INSERT INTO todo_items (title, description, completed, created_at, due_date, completed_at, user_id) VALUES 
('完成月報告', '整理本月的工作成果和下月計畫', false, '2025-10-20 10:30:00', '2025-10-31', null, 2),
('學習 React', '完成 React 官方教學和建立一個小專案', false, '2025-10-18 14:00:00', '2025-11-30', null, 2),
('買生日禮物', '為媽媽挑選生日禮物', false, '2025-10-21 16:20:00', '2025-10-29', null, 2),
('整理照片', '整理手機和電腦中的照片，備份到雲端', true, '2025-10-17 19:30:00', '2025-10-21', '2025-10-21 22:15:00', 2),
('預約牙醫', '預約牙齒檢查和洗牙', false, '2025-10-19 12:45:00', '2025-10-27', null, 2),
('準備簡報', '為下週的客戶會議準備產品展示簡報', false, '2025-10-23 09:15:00', '2025-10-25', null, 2),
('學習新技術', '研究 AI 相關技術和應用案例', false, '2025-10-22 16:00:00', '2025-11-15', null, 2);

-- bob 的待辦事項
INSERT INTO todo_items (title, description, completed, created_at, due_date, completed_at, user_id) VALUES 
('修理腳踏車', '腳踏車鏈條需要更換，輪胎需要打氣', false, '2025-10-20 15:10:00', '2025-10-26', null, 3),
('學習 Docker', '完成 Docker 基礎教學和容器化應用程式', false, '2025-10-19 09:20:00', '2025-11-20', null, 3),
('繳交水電費', '繳交本月的水費和電費帳單', true, '2025-10-18 11:00:00', '2025-10-22', '2025-10-22 14:30:00', 3),
('規劃週末旅行', '安排下週末的台中一日遊行程', false, '2025-10-21 18:45:00', '2025-10-25', null, 3),
('更新履歷', '更新個人履歷和作品集網站', false, '2025-10-20 21:30:00', '2025-11-10', null, 3),
('學習吉他', '練習新的吉他和弦和歌曲', false, '2025-10-17 20:15:00', '2025-12-31', null, 3),
('今天運動', '到健身房進行重量訓練和有氧運動', false, '2025-10-23 07:30:00', '2025-10-23', null, 3),
('程式碼重構', '重構舊專案的程式碼，提升可讀性', false, '2025-10-23 14:00:00', '2025-10-28', null, 3);

-- carol 的待辦事項
INSERT INTO todo_items (title, description, completed, created_at, due_date, completed_at, user_id) VALUES 
('準備考試', '準備下個月的證照考試，複習相關資料', false, '2025-10-20 08:30:00', '2025-11-25', null, 4),
('整理衣櫃', '整理換季衣物，捐贈不需要的衣服', true, '2025-10-18 15:45:00', '2025-10-21', '2025-10-21 17:20:00', 4),
('學習日文', '完成日文 N3 級別的學習課程', false, '2025-10-19 19:00:00', '2025-12-31', null, 4),
('買菜做飯', '採購本週的食材，準備健康餐點', false, '2025-10-21 17:30:00', '2025-10-24', null, 4),
('閱讀小說', '完成這個月的閱讀目標，讀完兩本小說', false, '2025-10-22 20:00:00', '2025-10-31', null, 4),
('今日瑜伽', '進行30分鐘的瑜伽練習', false, '2025-10-23 06:00:00', '2025-10-23', null, 4);