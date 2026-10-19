-- =============================================================
-- 小蓝鲸优选 · 管理后台  数据库初始化（使用 root 执行一次）
-- 1. 创建业务库 xlj_mall
-- 2. 创建项目专用数据库用户 xlj_mall（避免业务直接使用 root）
-- 3. 仅授予业务库权限，满足最小权限原则
-- =============================================================

CREATE DATABASE IF NOT EXISTS `xlj_mall`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'xlj_mall'@'localhost' IDENTIFIED BY 'XljMall_2026';
CREATE USER IF NOT EXISTS 'xlj_mall'@'127.0.0.1' IDENTIFIED BY 'XljMall_2026';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES
  ON `xlj_mall`.* TO 'xlj_mall'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES
  ON `xlj_mall`.* TO 'xlj_mall'@'127.0.0.1';

FLUSH PRIVILEGES;
