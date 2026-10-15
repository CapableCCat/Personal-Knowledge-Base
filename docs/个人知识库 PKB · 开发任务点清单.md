# PKB 个人知识库 · 开发任务点清单（工作台账）

> **用途**：把 PRD V0.1 拆成**可逐个交付的任务点**，并记录每个任务点的方案、实现结果、验证方式与实现层决策。
>
> **职责边界（单一出处）**：本清单只管三件事 —— **§3 总表看进度**、**§4 任务点详情**、**§6 实现层决策**。
> 开会话要用的一切（开场提示词、当前阶段、关键词速查、环境坑、全局铁律）一律在
> `docs/PKB 个人知识库 · 新对话交接提示词（通用版）.md`，**不在本文件重复抄写**。
>
> **配套文档**：
> - `docs/个人知识库 PKB · 产品需求文档 V0.1.md`（**权威需求依据**，代码以 PRD 为准）
> - `docs/PKB 个人知识库 · 新对话交接提示词（通用版）.md`（开工与铁律，新对话第一份读物）
> - `docs/PKB 个人知识库 · 现状信息说明.md`（项目缘起与决策讨论结论，历史存档）
> - `.workbuddy/memory/`（项目记忆：`MEMORY.md` + 每日日志）
>
> **维护方式**：每完成一个任务点 → 更新 §3 状态列 + 在 §4 该条目补「实现结果」+ §6 追加决策。
>
> **本文件当前状态**：🟡 **初始化模板**。§3 总表为**草案编号**（T1~T12，取自 PRD §3.1 与交接文档 §三）；§4 只填了 T1（本轮任务），T2 起在开工前补「技术方案」。

---

## 1. 项目基本盘

| 项 | 内容 |
| :--- | :--- |
| 项目 | PKB 个人知识库（Personal Knowledge Base）· V0.1 |
| 定位 | 自用「第二大脑」，单用户，本机 / 局域网 |
| 工作空间 | `F:\Project\Own Project\Personal-Knowledge-Base`（**DSH 工作空间绑定，不可改名**） |
| 需求依据 | `docs/个人知识库 PKB · 产品需求文档 V0.1.md` |
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
| T2 | 数据层（4 张业务表 + `user` 简化 + 实体 + Mapper + 建库脚本） | PRD §5 / §6 | T1 | ⬜ 未开始 |
| T3 | 单账号登录（引导页 + Sa-Token + 图形验证码） | P-011 | T2 | ⬜ 未开始 |
| T4 | 外壳改造（菜单砍项 + 底部 3 Tab + 只留登录守卫 + `roles.js` 整删） | PRD §8.2 / §2.2 | T3 | ⬜ 未开始 |
| T5 | 笔记 CRUD（后端，含 `content_text` 同步 / 归档 / 取回 / 置顶） | P-001 / P-005 | T2 | ⬜ 未开始 |
| T6 | 笔记列表 + 详情（前端，drawer / 全屏页 + 复用同一份表单） | P-002 / P-003 | T5 | ⬜ 未开始 |
| T7 | 双格式编辑（md 编辑器选型 + 富文本复用 + 分流清洗） | P-004 | T6 | ⬜ 未开始 |
| T8 | 项目 / 标签（CRUD + 关联 + **项目单选后端兜底** + 计数） | P-006 / P-007 | T5 | ⬜ 未开始 |
| T9 | 首页工作台（桌面三列各自滚动 + 手机 3 Tab + 状态条） | P-010 | T6 | ⬜ 未开始 |
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

### T2 数据层 ⬜

> 状态：**未开始**。**开工前必须先出技术方案并经用户确认**（涉及表结构，铁律 2）。

- **覆盖**：PRD §5（数据模型）、§6（数据与字段决策）
- **依赖**：T1
- **范围**：4 张业务表（`note` / `tag` / `note_tag` / `attachment`）+ `user` 表简化 + 实体 + Mapper + 建库脚本（`00_init_database.sql` / `01_schema.sql`，**无 DROP、幂等**）
- **开工前需用户拍板**（PRD 第十二章）：#4 `user` 表保留字段、#15 `tag` 唯一约束与逻辑删除共存方式
- **技术方案**：⬜ 待开工前产出

#### T2 实现结果

⬜ 未开始。

---

### T3 单账号登录 ⬜

> 状态：**未开始**。**开工前必须先出技术方案并经用户确认**（涉及鉴权公共抽象，铁律 2）。

