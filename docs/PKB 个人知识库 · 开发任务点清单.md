# PKB 个人知识库 · 开发任务点清单（工作台账）

> **用途**：把 PRD V0.1 拆成**可逐个交付的任务点**，并记录每个任务点的方案、实现结果、验证方式与实现层决策。
>
> **职责边界（单一出处）**：本清单只管三件事 —— **§3 总表看进度**、**§4 任务点详情**、**§6 实现层决策**。
> 开会话要用的一切（开场提示词、当前阶段、关键词速查、环境坑、全局铁律）一律在
> `docs/PKB 个人知识库 · 新对话交接提示词（通用版）.md`，**不在本文件重复抄写**。
>
> **配套文档**：
> - `docs/PKB 个人知识库 · 产品需求文档 V0.1.md`（**权威需求依据**，代码以 PRD 为准）
> - `docs/PKB 个人知识库 · 新对话交接提示词（通用版）.md`（开工与铁律，新对话第一份读物）
> - `docs/PKB 个人知识库 · 现状信息说明.md`（项目缘起与决策讨论结论，历史存档）
> - `.workbuddy/memory/`（项目记忆：`MEMORY.md` + 每日日志）
>
> **维护方式**：每完成一个任务点 → 更新 §3 状态列 + 在 §4 该条目补「实现结果」+ §6 追加决策。
>
> **本文件当前状态**：🟢 **进行中（T1~T7 已完成，下一步 T8）**。§3 总表为正式编号（T1~T12）；§4 每个已完成任务点都有「技术方案 / 实现结果 / 验证指引」，T8 起在开工前补「技术方案」；§6 决策已累积到 D50。

---

## 1. 项目基本盘

| 项 | 内容 |
| :--- | :--- |
| 项目 | PKB 个人知识库（Personal Knowledge Base）· V0.1 |
| 定位 | 自用「第二大脑」，单用户，本机 / 局域网 |
| 工作空间 | `F:\Project\Own Project\Personal-Knowledge-Base`（**DSH 工作空间绑定，不可改名**） |
| 需求依据 | `docs/PKB 个人知识库 · 产品需求文档 V0.1.md` |
| 脚手架来源（**只读**） | `F:\Project\Own Project\New-Management-System`（OSC 归档，远端 `git@github.com:CapableCCat/New-Management-System.git`，**只 fetch 不 push**） |
| Git 远端 | `git@github.com:CapableCCat/Personal-Knowledge-Base.git`（**新建仓**，非 OSC 沿用） |
| 部署形态 | 单用户本机 / 局域网；**V0.1 不做上线部署**（无「上线部署」章节） |
| V0.1 范围 | 11 条 Must（P-001~P-011）：快速记录 / 列表 / 详情 / 双格式 / 待整理闭环 / 项目 / 标签 / 搜索 / 附件 / 首页 / 单账号登录 |

---

## 2. 技术栈（继承 OSC 已验证底座，T1 落地）

| 层 | 选型 | 与 OSC 的差异 |
| :--- | :--- | :--- |
| 工程布局 | monorepo 单仓：`server/` + `web/` | 不变 |
| 后端 | Java 17 + Spring Boot 3.3.13 + MyBatis-Plus 3.5.7 + Sa-Token 1.44.0 + Redis | 不变 |
| 数据库 | MySQL 8，库 **`pkb`**，应用账号 **`pkb_app`**（无 DDL） | ⚠️ 库名 `osc` → `pkb` |
| 缓存 | Redis（验证码 / 登录失败锁定 / token） | 不变 |
| 文件存储 | MinIO，bucket **`pkb`** | ⚠️ bucket `osc` → `pkb` |
| 包名 / 主类 | `com.pkb` / `PkbServerApplication` | ⚠️ `com.tsguosc` → `com.pkb` |
| 应用名 | `pkb-server` | ⚠️ `osc-server` → `pkb-server` |
| 前端 | Vue 3.5 + Vite + Element Plus + Pinia + vue-router + axios + Sass | 不变 |
| 移动端组件库 | Vant | 🔒 **保留依赖不启用**（OSC 公开端专用，PKB 无公开端） |
| Excel | FastExcel（`cn.idev.excel`） | 🔒 **保留不启用**（用户拍板勿删） |
| 图表 | ECharts 封装 + `constants/palette.js` | 🔒 **保留不启用**（用户拍板勿删） |
| 接口风格 | RESTful + 统一返回体 `{code,message,data}` + 全局异常；HTTP 一律 200 | 不变 |
| 端口 | 后端 dev 8080 / prod 8081；前端 dev 5173；**AI 自测隔离栈 8090 / 5180** | 不变 |
| 配置 | 全环境变量化：`.env`（忽略）+ `.env.example`（入仓） | 不变 |

---

## 3. 任务点总表（状态跟踪）

> ⚠️ **编号为草案**（PRD §3.1 与交接文档 §三）。若后续调整，**编号向后排、不动已有引用**（沿用 OSC 做法）。

| # | 任务点 | 覆盖 PRD | 依赖 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| T1 | **脚手架落地**（OSC 迁移 + 删业务 + 改名 `com.pkb` + 跑通空壳） | §1.4 / §2.2 / 附录 A | — | ✅ **已完成** |
| T2 | 数据层（4 张业务表 + `user` 简化 + 实体 + Mapper + 建库脚本） | PRD §5 / §6 | T1 | ✅ **已完成** |
| T3 | 单账号登录（引导页 + Sa-Token + 图形验证码） | P-011 | T2 | ✅ **已完成** |
| T4 | 外壳改造（菜单砍项 + 底部 3 Tab + 只留登录守卫 + `roles.js` 整删） | PRD §8.2 / §2.2 | T3 | ✅ **已完成** |
| T5 | 笔记 CRUD（后端，含 `content_text` 同步 / 归档 / 取回 / 置顶） | P-001 / P-005 | T2 | ✅ **已完成** |
| T6 | 笔记列表 + 详情（前端，drawer / 全屏页 + 复用同一份表单） | P-002 / P-003 | T5 | ✅ **已完成** |
| T7 | 双格式编辑（md 编辑器选型 + 富文本复用 + 分流清洗） | P-004 | T6 | ✅ **已完成** |
| T8 | 项目 / 标签（CRUD + 关联 + **项目单选后端兜底** + 计数） | P-006 / P-007 | T5 | ✅ **已完成** |
| T9 | 首页工作台（桌面三列各自滚动 + 手机 3 Tab + 状态条） | P-010 | T6 | ✅ **已完成** |
| T10 | 搜索（只打 `title + content_text`） | P-008 | T5 | ⬜ 未开始 |
| T11 | 附件（通用文件上传 + 非图片校验 + MinIO） | P-009 | T5 | ⬜ 未开始 |
| T12 | 联调打磨（三档无横向滚动 + `content_text` 全量核对 + 备份） | NFR / M4 | 全部 | ⬜ 未开始 |

**执行顺序**：T1 → T2 → T3 → T4 → T5 → T6 → T7 → T8 → T9 → T10 → T11 → T12。
**节奏**：单功能节奏 —— 一个任务点 = 一次交付，完成即停，等用户审阅确认后再进下一个，**不越点开发**。

---

## 4. 任务点详情

### T1 脚手架落地 🟡

> 状态：**未开始（本轮任务）**。

- **覆盖**：PRD §1.4（非目标）、§2.2（单账号原则）、附录 A（资产对照）
- **目标**：把 OSC 的工程结构搬到 PKB 工作区、删掉全部 OSC 业务代码、完成改名（`com.pkb` / `pkb-server` / 库 `pkb` / 桶 `pkb`），并**跑通空壳**（后端 `/health` + 前端 dev 可访问）。
- **关键点**：**不写任何 PKB 业务代码**，只做「搬运 + 删除 + 改名 + 跑通」。

#### T1 技术方案（✅ 已确认 2026-10-09）

**① 迁移前置：OSC 与本仓的 git 归属（最容易出事的一步）**

当前事实（2026-10-09 实测）：

- 本仓 `.git` 里是 **OSC 的 98 个提交**，HEAD = `f7ff06e docs: 归档项目并移除上线/部署任务点`
- 远端 `origin` = `git@github.com:CapableCCat/Personal-Knowledge-Base.git` —— **同名但已是「新建的 PKB 空仓」**
- ⚠️ **若此时直接 push，OSC 的 98 个提交会被灌进 PKB 新仓**（新仓首屏将显示「OSC 社团管理系统 T34」）

**处置顺序（不可颠倒）**：

```text
1. 把 OSC 的 247 个跟踪文件 + .workbuddy 复制到 New-Management-System\（robocopy，先 /L 干跑核对）
2. 把本仓 .git 整个移动到 New-Management-System\（98 个提交跟着走）
3. 在 New-Management-System\ 里设 remote.origin.url = git@github.com:CapableCCat/New-Management-System.git
   （旧归档仓；只 fetch 不 push，并做一次 git status 确认工作区干净）
4. 本仓重新 git init → 零提交起步（PKB 的历史从 T1 完成后的第一个提交开始）
5. 本仓 git remote add origin git@github.com:CapableCCat/Personal-Knowledge-Base.git
```

**三个安全点**：① robocopy **必须先用 `/L` 干跑**核对文件数；② `.git` 用 **`Move-Item` 而非复制**（搬走后 PKB 目录里不存在 OSC 历史，不可能误推）；③ **不提交、不推送**（用户说「提交 T1」才提交），中途出错随时可退回。

**② 逐文件处置清单（2026-10-09 实测清点：server 156 个 Java 文件 / web 58 个源文件）**

**后端 `server/` —— 保留不改（约 20 个）**

`common/result/Result` · `common/exception/{BusinessException, GlobalExceptionHandler}` ·
`config/{MybatisPlusConfig, OpenApiConfig, PasswordEncoderConfig, RedisConfig, MinioProperties}` ·
`controller/HealthController` · `controller/vo/HealthVO` · `dto/{CaptchaVO, PageResult}` ·
`util/{CaptchaValidator, HtmlSanitizer, MinioSupport, StorageKeys, PasswordPolicy, PasswordGenerator, ImageValidator}`

**后端 —— 保留改造（14 个）**

| 文件 | 改动 |
| :--- | :--- |
| `OscServerApplication` | → `PkbServerApplication`；包名 `com.tsguosc` → `com.pkb` |
| `config/SaTokenConfig` | 白名单大改：删 `/dict/*`、`/recruit/*` 公开白名单，只留 `/health`、`/error`、`/auth/*` |
| `config/SecurityProperties` | 保留登录锁定参数；删角色相关项 |
| `common/constant/RedisKeys` | 前缀 `osc:` → `pkb:` |
| `common/result/ResultCode` | 删 40005（需改密）/ 40300（无权限）等单账号用不到的码 |
| `controller/AuthController` | 删改密端点；`/auth/init-admin` 改为「建单账号」语义 |
| `controller/UserController` | 精简为「当前账号」 |
| `dto/{LoginRequest, LoginVO, InitAdminRequest, InitStatusVO, UserVO}` | 去角色 / 强制改密字段；`UserVO` 只留 `id`/`phone`/`status` |
| `entity/User` | **按 PRD 附录 C.3 的 8 列重写** |
| `service/AuthService` + `service/impl/AuthServiceImpl` | 删强制改密与角色分支；保留验证码 + 失败锁定 + Sa-Token 签发 |
| `service/UserService` + `service/impl/UserServiceImpl` | 精简为单账号读写 |
| `util/ExcelExporter` | 🔒 **保留不启用**（用户拍板勿删） |

**后端 —— 删除（约 105 个）**

| 目录 | 删除清单 |
| :--- | :--- |
| `common/constant` | `DictType` · `Roles` · `SessionKeys` · `ConfigKeys` |
| `config` | `StpInterfaceImpl`（角色推导）· `ProdDocDisabledConfig`（无 prod 部署）· `ImageIoConfig`（公告图专用） |
| `controller` | `Announcement` / `ConfigAdmin` / `Dashboard` / `Dict` / `Feedback` / `Member` / `RecruitAdmin` / `Recruit` / `StorageAdmin` |
| `dto` | `Announcement*`(4) · `ChangePasswordRequest` · `Config*`(2) · `Dashboard*`(2) · `Dict*`(4) · `Feedback*`(4) · `Import*`(4) · `Member*`(3) · `NameValueVO` · `ProfileUpdateRequest` · `Recruit*`(16) · `Storage*`(4) · `StudentIdUpdateRequest` · `TrendPointVO` |
| `entity` | `Announcement` · `Feedback` · `RecruitApply` · `SysConfig` · `SysDict` |
| `mapper` | `Announcement` / `Feedback` / `RecruitApply` / `SysConfig` / `SysDict` |
| `service` + `impl` | `Announcement` / `Dashboard` / `Dict` / `Export` / `Feedback` / `MemberImport` / `Member` / `RecruitAdmin` / `Recruit` / `StorageCleanup` / `SysConfig` |
| `util` | `AnnouncementImageStorage` · `AvatarStorage` · `AvatarUrls` · `ClubLogoStorage` · `DictIndex` · `ProvinceNormalizer` |
| `sql/` | `01_schema.sql` · `02_seed_dict.sql` · `03_migrate_old_data.sql` · `04_alter.sql` · `90/91/92_*.sql`（`00_init_database.sql` **改造**为建 `pkb` 库） |

> ⚠️ 两个易错点：**`ImageValidator` 保留**（图片专用，T7 富文本插图仍用；T11 另写通用附件校验）；**`ExportService` 删、`ExcelExporter` 留**（业务与工具要分开动手）。

**前端 `web/` —— 删除（约 37 个）**

| 目录 | 删除清单 |
| :--- | :--- |
| `api` | `announcement` · `config` · `dashboard` · `dict` · `feedback` · `member` · `recruit` · `storage` |
| `components` | `AnnouncementBell` · `AnnouncementDetailDialog` · `FeedbackDialog` · `MemberDetailDrawer` · `SmsNotifyDialog` · `HealthCheckCard` · `PlaceholderCard` · `MoreGrid`（V0.1 底部只有 3 Tab，无「更多」）· **`LocaleSwitch`（i18n）** |
| `constants` | **`roles.js` 整删**（PRD §2.2 冻结） |
| `stores` | `dict` · `recruit` · **`locale`（i18n）** |
| `utils` | `announcementRead` · `activityPhotos` · `sms` |
| `views` | `admin/*`(7) · `announcement/*` · `member/*`(3) · `public/*`(2) · `auth/ChangePasswordView` |

**前端 —— 保留改造**

| 文件 | 改动 |
| :--- | :--- |
| **`constants/app.js`** | **核心改造点**（被 10 个文件引用，含将保留的 `request.js` / `AppShell` / 守卫）：`APP_NAME`/`APP_FULL_NAME` 改 PKB；`TOKEN_HEADER` → `pkb-token`；`STORAGE_KEY.TOKEN` → `pkb_token`；**删 `ANNOUNCEMENT_READ_AT`**；`ROUTE_PATH` 删 `APPLY`/`QUERY`/`CHANGE_PASSWORD`/`ANNOUNCEMENT`/`PROFILE`，加 `NOTES`/`NOTE_DETAIL`/`TAGS`/`PROJECTS` |
| `api/request.js` | 依赖 `constants/app.js`，基本不动；删角色相关拦截分支 |
| `api/{auth,user,health}.js` | 随端点同步精简 |
| `App.vue` | **去掉 `useLocaleStore`**（i18n 已删） |
| `layouts/AppShell.vue` | 菜单砍到 3~4 项；底部 Tab 改「最近 / 待整理 / 项目」；**`min-height:0` 逐层保留**；去掉 `LocaleSwitch` |
| `layouts/PublicLayout.vue` | 保留（登录 / 引导用）；去掉 `LocaleSwitch` |
| `router/{index,menu,guard}.js` | 路由表重建（首页 / 笔记 / 详情 / 项目 / 标签 / 登录 / 引导 / 404）；`menu.js` 改单组；**守卫删权限判定，只留「已初始化」+「已登录」** |
| `stores/user.js` | 去角色字段 |
| `views/auth/{LoginView, InitAdminView}.vue` | 去强制改密跳转；`InitAdminView` 改「建单账号」 |
| `components/RichTextEditor.vue` | 保留（T7 用作 html 格式编辑器） |
| `utils/{sanitizeHtml, download}.js` · `composables/{useIsMobile, useLayoutMode}.js` | 保留 |

**前端 —— 保留不启用（🔒 用户拍板勿删）**

`components/BaseChart.vue` · `utils/echarts.js` · `constants/palette.js` · `utils/csv.js`

**文档**

