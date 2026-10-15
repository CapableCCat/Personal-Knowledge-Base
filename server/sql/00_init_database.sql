-- ============================================================
-- PKB 个人知识库 · 00 建库与业务账号
-- 执行者：MySQL root（业务账号 pkb_app 无权 DDL）
-- 执行方式（PowerShell 5.1，PS 不支持 < 重定向）：
--   & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=***
--       --default-character-set=utf8mb4 --execute="source F:/项目路径/server/sql/00_init_database.sql"
--
-- ⚠️ 安全约定（沿用 OSC 已验证的做法）
--   1) 本文件是**模板**，密码占位符不得提交真实值；
--      本地执行时请用内联 --execute 的方式传真实密码，或临时改本文件后**不要提交**。
--   2) 重复执行会重置 pkb_app 的密码（ALTER USER），注意。
--   3) 账号只授予 DML，DDL 由 root 执行；host 限定 localhost（最小权限）。
-- ============================================================

SET NAMES utf8mb4;

-- 1. 建库（字符集 utf8mb4）
--    ⚠️ OSC 的 `osc` 库**原样保留不动**（归档数据），PKB 用全新空库，两者互不干扰
CREATE DATABASE IF NOT EXISTS `pkb`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 2. 业务账号（⚠️ 本地实例已于 2026-10-09 建好，密码见 server/.env —— 本文件保持模板态，
--    若要重建账号，把 CHANGE_ME_StrongPwd 换成真实强密码再执行）
SET @pkb_app_password = 'CHANGE_ME_StrongPwd';

SET @sql = CONCAT('CREATE USER IF NOT EXISTS ''pkb_app''@''localhost'' IDENTIFIED BY ''', @pkb_app_password, '''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 重置密码（换成强密码后执行；仅需改密码时也可单独执行本段）
SET @sql = CONCAT('ALTER USER ''pkb_app''@''localhost'' IDENTIFIED BY ''', @pkb_app_password, '''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 只授权 pkb 库的 DML（不含 DDL / GRANT / FILE 等）
GRANT SELECT, INSERT, UPDATE, DELETE ON `pkb`.* TO 'pkb_app'@'localhost';

FLUSH PRIVILEGES;

-- 5. 自检
SELECT user, host FROM mysql.user WHERE user = 'pkb_app';
SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME
FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'pkb';
