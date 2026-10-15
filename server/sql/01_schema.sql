-- ============================================================
-- PKB 个人知识库 · 01 建表（V0.1）
-- 执行者：MySQL root（业务账号无 DDL）
-- 执行方式（PowerShell 5.1）：
--   & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=***
--       --default-character-set=utf8mb4 --execute="source F:/项目路径/server/sql/01_schema.sql"
--
-- ⚠️ 安全约定：**不含 DROP**，只 `CREATE TABLE IF NOT EXISTS`，误执行不丢数据。
--
-- ⚠️ 字段依据：PRD V0.1 §5（数据模型）+ §6（数据与字段决策）+ 附录 C.3（user 表 7 列）
--   4 张业务表：note / tag / note_tag / attachment
--   3 条铁律（冻结，改动需用户拍板）：
--     ① 一条笔记一种格式（content_format 新建选定、之后不切换，默认 md）
--     ② 项目 = tag.type=1（表允许多对多，UI 约束单个；后端关联写入必须兜底）
--     ③ content_text 是搜索专用列（搜索只打 title + content_text，永不打 content）
--
-- ⚠️ **索引口径待 T2 方案定稿**：本文件只给「主键 + 必要唯一约束 + 最明显的外键索引」，
--    T2 会根据 PRD §5.4 的待拍板项（唯一约束与逻辑删除如何共存、搜索列是否建索引）再补。
-- ============================================================

SET NAMES utf8mb4;
USE `pkb`;

-- ------------------------------------------------------------
-- user：账号（**单账号系统，表中只有 1 行**）
-- 字段依据：PRD 附录 C.3（由 OSC 的 25 列精简为 7 列）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone`      VARCHAR(11)     NOT NULL                COMMENT '手机号（业务主键，全局唯一，登录账号）',
  `password`   VARCHAR(100)    NOT NULL                COMMENT '密码（BCrypt 密文）',
  `status`     TINYINT         NOT NULL DEFAULT 0      COMMENT '状态 0正常 1停用',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='账号（单账号，仅 1 行）';

-- ------------------------------------------------------------
-- note：笔记
-- 铁律①：content_format 新建时选定、之后不切换（默认 0=md），后端需显式拦截该字段更新
-- 铁律③：content_text 是搜索专用列（md 轻量去标记 / html 由 jsoup 剥标签），必须与 content 同步
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `note` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`          VARCHAR(255)    NOT NULL DEFAULT ''     COMMENT '标题（允许空，UI 显示占位文案）',
  `content`        LONGTEXT                                COMMENT '正文（格式由 content_format 决定）',
  `content_format` TINYINT         NOT NULL DEFAULT 0      COMMENT '格式 0=md 1=html（建后不可切换）',
  `content_text`   LONGTEXT                                COMMENT '搜索专用纯文本（md 去标记 / html 剥标签）',
  `status`         TINYINT         NOT NULL DEFAULT 0      COMMENT '状态 0=inbox 待整理 1=archived 已归档',
  `pinned`         TINYINT         NOT NULL DEFAULT 0      COMMENT '置顶 0否 1是（参与排序）',
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted`     TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_note_status_pinned_updated` (`status`, `pinned`, `updated_at`),
  KEY `idx_note_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='笔记';

-- ------------------------------------------------------------
-- tag：标签 / 项目（共用一张表，靠 type 区分）
-- 铁律②：项目 = tag.type=1；表允许多对多，UI 约束项目单个
-- ⚠️ 待 T2 拍板：name 的唯一约束与逻辑删除如何共存
--    （若建 UNIQUE(name, type)，被逻辑删除的标签仍会占用同名，新建同名会撞键）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tag` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`       VARCHAR(64)     NOT NULL                COMMENT '名称（同类型内不重复）',
  `type`       TINYINT         NOT NULL DEFAULT 0      COMMENT '类型 0=标签 1=项目',
  `color`      VARCHAR(16)              DEFAULT NULL   COMMENT '颜色（十六进制，可空）',
  `sort`       INT             NOT NULL DEFAULT 0      COMMENT '排序（小的在前）',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_tag_type_sort` (`type`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签 / 项目（type 区分）';

-- ------------------------------------------------------------
-- note_tag：笔记—标签 / 项目关联（联合主键）
-- ⚠️ 表层面**不限制**一条笔记只能有一个 type=1 的项目 —— 约束在后端写入逻辑
--    （已有关联 type=1 时替换而非新增，见 PRD P-006）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `note_tag` (
  `note_id` BIGINT UNSIGNED NOT NULL COMMENT '笔记 id',
  `tag_id`  BIGINT UNSIGNED NOT NULL COMMENT '标签 / 项目 id',
  PRIMARY KEY (`note_id`, `tag_id`),
  KEY `idx_note_tag_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='笔记—标签关联';

-- ------------------------------------------------------------
-- attachment：附件（一条笔记可挂多个；**不止图片**）
-- 库里只存对象 key，读时拼公开前缀；MinIO bucket = pkb
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `attachment` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `note_id`    BIGINT UNSIGNED NOT NULL                COMMENT '所属笔记 id',
  `file_key`   VARCHAR(255)    NOT NULL                COMMENT '对象存储 key（非完整 URL）',
  `file_name`  VARCHAR(255)    NOT NULL                COMMENT '原始文件名',
  `size`       BIGINT UNSIGNED NOT NULL DEFAULT 0      COMMENT '字节数',
  `mime`       VARCHAR(128)             DEFAULT NULL   COMMENT 'MIME 类型',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_attachment_note` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='附件';

-- ------------------------------------------------------------
-- 自检
-- ------------------------------------------------------------
SELECT TABLE_NAME, TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'pkb'
ORDER BY TABLE_NAME;
