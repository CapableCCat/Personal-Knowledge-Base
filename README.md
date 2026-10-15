# PKB 个人知识库（Personal Knowledge Base）

**自用的「第二大脑」· V0.1（存得进、找得到）**

把「写下来」的摩擦降到最低，并保证写下来的东西一定找得回来。
**不是**笔记软件、**不是**协作工具 —— 唯一用户是作者本人。

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.13-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3.5-42b883)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.14-409eff)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

> **状态：V0.1 开发中（T1 脚手架落地）**
> 本仓库从 0 到 1 新建，**不是 OSC 社团管理系统的延续** —— OSC 已于 2026-09-30 归档，
> 仅作**工程脚手架参考**（只读，位于 `F:\Project\Own Project\New-Management-System`）。
> 业务逻辑、权限模型、数据模型全部重新设计，不复用 OSC 的任何业务代码。

---

## 一、项目定位

**ABC 三段合一，共用一份数据：**

| 段 | 内容 | 版本 |
| :--- | :--- | :--- |
| **A 快速记录** | 一个输入框，回车即存，不用先想分类 | V0.1 |
| **B 项目档案** | 把散落的笔记归到「项目」下 | V0.1 |
| **C AI 检索** | 向量检索 + RAG 问答 | V0.3（等数据积累） |

**核心闭环**：`写（不管分类）→ 进「待整理」inbox → 清理归类 → 进正式库 archived`

**V0.1 只解决「存得进、找得到」**；RAG / 向量检索 / AI 问答放 V0.3。

**V0.1 明确不做**：多用户 / 注册 / 分享 / 协作、版本历史、回收站 UI、多角色权限矩阵、
导入导出界面、看板、公告、反馈、报名审核、短信、成员档案、飞书集成、上线部署。

---

## 二、功能范围（V0.1 · 11 条 Must）

| 编号 | 功能 | 说明 |
| :--- | :--- | :--- |
| P-001 | 快速记录 | 首页输入框回车即建 inbox，不跳页 |
| P-002 | 笔记列表 | 状态 / 项目 / 标签筛选 + 排序 `pinned DESC, updated_at DESC` |
| P-003 | 笔记详情 / 编辑 | 桌面 `el-drawer`，手机全屏页 `/note/:id`，**复用同一份表单** |
| P-004 | 双格式编辑 | md / html 新建时选定，**之后不切换**（默认 md） |
| P-005 | 待整理闭环 | inbox → archived 的归档 / 取回 |
| P-006 | 项目管理 | 项目 = `tag.type=1`；UI 项目**单选**，后端写入兜底 |
| P-007 | 标签管理 | 标签 = `tag.type=0`，多选关联 |
| P-008 | 搜索 | 只搜 `title + content_text`，**永不打 `content`** |
| P-009 | 附件 | 通用文件上传（**非图片也支持**），MinIO bucket `pkb` |
| P-010 | 首页工作台 | 桌面三列各自滚动 + 手机底部 3 Tab |
| P-011 | 单账号登录 | 引导页首启建 1 账号 + 图形验证码 |

---

## 三、技术栈

### 后端（`server/`）

| 类别 | 技术 | 版本 | 说明 |
| :--- | :--- | :--- | :--- |
| 语言 | Java | 17 | — |
| 框架 | Spring Boot | 3.3.13 | Web + Validation |
| ORM | MyBatis-Plus | 3.5.7 | 逻辑删除 `is_deleted`，驼峰映射 |
| 鉴权 | Sa-Token | 1.44.0 | Header 模式（`pkb-token`），token 存 Redis |
| 数据库 | MySQL | 8.x | 库 **`pkb`**；应用账号 `pkb_app` **无 DDL 权限** |
| 缓存 | Redis | 6.x / 3.x | 登录 token、图形验证码、失败计数 |
| 对象存储 | MinIO | 8.5.12 | 附件（bucket `pkb`；未配置时降级，不影响启动） |
| 富文本清洗 | jsoup | 1.21.2 | html 笔记入库前 XSS 白名单清洗 |
| Excel | FastExcel | 1.3.0 | 🔒 **保留不启用**（`cn.idev.excel`，用户拍板勿删） |
| 验证码 | easy-captcha + nashorn-core | 1.6.2 / 15.7 | 算术图形验证码 |

### 前端（`web/`）

Vue 3.5 + Vite + Element Plus + Pinia + vue-router + axios + Sass（**JS 非 TS**）。
Vant 供登录与引导页；ECharts 与 `constants/palette.js` **保留不启用**（用户拍板勿删）。

---

## 四、目录结构