- **覆盖**：P-011（单账号登录）
- **依赖**：T2
- **范围**：引导页（首启建单账号）+ 登录（图形验证码 + 失败锁定）+ Sa-Token Header + 登录守卫；**强制改密删除**；`constants\roles.js` 整删
- **开工前需用户拍板**（PRD 第十二章）：#3 Header key、#10 忘记密码重置方式、#16 token 有效期策略
- **技术方案**：⬜ 待开工前产出

#### T3 实现结果

⬜ 未开始。

---

### T4 外壳改造 ⬜

> 状态：**未开始**。

- **覆盖**：PRD §8.2（页面地图）、§2.2（单账号原则）
- **依赖**：T3
- **范围**：`AppShell` 菜单砍到 3~4 项（笔记 / 项目 / 标签）、底部 3 Tab 改造、路由表重建、守卫只留登录判定
- **开工前需用户拍板**（PRD 第十二章）：#2 品牌主色与视觉 token
- **技术方案**：⬜ 待开工前产出

#### T4 实现结果

⬜ 未开始。

---

### T5 笔记 CRUD（后端） ⬜

> 状态：**未开始**。**开工前必须先出技术方案并经用户确认**（涉及 `content_text` 公共抽象，铁律 2）。

- **覆盖**：P-001（快速记录）、P-005（待整理闭环）
- **依赖**：T2
- **范围**：笔记新增 / 更新 / 归档 / 取回 / 置顶 / 逻辑删除；**`content_text` 同步收口在 Service 层**（铁律 3）；`content_format` 建后不可更新（显式拦截）
- **开工前需用户拍板**（PRD 第十二章）：#13 `content_text` 去标记的具体规则
- **风险**：R1（漏同步导致搜索漏数据，且不报错）
- **技术方案**：⬜ 待开工前产出

#### T5 实现结果

⬜ 未开始。

---

### T6 笔记列表 + 详情（前端） ⬜

> 状态：**未开始**。

- **覆盖**：P-002（笔记列表）、P-003（笔记详情 / 编辑）
- **依赖**：T5
- **范围**：列表页（筛选 + 排序 `pinned DESC, updated_at DESC` + 分页）；桌面 `el-drawer`（~720px）+ 手机全屏页 `/note/:id`，**两处复用同一份表单组件**
- **关键坑**：`el-drawer` 关闭是 `v-show`，验证「已关闭」用 `!!el.offsetParent`
- **技术方案**：⬜ 待开工前产出

#### T6 实现结果

⬜ 未开始。

---

### T7 双格式编辑 ⬜

> 状态：**未开始**。**开工前必须先出技术方案并经用户确认**（涉及技术选型，铁律 2）。

- **覆盖**：P-004（双格式编辑）
- **依赖**：T6
- **范围**：md 编辑器接入 + 富文本编辑器复用 + 后端按格式分流清洗 + 前端渲染前 DOMPurify（**md 产物同样要过**）
- **开工前需用户拍板（开工阻塞项）**（PRD 第十二章 **#1**）：Markdown 编辑器选型 —— 候选 `md-editor-v3`（倾向）/ `Vditor` / `CodeMirror + marked`
- **风险**：R2（选型不当导致表格 / 代码块异常，而这正是核心场景）、R5（md 渲染路径 XSS 穿透）
- **技术方案**：⬜ 待开工前产出

#### T7 实现结果

⬜ 未开始。

---

### T8 项目 / 标签 ⬜

> 状态：**未开始**。

- **覆盖**：P-006（项目管理）、P-007（标签管理）
- **依赖**：T5
- **范围**：`tag` CRUD（`type=0` 标签 / `type=1` 项目）+ `note_tag` 关联 + **项目单选后端兜底（已有关联 `type=1` 时替换而非新增）** + 项目卡片计数
- **开工前需用户拍板**（PRD 第十二章）：#12 项目计数口径
- **风险**：R4（项目单选在数据层失守，产生脏数据且发现晚）
- **技术方案**：⬜ 待开工前产出

#### T8 实现结果

⬜ 未开始。

---

### T9 首页工作台 ⬜

> 状态：**未开始**。

- **覆盖**：P-010（首页工作台）
- **依赖**：T6
- **范围**：桌面顶部输入框（回车即建 inbox，不跳页）+ 状态条 + 三列（项目 / 最近 / 待整理）**各自滚动**；手机顶部输入框常驻 + 精简状态条 + 底部 3 Tab（切 Tab 只换内容区）
- **开工前需用户拍板**（PRD 第十二章）：#11 「本周 +9」口径
- **风险**：R3（嵌套滚动失效 —— `min-height:0` 逐层加）
- **技术方案**：⬜ 待开工前产出

#### T9 实现结果

⬜ 未开始。

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