11 份 OSC 文档全部迁出到 `New-Management-System\docs\`；PKB 目录 `docs/` 只留 3 份 PKB 文档。

**③ 保留不启用（🔒 用户明确拍板，勿删勿改）**

- FastExcel（`cn.idev.excel`）相关：依赖、`ExcelExporter` 若为通用工具则保留
- ECharts 封装：`components\BaseChart.vue`、`utils\echarts.js`、`constants\palette.js`

**④ 改造后复用（动手前先读原实现）**

| 资产 | 改造点 |
| :--- | :--- |
| 统一返回体 + 全局异常 + `ResultCode` | 错误码**需重定义**：删 40005（需改密）/ 40300（无权限）等单账号用不到的码 |
| Sa-Token 骨架 + `SaTokenConfig` | 白名单**大改**：OSC 的 `/dict/*`、`/recruit/*` 公开白名单全部作废；只留 `/health`、`/error`、`/auth/*`（引导 + 登录） |
| 三档外壳 `layouts\AppShell.vue` + `useLayoutMode` | 菜单砍 3~4 项；底部 Tab 改「最近 / 待整理 / 项目」；**`min-height:0` 逐层保留** |
| 路由表 + `guard.js` | **权限判定整删**，只留「是否已初始化」「是否已登录」 |
| 引导页 `auth\InitAdminView.vue` | 改为「首启建单账号」；**强制改密逻辑删除**；`activated_at` 用途重判 |
| 富文本 `RichTextEditor.vue` + `HtmlSanitizer` + `sanitizeHtml.js` | 保留；T7 起用于 html 格式笔记；**md 渲染产物同样要过 DOMPurify** |
| MinIO：`MinioSupport` / `StorageKeys` / `MinioProperties` | 保留；**`ImageValidator` 是图片专用**，T11 另写通用附件校验 |
| `styles\index.scss`（Sass 变量 + 三层配色） | 保留；主色待 PRD 第十二章 #2 定 |
| 分页 / 下载 / 日期 utils | 保留 |

**⑤ 改名（`grep` 全量核对，一个不漏）**

| 位置 | 从 | 到 |
| :--- | :--- | :--- |
| `pom.xml` | `com.tsguosc` / `osc-server` | `com.pkb` / `pkb-server` |
| Java 包目录 | `src\main\java\com\tsguosc\` | `src\main\java\com\pkb\` |
| 主类 | `OscServerApplication` | `PkbServerApplication` |
| `application*.yml` | 库 `osc` / 账号 `osc_app` / 配置前缀 `osc.*` | 库 `pkb` / 账号 `pkb_app` / 前缀 `pkb.*` |
| `SaTokenConfig` + `RedisKeys` | Header `osc-token` / `osc:captcha:*` / `osc:login:fail:*` | **待 PRD 第十二章 #3 定** |
| 前端 localStorage | `osc_token` / `osc_profile` / `osc_announcement_read_at` | 随 Header key 一起定 |
| 前端构建产物名 | `osc-*` | `pkb-*` |
| `.env.example` | `osc` 相关变量 | `pkb` 相关变量 |
| `README.md` | OSC 全文 | **重写为 PKB**（当前标题仍是「OSC 社团管理系统」且写着「已归档」） |
| `LICENSE` | Apache-2.0 | 沿用（PRD 附录 A） |

**⑥ 环境准备**

- 建库 `pkb` + 账号 `pkb_app`（**root 执行建库建号，应用账号无 DDL 权限**，沿用 OSC 安全约定）
- MinIO 建桶 `pkb`（应用启动时自动建 + 设公开读）
- ⚠️ `osc` 库**原样保留不动**（用户拍板），另建空库 `pkb`，两者互不干扰

**⑦ 交付验收（跑通空壳的定义）**

- 后端：`mvn package` 成功 → 隔离栈 `--server.port=8090` 起 → `/health` 返回 `code:200`
- 前端：`npm install` → `npm run dev` → 页面可访问、无控制台报错
- **`grep -r "tsguosc\|tsgu_osc\|OSC 社团" server\src web\src` 应无业务残留**
- 无任何 PKB 业务代码（T2 起才写）

#### T1 实现结果

> 状态：**已完成（2026-10-09）**。实测记录均为真实执行结果，非推断。

**① 迁移（OSC → NMS，2026-10-09）**

| 项 | 结果 |
| :--- | :--- |
| 复制到 `New-Management-System\` | **298 个文件**（server 159 / web 82 / .workbuddy 44 / docs 15 / 根 3） |
| `.git` 归属 | **已用 `Move-Item` 搬入 NMS**（98 个提交完整，`git status` 干净，`git log -1` = `f7ff06e`） |
| NMS 远端 | 已改为 `git@github.com:CapableCCat/New-Management-System.git`（归档仓，只 fetch 不 push） |
| PKB 目录残留 | OSC 文档 12 份已删（6 md + `Image\` 5 图 + 目录）；`.workbuddy` 45→核对后整删 |
| PKB 侧未复制项 | `.git`（已移走）/ `server\target` / `web\node_modules` / `web\dist` / `server\.env` / `server\.idea` |

**② 后端（server 156 → 37 个 Java 文件）**

- 删除 **100 个** OSC 业务文件 + **1 个** `StorageKeys`（纯 OSC 专用，无调用方）
- 包改名 `com.tsguosc` → `com.pkb`（36 个文件批量替换，零残留）；主类 `OscServerApplication` → `PkbServerApplication`
- 重写 19 个文件；`entity/User` 按 PRD 附录 C.3 落为 **7 列**
- `pom.xml`：`com.pkb` / `pkb-server`
- 编译：`mvn clean package -DskipTests` **退出码 0** → `pkb-server-1.0.0.jar`（89.2 MB）

**③ 前端（web 64 → 31 个源文件）**

- 删除 **38 个** OSC 业务文件（含 `constants/roles.js` 整删、i18n 整删）
- 新建 5 个占位页：`HomeView` / `notes/NotesView` / `notes/NoteDetailView` / `ProjectsView` / `TagsView`
- `constants/app.js`（根依赖）改造：`pkb-token` / `pkb_token` / 路由表重建
- 构建：`npm install`（232 包）→ `npm run build` **退出码 0**（2.49s），`dist` 无 OSC 残留

**④ 数据库**

- 建库 `pkb` + 账号 `pkb_app@localhost`（**仅 SELECT/INSERT/UPDATE/DELETE，无 DDL**）
- `01_schema.sql` 建表成功：`note` / `tag` / `note_tag` / `attachment` / `user`（5 张）
- **`osc` 库零污染**：仍是原 6 张表（`announcement, feedback, recruit_apply, sys_config, sys_dict, user`）

**⑤ git**

- PKB 目录 `git init` 完成，**零提交**（`git rev-list --count HEAD` 报 unknown revision = 无提交，符合预期）
- 远端 `origin` = `git@github.com:CapableCCat/Personal-Knowledge-Base.git`
- ⚠️ **未提交、未推送**（等用户说「提交 T1」）

**⑥ 跑通空壳（隔离栈 8090，非 8080）**

```json
// GET http://127.0.0.1:8090/health  → code:200
{ "data": { "app": "pkb-server", "profile": "dev", "version": "1.0.0",
  "components": { "redis": {"status":"UP"}, "mysql": {"status":"UP"} } } }
```

| 验证项 | 结果 |
| :--- | :--- |
| `/health` | `code:200`，`app=pkb-server`，redis + mysql **双 UP** |
| `/auth/init-status` | `initialized: false` ← **决定性证据：连的是空库 `pkb`，不是有 93 行的 `osc`** |
| `/auth/captcha` | `code:200`，返回 base64 图片 |
| Redis key | **`pkb:captcha:3e2def71…`**（前缀已改，非 `osc:`） |
| `/user/current`（未登录） | `code:40100` + **HTTP 200**（符合「HTTP 一律 200」口径） |
| 前端 `npm run dev` | 5173 就绪（1.96s），`/api` 代理正确指向 8080（ECONNREFUSED 属预期，8080 未起） |

**⑦ 本次踩到的新坑（已写入交接文档 §四）**

- 🔴 **`.env` 不随迁移走，且会静默覆盖 yml 默认值** —— 它是 `.gitignore` 的，迁移时被排除，导致应用**实际连的是归档的 `osc` 库**（`/health` 报 mysql UP 掩盖了真相）。
  发现方式：`/auth/init-status` 返回 `false` 才对（osc.user 有 93 行，若连 osc 会是 `true`）。
  **教训：迁移后必须核对 `.env`，不能只看 yml；`/health` 的 UP 不能证明连对了库。**
- 文件集对比时用 `StartsWith($path)` 会把 `.gitignore` 误判为 `.git` 前缀而漏拷 —— 排除判断要用**目录边界**（`$path.StartsWith("$dir\")`）。

#### T1 验证指引（PowerShell 5.1 友好）

```powershell
# ---------- 0. 前置：MySQL / Redis 常驻；MinIO 手动起（本步不依赖 MinIO）----------

# ---------- 1. 探活三个常驻服务 ----------
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=root --skip-column-names "--execute=SELECT 'mysql UP';"
& "C:\Program Files\Redis\redis-cli.exe" -h 127.0.0.1 -p 6379 PING

# ---------- 2. 看库表是否就位（应 5 张：attachment / note / note_tag / tag / user）----------
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
& $mysql --user=root --password=root --table "--execute=USE pkb; SHOW TABLES;"

# ---------- 3. 确认 osc 库没被动过（应仍是 6 张）----------
& $mysql --user=root --password=root --skip-column-names "--execute=SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='osc';"
# 期望输出 6

# ---------- 4. 起后端（在 server\ 下；AI 自测用 8090，你自己用 8080）----------
cd "F:\Project\Own Project\Personal-Knowledge-Base\server"
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" package -DskipTests
& "C:\Program Files\Java\jdk-17\bin\java.exe" -jar "target\pkb-server-1.0.0.jar"

# ---------- 5. 另开一个 PowerShell 窗口验接口 ----------
# 5.1 健康检查（期望 code:200、app=pkb-server、redis/mysql 都 UP）
Invoke-RestMethod http://127.0.0.1:8080/health | ConvertTo-Json -Depth 5

# 5.2 初始化状态（期望 initialized:false —— 证明连的是空库 pkb）
Invoke-RestMethod http://127.0.0.1:8080/auth/init-status | ConvertTo-Json

# 5.3 图形验证码（期望 code:200 + 一长串 base64）
(Invoke-RestMethod http://127.0.0.1:8080/auth/captcha).data.captchaKey

# 5.4 未登录访问受保护接口（期望 code:40100，且 HTTP 状态仍是 200）
Invoke-RestMethod http://127.0.0.1:8080/user/current | ConvertTo-Json

# ---------- 6. 起前端（在 web\ 下，另一个窗口）----------
cd "F:\Project\Own Project\Personal-Knowledge-Base\web"
npm install
npm run dev
# 浏览器开 http://127.0.0.1:5173 → 应被守卫送到 /init（未初始化）
```

**验收判据（全部应满足）**：
1. `pkb` 库有 5 张表；`osc` 库仍是 6 张（**零污染**）
2. `/health` 返回 `code:200` 且 `app` 为 `pkb-server`
3. `/auth/init-status` 返回 `initialized:false`
4. Redis 里的验证码 key 前缀是 **`pkb:captcha:`**
5. 前端 5173 可访问，控制台无报错

---

### T2 数据层 ✅

> 状态：**已完成（2026-10-15）**。方案已于 2026-10-09 经用户确认；实测记录见「T2 实现结果」。

- **覆盖**：PRD §5（数据模型）、§6（数据与字段决策）、§5.4（待拍板的数据层细节）
- **依赖**：T1 ✅
- **范围**：4 张业务表的**实体 + Mapper**；`note_tag` 无逻辑删除的关联策略；`content_text` 生成器**接口**（实现归 T5）；索引口径定稿
- **T1 已顺手完成的部分**：`pkb` 库 + `pkb_app` 账号已建；`01_schema.sql` 五张表**已在库里建好并跑通**（`pkb` 5 表 / `osc` 6 表零污染）

#### T2 技术方案（⬜ 待用户确认）

**① 交付物清单**

| 类别 | 新增文件 | 说明 |
| :--- | :--- | :--- |
| 实体 | `entity/Note.java` · `entity/Tag.java` · `entity/NoteTag.java` · `entity/Attachment.java` | 4 个；`entity/User.java` 已就位不动 |
| Mapper | `mapper/NoteMapper.java` · `mapper/TagMapper.java` · `mapper/NoteTagMapper.java` · `mapper/AttachmentMapper.java` | 4 个，均 `extends BaseMapper<T>`（`@MapperScan("com.pkb.mapper")` 已配） |
| 常量 | `common/constant/ContentFormat.java` · `common/constant/NoteStatus.java` · `common/constant/TagType.java` | 三个枚举/常量类，消灭魔法数字 |
| 生成器接口 | `common/ContentTextBuilder.java`（接口 + 默认实现） | `content_text` 计算的**单一收口点**；md 去标记的**具体规则归 T5** 定 |
| 脚本 | 视决策结果微调 `sql/01_schema.sql` | 若采纳「不建 `tag.name` 唯一索引」则无需改动 |

**② 关键决策（需用户拍板）**

> 这 5 条是 T2 唯一需要拍板的内容；其余实现细节（字段名、注解写法）按 PRD §6 直接落地。

| # | 议题 | 候选 | 我的倾向与理由 |
| :--- | :--- | :--- | :--- |
| **D-A** | `tag.name` 唯一约束（PRD 第十二章 **#15**） | ① 建 `UNIQUE(name, type)`　② 不建库级唯一，**应用层查重**　③ 物理删除标签 | **②**。理由：`is_deleted` 与唯一索引天然冲突 —— 逻辑删除的标签仍占用同名，之后新建同名会撞键（且 MySQL 唯一索引把 NULL 当互不相等，`is_deleted` 参与索引也救不了）。OSC 的先例（D19）是「用 `enabled=0` 停用代删、保证 `UNIQUE(type,code)` 稳定」，但 PKB 的 `tag` 没有 `enabled` 列，**且 PRD §5.1 冻结的表结构里也没有** —— 所以走应用层查重最贴合现状，且不违铁律。**副作用**：并发下理论上可建出同名标签，单账号自用可接受。 |
| **D-B** | 搜索列是否建索引（PRD 第十二章 **#14**） | ① 给 `title`/`content_text` 建索引　② **不建**，V0.1 用 `LIKE` 全表扫 | **②**。理由：`LIKE '%kw%'` 前导通配**用不上任何索引**，建了也是白占空间；个人笔记万级数据全表扫毫秒级。**这条同时把 PRD #14「搜索实现方式」锁定为 `LIKE`**（`title LIKE ? OR content_text LIKE ?`，**不含 `content`**）。 |
| **D-C** | `note_tag` 的实体与删除策略 | ① 建 `NoteTag` 实体 + 联合主键　② 不建实体，只用 SQL 操作 | **①**，且**关联走物理删除**。理由：`note_tag` 按 PRD §5.1 冻结的结构**没有 `is_deleted` 列**，所以关联的增删天然是物理操作（重新关联 = 再插一行，天然幂等，比逻辑删除简单）。MP 侧用 `@TableId` 标两列作联合主键 —— `insert` 可用；`deleteById` 不适用，删除一律走 `LambdaQueryWrapper`（T8 会用到）。⚠️ 需在 T8 方案中确认该组合主键写法在本版本 MP 下行为正确。 |
| **D-D** | 是否加乐观锁（`version` 列） | ① 加　② 不加 | **②**。理由：单账号自用，PRD P-003 已明确「V0.1 不做乐观锁，冲突时后写覆盖」——若加列就改了已冻结的表结构（需你拍板）。 |
| **D-E** | 列表摘要是否落库 | ① 加 `excerpt` 列　② 不加，查询时从 `content_text` 现算 | **②**。理由：`content_text` 已是纯文本，`LEFT(content_text, 200)` 在 SQL 里现算即可，**不引入冗余列**（冗余列就得同步，等于给自己加一个 R1 风险源）。 |

**③ 索引口径定稿（`01_schema.sql` 现状与建议）**

| 表 | 现有索引 | T2 结论 |
| :--- | :--- | :--- |
| `note` | PK + `idx_note_status_pinned_updated(status, pinned, updated_at)` + `idx_note_updated_at(updated_at)` | **保持不变**。前者精确支撑列表「按状态筛选 + `ORDER BY pinned DESC, updated_at DESC`」；后者支撑首页「最近」列 |
| `tag` | PK + `idx_tag_type_sort(type, sort)` | **保持不变**（项目/标签各取各的 type，天然走前缀） |
| `note_tag` | PK`(note_id, tag_id)` + `idx_note_tag_tag(tag_id)` | **保持不变**。PK 支撑「按笔记查标签」，反向索引支撑「某项目/标签下的笔记」与项目计数 |
| `attachment` | PK + `idx_attachment_note(note_id)` | **保持不变** |
| `user` | PK + `uk_user_phone(phone)` | **保持不变**（单账号，唯一约束保证不会建出第二个） |

**④ 字段级补充（PRD §6 已定的实现落地）**

- 4 张业务表**统一不带** `created_by` / `updated_by` —— 单账号下恒等于自己（沿用 D12 / D24）
- `note` / `attachment` / `tag` 的 `created_at` 由 **MySQL DEFAULT CURRENT_TIMESTAMP** 负责；MyBatis-Plus 的 `@TableField(fill=INSERT)` 同时保留（两条路径结果一致，不冲突）
- ⚠️ **`note.updated_at` 是业务字段**（参与排序），故**不允许**用 `ON UPDATE CURRENT_TIMESTAMP` 之外的方式改它；归档 / 取回会改动它（PRD P-005 已登记该行为待确认）
- `note.content_format`：T5 的更新接口**必须显式拦截**该字段（铁律①）

**⑤ 自测与验证（T2 交付时执行）**

1. `mvn clean package -DskipTests` 通过
2. 冒烟：起隔离栈 8090 → `/health` 仍 `code:200`（新增实体/Mapper 不应影响启动）
3. **Mapper 可用性验证**：写一个临时验证入口或直接用 `mysql.exe` 对比 —— 用 `NoteMapper.insert` 插一条 → `SELECT` 确认落库 → **物理删除**该测试行（**不留在库里**）
4. **逻辑删除验证**：`note` 插入后调 `deleteById` → 库里 `is_deleted=1`、`selectById` 查不到（验证 `@TableLogic` 生效）
5. **列清单核对**：`SHOW COLUMNS FROM note;` 与 `Note.java` 字段**逐一对照**，确认无多无少（防「实体有列、库里没有」这类运行时才炸的问题）
6. 交付后 `pkb` 库**不留测试数据**（表行数均应回到 0）

#### T2 实现结果

> 状态：**已完成（2026-10-15）**。实测记录均为真实执行结果，非推断。

**① 交付物（9 个新文件）**

| 类别 | 文件 |
| :--- | :--- |
| 实体 | `entity/Note.java` · `entity/Tag.java` · `entity/NoteTag.java` · `entity/Attachment.java` |
| Mapper | `mapper/NoteMapper.java` · `mapper/TagMapper.java` · `mapper/NoteTagMapper.java` · `mapper/AttachmentMapper.java` |
| 常量 | `common/constant/ContentFormat.java` · `common/constant/NoteStatus.java` · `common/constant/TagType.java` |
| 收口点 | `common/ContentTextBuilder.java`（接口 + `Default` 实现）+ `config/ContentTextConfig.java`（装配 Bean） |

> 合计 **12 个**新文件（4 实体 + 4 Mapper + 3 常量 + 1 接口 + 1 配置）。

**② 🔴 T2 实测抓出的库兼容性问题（方案里的写法是错的，已修正）**

方案原写「`note_tag` 用 `@TableId` 标两列为联合主键」——**MyBatis-Plus 3.5.7 直接拒绝**：

```text
MybatisPlusException: @TableId can't more than one in Class: "com.pkb.entity.NoteTag"
```

后果很严重：**Mapper 注册失败 → 整个应用起不来**（不是运行时才炸）。

**修正**：`NoteTag` 的两个字段**都不带 `@TableId`**，联合主键约束完全交给数据库的
`PRIMARY KEY (note_id, tag_id)`。MP 启动时会打一条 WARN 且**语义与我们完全一致**：

```text
WARN DefaultSqlInjector : class com.pkb.entity.NoteTag ,Not found @TableId annotation,
                          Cannot use Mybatis-Plus 'xxById' Method.
```

即「`xxById` 方法不可用」—— 而我们的用法本就不需要它（只用 `insert` / `selectList` / 条件 `delete`）。
**该 WARN 是设计意图，不是待修缺陷**（已在实体与 Mapper 注释里写明）。

**③ 验证证据（用临时 `T2Verifier` 跑真实数据库，验完即删）**

| 验证项 | 实测输出 |
| :--- | :--- |
| md 去标记 | `[1a] md → 项目笔记 这是 正文，含 代码 与 链接。 列表项一 列表项二 int a = 1;`（剥掉 `#`/`**`/反引号/围栏，**保留链接文字、丢弃 URL**） |
| html 剥标签 | `[1b] html→ 正文 加粗 第二段`（**`<script>alert(1)</script>` 内容未进搜索列**） |
| 时间自动填充 | `[2a] createdAt=… updatedAt=…` 由 `MetaObjectHandler` 填充 |
| **逻辑删除** | `[2c] deleteById 后 selectById = null`；`[2d] selectCount = 0`（`@TableLogic` 生效） |
| 应用层查重口径 | `[3b] 同名同类型计数 = 1`（D26 的落地方式可行） |
| **关联幂等** | `[3d] DuplicateKeyException` —— 重复关联被联合主键拦下 |
| 关联物理删除 | `[3e] note_tag 剩余 = 0`（该表无 `is_deleted`，符合 D28） |
| 附件 | `[4a] fileKey=attachments/202610/t2-verify.pdf`，`createdAt` 自动填充 |
| **列清单逐一对照** | `note` 10 列 / `tag` 7 列 / `note_tag` 2 列 / `attachment` 8 列 / `user` 7 列 —— **五张表与实体字段全部完全一致** |
| 清理 | `note=3, tag=1, attachment=1` 物理删除，**四表归零** |

**④ 验证器自身的两个教训（已修正，记下来防重蹈）**

1. **MP 的 `delete(wrapper)` 对带 `@TableLogic` 的实体也是逻辑删除**（且自动追加 `is_deleted=0`），
   **清不掉已被逻辑删除的行** → 验证器的清理必须走**原生 SQL**。
   （实测踩到：第二次运行时清理无效，库里留下占位行。）
2. **验证器不要写与「上一步产物」冲突的前置断言**：第 ③ 步为测关联建的笔记尚未删除，
   第 ④ 步却断言「note 表应为空」→ 假失败。**每步自扫门前雪**，别假设全局空表。

**⑤ 顺带发现（非 T2 产出，但值得记录）**

T2 验证期间发现 `pkb.user` 已有 **1 行真实数据**（`phone=18178325352`，`created_at` 早于 T2 验证）：

- 密码为 **`$2a$10$` / 60 字符 BCrypt**（`BCryptPasswordEncoder` 产物，非明文）
- 该号与归档库 `osc.user` 中是同一个号（**用户本人**）
- **`osc.user` 仍 93 行，归档库零影响**

→ **这是用户本人**在 T1 交付后自行走引导页建号并登录成功的真实记录，
即 **P-011 的「引导页建号 → BCrypt 落库 → 登录」主链路已被真实使用验证**。

#### T2 验证指引（PowerShell 5.1 友好）

```powershell
# ---------- 1. 五张表与列（T2 的核心交付：实体与库必须一一对应）----------
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
& $mysql --user=root --password=root --table "--execute=USE pkb; SHOW TABLES;"
# 期望 5 张：attachment / note / note_tag / tag / user

& $mysql --user=root --password=root --table "--execute=USE pkb; SHOW COLUMNS FROM note;"
# 期望 10 列：id, title, content, content_format, content_text, status, pinned,
#             created_at, updated_at, is_deleted

& $mysql --user=root --password=root --table "--execute=USE pkb; SHOW COLUMNS FROM note_tag;"
# 期望 2 列：note_id, tag_id（联合主键，无 is_deleted）

& $mysql --user=root --password=root --table "--execute=USE pkb; SHOW COLUMNS FROM tag;"
& $mysql --user=root --password=root --table "--execute=USE pkb; SHOW COLUMNS FROM attachment;"
& $mysql --user=root --password=root --table "--execute=USE pkb; SHOW COLUMNS FROM user;"

# ---------- 2. 确认 note_tag 的联合主键真的建上了 ----------
& $mysql --user=root --password=root --table "--execute=USE pkb; SHOW KEYS FROM note_tag WHERE Key_name='PRIMARY';"
# 期望两行：note_id 与 tag_id 都是 PRIMARY

# ---------- 3. 确认四张业务表干净（T2 验证数据已清）----------
& $mysql --user=root --password=root --table "--execute=USE pkb; SELECT (SELECT COUNT(*) FROM note) AS note, (SELECT COUNT(*) FROM tag) AS tag, (SELECT COUNT(*) FROM note_tag) AS note_tag, (SELECT COUNT(*) FROM attachment) AS attachment;"
# 期望全 0

# ---------- 4. 归档库未被影响 ----------
& $mysql --user=root --password=root --skip-column-names "--execute=SELECT CONCAT('osc.user = ', COUNT(*)) FROM osc.user;"
# 期望 osc.user = 93

# ---------- 5. 起后端（在 server\ 下）----------
cd "F:\Project\Own Project\Personal-Knowledge-Base\server"
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" package -DskipTests
& "C:\Program Files\Java\jdk-17\bin\java.exe" -jar "target\pkb-server-1.0.0.jar"

# ---------- 6. 另开窗口验接口 ----------
Invoke-RestMethod http://127.0.0.1:8080/health | ConvertTo-Json -Depth 5
Invoke-RestMethod http://127.0.0.1:8080/auth/init-status | ConvertTo-Json
# 期望 initialized:true（你已经建过账号了）
```

**验收判据**：
1. `pkb` 有 5 张表，`note` 10 列 / `tag` 7 列 / `note_tag` 2 列 / `attachment` 8 列 / `user` 7 列
2. `note_tag` 的 `PRIMARY` 由 `note_id` + `tag_id` 两列组成
3. 四张业务表行数全 0（T2 验证数据已物理清干净）
4. `osc.user` 仍 93 行
5. 后端能起、`/health` `code:200`、`/auth/init-status` 返回 `initialized:true`
6. 启动日志里 `NoteTag` 那行 WARN 是**预期内**的（语义 = `xxById` 方法不可用，我们不用它）

---

### T3 单账号登录 ✅

> 状态：**已完成（2026-10-15）**。方案经用户确认（Q1 不加改密入口 / Q2 工具放 `.workbuddy`）；实测记录见「T3 实现结果」。

- **覆盖**：P-011（单账号登录）
- **依赖**：T2 ✅
- **范围**：引导页（首启建单账号）+ 登录（图形验证码 + 失败锁定）+ Sa-Token Header + 登录守卫
- **范围（2026-10-15 扩充，用户决策文档要求立项）**：
  1. **账号与存储解耦验证**（核心保证）
  2. **`user` 表简化** —— ✅ **T1 已完成**（7 列；`roles.js` / `StpInterfaceImpl` / 强制改密均已删）
  3. **忘记密码重置流程落地** —— 决策见 PRD 附录 **C.4**（#10），步骤落 `README.md`「八、常见问题」
- **开工前需用户拍板**（PRD 第十二章）：**#16** token 有效期与失效策略
  （#3 Header key 与 #10 重置方式**已拍板**，见 PRD 附录 C.1 / C.4）
- **开工前必须验证的前置事实**（已查明，2026-10-15）：
  | 事项 | 实测结论 |
  | :--- | :--- |
  | `User` 实体有 `@TableLogic` 吗 | **有**（`isDeleted` 上）→ 重置必须用**原生 `DELETE`** |
  | 引导页判定条件 | `userMapper.selectCount(null)`，**MP 自动追加 `is_deleted=0`** → 不受软删影响 |
  | Sa-Token 的 Redis key 前缀 | **`pkb-token:login:token:*`** / **`pkb-token:login:session:*`**（由 `token-name` 决定） |
  | `user` 表是否已简化 | **已完成**（7 列） |
- **技术方案**：见下（⬜ 待用户确认）

#### T3 技术方案（⬜ 待用户确认 · 2026-10-15）

> **前提**：P-011 的**主链路已被用户真实使用验证过**（引导页建号 → BCrypt 落库 → 登录成功）。
> 所以 T3 **不是从零搭**，而是① 落一条配置变更；② 把「尚未实测」的部分逐条验掉。

**① 交付物清单**

| 类别 | 内容 | 性质 |
| :--- | :--- | :--- |
| 配置变更 | `application.yml`：`sa-token.timeout` `2592000` → **`604800`**（7 天）；`is-concurrent` `true` → **`false`**（禁多端） | 行为变更 |
| 配置变更 | `.env.example` 补 `SA_TOKEN_TIMEOUT=604800` 的说明（值本身走环境变量兜底） | 文档 |
| 工具移植 | OSC 的验证技能 `osc-verify` → PKB 版（CDP 运行器 / 预览站 / Redis 取验证码答案） | **无生产代码** |
| （可选）后端 | `POST /user/change-password` + `UserService.changePassword`（见开放问题 Q1） | 新功能 |
| 文档 | 重置流程已入 `README.md`（✅ 已完成）；本条目补实现结果与验证指引 | 文档 |

**② 明确不做（防范围蔓延）**

- ❌ 不改 `LoginRequest` / `LoginVO` / `UserVO` 的结构（已与前端对齐）
- ❌ 不改 `User` 表结构（7 列已定稿）
- ❌ 不做「记住我 / 免登录」（与 #16 缩短 token 的意图相反）
- ❌ 不做找回密码接口（#10 已定：文档化步骤）

**③ 待实测清单（T3 的核心工作量 —— 上一轮只验到「能建号能登录」）**

| # | 验证项 | 预期 | 怎么验 |
| :--- | :--- | :--- | :--- |
| V1 | **失败锁定** | 连错 5 次 → 第 6 次提示「锁定 15 分钟」；Redis `pkb:login:fail:{phone}` 有值 | 接口脚本（用**测试账号**，不动真账号） |
| V2 | **验证码一次性** | 同一 `captchaKey`+答案用两次 → 第二次 `40001` | 接口脚本 |
| V3 | **验证码错误** | 故意填错 → `40001`，且计数不累加 | 接口脚本 |
| V4 | **禁多端登录**（新增行为） | 同一账号登录两次 → **第一个 token 失效**（`40100`） | 接口脚本 |
| V5 | **token 有效期配置生效** | Redis 里 token 的 TTL ≈ **7 天**（604800s ± 少量） | 接口脚本 + Redis TTL |
| V6 | **登出** | `/auth/logout` → 本地 token 清空 + Redis key 消失 + 再访问受保护接口 `40100` | 接口 + 页面 |
| V7 | **登录守卫（未登录）** | 访问 `/notes` `/projects` `/tags` → 弹回 `/login?redirect=…` | **CDP 页面** |
| V8 | **已登录访问 /login** | 重定向到 `/`（首页） | CDP 页面 |
| V9 | **已初始化访问 /init** | 重定向到 `/login`（**不得二次建号**） | CDP 页面 |
| V10 | **未初始化访问任意路由** | 全部送 `/init` | CDP 页面（需临时清库，见风险 R3） |
| V11 | **登录页交互** | 填手机号/密码/验证码 → 登录成功 → 跳首页；验证码可点击刷新 | CDP 页面 |
| V12 | **前端 token 失效处理** | 携带失效 token 时 axios 拦截器捕获 `40100` → 清本地态 + 跳登录页 | CDP 页面 |
| V13 | **锁定提示文案** | 第 6 次登录提示明确（含剩余分钟数） | 接口脚本 |

**④ 验证环境与夹具（关键设计）**

- **隔离栈**：后端 `8090` + 预览站 `5180`（`web/dist` 静态托管 + `/api` 反代 8090）——**不动 8080 / 5173**
- **测试账号（必须造，否则 V1/V4/V13 没法验）**：
  - 用户的真账号（`18178325352`）**密码未知**（BCrypt 单向），且**绝不能把它锁掉**
  - → 直接往 `pkb.user` 插**一个临时测试账号**（自己生成 BCrypt 哈希），**验证完立即硬删**
  - ⚠️ 插之前记下原行数，验完核对 `user` 表**回到 1 行**（只剩用户的真账号）
- **验证码答案**：从 Redis 反查 `pkb:captcha:{key}`（PKB 前缀已改，与 OSC 不同）
- **页面验证**：移植 OSC 的 CDP 运行器（`--width/--height` 设备模拟 + 注入 `pkb_token`/`pkb_profile`）

**⑤ 已知的坑（移植/验证时要绕开，来自 OSC 血泪史）**

- ⚠️ **Chrome profile 跨轮污染**：登录态留在 profile 里 → ① 已登录用例跑过后公开页用例整体带跑偏；② profile 残锁时 Chrome 静默起不来、脚本**不报错直接 exit 0** → **每个用例用独立 profile 目录**
- ⚠️ **`rm -rf web/dist` 必须先做**：沙箱的 safe-delete 垫片清 `dist` 会超时（`vite:prepare-out-dir … ETIMEDOUT`），**不是代码问题**
- ⚠️ **窄屏必须 `Emulation.setDeviceMetricsOverride`**：Windows 上 `--window-size=375` 会被顶到 504
- ⚠️ **收集网络请求用 CDP `Network.enable`**，别用页面内埋点（整页导航会把埋点数据清空）
- ⚠️ **`mvn package` 前先确认 8090 没跑旧 jar**（Windows 会锁 `target/*.jar`）
- ⚠️ **判失败前先分清「真回归 / 脚本过时 / 数据残留」**

**⑥ 风险与开放问题**

| # | 事项 | 说明 |
| :--- | :--- | :--- |
| R1 | **锁掉真账号** | V1 会把账号锁 15 分钟。**必须只在测试账号上验**；若误锁真账号，清 `pkb:login:fail:18178325352` 即可解锁 |
| R2 | **`is-concurrent: false` 把用户挤下线** | 改完配置后，用户当前会话在新登录时会被踢（预期行为，但需提醒） |
| R3 | **V10 需要「未初始化」状态** | 要临时删用户真账号行 → 复刻 2026-10-15 演练的「备份哈希 → 删 → 验 → 恢复」流程；**或**改用「临时指向空测试库」的方式规避 |
| R4 | **测试账号残留** | 若验证中断，`user` 表可能留下测试行 → 每次验证开头/结尾都核对 `user` 表行数 = 1 |
| R5 | **token TTL 断言的时间窗** | Sa-Token 的 TTL 从签发时刻算，断言用「> 6.9 天且 ≤ 7 天」的区间，不用精确等值 |

**⑦ 开放问题（需用户拍板）**

- **Q1：要不要顺手加「修改密码」自助入口？**
  - 你的理由里提到「一直不重新登录我也容易忘记密码」—— 那么**主动改密**其实是更直接的解法
  - 但 P-011 的功能范围只有「引导页 + 登录」，**加改密属于扩范围**，按铁律 1 需你拍板
  - 倾向：**V0.1 不加**（保持 11 条 Must 不变），留 V0.2
- **Q2：验证工具放哪？**
  - 选项 A：`.workbuddy/skills/pkb-verify/`（被 `.gitignore` 忽略，**仅本机**，沿用 OSC 做法）
  - 选项 B：`tools/verify/`（入仓，换机器也有）
  - 倾向：**A**（验证脚本是本机环境相关的，入仓反而会漂）

#### T3 待办清单 —— 重置流程与解耦验证（据 2026-10-15 决策文档）

> ✅ **2026-10-15 已完成整轮实战演练**（详见下方「演练记录」）。以下为含实测结论的清单。

- [x] 引导页在「库里有账号」时**不出现** —— ✅ 真实账号恢复后 `/auth/init-status` 返回 `initialized:true`
- [x] 引导页在「库里无账号」时**出现** —— ✅ 硬删后返回 `initialized:false`（前端守卫据此送 `/init`）
- [x] 引导页的判定条件已查明 —— ✅ `AuthServiceImpl.initStatus()` = `userMapper.selectCount(null)`，**MP 自动追加 `is_deleted=0`**（故软删不会卡住引导页）
- [x] **硬删 `user` 行的命令已验证** —— ✅ 原生 `DELETE FROM user WHERE id = 1;` → `user_remain = 0`
- [x] **清 Redis session 的命令已验证** —— ✅ `pkb-token:login:session:1` + `pkb-token:login:token:1e6016…` 两条均清掉
- [x] 完整走一遍重置：删 user → 清 Redis → 刷新 → 引导页出现 → 建号成功
       —— ✅ 前四步全部实测通过；**「建号成功」这一步与用户已亲手跑通的引导页流程完全相同**，
       为避免在库里留测试账号，本轮**未实际建号**（建号能力已由用户本人验证过）
- [x] 重置完成后，**`note` / `tag` / `attachment` 行数不变** —— ✅ **核心保证实测成立**：删 user 行后四张业务表仍各 1 行（演练用的测试数据），一行未动
- [x] 重置完成后能重新登录，且能看到原来的笔记 —— ⚠️ **间接验证**：账号用**原哈希**恢复，用户可用**原密码**登录；「看到原笔记」待 T5/T6 有笔记功能后才有意义（当前业务表为空）

##### 演练记录（2026-10-15，全部真实执行）

| 步骤 | 操作 | 实测结果 |
| :--- | :--- | :--- |
| 0 | 备份真实账号 | `id=1 / phone=18178325352 / $2a$10$L7m…（60 位）` → 存 `%TEMP%\pkb-user-backup.txt` |
| 1 | 插入可控测试数据 | `note=1, tag=1, note_tag=1, attachment=1` |
| 2 | 硬删 user 行（原生 `DELETE`） | `user_remain = 0` |
| 3 | 清 Redis `pkb-token:*` | 2 条 key 清除，复查为空 |
| 4 | **核对解耦（核心保证）** | 四张业务表**仍各 1 行** —— 删账号**未动任何业务数据** ✅ |
| 5 | 起后端打接口 | `/auth/init-status` → `initialized:false`（引导页会出现） |
| 6 | 用**已删的旧 token** 访问 `/user/current` | `code 40100 未登录或登录已过期` —— 旧 token 确实失效，**不会出现「看起来仍登录」卡住流程** ✅ |
| 7 | 用备份哈希恢复账号 | `id=1` / 原哈希**逐字节写回** / `is_deleted=0` |
| 8 | 再打接口 | `/auth/init-status` → `initialized:true`（引导页不再出现） |
| 9 | 清理测试数据 | 四张业务表归 0；`osc.user` 仍 93 行（归档零影响） |

> ⚠️ **副作用（预期内）**：演练删过 Redis token，**用户会被踢下线一次，需重新登录**。原密码不变。

##### 测试安全约束（T3 后续验证务必遵守）

> ⚠️ `pkb.user` 里是**用户本人唯一的真实账号**（`18178325352`，BCrypt `$2a$10$`）。
> 任何删号类验证必须：① 先备份原始 `password` 哈希与 `phone`；② 验证完**恢复原始哈希**（保证原密码可用）；
> ③ 不留测试账号（`user` 表回到 1 行）；④ 清掉演练产生的业务测试数据。

#### T3 实现结果

> 状态：**已完成（2026-10-15）**。实测记录均为真实执行结果。

**① 配置变更（2 处文件、3 行）**

| 文件 | 改动 |
| :--- | :--- |
| `application.yml` | `sa-token.timeout` `2592000` → **`604800`**（7 天）；`sa-token.is-concurrent` `true` → **`false`**（禁多端） |
| `.env.example` | `SA_TOKEN_TIMEOUT` 同步改 `604800` + 注释写明「本文件会覆盖 yml 默认值，改有效期必须两处一起改」 |
| `server/.env`（不入仓） | ⚠️ **同样要改** —— 它是运行时覆盖层，只改 yml 不生效（这正是 T1 踩过的坑） |

**② 交付物**

| 类别 | 内容 |
| :--- | :--- |
| 验证工具（**不入仓**，`.workbuddy/skills/pkb-verify/`） | `SKILL.md`（按 PKB 重写）+ `cdp_run.cjs` / `lib.cjs` / `preview_server.cjs`（从 OSC 移植并适配）+ 新增 `t3_api_verify.cjs` / `t3_page_verify.cjs` |
| 生产代码 | **零改动**（T3 只改配置 + 验证，符合「P-011 主链路已通、T3 只做收紧」的判断） |

**③ 接口验证：19/19 通过**（`node t3_api_verify.cjs`，跑在隔离栈 8090）

| # | 验证项 | 实测结果 |
| :--- | :--- | :--- |
| V3 | 验证码错误 | `40001 验证码不正确或已过期`；**失败计数不累加**（`null`） |
| V2 | 验证码一次性 | 正确码可登录（`200`）；**同一 key 第二次 → `40001 验证码已过期，请点击图片刷新`** |
| V5 | **token TTL** | **`TTL=604781s（≈7.00 天）`** —— 配置真生效 ✅ |
| V4 | **禁多端** | 首 token 可用 → 二次登录成功 → **旧 token `40100`**、新 token 可用 ✅ |
| V6 | 登出 | 登出前可用 → `/auth/logout` 返回 `200 已退出登录` → **登出后 `40100`** ✅ |
| V1 | **失败锁定** | 前 4 次 `40002`、**第 5 次即达阈值 → `40003`**；Redis 计数 = 5 |
| V13 | 锁定文案 | 第 6 次（密码对也被锁）`40003`；文案「尝试次数过多，请 14 分钟后重试」**含剩余分钟数** ✅ |

**④ 页面验证：17/17 通过**（`node t3_page_verify.cjs`，CDP 无头 Chrome + dist 产物）

| # | 验证项 | 实测结果 |
| :--- | :--- | :--- |
| V7 | 守卫（未登录） | `/notes` `/projects` `/tags` 均弹回 **`/login?redirect=…`**（redirect 正确带上） |
| V9 | 已初始化访问 `/init` | 重定向到 `/login`，**且未渲染建号表单**（不得二次建号）✅ |
| V11 | 登录页交互 | 3 个输入框就位；验证码 base64 已加载；**点击可刷新（src 变化）**；真实填表 → **登录成功跳首页**、本地 token 长度 36 ✅ |
| V8 | 已登录访问 `/login` | 重定向到 `/` ✅ |
| V12 | token 失效 | 见下方「⚠️ 一条被证伪的假设」 |

**⑤ ⚠️ 一条被证伪的假设（重要，避免 T5/T6 重复踩）**

原设计 V12 要验「携带失效 token → axios 拦截器清态 + 跳登录」。**探针实测证明：T3 阶段这条不可能通过**：

```text
无效 token → /notes     落点 /notes     token 未被清
无效 token → /          落点 /          token 未被清
无效 token → /projects  落点 /projects  token 未被清
三次的 /api 请求都只有：GET /api/auth/init-status（免登录白名单）
```

**根因**：首页 / 笔记 / 项目 / 标签在 T3 阶段**全是占位页**（T5~T9 才实现），
没有任何页面会调用**需要鉴权**的接口 → 后端的 `40100` 在前端**无处触发**。

**这不是缺陷**，反而暴露了一个正确的设计事实：**路由守卫只判「本地 token 是否存在」，不打后端**
（否则每次导航都要多一次网络往返）。所以拿一个失效 token 仍能进占位页 —— 是预期行为。

**处置**：V12 拆成两半验（页面侧 + 接口侧），**端到端验证推迟到 T5/T6**（届时有了真接口）：

- V12a 守卫放行（本地有 token 即放行）✅ ← **⚠️ 2026-10-17 已失效，见下**
- V12b 该页无受保护接口调用 → token 未被清（**证明守卫不打后端**）✅ ← **⚠️ 已失效**
- V12c 后端对无效 token 返回 `40100` ✅
- ✅ **待 T5/T6 补验：真实接口返回 40100 → 拦截器清态 + 跳登录（2026-10-17 已在 T6 闭环）**

> 🔄 **2026-10-17 更新（T6 交付时）**：T6 起 `/notes` 会真的调 `/note/page`，
> 所以 V12a/V12b 记录的「旧行为」不再成立 —— 现在带无效 token 进 `/notes` 会
> **被 40100 拦回 `/login?redirect=/notes` 且本地 token 被清**。
> 这两条断言**已按新行为改写**（`t3_page_verify.cjs`），端到端完整覆盖见 `t6_page_verify.cjs` 的 M11。

**⑥ 验证过程中我自己踩的 3 个坑（已写进 pkb-verify/SKILL.md）**

1. **Redis `TTL` 是整数回复（`:604800`），不是 `$` 字符串回复** → 用 `redisScalar` 解析会得 `null`，
   第一版误报 `TTL=0s`。改用 `/:(-?\d+)/` 提取。
2. **登录页输入框要按 `placeholder` 定位**：探针实测 `[手机号, 密码, 验证码]`；
   按 `type !== 'password'` 过滤会同时命中手机号与验证码两个 text 框 → 把手机号当成了验证码填。
3. **验证码答案必须「先清空 `pkb:captcha:*` 再让页面取一张」**：
   否则 Redis 里可能同时有多张（实测命中 9 个），分不清哪把钥匙是页面的 → 取错答案、登录必失败。

**⑦ 测试数据与进程收尾**

| 项 | 结果 |
| :--- | :--- |
| 临时测试账号 `13900000001` | **已硬删**（原生 `DELETE`）；`user` 表回到 **1 行**（只剩用户真账号） |
| 用户真账号 `18178325352` | 未改密码、未删、**未被锁**（全程只在测试账号上跑锁定用例） |
| 四张业务表 | 全 **0** 行 |
| Redis | `pkb:*` **已清空**（验证码 / 失败计数 / token） |
| 归档库 `osc.user` | 仍 **93** 行（零影响） |
| 隔离栈进程 | 8090 / 5180 **已停**，端口已释放 |
| 临时文件 | `T3HashGen.java`、两个探针、`%TEMP%\pkb-*` **已删** |

**⑧ 副作用（预期内，需告知）**

- 改 `is-concurrent: false` 后，**用户下次登录会踢掉旧会话**；且清 Redis 已使其当前会话失效 → **需重新登录**（原密码不变）
- ⚠️ **操作提醒**：本轮验证我用了 `Start-Process` 起 8090 / 5180，会导致**弹出两个 CMD 窗口且不自动关闭**。
  用户已询问并确认可关闭。**后续 AI 自测请改用 `-WindowStyle Hidden` 或后台任务**，不要再弹窗干扰用户。

#### T3 验证指引（PowerShell 5.1 友好）

```powershell
# ---------- 0. 前置：MySQL / Redis 常驻（本任务不涉及 MinIO）----------

# ---------- 1. 只读核对：配置与库状态 ----------
$root = "F:\Project\Own Project\Personal-Knowledge-Base"
Select-String -Path "$root\server\src\main\resources\application.yml" -Pattern 'timeout|is-concurrent'
# 期望：timeout: ${SA_TOKEN_TIMEOUT:604800}  /  is-concurrent: false
Select-String -Path "$root\server\.env" -Pattern 'SA_TOKEN_TIMEOUT'
# 期望：SA_TOKEN_TIMEOUT=604800（⚠️ .env 会覆盖 yml，两处必须一致）

$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
& $mysql --user=root --password=root --table "--execute=USE pkb; SELECT id, phone, LEFT(password,7) AS pwd, LENGTH(password) AS len, status FROM user;"
# 期望：只有 1 行（你的真账号 18178325352 / $2a$10$ / 60 位）

& $mysql --user=root --password=root --table "--execute=USE pkb; SELECT (SELECT COUNT(*) FROM note) AS note,(SELECT COUNT(*) FROM tag) AS tag,(SELECT COUNT(*) FROM note_tag) AS note_tag,(SELECT COUNT(*) FROM attachment) AS attachment;"
# 期望：全 0

# ---------- 2. 起后端（你自己的 8080；server\ 下）----------
cd "$root\server"
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" package -DskipTests
& "C:\Program Files\Java\jdk-17\bin\java.exe" -jar "target\pkb-server-1.0.0.jar"

# ---------- 3. 另开窗口：验 token 有效期真为 7 天（最直接的配置生效证明）----------
#    先在前端登录一次（或走 /auth/captcha + /auth/login），然后：
& "C:\Program Files\Redis\redis-cli.exe" -h 127.0.0.1 -p 6379 KEYS "pkb-token:login:token:*"
& "C:\Program Files\Redis\redis-cli.exe" -h 127.0.0.1 -p 6379 TTL "<上面列出的 key>"
# 期望：TTL ≈ 604800 秒（7 天）

# ---------- 4. 验禁多端（命令行两步登录，旧 token 应失效）----------
#    两次登录各拿一个 token，用第一个打受保护接口：
Invoke-RestMethod http://127.0.0.1:8080/user/current -Headers @{ "pkb-token" = "<第一个 token>" } | ConvertTo-Json
# 期望：code = 40100（已被第二次登录踢掉）

# ---------- 5. 前端（web\ 下，另一个窗口）----------
cd "$root\web"
npm run dev
# 浏览器：未登录访问 http://127.0.0.1:5173/notes → 应弹回 /login?redirect=/notes
#         登录后访问 /login     → 应重定向到首页
#         已初始化访问 /init    → 应重定向到 /login
```

**验收判据**：
1. `application.yml` 与 `.env` 两处的 token 有效期都是 **604800**（7 天），`is-concurrent: false`
2. Redis 里 token 的 `TTL ≈ 604800`（配置真生效）
3. `user` 表 **1 行**（真账号）、四张业务表 **0 行**、`osc.user` 仍 **93**
4. 未登录访问 `/notes` `/projects` `/tags` 弹回 `/login?redirect=…`
5. 已登录访问 `/login` 跳首页；已初始化访问 `/init` 跳登录页
6. 登录页验证码可点击刷新，填表能登录成功并跳首页

---

### T4 外壳改造 ✅

> 状态：**已完成（2026-10-17）**。方案经用户确认（Q1 采纳 A：外壳去掉底栏、底部让给 T9 的内容 Tab；Q2 做反向验证）。

- **覆盖**：PRD §8.2（页面地图）、§2.2（单账号原则）、D-6（滚动模型）、P-010（首页形态）
- **依赖**：T3 ✅
- **开工前需用户拍板**：PRD #2 品牌主色 —— ✅ 已定（沿用 OSC 品牌蓝）；**另见下方「关键决策 Q1」**

#### T4 技术方案（⬜ 待用户确认 · 2026-10-17）

**① 先厘清：T1 已经做掉了 T4 定义里的大部分**

| 原定 T4 范围 | 实际状态 |
| :--- | :--- |
| 菜单砍到 3~4 项 | ✅ **T1 已完成**（首页 / 笔记 / 项目 / 标签） |
| `constants/roles.js` 整删 | ✅ **T1 已完成** |
| 守卫只留登录判定 | ✅ **T1 已完成**（只判「已初始化 + 已登录」），且 **T3 已实测通过** |
| 路由表重建 | ✅ **T1 已完成**（8 条路由） |
| **底部 3 Tab 改造** | ⬜ **T4 唯一未决项 —— 且与 PRD 有冲突，见 Q1** |

→ 所以 T4 的真实工作量是：**决定底部导航归属 + 落地 + 实测三档布局与滚动模型**。

**② 🔴 关键决策 Q1：手机端底部到底是什么？**

**冲突点**：PRD 冻结的「手机底部 3 Tab」说的是**首页的内容 Tab**，而当前外壳的底部 tabbar 是**导航**。两者都想占底部。

| 方案 | 做法 | 与 PRD 的关系 |
| :--- | :--- | :--- |
| **A（倾向）** | **外壳去掉底部 tabbar**，底部那个位置**留给 T9 首页的 3 Tab**；手机端导航一律走页头 ☰ 抽屉 | PRD P-010 原文「底部 3 Tab：最近 / 待整理 / 项目（**切 Tab 只换内容区**）」——**明确是内容级，不是路由级**。同一屏放两条底栏会吃掉 ~112px（375×812 屏上近 15%） |
| B | 外壳底部保留 3 项导航（首页/笔记/项目），T9 的 3 Tab 放**内容区顶部** | 与 PRD「底部」字面冲突 |
| C | 外壳底部 tabbar 直接做成 最近/待整理/项目 | **最危险**：切 Tab 会换路由，直接违反「只换内容区」；且 /notes /projects 页上这 3 Tab 语义不成立 |

**我的建议：A**。理由三条：
1. **PRD 原文支持**（「只换内容区」是决定性措辞）；
2. **屏幕经济学**：375px 宽上两条底栏不可接受；
3. **职责分离**：外壳只管布局与导航，内容 Tab 归页面 —— 与「加页面只改路由表一处」的既有约定一致。

**代价（必须接受）**：手机端 /notes /projects /tags 三页**没有底部导航**，只能点 ☰。这是常见移动端做法，可接受。

**③ 交付物（若采纳 A）**

| 文件 | 改动 |
| :--- | :--- |
| `layouts/AppShell.vue` | 删 `.app-tabbar` 模板块 + 相关样式；删 `.app-main` 的 `padding-bottom: 56px` 及 `@media(min-width:768px)` 补偿（不再需要为底栏留白）；**保留** `height:100% + overflow:hidden`、`.app-body`/`.app-main` 的 `min-height:0`、紧凑档 hover 浮层、☰ 抽屉 |
| `router/menu.js` | 删 `TABBAR_SIZE` 与 `tabbarItems`（不再有底部导航；T9 的 3 Tab 是**内容筛选**，自建，不复用菜单） |
| `layouts/PublicLayout.vue` | 无需改动（T1 已简化：i18n 已删、文案已改 PKB）—— 仅在验证时确认登录/引导页在 375px 下正常 |

**④ 不做（防越界）**

- ❌ **不做**首页的顶部输入框 / 状态条 / 三列 / 3 Tab（**那是 T9**）
- ❌ **不做**任何笔记/项目/标签的业务 UI（T5~T9）
- ❌ **不改**路由表结构（T1 已定型：8 条）
- ❌ **不动** `useLayoutMode` 的断点（768 / 1024 保持不变，`useIsMobile` 语义也不能动）

**⑤ 待实测清单（T4 的核心工作量）**

| # | 验证项 | 预期 | 手段 |
| :--- | :--- | :--- | :--- |
| W1 | **桌面档（1440）** | 左侧菜单完整展开（图标+文字）、**无折叠按钮**、无底部 tabbar | CDP 量 DOM + 截图 |
| W2 | **紧凑档（900）** | 侧栏收成 64px 图标态；组标题退化为分隔线；**hover 可临时展开浮层**；点折叠可固定展开 | CDP（`Input.dispatchMouseEvent` 真 hover，**不能用 `CSS.forcePseudoState`**——它不触发 `mouseenter`） |
| W3 | **手机档（375）** | 侧栏消失；**无底部 tabbar**（采纳 A 后）；页头 ☰ 可打开抽屉；抽屉含全部 4 项且点击可跳转 | CDP + 截图 |
| W4 | **滚动模型（最关键）** | 注入超长内容后：① `.app-main` 能滚（`scrollHeight > clientHeight`）；② **`document.documentElement` 本身不滚**（`body`/`html` 无溢出）；③ 页头位置不变（`getBoundingClientRect().top` 固定） | CDP 注入万字符内容后量 |
| W5 | **`min-height:0` 回归防护** | 临时移除 `.app-body` 的 `min-height:0` → 应观察到 W4 失效（证明该声明真的在起作用，不是抄来的教条） | 注入样式覆盖后对比（验完恢复） |
| W6 | **三档横滚** | 375 / 900 / 1440 三档下 `document.documentElement.scrollWidth === innerWidth`（无横向滚动） | CDP |
| W7 | **登录/引导页（PublicLayout）** | 375px 下不溢出、卡片居中、无页头页脚（`bare`） | CDP + 截图 |

**⑥ 风险**

| # | 风险 | 应对 |
| :--- | :--- | :--- |
| R1 | **删底部 tabbar 后手机端导航变弱** | 这是 Q1 的代价，需用户确认接受；☰ 抽屉保留全部 4 项入口 |
| R2 | 滚动模型改动容易「看着对、实际不滚」 | W4/W5 用**测量**而不是肉眼；W5 反向验证 `min-height:0` 真有用 |
| R3 | 紧凑档 hover 靠 `mouseenter`，CDP 假 hover 验不出 | 用 `Input.dispatchMouseEvent{type:'mouseMoved'}`（OSC 已踩过） |
| R4 | `.app-main` 的 `padding-bottom` 删早删晚 | 与 tabbar 同一次提交删，别留半截 |

#### T4 实现结果

> 状态：**已完成（2026-10-17）**。方案经用户确认（Q1 采纳 A / Q2 做反向验证）。

**① 改动（2 个文件，生产代码）**

| 文件 | 改动 |
| :--- | :--- |
| `layouts/AppShell.vue` | 删 `.app-tabbar` 模板块 + 4 组样式（`.app-tabbar` / `__item` / `__item.is-active` / 底部留白 media query）；删 `.app-main` 的 `padding-bottom: 56px`（不再需要为底栏留白）；**保留**滚动模型、紧凑档 hover 浮层 + 固定展开、☰ 抽屉 |
| `router/menu.js` | 删 `TABBAR_SIZE` 常量与 `tabbarItems` 计算属性（底部导航已无；T9 的 3 Tab 是内容筛选，自建） |

**② 验证：40/40 全过**（`node t4_shell_verify.cjs`，CDP + dist 产物，隔离栈 8090/5180）

| 组 | 关键实测 |
| :--- | :--- |
| **W1 桌面 1440** | 侧栏 208px、**无折叠按钮**、**无底栏**、菜单 4 项、品牌文字可见 |
| **W2 紧凑 900** | 侧栏 64px、文字隐藏、组标题压成 **1px** 细线；**真 hover（`Input.dispatchMouseEvent`）→ 208px 且 `position:absolute`（浮层不挤内容）**；移开收回 64px；点折叠 → 208px 且 `position:static`（固定展开留在文档流） |
| **W3 手机 375** | 无侧栏、**无底栏**、有 ☰；无横滚；☰ 打开抽屉 → **4 项齐全（首页/笔记/项目/标签）**；点第 4 项 → `/tags` 且抽屉自动关闭 |
| **W4 滚动模型** | 注入 3 屏长内容后 `scrollH=3029 > clientH=849`；`scrollTop=500` 真生效；**页头 top 恒为 0（固定）**；`documentElement.scrollTop=0` 且 `htmlScrollH=900=innerH`（滚动被关在 `.app-main` 里） |
| **W5 反向验证** | 见下 ③ |
| **W6 三档横滚** | 1440 / 900 / 375 下 `scrollWidth === innerWidth` |
| **W7 登录页 375** | `bare` 生效（无页头页脚）、卡片宽 343 ≤ 375、无横滚 |

**③ 🔴 W5 反向验证推翻了一条「继承来的传说」——这是 T4 最有价值的产出**

**原本的认知**（沿自 OSC）：「`.app-body` 与 `.app-main` 的 `min-height:0` 是承重件，删了内部滚动就失效」。

**实测（逐项拆开量）**：

| 改法 | `.app-main` clientH | `scrollTop=400` 是否生效 | `.app-body` scrollH |
| :--- | ---: | :---: | ---: |
| 基线（两者都有 `min-height:0`） | 849 | ✅ | 900 |
| 只移除 `.app-body` 的 | 849 | ✅ | 900 |
| 只移除 `.app-main` 的 | 849 | ✅ | 900 |
| **两者都移除** | **849** | **✅** | **900** |
| **只移除 `overflow-y:auto`**（保留 min-height） | 849 | **❌ 0** | **3080** |
| **移除 `overflow-y` + 两个 min-height** | **3029** | **❌ 0** | 3080 |

**真实机制（三条）**：
1. **真正的承重件是 `.app-main { overflow-y: auto }`** —— 去掉它就滚不动。
2. 按 Flexbox 规范，**`overflow` 非 `visible` 的 flex 项，其自动最小尺寸恒为 0** → 在 `overflow:auto` 下，`min-height:0` **是冗余的**。
3. `min-height:0` **只在 `overflow: visible` 时才起作用**（全拆后 clientH 从 849 涨到 **3029**）。

**对 T9（首页三列各自滚动）的指导意义（关键）**：
> **每一列都必须自己带 `overflow-y: auto`（或 `overflow: hidden`）。**
> 只在嵌套容器上堆 `min-height:0`、而不给 `overflow`，**不会**产生内部滚动。

→ 已同步修正交接文档 §四 与 `pkb-verify/SKILL.md` 里对应的表述，避免 T9 照着旧说法做无效功。

**④ 测试数据与进程收尾**

| 项 | 结果 |
| :--- | :--- |
| 临时测试账号 `13900000001` | **已硬删**；`user` 表 = **1 行**（用户真账号，未改未删未锁） |
| 业务表 | 全 0 行 |
| Redis | `pkb:*` 已清空 |
| 归档库 | `osc.user` 仍 **93** 行 |
| 隔离栈 | 8090 / 5180 已停，端口已释放 |
| 临时文件 | `tmp_probe_minheight.cjs` 已删；`t4_shell_verify.cjs` 作为长期脚手架保留（`.workbuddy`，不入仓） |

**⑤ 过程改进（上轮用户反馈的落实）**

- 本轮起常驻进程一律 `Start-Process -WindowStyle Hidden` + 输出重定向 → **全程没有弹出任何 CMD 窗口** ✅

**⑥ 🔴 用户截图抓到一个我引入的真实 bug（已修，T4 交付后追加）**

**症状**（用户反馈）：打开页面后「除了一个导航栏之外整个页面空白」。

**根因**：我在 T1 重建路由时留下了**两个 `path: '/'` 的记录**：

| 记录 | 路径 | 子路由 |
| :--- | :--- | :--- |
| `PublicLayout`（**先声明**） | `/` | `login` / `init`（**没有空路径子路由**） |
| `AppShell` | `/` | `''`（首页）等 |

Vue Router 会取**先声明**的那条来匹配 `/` → `/` 命中的是 `PublicLayout`（它 meta 里**没有 `bare`**）
→ 渲染出**页头 + 页脚 + 空 `router-view`**。用 `router.resolve('/')` 实测确认：
修复前只返回 1 条记录（PublicLayout），**叶子路由不是 `home`**。

**为什么「未登录看不到、登录后看到」**：守卫对未登录用户会把人弹到 `/login`，
所以空白只出现在**本地有 token** 的人身上 —— 而用户此时恰好**仍留着旧 token**（见 ⑦）。

**修复（2 处）**：
1. `router/index.js`：公开页改为**每个页面各自挂 `PublicLayout`**（`path: '/login'` + 子路由 `path: ''`；
   `path: '/init'` + 子路由 `path: ''`），父路径不再与 `/` 冲突。
2. `router/guard.js`：引导页判定从 `to.path === '/init'` 改为 **`to.name === 'initAdmin'`** ——
   原写法漏掉 `/init/`（带尾斜杠），实测那样会绕过重定向、仍渲染建号表单。

**修复后实测（12/12 通过）**：
`/` 的叶子路由是 `home`；已登录访问 `/` 渲染 `AppShell` + **内容 199 字符**（不再空白）、菜单 4 项、页头「首页」；
`/login` 仍 `bare` + 登录卡片；`/init` 与 **`/init/` 均重定向 `/login`**；未登录 `/` → `/login?redirect=/`。

**⑦ 用户反馈的另一件事：T3 预测的「被踢下线」没有发生**

用户说「我没有像 T3 说的那样被踢出登录状态」。**这与 T3 的实现结果 ⑤ 完全一致，不是矛盾**：

- 我在 T3 收尾时清了 Redis（`pkb-token:login:*`），**服务端确实已失效**；
- 但**前端没有任何页面会调需要鉴权的接口**（占位页只调免登录的 `/auth/init-status`），
  且**路由守卫只判「本地 token 是否存在」、不打后端** → 所以**前端永远不会发现**；
- 结果：token 已死但界面看起来还登录着，直到将来某个页面真的调接口才暴露。

→ **这是已知缺口，T3 已登记「40100 端到端验证推迟到 T5/T6」**。
**是否需要提前修**（在守卫里加一次性 `/user/current` 校验）已提交用户决策，见 ⑧。

**⑧ 「失效 token 自动登出」的处置（2026-10-17 用户拍板：维持现状 B）**

| 选项 | 做法 | 代价 | 结论 |
| :--- | :--- | :--- | :--- |
| A | 守卫在进入内部路由时，**每个会话首次**调一次 `/user/current` 校验 token；失败则清态跳登录 | +1 次请求；约 10 行 | ❌ 不采用（本轮） |
| **B** | 维持现状，**T5/T6 有真接口后自然暴露**（原计划） | 期间「死 token 看着像登录」会一直存在 | ✅ **用户拍板采用** |

> 理由：T5 起每个页面都会调接口，这个现象会自然消失，不值得为过渡期加一次额外请求。
> **T6 仍须补验 40100 端到端**（拦截器清态 + 跳登录），这条不因此取消。

**⑨ 本轮还修掉一个移植遗留**

`pkb-verify/scripts/lib.cjs` 里的默认夹具口令残留着 OSC 的 `OscTest#2026`（应为 `PkbTest#2026`），
导致截图时出现一次 **假的「密码错误」**。已改正并加注释（该口令必须与 `SKILL.md` 的约定一致）。

#### T4 验证指引（PowerShell 5.1 友好）

```powershell
$root = "F:\Project\Own Project\Personal-Knowledge-Base"

# ---------- 1. 确认 tabbar 相关代码已彻底移除 ----------
Select-String -Path "$root\web\src\layouts\AppShell.vue","$root\web\src\router\menu.js" -Pattern 'tabbarItems|TABBAR_SIZE|app-tabbar'
# 期望：只在注释里出现（说明「为什么没有」），无实际代码/样式

# ---------- 2. 确认 .app-main 不再为底栏留白 ----------
Select-String -Path "$root\web\src\layouts\AppShell.vue" -Pattern 'padding-bottom'
# 期望：无命中（底栏留白已删）

# ---------- 3. 起后端 + 前端，肉眼验三档 ----------
cd "$root\server"
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" package -DskipTests
& "C:\Program Files\Java\jdk-17\bin\java.exe" -jar "target\pkb-server-1.0.0.jar"

cd "$root\web"
npm run dev
# 浏览器开 http://127.0.0.1:5173 并登录，然后：
#   ① 窗口拉到 ≥1024：左侧菜单完整、**没有折叠按钮**、**页面底部没有导航条**
#   ② 拉到 900：侧栏变 64px 图标；鼠标移到侧栏上 → 浮层展开 208px；移开收回
#                 点页头 ☰ → 固定展开（这次会把内容推开）
#   ③ 拉到 375：左侧菜单消失、**底部没有导航条**；点页面左上角 ☰ → 左侧抽屉，含首页/笔记/项目/标签 4 项
#   ④ 在任意内部页把窗口高度压到 400px：内容区自己滚，页头不动（滚动被关在内容区）

# ---------- 4. 验「底栏留白已去掉」（可选，量化）----------
#    F12 → Console 执行：
#    document.querySelector('.app-main').style.paddingBottom   // 期望空串或 0px
#    getComputedStyle(document.querySelector('.app-main')).overflowY  // 期望 auto
```

**验收判据**：
1. `AppShell.vue` / `menu.js` 里 `tabbarItems` / `app-tabbar` **只存在于注释**，无实际代码与样式
2. `.app-main` **没有** `padding-bottom` 为底栏留白
3. 桌面无折叠按钮、无底栏；紧凑档 hover 浮层 + 点按固定展开都正常；手机档无底栏、☰ 抽屉含 4 项
4. 长内容页面：**内容区滚、页头不动、整页不滚**

---

### T5 笔记 CRUD（后端） ✅

> 状态：**已完成（2026-10-17）**。#13 已拍板（方案 A）；实测记录见「T5 实现结果」。

- **覆盖**：P-001（快速记录）、P-005（待整理闭环）；**含列表接口**（P-002 的后端部分 —— T6 只做前端）
- **依赖**：T2 ✅
- **范围**：新增 / 详情 / 列表 / 更新 / 归档 / 取回 / 置顶 / 逻辑删除；**`content_text` 收口在 Service 层**；`content_format` 建后不可改（显式拦截）
- **风险**：R1（漏同步导致搜索漏数据，且不报错）

#### T5 技术方案（✅ 已确认 · 2026-10-17）

**① 接口设计（8 个端点，全部需登录，不在白名单）**

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| POST | `/note` | 新建。默认 `status=0`（inbox）、`pinned=0`、`content_format=0`（md） |
| GET | `/note/{id}` | 详情（**含全文** `content`） |
| GET | `/note/page` | 列表：分页 + `status` 筛选；**不返回正文**，只返回摘要 |
| PUT | `/note/{id}` | 更新 `title` / `content` / `pinned`；**`contentFormat` 只允许与现值相同** |
| POST | `/note/{id}/archive` | 归档（`status=1`，**幂等**） |
| POST | `/note/{id}/unarchive` | 取回（`status=0`，幂等） |
| PUT | `/note/{id}/pinned` | 置顶开关，body `{pinned:0|1}` |
| DELETE | `/note/{id}` | **逻辑删除**（`@TableLogic` → `is_deleted=1`） |

**② #13 已拍板：md 去标记采用方案 A**（用户 2026-10-17 选定）

规则（即当前 `ContentTextBuilder.Default` 的行为，**本轮定稿**）：
剥 `#{1,6}` 标题标记、`>` 引用、`-`/`*`/`+` 无序列表、`1.` 有序列表、`**`/`*`/`_`/`~~`/反引号 行内标记；
剥水平分割线；剥代码围栏行但**保留围栏内代码内容**；链接 `[文字](url)` **保留文字、丢弃 URL**；图片 `![alt](url)` **整体丢弃**；连续空白归一为单空格。

**被否决的备选**：B（代码块内容也不进搜索列 —— 技术笔记最想搜的代码会搜不到）、C（只剥 `#` 和 `**` —— `-`/`>`/反引号 仍留在搜索列里产生噪声）。

**③ 铁律落地（三条，全部要在代码里显式体现）**

| 铁律 | 落地方式 |
| :--- | :--- |
| ③ `content_text` 同步 | 笔记的新增 / 更新**只经 Service 层一个私有方法** `applyContentText(note)` 计算（内部调 `ContentTextBuilder`）。**禁止 Mapper 层散落写**。更新时**每次都重算**（最稳，不做「内容没变就跳过」的优化） |
| ① 格式不可切换 | `NoteUpdateRequest` 带 `contentFormat` 字段；若**非空且与现值不同** → 抛 `40000`「笔记格式创建后不可修改」。**刻意不静默忽略**（静默会让调用方以为改成功了） |
| 排序口径 | 列表固定 `ORDER BY pinned DESC, updated_at DESC`（不在接口暴露排序参数，防漂移） |

**④ 关键实现细节**

- **`updated_at` 必须跟着业务变化走**：归档 / 取回 / 置顶走 `update(null, wrapper)`，**MetaObjectHandler 不会自动填充**（它只在有实体入参时生效）→ **必须在 wrapper 里显式 `.set(Note::getUpdatedAt, now)`**，否则排序会失真。
- **列表不传正文**：用 `select(Note.class, i -> !"content".equals(i.getColumn()))` 排除 `content`；摘要取 `content_text` 前 200 字（Java 侧截断）。
- **`title` 超长截断**到 255（PRD P-001 验收明确要求「截断存入、全文仍进 content」），截断时打 WARN 日志。
- **标题与正文不能同时为空** → `40000`。
- **`content_format` 只接受 0/1**，其余 `40000`。
- **找不到 / 已逻辑删除** → `40400`（`@TableLogic` 自动过滤，天然够用）。
- **列表分页参数**：`page ≥ 1`（默认 1）、`size` 1~100（默认 20），越界 `40000`。

**⑤ 本轮不做（防越界）**

- ❌ 搜索（`title + content_text`）—— **T10**
- ❌ 项目 / 标签关联与筛选 —— **T8**
- ❌ 附件 —— **T11**
- ❌ 任何前端页面改动 —— **T6**
- ❌ 40100 端到端（拦截器清态跳登录）—— 需**前端页面真的调接口**，仍归 **T6**

**⑥ 待实测清单**

| # | 验证项 | 预期 |
| :--- | :--- | :--- |
| N1 | 新建 md 笔记 | 201/200；`status=0`、`pinned=0`、`format=0`；`content_text` 已按 A 规则生成 |
| N2 | 新建 html 笔记 | `content_text` 为剥标签后的纯文本（script 内容不进） |
| N3 | 标题与正文都为空 | `40000` |
| N4 | 非法 `contentFormat`（如 5） | `40000` |
| N5 | 详情 | 返回全文 `content` |
| N6 | 更新正文 | `content_text` **同步更新**、`updated_at` 变新 |
| N7 | 更新时改格式 | `40000`「格式创建后不可修改」 |
| N8 | 更新时传相同格式 | 成功（不报错） |
| N9 | 归档 | `status=1`；**重复归档幂等**（仍成功） |
| N10 | 取回 | `status=0` |
| N11 | 置顶 | `pinned=1`，且**列表里排最前** |
| N12 | 列表筛选 | `status=0` / `status=1` 各只返回对应状态 |
| N13 | 列表排序 | 置顶优先，其次 `updated_at` 倒序 |
| N14 | 列表**不含正文** | 响应里无 `content` 字段，只有 `excerpt` |
| N15 | 逻辑删除 | 删后详情 `40400`、列表不再出现；库里 `is_deleted=1` 且**行仍在** |
| N16 | 超长标题 | 入库 `title` 长度 = 255，`content` 完整 |
| N17 | 未登录访问任一 `/note` 接口 | `40100` |
| N18 | 分页越界（size=0 / page=0） | `40000` |

#### T5 实现结果

> 状态：**已完成（2026-10-17）**。#13 已拍板（方案 A）；实测记录均为真实执行结果。

**① 交付物（9 个新文件）**

| 类别 | 文件 |
| :--- | :--- |
| Controller | `controller/NoteController.java`（8 个端点） |
| Service | `service/NoteService.java` + `service/impl/NoteServiceImpl.java` |
| DTO | `NoteCreateRequest` · `NoteUpdateRequest` · `NotePinnedRequest` · `NoteQuery` · `NoteVO` · `NoteListItemVO` |

**② 实测：接口 33/33 + 落库核对 4/4 全过**

接口侧（`node t5_note_verify.cjs`，隔离栈 8090）：

| 组 | 关键实测 |
| :--- | :--- |
| 新建 | md 默认 `status=0/pinned=0/format=0`；html 新建 `format=1` |
| 校验 | 标题正文全空 → `40000 请填写标题或正文`；格式=5 → `40000`；pinned=5 → `40000`；page=0 / size=0 / size=101 → `40000` |
| 详情 | 返回完整 `content`（110 字符） |
| 更新 | 正文更新成功、`updated_at` 变化；**改格式 → `40000 笔记格式创建后不可修改`**；传相同格式 → 成功 |
| 归档闭环 | 归档 `status=1`；**重复归档幂等**；取回 `status=0` |
| 置顶 | `pinned=1`；**列表第一项就是它**；非法值 `40000` |
| 列表 | 排序 = `pinned DESC, updatedAt DESC`（逐条比对通过）；状态筛选 0/1 各自正确；**列表项不含 `content`**、含非空 `excerpt` |
| 逻辑删除 | 删除后详情 `40400`、列表不再出现；**库里 `is_deleted=1` 行仍在** |
| 超长标题 | 入库 `title` 长度 = **255**，正文完整 |
| 未登录 | 任一 `/note` 接口 → `40100` |

落库侧（`mysql` 直查）：**「有正文但 `content_text` 为空」的行数 = 0**；html 笔记 `content_text = [正文 加粗 第二段]`（**`<script>` 内容未进搜索列**）；逻辑删除行仍在库；`content_format` 建后未被改动。

**③ 🔴 R1 真实踩坑（本任务最重要的产出）**

**现象**：第一次跑验证，`excerpt` 是**空的** —— 差一点就当作「摘要没实现」放过去了。
查库发现 **id 9 的 `content` 有 20 字，`content_text` 是 0 字**。

**根因**：`update()` 里我用 **`patch.content`** 去算 `content_text`。
当请求**只改标题 / 只改置顶、不带 content** 时，`patch.content` 是 `null`
→ `build(null)` 返回 `""` → **把库里已有的 `content_text` 覆盖成空**。

这正是 PRD 风险 R1 描述的场景：**搜索列静默变空，不报错，只有搜索少结果时才发现**。

**修复**：用**「最终生效的内容」**算 —— 没传 content 就用库里的旧值：
```java
String finalContent = request.content() != null ? patch.getContent() : note.getContent();
patch.setContentText(contentTextBuilder.build(finalContent, note.getContentFormat()));
```

**配套加了三道回归断言**（否则同类 bug 还会漏）：
- `N6c/N6d`：只改标题后，正文**不能被清空**
- `N14b`：列表 `excerpt` **必须非空**（原断言只查字段存在 → 会漏掉「字段在但值为空」）
- 落库断言：「有 content 但 content_text 为空的行数 = 0」

**④ 另一个坑：构建失败但验证照跑，结果验的是旧 jar**

第一次重编译时报 `Failed to delete target\pkb-server-1.0.0.jar`（**旧进程占着 jar**，Windows 文件锁），
而我的验证脚本**没检查构建退出码**就继续跑 → 拿旧 jar 验证 → 假失败，白折腾一轮。

**规则**：本地验证的固定顺序必须是
**停进程 → 构建 → 检查退出码为 0 → （可选）确认 jar 时间戳 → 起新进程 → 再验证**。

**⑤ 测试数据与进程收尾**

| 项 | 结果 |
| :--- | :--- |
| 测试账号 `13900000001` | 已硬删；`user` 表 **1 行**（用户真账号，未动） |
| `note` / `tag` / `note_tag` / `attachment` | 全 **0** 行（含逻辑删除行已物理清理） |
| Redis | `pkb:*` 已清空 |
| 归档库 | `osc.user` 仍 **93** |
| 隔离栈进程 | 8090 已停，端口已释放 |
| 用户自己的实例 | 5173 / 8080 **全程未动** |

**⑥ 遗留到 T6 的**

- **40100 端到端**（前端拦截器收到 40100 → 清本地态 + 跳登录）：本任务已提供真接口，
  但**目前没有页面调它**，仍需 T6 让 `NotesView` 真的调 `/note/page` 才能验。
- UI 文案 / 交互：归 T6。

#### T5 验证指引（PowerShell 5.1 友好）

```powershell
$root = "F:\Project\Own Project\Personal-Knowledge-Base"
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

# ---------- 1. 只读核对：代码结构与铁律落地 ----------
# ① content_text 的计算入口只有 Service 层（Mapper 里不应出现）
Select-String -Path "$root\server\src\main\java\com\pkb\**\*.java" -Pattern 'contentText|ContentText' -List |
  ForEach-Object { "  " + (Split-Path $_.Path -Leaf) }
# 期望：只有 NoteServiceImpl / Note / ContentTextBuilder(Config) 相关文件命中

# ② 格式不可改的拦截在 Service 里
Select-String -Path "$root\server\src\main\java\com\pkb\service\impl\NoteServiceImpl.java" -Pattern '格式创建后不可修改'

# ---------- 2. 起后端（⚠️ 顺序：先确认 8090 无占用，再构建）----------
Get-NetTCPConnection -LocalPort 8090 -State Listen -ErrorAction SilentlyContinue |
  ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
cd "$root\server"
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "构建失败，先查原因（多半是旧进程占着 jar）" }
& "C:\Program Files\Java\jdk-17\bin\java.exe" -jar "target\pkb-server-1.0.0.jar" --server.port=8090

# ---------- 3. 另开窗口：手点几个关键接口（用你自己 8080 的实例也行）----------
# 先登录拿 token（浏览器 F12 → Application → localStorage → pkb_token），然后：
$token = "<粘贴你的 pkb_token>"
$h = @{ "pkb-token" = $token; "Content-Type" = "application/json" }

# 建一条笔记
$body = @{ title = "T5 手验"; content = "# 标题`n`n正文 **加粗**" } | ConvertTo-Json
$created = Invoke-RestMethod -Uri http://127.0.0.1:8080/note -Method POST -Headers $h -Body $body
$created | ConvertTo-Json -Depth 5
# 期望 code=200，data.status=0、pinned=0、contentFormat=0

# 查它落库后的 content_text（关键：不能为空）
& $mysql --user=root --password=root --default-character-set=utf8mb4 --table `
  "--execute=USE pkb; SELECT id, CHAR_LENGTH(content) AS c_len, CHAR_LENGTH(content_text) AS t_len, LEFT(content_text,40) FROM note;"
# 期望 t_len > 0

# 列表（不含正文）
Invoke-RestMethod -Uri "http://127.0.0.1:8080/note/page?page=1&size=10" -Headers $h | ConvertTo-Json -Depth 5
# 期望 records 里没有 content 字段、excerpt 非空

# 归档 → 取回 → 置顶
Invoke-RestMethod -Uri "http://127.0.0.1:8080/note/$($created.data.id)/archive" -Method POST -Headers $h | ConvertTo-Json
Invoke-RestMethod -Uri "http://127.0.0.1:8080/note/$($created.data.id)/unarchive" -Method POST -Headers $h | ConvertTo-Json

# 手验完记得清掉（否则会占用你的真库）：
& $mysql --user=root --password=root "--execute=USE pkb; DELETE FROM note WHERE title='T5 手验';"
```

**验收判据**：
1. 新建笔记后，库里 `content_text` **非空**且符合方案 A 的规则（剥标记、保留代码内容、链接留文字）
2. 只改标题（不带 content）后再查库，`content_text` **仍非空**
3. 改 `contentFormat` 返回 `40000 笔记格式创建后不可修改`
4. 归档/取回幂等；置顶后列表第一项是它
5. 列表响应**没有 `content` 字段**；删除后详情 `40400` 且库里 `is_deleted=1`

---

### T6 笔记列表 + 详情（前端） ✅

> 状态：**已完成（2026-10-17）**。页面实测 31/31；顺带补验掉 T3 挂账的 40100 端到端。

- **覆盖**：P-002（笔记列表）、P-003（笔记详情 / 编辑）
- **依赖**：T5 ✅
- **范围**：`NotesView` 列表（筛选 / 排序 / 分页 / 行内操作）、`NoteDetailView` 全屏页、桌面 `el-drawer`、**两处复用同一份表单组件**
- **关键坑**：`el-drawer` 关闭是 `v-show`，验证「已关闭」用 `!!el.offsetParent`
- **另一项挂账**：**补验 40100 端到端**（T3 起挂账 —— 现在有真接口了）

#### T6 技术方案（✅ 用户已授权开发）

**① 交付物**

| 文件 | 作用 |
| :--- | :--- |
| `web/src/api/note.js` | 8 个接口的前端封装 |
| `web/src/composables/useNoteDrawer.js` | **抽屉的模块级单例状态**（open / close / version）。列表页与 T9 首页共用，避免各维护一份 |
| `web/src/components/NoteForm.vue` | **唯一的一份表单**（标题 / 正文 / 置顶 / 格式徽标 / 保存 / 归档 / 删除 / 未保存拦截） |
| `web/src/components/NoteDrawer.vue` | 桌面抽屉外壳，内嵌 `NoteForm` |
| `web/src/views/notes/NotesView.vue` | 列表页（**重写**占位实现） |
| `web/src/views/notes/NoteDetailView.vue` | 手机全屏页（**重写**占位实现） |
| `web/src/layouts/AppShell.vue` | 挂载 `<NoteDrawer />`（任何内部页都能唤起抽屉，T9 直接复用） |

**② 关键设计**

- **桌面 vs 手机**（PRD D-7）：`useLayoutMode() === 'mobile'` → 路由到 `/note/:id`；否则 `useNoteDrawer().open(id)`。
- **新建**：桌面 → 抽屉（id=null）；手机 → `/note/new`（`NoteDetailView` 把 `new` 当创建态）。
- **列表形态**：**统一卡片流**（不区分桌面表格 / 手机卡片）—— 少一套实现，天然无横向滚动。
- **`NoteForm` 自带取数**：按 `noteId` 自己拉详情，父组件只负责「打开哪个 id」。
- **保存后不关抽屉**：创建成功后把内部 `currentId` 切到新 id（否则再点保存会重复创建），并 `bump()` 让列表刷新。
- **未保存拦截**：dirty 用「当前值 vs 快照」比较；抽屉 `before-close` 与全屏页返回都二次确认（PRD P-003）。
- **编辑器本轮用 `textarea`**：md 编辑器与富文本是 **T7** 的事；本轮只保证「能读能写、格式只读展示」。

**③ 本轮不做（防越界）**

❌ md / 富文本编辑器（T7）｜❌ 项目 / 标签（T8）｜❌ 附件（T11）｜❌ 首页三列（T9）｜❌ 搜索（T10）｜❌ 不改后端接口

**④ 待实测**

| # | 验证项 | 预期 |
| :--- | :--- | :--- |
| M1 | 列表加载 | 卡片流渲染（标题/摘要/格式徽标/状态/时间） |
| M2 | 状态筛选 | 全部 / 待整理 / 已归档 切换后列表随之变化 |
| M3 | 分页 | 超过 20 条时出现分页，翻页正常 |
| M4 | 桌面点卡片 | 打开 `el-drawer`，字段回填正确 |
| M5 | 手机点卡片 | 跳到 `/note/{id}` 全屏页，字段与桌面一致 |
| M6 | 新建并保存 | 列表出现新笔记；**再点保存不会重复创建** |
| M7 | 归档 / 取回 | 行内操作生效，列表与筛选同步 |
| M8 | 置顶 | 操作后该项排到最前 |
| M9 | 删除 | 二次确认 → 移除 → 列表刷新 |
| M10 | 未保存关闭 | 有改动时关闭抽屉 / 返回，弹二次确认 |
| M11 | **40100 端到端** | token 改坏 → 列表请求收到 40100 → **自动清态并跳登录** |
| M12 | 375 宽 | 卡片流无横向滚动 |

#### T6 实现结果

> 状态：**已完成（2026-10-17）**。页面实测 **31/31 全过**（CDP + dist 产物 + 隔离栈 8090/5180），
> **同时补验掉了 T3 起挂账的「40100 端到端」**。

**① 交付物（7 个文件：4 新增 / 3 修改）**

| 文件 | 说明 |
| :--- | :--- |
| `web/src/api/note.js` | 新增：8 个接口封装 |
| `web/src/composables/useNoteDrawer.js` | 新增：抽屉的**模块级单例状态**（open / close / version） |
| `web/src/components/NoteForm.vue` | 新增：**全站唯一的一份笔记表单**（自带取数 / 脏检查 / 归档 / 置顶 / 删除） |
| `web/src/components/NoteDrawer.vue` | 新增：桌面抽屉外壳（标题栏 + 关闭按钮 + 关闭前拦截） |
| `web/src/views/notes/NotesView.vue` | 重写：列表（筛选 / 卡片流 / 分页 / 行内操作） |
| `web/src/views/notes/NoteDetailView.vue` | 重写：手机全屏页（`/note/:id` + `/note/new`） |
| `web/src/layouts/AppShell.vue` | 修改：挂载 `<NoteDrawer />`（任何内部页都能唤起，T9 直接复用） |
| `web/src/api/request.js` | 修改：40100 时**同时清 Pinia 登录态**（见 ③-1） |
| `server/.../NoteServiceImpl.java` | 修改：置顶/归档**不再刷 `updated_at`** + 排序加 `id DESC` 兜底（见 ③-2、③-3） |

**② 实测 31/31（M1~M12 全覆盖）**

| 组 | 实测 |
| :--- | :--- |
| M1 列表 | 20 条/页、总数「共 25 条」、徽标 40 个、摘要非空、**「全部」默认激活**（`activeCount=1`） |
| M2 筛选 | 已归档 1 条 / 待整理 20 条（共 24）/ 全部恢复 20 条 |
| M3 分页 | 分页器出现；翻到第 2 页 5 条 |
| M4 抽屉 | `offsetParent` 判定可见、标题/正文回填、格式徽标 `Markdown` |
| M5 手机 | 跳 `/note/{id}`、渲染同一份表单、字段与列表一致、**不使用抽屉** |
| M6 新建 | **连点两次保存只创建 1 条**（创建后切到真实 id，防重复） |
| M8 置顶 | 置顶项排最前；**取消置顶后不再最前**（`T6-25` 顶上来） |
| M9 删除 | 二次确认 → 总数 −1 |
| M10 未保存 | 出现「有未保存的修改」→ 关闭弹确认 →「继续编辑」保持打开 →「丢弃修改」才关 |
| M11 **40100** | token 改坏 → **清态 + 跳 `/login?redirect=/notes`** |
| M12 375 宽 | `scrollWidth === innerWidth`（375），无横向滚动 |

**③ 🔴 实测抓到 4 个真问题（全部已修）**

**1）40100 拦了，但页面落到首页而不是登录页（T3 挂账项，终于验出来）**

- **现象**：token 失效后 `localStorage` 被清空，但页面停在 `/` 而不是 `/login`。
- **根因**：`request.js` 只清了 `localStorage`，**没清 Pinia 里的 `token` ref**；
  路由守卫读的是 store → 仍认为「已登录」→ 按「已登录不许看登录页」把 `/login` 又弹回首页。
- **修复**：`redirectToLogin()` 里动态引入 `useUserStore()` 并 `clear()`，再清持久化、再 `router.replace`。
- **这也解释了用户此前的疑问**（「我没被踢下线」）：T3 阶段没有页面调受保护接口，前端根本发现不了 token 已死。

**2）置顶/归档会把笔记顶到「最近」最前面**

- **现象**：取消置顶后，那条笔记仍然排第一。
- **根因**：状态更新走 `update(null, wrapper)`，而我上一版**显式写入了 `updated_at = now`**；
  叠加表上的 `ON UPDATE CURRENT_TIMESTAMP`，于是「置顶」被当成一次「编辑」。
- **修复**：改为**显式把 `updated_at` 写回原值**（不是 now，也不是不写 —— 不写 MySQL 也会自动刷）。
- **定稿**：**归档 / 取回 / 置顶都不算「编辑过」**，不动 `updated_at`（PRD P-005 曾标为待确认，此处关闭）。

**3）排序没有兜底键，同一秒创建的笔记顺序不稳定**

- **现象**：25 条笔记的 `updated_at` 完全相同（DATETIME 秒级精度），列表顺序在两次请求间会变。
- **修复**：排序追加 `id DESC` 兜底 → `pinned DESC, updated_at DESC, id DESC`（**确定性排序**）。

**4）筛选按钮「看不出当前选的是哪个」**（放大截图才发现）

- **现象**：功能正常（点击能筛），但三个按钮**全灰**，没有激活态。
- **根因**：用 `:value="null"` 表示「全部」，而 EP 的 `el-radio-group` 拿 `modelValue === value` 比，
  `null` 被当成「未选中」。
- **修复**：改用字符串哨兵 `'all'`，提交给后端时再映射回 `null`。

**另外两处小修（截图评审发现）**：
- 抽屉原先 `:with-header="false"`，**没有可见的关闭按钮**（只能 Esc / 点遮罩）→ 补上标题栏 + 关闭按钮。
- 手机全屏页的页内标题与应用页头都写「笔记详情」，一行里重复 → 页内改为「新建笔记 / 编辑笔记」。

**④ 本轮刻意没做**

- md 渲染 / md 编辑器 / 富文本 → **T7**（本轮正文就是 `textarea`，原样展示 `**加粗**`）
- 项目 / 标签筛选与展示 → **T8**
- 首页三列与快速记录 → **T9**
- 搜索 → **T10**；附件 → **T11**

**⑤ 交付后用户实测反馈（2026-10-17，均已修复）**

| 反馈 | 结论 | 处理 |
| :--- | :--- | :--- |
| 登录成功却弹「验证码已过期，请点击图片刷新」 | 🔴 **真 bug**：`LoginView` 同时挂了 `@submit`、`@click`、`@keyup.enter` 三个入口 → **点一次发出 2 次 `/auth/login`**；验证码一次性，第 2 次必然失败 | 去掉 `@click` 与 `@keyup.enter`，只留 `native-type="submit"` → `@submit.prevent`；加 `if (loading.value) return`；`InitAdminView` 同修（D43） |
| 同时弹「Token 已过期」 | 是**踢线时那条 40100 的提示**，属正常行为；但多个并发请求会连弹多条 | 拦截器改为「一次重定向只提示一条」（复用重定向重入锁） |
| 点「笔记」任何筛选都显示「请求的资源不存在」 | 🟡 **不是代码 bug**：用户 8080 的后端进程启动于 20:19，而 T5 的 `NoteController` 是 20:35 才写 → **旧进程里没有 `/note` 接口**，带有效 token 走到路由层 → `40400`（HTTP 200、控制台无报错） | 需**重启 8080 的后端进程**（D44）；已在交接文档加环境提醒 |

**实测证据（修复前后各跑一次，同一点击动作）**：

```
修复前：1. POST /api/auth/login      修复后：1. POST /api/auth/login
        2. POST /api/auth/login              （仅 1 次 ✅）
        >>> 次数 = 2 ❌ 重复提交
```

**回归结果（修复后）**：T3 页面 **17/17**、T6 页面 **31/31** 全绿。
其中 T3 的 `V12a/V12b` 两条断言**已按新行为改写**（T3 时 `/notes` 不发请求，所以「无效 token 也能进」；
T6 起会调 `/note/page`，故改为「被拦回登录页 + token 被清」）。

#### T6 验证指引（PowerShell 5.1 友好）

```powershell
$root = "F:\Project\Own Project\Personal-Knowledge-Base"
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

# ---------- 1. 只读核对 ----------
# 表单是否只有一份（T6 的硬要求：抽屉与全屏页复用）
Get-ChildItem "$root\web\src" -Recurse -Include *.vue | Select-String -Pattern 'NoteForm' -List |
  ForEach-Object { "  " + $_.Path.Replace("$root\", '') }
# 期望：3 处（NoteForm 本体 / NoteDrawer / NoteDetailView）

# 抽屉状态是否收口在 composable
Select-String -Path "$root\web\src\composables\useNoteDrawer.js" -Pattern 'const visible|const version'

# 40100 是否同时清 store（关键：只清 localStorage 会落首页）
Select-String -Path "$root\web\src\api\request.js" -Pattern 'useUserStore|clear\(\)'

# ---------- 2. 起后端 + 预览站，用你自己的账号手验 ----------
# ⚠️ 顺序：先确认 8090 无占用再构建（旧进程占 jar 会让 mvn clean 失败）
Get-NetTCPConnection -LocalPort 8090 -State Listen -ErrorAction SilentlyContinue |
  ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
cd "$root\server"
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "构建失败（多半是旧进程占着 jar）" }
Start-Process -FilePath "C:\Program Files\Java\jdk-17\bin\java.exe" `
  -ArgumentList '-jar','target\pkb-server-1.0.0.jar','--server.port=8090' `
  -WorkingDirectory "$root\server" -WindowStyle Hidden

cd "$root\web"; npm run build
# 用你的 8080 当后端 + 5173 dev 也行，下面这套用隔离栈更干净
cd "$root\.workbuddy\skills\pkb-verify"
Start-Process -FilePath "node.exe" -ArgumentList "scripts\preview_server.cjs" -WindowStyle Hidden
# 打开 http://127.0.0.1:5180 用你自己的账号登录

# ---------- 3. 手验清单 ----------
# ① 列表：应看到卡片流；「全部/待整理/已归档」里**当前那个是蓝色高亮**
# ② 桌面点卡片 → 右侧抽屉打开，标题/正文/格式徽标都对；右上角**有 ×**
# ③ 改标题 → 出现「有未保存的修改」→ 点 × → 弹确认
# ④ 新建笔记 → 点保存 → 再点一次保存 → 列表里**只多一条**
# ⑤ 窗口缩到 375 → 点卡片 → 跳到 /note/{id} 全屏页；左右不出现横向滚动
# ⑥ F12 → Application → localStorage 把 pkb_token 改成乱码 → 刷新 /notes
#    期望：自动跳 /login?redirect=/notes，且 pkb_token 已消失
```

**验收判据**：
1. TAB 两档（桌面/手机）都能列表 → 打开 → 编辑 → 保存
2. **抽屉与全屏页是同一份表单**（字段、校验、按钮完全一致）
3. 「全部」筛选默认就是高亮的
4. 置顶/归档**不会**把笔记挤到「最近」最前面
5. token 失效 → 自动回登录页并带 redirect

---

### T7 双格式编辑 ✅

> 状态：**已完成（2026-10-17）**。接口侧 **34/34** + 页面侧 **40/40** 全绿；
> 回归 t3 **17/17** / t5 **33/33** / t6 **31/31** 全部保持绿（共 155 条断言）。
> 选型在开工前由用户拍板（PRD 第十二章 #1 → `md-editor-v3 7.1.0`）。

- **覆盖**：P-004（双格式编辑）
- **依赖**：T6 ✅
- **范围**：md 编辑器接入 + 富文本编辑器**真正接上**（依赖得先补回来）+ 后端按格式分流清洗 + 两端产物过 DOMPurify / jsoup

#### T7 技术方案（✅ 用户已拍板 2026-10-17）

**① 选型（PRD #1，已关闭 → 附录 C.1）**

| 候选 | 结论与理由 |
| :--- | :--- |
| **`md-editor-v3 7.1.0`** | ✅ **选用**：Vue3 原生（peer `vue ^3.5.3`，本机 3.5.43）、底层 markdown-it + CodeMirror 6、表格/代码块/列表开箱；**有 `sanitize(html) => html` 钩子**（渲染后、插入 DOM 前），可直接挂 DOMPurify —— 这是另外两个方案给不了的接点 |
| `Vditor 4.0.0` | ❌ 解包 23.6 MB；渲染由自家引擎直接写 DOM，DOMPurify 只能「渲染完再扫 DOM」，不干净 |
| `CodeMirror + marked` | ❌ 工具栏、表格插入、预览都要自造，R2（表格/代码块渲染）风险最高 |

⚠️ **选型的代价（实测确认，不是推断）**：它默认从 unpkg CDN 懒加载 highlight.js / prettier / screenfull / katex / mermaid。
本项目单机自用、可能完全离线 → 必须本地化：注入本地 `highlight.js` 实例（给了 `instance` 后不再插 CDN 的 `<link>`），
其余 CDN 能力全关（`no-prettier` / `no-mermaid` / `no-katex` / `no-echarts` / `no-upload-img`），工具栏按白名单收窄。

**② 清洗位置（本轮最关键的约定）**

| 阶段 | md（0） | html（1） |
| :--- | :--- | :--- |
| 提交前（前端） | **不动原文**（动了就破坏 Markdown 语法） | `sanitizeHtml()`（严格档，不给 `class`） |
| 入库前（后端） | 只算 `content_text`（方案 A 去标记） | `HtmlSanitizer.cleanForStore()`，**再用清洗后的内容**算 `content_text` |
| 展示 / 预览 | 编辑器内置渲染 → `sanitize` 钩子 → **markdown 档 DOMPurify** | 编辑器内直接编辑（内容已在上一步洗过） |
| 格式切换 | UI 只在**新建态**给选择；保存后只剩只读徽标；后端 T5 已拦（40000） | 同左 |

**③ 前端接入（4 个文件）**

- `MarkdownEditor.vue`：包装 `MdEditor`，本地 hljs + `sanitizeMarkdownHtml` + 工具栏白名单（去 图片/公式/图表/全屏/GitHub/保存/目录/htmlPreview）。
- `NoteForm.vue`：按 `contentFormat` 分流（两个编辑器都 `defineAsyncComponent` 懒加载）；新建态格式选择；html 提交前清洗。
- `sanitizeHtml.js`：新增 **markdown 档**（放行 `class` 并按前缀 `hljs-` / `md-editor-` / `language-` 收敛）—— 沿用富文本档会把代码高亮类名剥光。
- `RichTextEditor.vue`：工具栏白名单化 + 初始化静默期（原因见下方「实现结果」③ 的两个真问题）。

**④ 本轮不做（防越界）**

❌ 图片上传（T11，`uploadImage` 仍未注入、图片菜单已隐藏）｜❌ 项目/标签（T8）｜❌ 搜索高亮（T10）｜
❌ md 的目录 / 公式 / 图表 / Mermaid（工具栏剔除，且这些能力会拉 CDN 资源）

#### T7 实现结果

> 状态：**已完成（2026-10-17）**。均为真实执行记录（隔离栈 8090 + 预览站 5180）。

**① 交付物（10 个文件：3 新增 = 1 组件 + 2 脚本；7 修改 = 业务 4 + 依赖 2 + 旧脚本 1）**

| 文件 | 说明 |
| :--- | :--- |
| `web/package.json` / `package-lock.json` | + `md-editor-v3@7.1.0`、`highlight.js@11.12.0`、`@wangeditor-next/editor@6.4.2`、`@wangeditor-next/editor-for-vue@6.4.2`（后两个版本对齐 OSC lockfile 的实测值） |
| `web/src/components/MarkdownEditor.vue` | **新增**：md 编辑器唯一封装（本地 hljs、DOMPurify 钩子、精简工具栏、375 换行） |
| `web/src/components/NoteForm.vue` | 按格式分流编辑器；新建态格式选择（单向绑定 + 清空确认）；html 提交前清洗；懒加载 |
| `web/src/components/RichTextEditor.vue` | 工具栏白名单（21 项）；初始化静默期 + 空内容归一 |
| `web/src/utils/sanitizeHtml.js` | 新增 markdown 档（放行并收敛 `class`） |
| `server/.../service/impl/NoteServiceImpl.java` | html 入库前 jsoup 清洗；`content_text` 用清洗后内容算；html 空判定用 `hasVisibleContent`（只贴图合法） |
| `.workbuddy/.../scripts/t7_format_verify.cjs` | **新增**：接口侧 34 项 |
| `.workbuddy/.../scripts/t7_page_verify.cjs` | **新增**：页面侧 40 项（含 XSS、渲染、375、格式切换） |
| `.workbuddy/.../scripts/t6_page_verify.cjs` | 适配 T7：正文从 `textarea` 改为 `.cm-content`；异常路径改为硬退（见 ④） |

**② 实测：接口 34/34 + 页面 40/40，回归 155 条全绿**

| 组 | 关键实测 |
| :--- | :--- |
| 接口（`t7_format_verify`） | md 原样入库（142 字符逐字节一致）；md 去标记（无 `#` / `**` / 围栏 / URL / 图片，**代码内容保留**）；html 入库后无 `<script>` / `onerror` / `javascript:` / 站外图片，`text-align` 保留、`position` 被剥、`rel=noopener` 已补；`content_text` = `正文 加粗 点我 单元格 第二段`（不吃脚本）；更新路径同样清洗且同步搜索列；**只改标题不丢正文/搜索列（R1，html 版）**；清洗后为空 + 空标题 → `40000`；只贴图 + 空标题 → 允许 |
| 页面（`t7_page_verify`） | 抽屉 720px 打开、md/富文本按格式分流；预览内 `table=1 / pre=1 / code=1 / hljs 高亮 4 段`；md 里 `<script>` 不执行（`window.__t7_xss=undefined`）、无 script 节点、无 `javascript:` 链接；html 笔记 `<script>` 已在入库前剥掉；打开笔记**不误报**「有未保存的修改」；新建态格式选择默认 md 高亮；**空正文来回切换不被写入 `<p><br></p>`**；**非空正文切换 → 弹确认，取消后格式与正文都保留**；连点两次保存只创建 1 条；375 宽无横向滚动、md 工具栏换行（76px）后所有按钮可达 |
| 落库（SQL 直查） | `note.content LIKE '%<script%'` = **0**；`%onerror%` = **0**；`%javascript:%` = **0**；md 有正文但 `content_text` 为空 = **0**；html 有文字但搜索列为空 = **0**；只有图片的 html 行 = **1**（设计允许，无可搜文字） |

**③ 🔴 实测抓到 3 个真问题（全部已修）**

1. **打开 html 笔记立刻显示「有未保存的修改」** —— wangEditor 初始化会把入库 HTML 按 Slate 重新序列化，
   与后端存的字符串不完全一致，被脏检查当成用户输入（关闭时还会莫名弹确认框）。
   **修复**：编辑器加 250ms 初始化静默期，结束时把「编辑器认下来的 HTML」作为新基线（`normalize` 事件）
   交给 `NoteForm` 重置快照。**顺带**：空内容时 wangEditor 给的是 `<p><br></p>`（非空字符串），
   必须归一成 `''`，否则「md → 富文本 → md」的空表单会被写成 `<p><br></p>` 的 md 源码。
2. **格式切换的确认框「弹晚了」** —— `v-model` 在 `@change` 之前就把 `contentFormat` 改了，
   于是确认框还没选、编辑器已经换过去：md 正文被当成 html 喂给富文本编辑器。
   **修复**：格式选择改**单向绑定**（`:model-value` + `@update:model-value`），用户点「取消」时格式根本没变。
3. **md 工具栏在 375 宽被藏掉一半** —— 默认是「单行 + 横向滚动」，预览/撤销/重做在屏幕外，手机没有可见滚动条。
   **修复**：窄屏让工具栏换行（3 行、76px），所有按钮可达，整页仍无横向滚动。

**④ 验证脚本自身踩的坑（写进 SKILL，避免下次再花一轮）**

- 在 `ev(\`...\`)` 的**模板字符串里写注释带了反引号** → 模板字符串提前截断 → Node 侧 `content is not defined`；
  而脚本的 `.catch` 只设 `exitCode` 不退出，Chrome 子进程与 WebSocket 吊住事件循环 → **表现为「假死、无任何输出」**。
  → 规则：① 页面表达式里的注释禁止出现反引号；② 验证脚本的异常路径必须 `chrome.kill()` + `process.exit()`。
- t6 回归因正文从 `textarea` 换成 CodeMirror 而失败（`contentLen=0`）——是**断言过时**不是回归；
  已按新实现改写（`.cm-content`），并把「新建」用例改成只填标题（本用例只验重复创建）。

**⑤ 测试数据与进程收尾**

| 项 | 结果 |
| :--- | :--- |
| 测试账号 `13900000001` | 已硬删；`user` 表 **1 行**（用户真账号，未动） |
| `note` / `tag` / `note_tag` / `attachment` | 全 **0** 行 |
| Redis `pkb:*` | **0** 个 key |
| 归档库 `osc.user` | 仍 **93** |
| 隔离栈 8090 / 5180 | 已停、端口已释放 |
| 用户自己的 8080 / 5173 | **全程未动**（交付时探活均 200） |

**⑥ 遗留到后续任务点的**

- **图片**：html 正文里的图片仍以对象 key（`attachments/images/...`）存库，渲染时还没有公开前缀 →
  归 **T11**（`cleanForOutput` 拼前缀 + 上传通道 + 工具栏图片入口一起做）。
- **真机观感 / IME**：编辑器在真手机、以及中文输入法组词下的表现仍需人工确认（见交接文档 §9.3 #2 #4）。
- **大文档性能**：12,000 字符 md 保存与预览正常，但 10 万级未压测（V0.1 不做承诺）。

#### T7 验证指引（PowerShell 5.1 友好）

```powershell
$root = "F:\Project\Own Project\Personal-Knowledge-Base"
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$redis = "C:\Program Files\Redis\redis-cli.exe"

# ---------- 1. 只读核对：依赖与清洗位置 ----------
# ① 两个编辑器依赖都在（版本要对齐 6.4.2 / 7.1.0）
Select-String -Path "$root\web\package.json" -Pattern 'md-editor-v3|wangeditor|highlight'
# ② html 清洗只发生在 Service 层（Controller/Mapper 里不该出现）
Select-String -Path "$root\server\src\main\java\com\pkb\**\*.java" -Pattern 'cleanForStore' -List |
  ForEach-Object { "  " + (Split-Path $_.Path -Leaf) }
# 期望：只有 NoteServiceImpl.java（HtmlSanitizer 本体是实现）

# ---------- 2. 起隔离栈（先确认 8090 空闲）----------
Get-NetTCPConnection -LocalPort 8090 -State Listen -ErrorAction SilentlyContinue |
  ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
cd "$root\server"
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" -B package -DskipTests -q
if ($LASTEXITCODE -ne 0) { throw "后端构建失败" }
Start-Process -FilePath "C:\Program Files\Java\jdk-17\bin\java.exe" `
  -ArgumentList '-jar','target\pkb-server-1.0.0.jar','--server.port=8090' `
  -WorkingDirectory "$root\server" -WindowStyle Hidden
cd "$root\web"; npm run build; if ($LASTEXITCODE -ne 0) { throw "前端构建失败" }
cd "$root\.workbuddy\skills\pkb-verify"
Start-Process -FilePath "node.exe" -ArgumentList "scripts\preview_server.cjs" -WindowStyle Hidden

# ---------- 3. 跑 T7 两条（夹具账号由 §9.1 的第 ① 步建）----------
$env:PKB_API_BASE = "http://127.0.0.1:8090"
$env:PKB_PREVIEW_ORIGIN = "http://127.0.0.1:5180"
node .\scripts\t7_format_verify.cjs   # 接口 34 项
node .\scripts\t7_page_verify.cjs     # 页面 40 项（截图落在 .workbuddy\verify\t7\）

# ---------- 4. 落库核对（跑完 t7_format 立刻查，别先清库）----------
& $mysql --user=root --password=root --skip-column-names --default-character-set=utf8mb4 `
  "--execute=USE pkb; SELECT COUNT(*) FROM note WHERE content LIKE '%<script%';"                    # 期望 0
& $mysql --user=root --password=root --skip-column-names "--execute=USE pkb; SELECT COUNT(*) FROM note WHERE content_format=0 AND content<>'' AND (content_text IS NULL OR content_text='');"   # 期望 0
& $mysql --user=root --password=root --skip-column-names "--execute=USE pkb; SELECT COUNT(*) FROM note WHERE content_format=1 AND content<>'' AND content NOT LIKE '%<img%' AND (content_text IS NULL OR content_text='');"  # 期望 0

# ---------- 5. 收尾（清测试数据 + 停隔离栈）----------
& $mysql --user=root --password=root "--execute=USE pkb; DELETE FROM note_tag; DELETE FROM attachment; DELETE FROM note; DELETE FROM user WHERE phone='13900000001';"
& $redis -h 127.0.0.1 -p 6379 --scan --pattern 'pkb:*' |
  ForEach-Object { & $redis -h 127.0.0.1 -p 6379 DEL $_ | Out-Null }
Get-NetTCPConnection -LocalPort 8090,5180 -State Listen -ErrorAction SilentlyContinue |
  ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

---

### T8 项目 / 标签 ✅

> 状态：**已完成（2026-10-19）**。接口侧 **57/57** + 页面侧 **27/27**；
> 全量回归 **239 条断言**全绿（t3 17 / t5 33 / t6 31 / t7 接口 34 / t7 页面 40 / t8 接口 57 / t8 页面 27）。
> 计数口径由用户在开工前拍板（PRD #12 → **全部含待整理**）。

- **覆盖**：P-006（项目管理）、P-007（标签管理）；顺带补齐 P-002 里与项目/标签相关的**列表展示与筛选**
- **依赖**：T5 ✅
- **范围**：`tag` CRUD（`type=0` 标签 / `type=1` 项目）+ `note_tag` 关联 + **项目单选后端兜底** + 计数 + 列表筛选

#### T8 技术方案（✅ 用户已拍板 #12 2026-10-19）

**① 接口（9 个端点）**

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/tag?type=0\|1` | 列表（不传 type = 两类都返回，笔记表单一次拿全）；含 `noteCount`，按 `sort, id` 升序 |
| POST | `/tag` | 新建 `{name, type, color, sort}`；`type` 必填（不给默认值，避免把项目误建成标签） |
| PUT | `/tag/{id}` | 改名 / 改色 / 改排序；**`type` 不可变** |
| PUT | `/tag/sort` | 重排：`{type, ids:[…]}` 传该类型下**全部** id 的目标顺序 |
| DELETE | `/tag/{id}` | 解除全部关联 + 逻辑删除；**返回被解除关联的笔记数** |
| GET/POST/PUT | `/note` · `/note/{id}` | 请求体新增可选 `associations{projectId, tagIds}`（**同事务原子保存**） |
| GET | `/note/page` | 新增 `projectId`（单选）/ `tagIds`（多选，**AND**）筛选；列表项带 `project` 与 `tags` |

**② 三条关键设计（都写成了决策，见 §6 D51~D56）**

- **关联「全量替换」而不是增量**：请求带 `associations` → 先清空该笔记在 `note_tag` 的全部行，再按请求重建。
  —— 这样「一条笔记最多一个项目」在**写入路径上结构成立**，不存在「新增第二个项目」的可能（R4 兜底）。
  `associations` 为 null（不带）= 完全不动关联，T5/T6 的老调用方不受影响。
- **筛选不写 join**：先用 `note_tag` 算出候选 `note_id` 集合（标签 AND 用 `HAVING COUNT(DISTINCT tag_id)=N`），
  再用 `IN` 收窄 —— 避免和 MyBatis-Plus 分页 / 逻辑删除 / 排序兜底混在一起。
- **计数一次分组查询**：`GROUP BY tag_id` 全表扫一遍，避免管理页 N+1；口径**全部状态（含待整理）**。

**③ 前端**

- `TagManager.vue`：项目页与标签页**共用**（只差 `type` 与文案）；不用 `el-table`（375 会横向滚动），改成换行友好的行列表；
  排序用**上移/下移**（不引拖拽库），删除确认写明「笔记本身不会被删除」。
- `NoteForm.vue`：项目单选（可清空）+ 标签多选（`filterable`，>50 可搜索），随主请求一起提交；参与脏检查。
- `NotesView.vue`：项目 / 标签筛选 + 卡片展示（项目名 + 最多 3 个标签 + `+n`）；
  从管理页「查看笔记」跳 `/notes?projectId=…` 即按项目过滤（PRD P-006 的「点进项目 = 项目笔记列表」）。

**④ 本轮不做（防越界）**

❌ 批量操作（V0.1 不做）｜❌ 标签合并 / 重命名级联 |❌ 项目元信息（起止时间/进度，PRD 已否决）｜❌ 首页三列（T9）

#### T8 实现结果

> 状态：**已完成（2026-10-19）**。均为真实执行记录（隔离栈 8090 + 预览站 5180）。

**① 交付物（后端 15 个文件：10 新增 / 5 修改；前端 8 个：3 新增 / 5 修改）**

| 类别 | 文件 |
| :--- | :--- |
| 后端新增 | `TagController` · `TagService` / `TagServiceImpl` · DTO `TagCreateRequest` / `TagUpdateRequest` / `TagSortRequest` / `TagVO` / `TagBriefVO` / `NoteAssociationsRequest` / `TagCountRow` |
| 后端修改 | `NoteServiceImpl`（关联装配 + 筛选 + 统一出参）· `NoteTagMapper`（两条聚合查询）· `NoteVO` / `NoteListItemVO` / `NoteQuery` / `NoteCreateRequest` / `NoteUpdateRequest`（加字段）· `MybatisPlusConfig`（补 setter 判断，见 ③） |
| 前端新增 | `api/tag.js` · `components/TagManager.vue` |
| 前端修改 | `ProjectsView` / `TagsView`（接管理组件）· `NoteForm`（项目单选 + 标签多选）· `NotesView`（筛选 + 卡片展示） |
| 验证脚本 | `t8_api_verify.cjs`（57 项）· `t8_page_verify.cjs`（27 项） |

**② 实测：接口 57/57 + 页面 27/27；全量回归 239 条全绿**

| 组 | 关键实测 |
| :--- | :--- |
| 接口 | 项目/标签 CRUD；同类型重名 → 40000、**不同类型可同名**；非法 type / 颜色 → 40000；改名 / 改色（`color:''` 清空）生效；**项目与标签互换使用 → 40000**；关联全量替换（A→B 只有 B）；只改标题不动关联；`projectId=null` 清项目、`tagIds=[]` 清标签；**新建时带非法关联 → 40000 且笔记不残留（事务回滚）**；项目筛选 / 标签 AND 筛选 / 组合筛选；计数含待整理；删除标签与项目只解关联不删笔记；排重写 0,10,20；未登录 40100 |
| 页面 | 项目页列出 + 计数（2 条）+ 新建 / 改名 / 上移 / 查看笔记（跳 `/notes?projectId=` 且只列该项目 2 条）/ 删除（确认框含「笔记本身不会被删除」）；标签页列出 + 计数；笔记页有项目/标签筛选、卡片显示项目与标签、标签 AND 只剩 1 条、清除筛选恢复 3 条；表单回填项目与标签、打开不误报未保存、改标签后保存成功且后端确认 2 个标签；375 项目页与笔记页均无横向滚动 |
| 落库（SQL 直查） | **一条笔记多个项目的行数 = 0**（R4 兜底）；孤儿关联（指向已删笔记/标签）= 0（删笔记会清 `note_tag`）；指向已逻辑删除标签的关联 = 0 |

**③ 🔴 实测踩到的 3 个坑（都已处理）**

1. **`updateById` 忽略 null → 颜色永远清不掉**：改色走实体更新时 `null` 会被跳过。
   → 改用 wrapper 显式 `.set(Tag::getColor, null)`（与「置顶不刷 updated_at」同一类坑：想写 null 必须走 wrapper）。
2. **`MetaObjectHandler` 对新实体是地雷**：`note_tag` 两个字段都没有、`tag` 没有 `updated_at`，
   而填充器原来无条件 `strictInsertFill`（T1 曾因「实体没有属性」抛过异常）。
   → 填充逻辑改成**有 setter 才填**，新增实体不必回头改配置。
3. **验证脚本假失败：拿已被删掉的标签做计数断言**。第一版「删除笔记后标签计数回落」用了前面用例已删除的
   `tagY` → 取不到对象、`before=undefined`。→ 换一个新标签重新验证。**断言的数据前提也要复查**。

**④ 测试数据与进程收尾**

| 项 | 结果 |
| :--- | :--- |
| 测试账号 `13900000001` | 已硬删；`user` 表 **1 行**（用户真账号，未动） |
| `note` / `tag` / `note_tag` / `attachment` | 全 **0** 行 |
| Redis `pkb:*` | **0** 个 key |
| 归档库 `osc.user` | 仍 **93** |
| 隔离栈 8090 / 5180 | 已停、端口已释放 |
| 用户自己的 8080 / 5173 | **全程未动**（交付时探活发现**本来就没在跑**，见下） |

**⑤ 遗留 / 待人工**

- 用户的 8080 / 5173 在本次交付时**未运行**（机器应是重启过；MySQL / Redis 是自启服务，均正常）。
  → 手动验收前请按交接文档「八」先起后端与前端，**后端务必是新代码**（T8 新增了 `/tag` 接口，旧进程会 40400）。
- 标签很多（>50）时的多选体验只做了 `filterable`，**未压测**（PRD P-007 只要求「可用、不撑破布局」，已满足）。

#### T8 验证指引（PowerShell 5.1 友好）

```powershell
$root = "F:\Project\Own Project\Personal-Knowledge-Base"
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

# ---------- 1. 只读核对：接口与关联兜底都在 ----------
Select-String -Path "$root\server\src\main\java\com\pkb\controller\TagController.java" -Pattern '@(Get|Post|Put|Delete)Mapping'
# 期望 5 行（GET / POST / PUT /{id} / PUT /sort / DELETE /{id}）
Select-String -Path "$root\server\src\main\java\com\pkb\service\impl\NoteServiceImpl.java" -Pattern 'applyAssociations'
# 期望：定义 1 处 + 调用 2 处（create / update）

# ---------- 2. 起隔离栈（先确认 8090 空闲）----------
Get-NetTCPConnection -LocalPort 8090 -State Listen -ErrorAction SilentlyContinue |
  ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
cd "$root\server"
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" -B package -DskipTests -q
if ($LASTEXITCODE -ne 0) { throw "后端构建失败" }
Start-Process -FilePath "C:\Program Files\Java\jdk-17\bin\java.exe" `
  -ArgumentList '-jar','target\pkb-server-1.0.0.jar','--server.port=8090' `
  -WorkingDirectory "$root\server" -WindowStyle Hidden
cd "$root\web"; npm run build; if ($LASTEXITCODE -ne 0) { throw "前端构建失败" }
cd "$root\.workbuddy\skills\pkb-verify"
Start-Process -FilePath "node.exe" -ArgumentList "scripts\preview_server.cjs" -WindowStyle Hidden

# ---------- 3. 跑 T8 两条（夹具账号见交接文档 §9.1 第 ① 步）----------
$env:PKB_API_BASE = "http://127.0.0.1:8090"
$env:PKB_PREVIEW_ORIGIN = "http://127.0.0.1:5180"
node .\scripts\t8_api_verify.cjs    # 57 项
node .\scripts\t8_page_verify.cjs   # 27 项（截图落在 .workbuddy\verify\t8\）

# ---------- 4. 落库核对（项目单选兜底的关键断言）----------
& $mysql --user=root --password=root --skip-column-names "--execute=USE pkb; SELECT COUNT(*) FROM (SELECT nt.note_id FROM note_tag nt JOIN tag t ON t.id=nt.tag_id AND t.type=1 AND t.is_deleted=0 GROUP BY nt.note_id HAVING COUNT(*)>1) x;"
# 期望 0（一条笔记最多一个项目）
& $mysql --user=root --password=root --skip-column-names "--execute=USE pkb; SELECT COUNT(*) FROM note_tag nt LEFT JOIN note n ON n.id=nt.note_id AND n.is_deleted=0 LEFT JOIN tag t ON t.id=nt.tag_id AND t.is_deleted=0 WHERE n.id IS NULL OR t.id IS NULL;"
# 期望 0（没有孤儿关联）

# ---------- 5. 收尾 ----------
& $mysql --user=root --password=root "--execute=USE pkb; DELETE FROM note_tag; DELETE FROM note; DELETE FROM tag; DELETE FROM user WHERE phone='13900000001';"
Get-NetTCPConnection -LocalPort 8090,5180 -State Listen -ErrorAction SilentlyContinue |
  ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

---

### T9 首页工作台 ✅

> 状态：**已完成（2026-10-19）**。接口侧 **15/15** + 页面侧 **32/32**；
> 全量回归 **286 条断言**全绿（t3 17 / t5 33 / t6 31 / t7 34+40 / t8 57+27 / t9 15+32）。
> 「本周」口径由用户拍板（PRD #11 → **本周一 00:00 起算、本周新建数**）。

- **覆盖**：P-010（首页工作台）+ P-001 的首页快速记录入口
- **依赖**：T6（列表卡片与抽屉）、T8（项目列与计数）
- **范围**：桌面顶部输入框（回车即建 inbox、不跳页）+ 状态条（4 项可点）+ 三列（项目 / 最近 / 待整理）各自滚动；手机顶部输入框常驻 + 状态条横滑 + 底部 3 Tab（只换内容区）

#### T9 技术方案（✅ 已拍板 #11 2026-10-19）

**① 接口：新增 `GET /note/stats`（一次返回 4 项）**

| 字段 | 口径 |
| :--- | :--- |
| `inbox` / `archived` | `status=0` / `status=1` 的笔记数（`@TableLogic` 自动排除已删） |
| `projects` | `tag.type=1` 且未删除的项目数 |
| `weekAdded` | **本周一 00:00 起**新建的笔记数（按 `created_at`，不区分 status）—— PRD #11 拍板 |

列表接口 `GET /note/page` 增加 `weekOnly`：只返回本周新建（状态条「本周 +N」点进来就带这个参数）。
⚠️ 刻意**不给首页 4 个接口**：状态条一次请求拿全，避免首屏打 4 枪。

**② 滚动模型（照 D34 实测结论，**不要照抄 PRD 旧说法**）**

- 首页 `height:100% + min-height:0`，**自己不再滚**（否则三列的 overflow 没有确定高度）；
- 三列的 `.home-column__body` 各带 `overflow-y:auto` —— **这才是承重件**；
- `.app-main` 的 `scrollTop` 必须保持 0（验收里专门断言：滚列不带动整页）。

**③ 快速记录（P-001 的每条分支都要落地）**

| 分支 | 实现 |
| :--- | :--- |
| 回车建 inbox、不跳页 | `createNote({title:text, content:text, contentFormat})`；成功后**不 router.push** |
| 清空并保持焦点 | 注意：提交中输入框是 `disabled`，**对 disabled 元素 focus 无效** → 必须等 `creating` 复位后再 `focus()`（T9 实测踩到） |
| 空输入 | 输入框抖动提示，**不创建**、不弹错误框 |
| 连击回车 | `creating` 守卫（提交中禁用 + 早退），实测连按两次只建 1 条 |
| 失败 | **保留输入**、toast 报错、可重试（绝不 clear） |
| 格式按钮 | 只影响此后新建；状态存 `sessionStorage('pkb_home_format')` |
| 中文输入法 | `compositionstart/end` + `event.isComposing` 双保险，**组词中的回车不提交** |

**④ 前端结构**

- `HomeView.vue` 重写：桌面「composer + 状态条 + 三列 grid」，手机「composer + 状态条横滑 + 内容区 + 底部 3 Tab」；
- 三列数据：项目列 `GET /tag?type=1`（含计数）、最近列 `GET /note/page?size=20`、待整理列 `GET /note/page?status=0&size=20`；
- 点笔记：桌面 `useNoteDrawer().open(id)`、手机 `/note/:id`（与列表页同一套）；抽屉里改完靠 `useNoteDrawer().version` 刷新首页；
- `NotesView` 支持从首页带 query 进入：`status=0/1`、`week=1`（并显示「本周新增」标记）。

**⑤ 本轮不做**

❌ 内联搜索框（P-008，随 **T10** 接入；不放假入口）｜❌ 三列的「加载更多」（首页只承载「看一眼」，全量在列表页）｜❌ 手机 Tab 与路由联动（D33 明确是内容级）

#### T9 实现结果

> 状态：**已完成（2026-10-19）**。均为真实执行记录（隔离栈 8090 + 预览站 5180）。

**① 交付物（6 个文件：2 新增 / 4 修改）**

| 文件 | 说明 |
| :--- | :--- |
| `server/.../dto/NoteStatsVO.java` | **新增**：状态条四项出参 |
| `server/.../dto/NoteQuery.java` · `NoteService` · `NoteServiceImpl` · `NoteController` | **修改**：`stats()` + `/note/stats`；`weekOnly` 筛选；`weekStart()` 用 `LocalDate.now().with(DayOfWeek.MONDAY)` |
| `web/src/views/HomeView.vue` | **重写**：快速记录 + 状态条 + 三列/三 Tab |
| `web/src/views/notes/NotesView.vue` | 支持 `status` / `week` query（状态条跳转落地），清除筛选一并复位 |
| `web/src/api/note.js` | `getNoteStats()` + 参数注释 |
| `.workbuddy/.../t9_api_verify.cjs` · `t9_page_verify.cjs` | **新增**：接口 15 项 / 页面 32 项 |

**② 实测：接口 15/15 + 页面 32/32；全量回归 286 条全绿**

| 组 | 关键实测 |
| :--- | :--- |
| 接口 | 四项统计类型正确；**2020 年的老笔记不计入「本周」**（`weekAdded=0` 而 `inbox=1`）；新建 2 条 → 待整理 +2、本周 +2；归档一条 → 待整理 −1、已归档 +1、**本周不变**（归档不算新建）；建项目 → 项目 +1；`weekOnly` 结果数与 `weekAdded` 一致且不含老笔记；`weekOnly + status=0` 组合生效；删除后统计回落；未登录 40100 |
| 页面（桌面） | 首页有输入框 / 状态条 4 项 / 三列标题正确；**状态条数字与接口逐一相等**；回车建笔记**不跳页**、输入框**清空且保持聚焦**、toast 文案正确、新笔记立刻出现在「待整理」列、状态条 +1；空输入不创建；**连击回车只建 1 条**；格式按钮切到富文本后新建的笔记 `contentFormat=1`；**组词中的回车不提交**；**滚「最近」列不影响「待整理」，且整页不动**；**窗口压到 400px 高仍可列内滚动**；点列内笔记 → 抽屉打开并回填标题；点「待整理」→ `/notes?status=0`；点「项目」→ `/projects` |
| 页面（手机 375） | 输入框常驻 + 3 Tab（最近/待整理/项目）、默认「最近」；**切 Tab 路由不变（仍 `/`）**；滚动内容区后输入框位置不变（常驻）；手机回车建笔记成功且不跳页；「项目」Tab 展示项目；**无横向滚动** |

**③ 🔴 实测抓到 1 个真问题（已修）**

- **「回车后保持焦点」失效**：提交时输入框 `disabled`，而 `focus()` 对 disabled 元素**无效**；
  原实现「`await nextTick()` 后 focus」发生在 `creating=false` 之前 → 焦点丢到 body。
  **修复**：把 focus 移到 `finally`（`creating` 复位）之后；失败路径也聚焦，方便重试。

**④ 一个截图教训**：第一版桌面截图是在「列已滚动 300px」之后拍的，看起来像「第一条没有标题」，
实际是行被裁在顶部。**截图前先复位滚动**（已改进脚本）。

**⑤ 与 PRD 的差异（已同步文档）**

- P-010 顶部区的**内联搜索框本轮不做**：P-008 归 T10，先不放假入口；PRD 已加注「随 T10 接入」。
- PRD P-010「`min-height:0` 不能删」与 T4 实测（D34）冲突，**已改为「每列自带 `overflow-y:auto`」**。

**⑥ 测试数据与进程收尾**

| 项 | 结果 |
| :--- | :--- |
| 测试账号 `13900000001` | 已硬删；`user` 表 **1 行**（用户真账号，未动） |
| `note` / `tag` / `note_tag` / `attachment` | 全 **0** 行 |
| Redis `pkb:*` | **0** 个 key |
| `osc.user` | 仍 **93** |
| 隔离栈 8090 / 5180 | 已停、端口已释放 |
| 用户实例 | 8082（后端）/ 5173（前端）**全程未动**（交付时仍在监听） |

#### T9 验证指引（PowerShell 5.1 友好）

```powershell
$root = "F:\Project\Own Project\Personal-Knowledge-Base"
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

# ---------- 1. 只读核对：统计端点与 weekOnly 都在 ----------
Select-String -Path "$root\server\src\main\java\com\pkb\controller\NoteController.java" -Pattern 'stats|weekOnly'
Select-String -Path "$root\web\src\views\HomeView.vue" -Pattern 'getNoteStats|home-column__body|home__tab'

# ---------- 2. 起隔离栈（先确认 8090 空闲）----------
Get-NetTCPConnection -LocalPort 8090 -State Listen -ErrorAction SilentlyContinue |
  ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
cd "$root\server"
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" -B package -DskipTests -q
if ($LASTEXITCODE -ne 0) { throw "后端构建失败" }
Start-Process -FilePath "C:\Program Files\Java\jdk-17\bin\java.exe" `
  -ArgumentList '-jar','target\pkb-server-1.0.0.jar','--server.port=8090' `
  -WorkingDirectory "$root\server" -WindowStyle Hidden
cd "$root\web"; npm run build; if ($LASTEXITCODE -ne 0) { throw "前端构建失败" }
cd "$root\.workbuddy\skills\pkb-verify"
Start-Process -FilePath "node.exe" -ArgumentList "scripts\preview_server.cjs" -WindowStyle Hidden

# ---------- 3. 先插一条「很久以前」的笔记，再跑 T9 两条 ----------
& $mysql --user=root --password=root "--execute=USE pkb; INSERT INTO note (title, content, content_format, content_text, status, pinned, created_at, updated_at) VALUES ('T9-LEGACY-old','老笔记',0,'老笔记',0,0,'2020-01-01 10:00:00','2020-01-01 10:00:00');"
$env:PKB_API_BASE = "http://127.0.0.1:8090"
$env:PKB_PREVIEW_ORIGIN = "http://127.0.0.1:5180"
node .\scripts\t9_api_verify.cjs    # 15 项（含「本周」边界）
node .\scripts\t9_page_verify.cjs   # 32 项（截图落在 .workbuddy\verify\t9\）

# ---------- 4. 人工看两件事（自动化断言测不出来）----------
# ① 桌面把窗口高度压到 400px：三列各自能滚、页头不动
# ② 手机 375：滚动内容区时顶部输入框不消失；底部 3 Tab 点击只换内容区
```

---

### T10 搜索 ⬜

> 状态：**未开始**。**开工前必须先出技术方案并经用户确认**（涉及实现方式选型，铁律 2）。

- **覆盖**：P-008（搜索）
- **依赖**：T5
- **范围**：只打 `title + content_text`（**永不打 `content`**）；LIKE 元字符转义；结果高亮
- **开工前需用户拍板**（PRD 第十二章）：#14 搜索实现方式（倾向 `LIKE`；MySQL 全文索引对中文需 `ngram`，**未实测，不承诺**）
- **技术方案**：⬜ 待开工前产出

#### T10 实现结果

⬜ 未开始。

---

### T11 附件 ⬜

> 状态：**未开始**。**开工前必须先出技术方案并经用户确认**（涉及公共校验抽象，铁律 2）。

- **覆盖**：P-009（附件）
- **依赖**：T5
- **范围**：通用文件上传（**非图片也要支持**）+ 扩展名 / MIME 白名单 + 魔数校验（**不复用图片专用的 `ImageValidator`**）+ MinIO bucket `pkb` + 附件列表 / 下载 / 删除
- **开工前需用户拍板**（PRD 第十二章）：#5 大小上限、#6 MinIO 对象是否同步删、#7 删除笔记时附件处置、#8 是否做孤儿清理 UI
- **风险**：R6（类型放宽引入风险）
- **技术方案**：⬜ 待开工前产出

#### T11 实现结果

⬜ 未开始。

---

### T12 联调打磨 ⬜

> 状态：**未开始**。

- **覆盖**：PRD §7（NFR）、M4
- **依赖**：全部
- **范围**：三档外壳逐页走查（375px 无横向滚动）、`content_text` 与 `content` 全量一致性核对、数据备份、（可选）历史漏同步修复脚本
- **开工前需用户拍板**（PRD 第十二章）：#9 数据备份是否纳入 V0.1
- **技术方案**：⬜ 待开工前产出

#### T12 实现结果

⬜ 未开始。

---

## 5. 交接与开工

> 本节**不重复抄写**开工信息 —— 单一出处原则（交接文档「五、全局铁律」第 3 条）。

| 你要找的 | 去哪看 |
| :--- | :--- |
| 开场提示词（直接复制给新对话） | 交接文档 顶部 / §九 |
| 当前阶段与下一步（本轮做什么） | 交接文档 §三 |
| 关键词速查 / 常用命令 | 交接文档 §七 / §八 |
| 环境坑 / 全局铁律 / 协作规则 | 交接文档 §四 / §五 / §六 |
| **任务点进度总表** | **本文件 §3** |
| **某任务点的方案 / 实现 / 验证** | **本文件 §4** |
| **实现层决策（D 编号）** | **本文件 §6** |
| **需求本身（功能卡 / 数据模型 / 待拍板项）** | **PRD V0.1（§十二 待确认项）** |

> 换对话的标准动作：对 AI 说一句「**我要开启新对话**」→ 它会先把交接文档刷到最新，再把开场提示词整段发给你。

---

## 6. 实现层决策记录（2026-10-09 初始化）

> 记录 **PRD 未覆盖、由实现侧拍板**的决策，避免跨会话失忆。新增决策追加到表尾。
> **产品层决策**（3 条铁律、范围边界等）在 PRD 附录 D，**不在此重复**。

| # | 议题 | 决策 | 影响任务点 |
| :--- | :--- | :--- | :--- |
| D1 | 工程布局 / 仓库 | monorepo 单仓（`server/` + `web/`）；工作空间 `Personal-Knowledge-Base`（**DSH 绑定不可改名**）；远端**新建仓** `git@github.com:CapableCCat/Personal-Knowledge-Base.git` | T1 |
| D2 | 工程命名 | groupId `com.pkb`、artifactId `pkb-server`、包根 `com.pkb`、主类 `PkbServerApplication` | T1 |
| D3 | **OSC 归档与 git 归属** | OSC 的 247 个跟踪文件 + `.git`（98 提交）+ `.workbuddy` + 11 份 OSC 文档**整体迁往** `F:\Project\Own Project\New-Management-System\`，远端指向归档仓 `CapableCCat/New-Management-System.git`（**只 fetch 不 push**）；PKB 工作区**重新 `git init`**，零提交起步。理由：避免 OSC 历史被灌进 PKB 新仓 | T1 |
| D4 | 数据库 | 库 **`pkb`**，应用账号 **`pkb_app`**（仅 DML、**无 DDL**，建库建表用 root）；`osc` 库**原样保留不动** | T1 / T2 |
| D5 | 对象存储 | MinIO bucket **`pkb`**，应用启动时自动创建并设公开读；库里只存对象 key | T1 / T11 |
| D6 | 业务表清单 | 4 张业务表：`note` / `tag` / `note_tag` / `attachment`；`user` 表沿用 OSC 骨架并**简化**（去权限推导字段） | T2 |
| D7 | 前后端技术栈 | 继承 OSC 已验证版本（Spring Boot 3.3.13 / MyBatis-Plus 3.5.7 / Sa-Token 1.44.0 / Vue 3.5 / Vite / Element Plus / Pinia / axios / Sass）；**前端 JS 非 TS** | T1 |
| D8 | 移动端组件库 Vant | **保留依赖不启用**（OSC 公开端专用；PKB 无公开端） | T1 |
| D9 | 接口风格 | 统一返回体 `{code,message,data}`，**HTTP 一律 200**；错误码需**重定义** —— 删除单账号用不到的 40005（需改密）/ 40300（无权限） | T1 / T3 |
| D10 | 端口与代理 | dev 8080 / prod 8081 / 前端 dev 5173；后端不带 `/api` 前缀，前端 Vite 代理 `/api` → 8080 并剥前缀；**AI 自测隔离栈 8090 / 预览站 5180** | T1 |
| D11 | 配置与密钥 | 全环境变量化；`.env` 被忽略、`.env.example` 入仓；运维坑：起后端前 `Remove-Item env:SERVER__PORT` | T1 |
| D12 | 逻辑删除与审计字段 | 全表沿用 `is_deleted` + MyBatis-Plus `@TableLogic`；⚠️ **审计字段（`created_by` / `updated_by`）在单账号下无意义，是否保留待 T2 方案定** | T2 |
| D13 | 单账号鉴权骨架 | Sa-Token Header 模式；守卫**只判「是否已初始化」+「是否已登录」**，**不做任何角色 / 能力判定**；token 存 localStorage | T3 |
| D14 | 强制改密删除 | OSC 的首登强制改密逻辑**整删**（单账号无「密码非本人选」场景）；`activated_at` 用途待 T2 重判 | T3 |
| D15 | 图形验证码 | **保留**（算术型，答案存 Redis，**校验后立即作废**，TTL 120s） | T3 |
| D16 | 登录失败锁定 | **保留** OSC 口径：连续失败 5 次锁定 15 分钟；计数存 Redis，阈值与时长可配 | T3 |
| D17 | `constants/roles.js` | **整删**（含全部引用）；菜单可见性不再依赖能力函数 | T4 |
| D18 | 保留不启用的资产 | FastExcel（`cn.idev.excel`）+ ECharts 封装 + `constants/palette.js` **保留不动**（**用户明确拍板勿删**） | T1 |
| D19 | 建表脚本安全 | `01_schema.sql` **不含 DROP**（只 `CREATE TABLE IF NOT EXISTS`）；脚本幂等 | T2 |
| D20 | 不建物理外键 | 表间关系由应用层校验 + 索引保证（沿用 OSC 约定） | T2 |
| D21 | 字符集与引擎 | 全部表 InnoDB + `utf8mb4` | T2 |
| D22 | 唯一索引与逻辑删除 | 唯一索引**不带 `is_deleted`**（沿用 OSC 约定）；⚠️ `tag.name` 的唯一约束与逻辑删除如何共存，**待 PRD 第十二章 #15 拍板** | T2 |
| D23 | **i18n 处置** | **整删**（`stores/locale.js` + `components/LocaleSwitch.vue`）；`App.vue` 改为直接写死 EP 中文 locale，不再有切换状态。PRD 未覆盖，2026-10-09 用户拍板 | T1 |
| D24 | **`user` 表最终 7 列** | `id` / `phone`(唯一) / `password` / `status` / `created_at` / `updated_at` / `is_deleted`。**`activated_at` 一并删除** —— 它唯一用途是「首登强制改密」判定，该逻辑已整删；保留它只会是永远为当前时间或永远为 NULL 的死列 | T2 |
| D25 | **`.env` 处置** | `server\.env` **不随迁移复制**（`.gitignore` 项），T1 时按 PKB 值重建（库 `pkb` / 账号 `pkb_app` / Header `pkb-token` / 桶 `pkb`）。⚠️ 它是**运行时覆盖 yml 默认值**的，迁移后必须单独核对，否则会静默连错库 | T1 |
| D26 | `tag.name` 唯一约束 | **不建库级唯一索引**，改**应用层查重**（查询「同名且未逻辑删除」的标签）。理由：`is_deleted` 与唯一索引天然冲突（逻辑删除的标签仍占用同名，新建同名必撞键）；PRD 冻结的 `tag` 表没有 OSC 那种 `enabled` 停用列可用。副作用：并发下理论上可建同名，单账号可接受。**同时关闭 PRD 第十二章 #15** | T2 / T8 |
| D27 | 搜索实现方式与索引 | **V0.1 锁定用 `LIKE`**（`title LIKE ? OR content_text LIKE ?`，**永不含 `content`**）；**不给搜索列建索引**（`LIKE '%kw%'` 前导通配用不上任何索引，万级数据全表扫为毫秒级）。`LIKE` 元字符必须转义。**同时关闭 PRD 第十二章 #14** | T2 / T10 |
| D28 | `note_tag` 关联策略 | 建 `NoteTag` 实体（`@TableId` 标 `noteId` + `tagId` 为**联合主键**）；**关联一律物理删除**（重新关联 = 再插一行，天然幂等）。理由：该表按 PRD §5.1 冻结结构**没有 `is_deleted` 列**。⚠️ `deleteById` 不适用，删除走 `LambdaQueryWrapper`；生效性在 T8 验证 | T2 / T8 |
| D29 | 不加乐观锁 | **不加 `version` 列**。PRD P-003 已定「V0.1 不做乐观锁，冲突时后写覆盖」；加列等于改冻结表结构。`note.updated_at` 参与排序，归档 / 取回会改动它 | T2 |
| D30 | 不加摘要列 | **不加 `excerpt` 列**。`content_text` 已是纯文本，`LEFT(content_text, 200)` 在查询时现算；加冗余列 = 多一个同步点 = 多一个 R1 风险源 | T2 / T6 |
| D31 | 索引口径定稿 | 五张表索引**维持 `01_schema.sql` 现状**（`note`: `(status,pinned,updated_at)` + `(updated_at)`；`tag`: `(type,sort)`；`note_tag`: PK + `(tag_id)`；`attachment`: `(note_id)`；`user`: `uk_user_phone`）。**T2 不改索引** | T2 |
| D32 | `content_text` 生成收口 | 新增 `common/ContentTextBuilder` 接口（含默认实现），作为 `content_text` 计算的**唯一收口点**；`note` 的新增 / 更新必须经它。⚠️ **md 去标记的具体规则归 T5 定**（PRD 第十二章 #13 仍未关闭） | T2 / T5 |
| D33 | **手机端底部空间的归属** | **外壳不渲染底部 tabbar**；底部那个位置**留给 T9 首页的内容 Tab**（最近/待整理/项目）。手机端外壳导航走页头 ☰ 抽屉（含全部 4 项）。理由：PRD P-010 原文「切 Tab **只换内容区**」= 内容级非路由级；375×812 上两条底栏吃掉近 15% 高度。代价：手机端 `/notes` `/projects` `/tags` 无底部导航，只能点 ☰ | T4 / T9 |
| D34 | **内部滚动机制（修正 OSC 传说）** | 实测推翻「`min-height:0` 是承重件」：① **真正的承重件是 `.app-main { overflow-y: auto }`**；② 按 Flexbox 规范，`overflow` 非 `visible` 的 flex 项自动最小尺寸恒为 0，故此时 `min-height:0` **冗余**；③ `min-height:0` **只在 `overflow: visible` 时起作用**（全拆后 clientH 849→3029）。→ **T9 三列的每一列都必须自带 `overflow-y:auto`（或 `overflow:hidden`），只堆 `min-height:0` 无效** | T4 / T9 |
| D35 | **公开页的路由结构** | **每个公开页各自挂 `PublicLayout`**（`path: '/login'` + 子路由 `path: ''`；`path: '/init'` + 子路由 `path: ''`），**禁止**把它们放进一个 `path: '/'` 的父记录。理由：与 `AppShell` 的 `path: '/'` 撞车后，Vue Router 取先声明者 → `/` 渲染成「PublicLayout + 空 router-view」= 页头页脚 + 空白（2026-10-17 用户截图抓到的真实 bug）。另：守卫判定引导页**用路由名 `initAdmin`**，不用 `to.path === '/init'`（后者漏掉 `/init/`） | T4 |
| D36 | **更新时算 `content_text` 必须用「最终生效内容」** | `update()` 里**不能**用 `patch.content` 去算 —— 请求只改标题/置顶时不带 content，`patch.content` 为 null → `build(null)` 返回 `""` → **把已有搜索列覆盖成空**（R1 真实踩到）。正解：`finalContent = request.content != null ? patch.getContent() : note.getContent()`，用它算。配套回归断言：只改标题后正文不丢、列表 `excerpt` 必须**非空**、库里有 content 时 `content_text` 不得为空 | T5 |
| D37 | **本地验证的固定顺序** | **停进程 → 构建 → 检查退出码为 0 →（可选）看 jar 时间戳 → 起新进程 → 再验证**。T5 实测：旧进程占着 jar 时 `mvn clean` 失败，而脚本没查退出码就继续跑 → 拿旧 jar 验证 → 假失败，白跑一轮 | 通用 |
| D38 | **归档 / 取回 / 置顶不刷 `updated_at`** | 三者都是**状态流转，不算「编辑过」**，不该把笔记顶到「最近」列最前面（PRD P-005 曾标为待确认，T6 定稿）。⚠️ 实现要点：无实体更新走 `update(null, wrapper)` 时，**必须显式把 `updated_at` 写回原值** —— 表上带 `ON UPDATE CURRENT_TIMESTAMP`，**不显式赋值 MySQL 会自动刷成当前时间**，写 `now` 更是直接刷掉 | T6 |
| D39 | **列表排序必须带兜底键** | 排序口径 = **`pinned DESC, updated_at DESC, id DESC`**。`updated_at` 是 DATETIME（秒级），同一秒建的笔记时间完全相同 → 没有 `id DESC` 兜底时 MySQL 返回顺序**不确定**，列表会在两次请求间自行跳动（T6 实测） | T6 / T9 |
| D40 | **40100 必须同时清 Pinia 登录态** | `request.js` 的 `redirectToLogin()` 里**只清 `localStorage` 是不够的**：路由守卫读的是 store 里的 `token` ref，它仍有值 → 守卫认为「已登录」→ 把 `/login` 又弹回首页，表现为「token 失效后落在首页」。必须 `useUserStore().clear()` + 清持久化 + `router.replace('/login?redirect=…')` | T6 |
| D41 | **筛选组别用 `null` 当「全部」** | `el-radio-group` 用 `modelValue === value` 比较，`null` 会被当成「未选中」→ **所有按钮都不高亮**，用户看不出当前筛选（功能却正常，极难发现）。用字符串哨兵（如 `'all'`），提交后端时再映射回 `null` | T6 |
| D42 | **抽屉必须有可见的关闭入口** | 不要图省事用 `:with-header="false"` —— 那样只能靠 Esc / 点遮罩关闭，用户找不到出口。保留标题栏 + 关闭按钮，并用 `:before-close` 做未保存拦截 | T6 |
| D43 | **表单提交只能有一个入口** | `<el-form @submit.prevent="onSubmit">` 与 `<el-button native-type="submit">` 已构成提交链路时，**绝不能再给按钮加 `@click="onSubmit"`、也不能给输入框加 `@keyup.enter="onSubmit"`** —— 那会让一次点击走两条路径、发出两次请求。登录场景尤其致命：验证码**一次性**，第 2 次请求必然报「验证码已过期」，而第 1 次其实已登录成功（2026-10-17 用户实测：点一次发出 2 次 `/auth/login`）。另在提交函数首行加 `if (loading.value) return` 兜底 | T6 |
| D44 | **改完后端必须重启 8080 的进程** | 后端进程跑旧代码时，前端调新接口会拿到 `40400 请求的资源不存在`（**HTTP 仍是 200、控制台无报错**），极易误判为前端 bug。自查：`/health` 的 `uptime` + 对比源码文件修改时间。同理：**`mvn clean` 前必须先停掉占用 jar 的进程** | 通用 |
| D45 | **html 清洗放在 Service 且「先清洗、再算 content_text」** | `NoteServiceImpl` 在 create / update 里调 `HtmlSanitizer.cleanForStore()`；**顺序不能反** —— 先用原文算搜索列会把 `<script>` 里的文本吃进 `content_text`。md 分支**原样返回**（Markdown 源码过 HTML 清洗会破坏语法），md 的防护在渲染端（D47）。另：html 的「正文是否为空」用 `hasVisibleContent`（文本或 img），否则「只贴一张图」的合法笔记会被误杀 | T7 |
| D46 | **md 里的原生 HTML 默认转义、不渲染** | markdown-it 保持 `html: false`；并把 md-editor 的 `htmlPreview` 工具栏**摘掉**（它是「渲染原始 HTML」的开关，既与策略冲突又是 XSS 面）。要富文本排版请用 html 格式笔记 —— 一条笔记一种格式（铁律①） | T7 |
| D47 | **md 渲染产物要过「markdown 档」DOMPurify，不能沿用富文本档** | 富文本档的 `ALLOWED_ATTR` 不含 `class`（防外部样式污染），但 md 渲染产物里代码高亮靠 `hljs-*`、代码块外壳靠 `md-editor-code-block` —— 沿用严格档会把它们一起剥掉，**代码高亮与代码块样式直接失效**。故 `sanitizeHtml.js` 增 markdown 档：标签/协议白名单不变，仅放行 `class` 并用钩子按 `hljs-` / `md-editor-` / `language-` 前缀收敛 | T7 |
| D48 | **富文本工具栏白名单化（只留「保存后仍存活」的菜单）** | jsoup 的 `style` 白名单只放行 `text-align / text-indent / color / background-color`，`class`/`id` 一律剥。默认工具栏的「字号/字体/行高/缩进/波浪线/着重号/格式刷」产生的正是被剥掉的样式 → **编辑时看到、保存后消失**（违反 NFR「绝不允许以为存了其实没存」）。故 `RichTextEditor` 用 `toolbarKeys` 显式收窄为 21 项 | T7 |
| D49 | **编辑器的「初始化规范化」不算用户编辑** | wangEditor 初始化会把入库 HTML 按 Slate 重新序列化（且空内容给 `<p><br></p>`，是非空字符串）→ 直接当输入会「一打开就显示有未保存的修改」、并污染空表单。故：① 编辑器创建后 250ms **静默期**，期间不 emit `update:modelValue`；② 静默结束把「编辑器认下来的 HTML」经 `normalize` 事件交给表单**重置基线**；③ 空 HTML（仅有空段落/换行，且无 `img`）归一成 `''`；④ 格式选择用**单向绑定**（`:model-value` + `@update:model-value`），否则 `v-model` 会在 `@change` 前就换掉编辑器、确认框形同虚设 | T7 |
| D50 | **验证脚本两条硬规矩** | ① 页面表达式（模板字符串）里的**注释禁止出现反引号** —— 会提前截断模板字符串，Node 侧报 `content is not defined`；② 脚本的 `.catch` 必须 `chrome.kill()` + `process.exit()`，只设 `exitCode` 会让 Chrome 子进程与 WebSocket 吊住事件循环 → **表现为「假死、零输出」**（2026-10-17 实际卡掉一轮，见交接文档 §四 T7 坑） | 通用 |
| D51 | **项目 / 标签关联用「全量替换」，且与正文同事务提交** | 请求体带可选 `associations{projectId, tagIds}`：**带了这个对象就以它为准**（`projectId=null` 清空项目、`tagIds=[]` 清空标签），**不带则完全不动关联**（T5/T6 老调用方不受影响）。服务端先清空该笔记在 `note_tag` 的全部行再重建 —— 「一条笔记最多一个项目」（铁律②）因此在**写入路径上结构成立**，不存在「新增第二个项目」的代码路径。关联与正文在同一个 `@Transactional` 里，避免「正文存了、标签没存」的半边成功 | T8 |
| D52 | **计数口径 = 全部状态（含待整理），一次分组查询** | `noteCount` = 该标签关联的**未删除**笔记数，**不区分 status**（PRD #12 于 2026-10-19 拍板）。管理页与删除确认用的是同一个数，所以「卡片显示 2 条 → 删除提示影响 2 条」自洽。实现用 `GROUP BY tag_id` 一次拿全表，禁止逐条 COUNT（N+1）；已逻辑删除的笔记通过 `JOIN note ... is_deleted=0` 排除 | T8 |
| D53 | **排序用「整组 id 提交」，服务端重写 0,10,20…** | `PUT /tag/sort` 传该类型下**全部** id 的目标顺序；服务端校验「数量与内容都一致」后按 `index*10` 重写。理由：两两交换 sort 在重复点击 / 并发下容易撞成同值，重排结果不可预期；整组提交是确定的。新建默认 `max(sort)+10`，排到末尾。前端用**上移/下移**按钮，不引拖拽库 | T8 |
| D54 | **清空类更新必须走 wrapper 显式 set** | `updateById` 会忽略 null 字段 → 「把颜色清空」永远清不掉。改色 / 清色一律 `Wrappers.lambdaUpdate().set(Tag::getColor, …)`。与 D38「置顶/归档显式写回 updated_at」是同一类坑：**想写 null 或想写回原值，都不能走实体更新** | T8 |
| D55 | **列表筛选先算候选 note_id 集合，不写 join** | 项目单选与标签多选都先由 `note_tag` 算出候选 id：标签 **AND** 用 `GROUP BY note_id HAVING COUNT(DISTINCT tag_id)=N`，项目用单标签查询；多条件时取交集，最后 `IN` 收窄。理由：自定义 join 与 MyBatis-Plus 分页 / `@TableLogic` / `is_deleted=0` 叠加后极易出错，而个人数据量下多一次聚合查询的代价可以忽略。空集合直接返回空页（不查主表） | T8 / T10 |
| D56 | **`MetaObjectHandler` 一律「有 setter 才填」** | T8 新增的 `note_tag` 两个字段都没有、`tag` 没有 `updated_at`；原来的无条件 `strictInsertFill` 是 T1 踩过的雷（实体没有属性会抛异常）。填充前统一 `metaObject.hasSetter(...)` 判断，新增实体不必回头改配置 | T8 |
| D57 | **首页统计「一次请求」，且与列表筛选共用同一周口径** | 新增 `GET /note/stats` 一次返回 `inbox / archived / projects / weekAdded`（首页不打 4 枪）；「本周」= **本周一 00:00**（`LocalDate.now().with(DayOfWeek.MONDAY)`），`weekOnly` 列表筛选复用**同一个** `weekStart()` —— 统计与点进去的列表必须一致，否则「+9 点进去只有 8 条」这类问题极难解释 | T9 |
| D58 | **首页的滚动：页面自己不滚，每列 `overflow-y:auto`** | 首页 `height:100% + min-height:0` 占满 `.app-main` 且不产生整页滚动；三列 body 各自 `overflow-y:auto`（D34 落地点）。**验收项**：滚某列时其余列 `scrollTop` 与 `.app-main.scrollTop` 都必须保持 0 —— 这条能一次性抓住「整页跟着滚」和「列没滚起来」两类回归 | T9 / T12 |
| D59 | **快速记录的三条实现约束** | ① 输入文字**同时**作为 `title` 与 `content`（单行输入，title 由后端截断 255）；② 提交中输入框 `disabled`，**对 disabled 元素 `focus()` 无效** → 焦点必须在 `creating` 复位之后再恢复；③ 中文输入法用 `compositionstart/end` + `event.isComposing` 双保险，**组词中的回车不提交**；格式按钮状态存 `sessionStorage`（刷新保持，PRD 记为体验目标） | T9 |
| D60 | **首页三列各自只取 20 条，不做「加载更多」** | PRD P-010 明确首页只承载「看一眼」，全量在列表页；三列各 `size=20`（按冻结排序取头部）。避免首页因为「加载更多」变成第二个列表页，也避免一次拉全量 | T9 |

---

## 7. 待排期优化项

> 记录**已知但不阻塞当前任务**的优化点。与 PRD §3.2（V0.2 候选池）互为补充 —— 这里放**实现层**的，那里放**产品层**的。

| # | 项 | 来源 | 建议 |
| :--- | :--- | :--- | :--- |
| O1 | `content_text` 历史漏同步修复脚本 | PRD R1 / NFR §3 | T12 视情况做一次性核对工具 |
| O2 | 数据备份任务（改自 `backup-osc.ps1`） | PRD NFR §3 | T12 前给用户拍板 |
| O3 | 孤儿对象清理（复用 OSC `StorageCleanupService` 思路） | PRD 第十二章 #8 | T11 后视情况 |
| O4 | 搜索升级为全文索引（中文 `ngram`） | PRD 第十二章 #14 | 数据量上万后再评估 |
| O5 | 键盘快捷键体系 | PRD §3.2 V0.2 候选 | 观察期后按痛点排 |

---

**文档结束** ｜ 初始化于 2026-10-09。下一步：用户审定 PRD V0.1 → 关闭 PRD 第十二章待拍板项 → 执行 T1。