```text
Personal-Knowledge-Base/
├─ docs/                                       ← 长期文档（单一出处）
│  ├─ 个人知识库 PKB · 产品需求文档 V0.1.md          ← 权威需求依据
│  ├─ 个人知识库 PKB · 开发任务点清单.md              ← 台账：进度 / 方案 / 实现 / 决策
│  ├─ PKB 个人知识库 · 新对话交接提示词（通用版）.md   ← 开工与铁律
│  └─ PKB 个人知识库 · 现状信息说明.md               ← 项目缘起（历史存档）
├─ server/                                     ← 后端（com.pkb / pkb-server）
│  ├─ sql/00_init_database.sql                 ← 建库 + 建账号（root 执行）
│  ├─ sql/01_schema.sql                        ← 4 张业务表 + user（无 DROP，幂等）
│  └─ src/main/java/com/pkb/
└─ web/                                        ← 前端（pkb-web）
```

---

## 五、快速开始

### 1. 环境准备

| 组件 | 地址 / 账号 | 启动方式 |
| :--- | :--- | :--- |
| MySQL 8 | `127.0.0.1:3306`，root/root | Windows 服务（开机自启） |
| Redis | `127.0.0.1:6379`，无密码 | Windows 服务（开机自启） |
| MinIO | `127.0.0.1:9000`，bucket `pkb` | **手动**（见下） |

```powershell
# MinIO 手动启动（必须显式带凭据，否则报 credentials 校验失败）
& "E:\Minio\minio\minio.exe" server E:\Minio\pkb-data --address :9000 --console-address :9001
```

### 2. 建库建表（root 执行）

```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$sqlRoot = "F:/Project/Own Project/Personal-Knowledge-Base/server/sql"
# ① 建库 + 建 pkb_app 账号（先把文件里的 CHANGE_ME_StrongPwd 换成强密码）
& $mysql --user=root --password=root --default-character-set=utf8mb4 "--execute=source $sqlRoot/00_init_database.sql"
# ② 建表（4 张业务表 + user）
& $mysql --user=root --password=root --default-character-set=utf8mb4 "--execute=source $sqlRoot/01_schema.sql"
```

### 3. 起后端（`server/` 下）

```powershell
# ⚠️ 先清掉宿主注入的端口变量，否则 Spring 宽松绑定会拿它抢端口
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" package -DskipTests
& "C:\Program Files\Java\jdk-17\bin\java.exe" -jar "target\pkb-server-1.0.0.jar"
# dev 8080
```

### 4. 起前端（`web/` 下）

```powershell
npm install
npm run dev      # http://127.0.0.1:5173（/api 代理到 8080 并剥前缀）
```

### 5. 验证

```powershell
Invoke-RestMethod http://127.0.0.1:8080/health   # 期望 code:200
```

---

## 六、开发约定（摘要）

完整清单见 `docs/PKB 个人知识库 · 新对话交接提示词（通用版）.md`「五、全局铁律」「六、协作规则」。

1. **单功能节奏**：一个任务点 = 一次交付，完成即停，等审阅确认后再进下一个
2. **先方案后代码**：涉及技术选型 / 表结构变更 / 公共抽象的任务点，先出方案经确认
3. **单一出处**：同一事实只写一处，别处只放指针
4. **实测优于推断**：交付必须附真实跑过的验证记录，不写「应该可以」
5. **不主动提交**：只有明确说「提交 Tx」才 `git add` + `commit` + `push`
6. **参考代码只读**：OSC 归档仓仅作参考读取，**不得改动**

**三条数据铁律（冻结，改动需拍板）**：

1. 一条笔记一种格式（`content_format` 新建选定、之后不切换、默认 md）
2. 项目 = `tag.type=1`（表允许对多，UI 约束单个，**后端写入必须兜底**）
3. `content_text` 是搜索专用列（**搜索只打 `title + content_text`，永不打 `content`**）

---

## 七、文档地图

| 你要找的 | 去哪看 |
| :--- | :--- |
| 需求本身（功能卡 / 数据模型 / 待拍板项） | `docs/个人知识库 PKB · 产品需求文档 V0.1.md` |
| 任务点进度 / 方案 / 实现结果 / 决策 | `docs/个人知识库 PKB · 开发任务点清单.md` |
| 当前阶段 / 环境坑 / 全局铁律 / 启动命令 | `docs/PKB 个人知识库 · 新对话交接提示词（通用版）.md` |
| 项目缘起（历史存档） | `docs/PKB 个人知识库 · 现状信息说明.md` |

---

## 八、许可

Apache License 2.0（沿用 OSC）。见 [LICENSE](LICENSE)。
