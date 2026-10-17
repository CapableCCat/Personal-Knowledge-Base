# PKB 个人知识库 · 新对话交接提示词（通用版）

> **本文件是本项目的「通用交接文档」，长期维护、每次交接前更新。**
> 两层用途：
> 1. **开新对话时** —— 把下面「新对话开场提示词」整段复制给新对话即可；
> 2. **新对话建立上下文时** —— 它就是「必读总纲」，写清项目全貌、当前阶段、环境与铁律。
>
> 维护约定见文末 **「九、交接约定」**。

## 新对话开场提示词（每次开新对话，直接复制这一段）

```markdown
你好 DS。开始「PKB 个人知识库（Personal Knowledge Base）」V0.1 开发 —— 新对话接续，麻烦先重建上下文。

第一步：完整读取 F:\Project\Own Project\Personal-Knowledge-Base\docs\个人知识库 PKB · 新对话交接提示词（通用版）.md ，
并严格按其中「二、动手前必读（按顺序）」把项目上下文建立起来。
⚠️ 本文件开头的「冻结决策速览」是不可动摇的红线，动任何一条前必须先问我。

第二步：按该文档「三、当前阶段与下一步」确定本轮要做什么；若那一节指向某个任务点，
就去《开发任务点清单》读该任务点的条目（技术方案 / 实现结果 / 决策 D 编号）。

第三步：先输出你的理解（3~5 条：当前进度、本轮任务、关键红线），再给出实施计划（改动文件、
影响面、怎么自测、怎么验证），等我确认后再动手。

务必遵守该文档的「五、全局铁律」与「六、协作规则」——尤其是：单功能节奏、先方案后代码、
不主动 git 提交（我说「提交Tx」才提交）、交付必须附 PowerShell 5.1 友好的验证指引。

⚠️ 提醒：PKB 是从 0 到 1 的新项目，不是 OSC 的延续；OSC 只作脚手架参考，
业务依据以本文件 + PRD V0.1 为准。
```

-----

## 零、冻结决策速览（不可动摇的红线）

> 以下决策已经用户拍板，**开发期间不得擅自更改**。要改必须先列出选项让用户重新拍板。

### 数据模型（4 张表，已冻结）

| 表           | 关键字段                                                     |
| :----------- | :----------------------------------------------------------- |
| `note`       | id, title, content, content_format(0=md/1=html), content_text, status(0=inbox/1=archived), pinned, created_at, updated_at, is_deleted |
| `tag`        | id, name, type(0=标签/1=项目), color, sort, created_at, is_deleted |
| `note_tag`   | note_id, tag_id（联合主键）                                  |
| `attachment` | id, note_id, file_key, file_name, size, mime, created_at, is_deleted |

### 五条铁律

1. **一条笔记一种格式**：`content_format` 新建时选定（默认 md），之后不切换。
   —— 已否决「双列镜像存 md+html」（双份存储易漂）和「只存 md 实时转」（转换有损）。
2. **项目 = `tag.type=1`**：表结构允许多对多，**UI 层约束为单个**（详情页项目单选、标签多选）。
   —— 已否决「项目独立建表」（V0.1 用不上元信息）和「note.parent_id 自关联」（语义混乱）。
3. **`content_text` 是搜索专用列**：md 存原文（或轻量去标记），html 由后端剥标签得到。
   **搜索只打 `title + content_text`，永不打 `content`**（否则命中 html 标签）。
4. **排序口径**：`ORDER BY pinned DESC, updated_at DESC`。`pinned` 明确保留。
5. **搜索范围（V0.1）**：只搜标题 + 正文；搜标签名留给后续版本。

### 首页形态（已冻结）

- **桌面**：顶部输入框 + 状态条 + 三列（项目 / 最近 / 待整理），三列各自滚动。
- **手机**：顶部输入框常驻 + 底部 3 Tab（最近 / 待整理 / 项目）。
- **顶部输入框回车即建 inbox，不跳页**；格式切换按钮默认 md。
- **打开笔记**：桌面 `el-drawer`（~720px），手机独立全屏页 `/note/:id`；两处复用同一份表单组件。

### 范围边界（已冻结）

- **V0.1 做**：P-001~P-011 共 11 条 Must（快速记录 / 列表 / 详情 / 双格式 / 待整理闭环 / 项目 / 标签 / 搜索 / 附件 / 首页 / 单账号登录）。
- **V0.1 不做**：多用户 / 注册 / 分享 / 协作、版本历史、回收站 UI、多角色权限矩阵、导入导出界面、看板、公告、反馈、报名审核、短信、成员档案、飞书集成、上线部署。
- **RAG / 向量检索 / AI 问答**：放 V0.3，V0.1 不碰。
- **保留但不启用**：FastExcel（`cn.idev.excel`）+ ECharts 封装代码**保留不动**，未来极可能用上，**不得顺手删**。
- **单账号**：无权限矩阵，`constants/roles.js` **整删**；强制改密删除；图形验证码保留。

### 起点策略（已冻结）

- **复制 OSC 仓库 → 删业务 → 改名（`com.pkb` / `pkb-server` / 库 `pkb` / 桶 `pkb`）→ 跑通空壳**。
- **OSC 原仓库只读参考，不得改动**；任何重构只发生在 PKB 工作区。

---

## 一、项目基本盘

- **项目**：PKB 个人知识库（Personal Knowledge Base）· V0.1（自用「第二大脑」，单用户）
- **定位**：A 快速记录 + B 项目档案 + C AI 检索 三段合一，共用一份数据；核心闭环 = 写（不管分类）→ 进 inbox → 清理归类 → 进 archived
- **工作空间**：`F:\Project\Own Project\Personal-Knowledge-Base`
  - ⚠️ **目录 / GitHub 仓库名 / git 配置待改名**：本文件撰写时物理目录仍是 OSC 的 `New-Management-System`（复制而来），社长将把目录改名为 `Personal-Knowledge-Base` 并同步改 GitHub 仓库名与本地 git 配置；**路径一律以目标名书写**，rename 后直接对齐。
  - （独立 git 仓库，分支 `main`；远端待建 `git@github.com:CapableCCat/Personal-Knowledge-Base.git`，协议 Apache-2.0 沿用）
- **脚手架来源（只读参考）**：`F:\Project\Own Project\New-Management-System`（OSC 仓库）—— 复制其工程结构后删业务代码，**PKB 开发期间不得改动 OSC 原仓库**（任何重构只发生在 PKB 工作区）。
- **技术栈**：
  - 后端 `server/`：Java 17 + Spring Boot 3.3.x + MyBatis-Plus 3.5.x + Sa-Token 1.44 + MySQL 8（库 `pkb`）+ Redis + MinIO（bucket `pkb`）
  - 前端 `web/`：Vue 3.5 + Vite + Element Plus + Vant + Pinia + vue-router + axios
  - monorepo 单仓：`server/` + `web/`
- **端口**（沿用 OSC 约定，待 T1 确认）：后端 dev 8080 / prod 8081；前端 dev 5173（`/api` 代理到 8080 并剥前缀）
- **部署形态**：单用户，本机 / 局域网；V0.1 **不做**上线部署（无公网、无多实例），故本文件**不含「上线部署」章节**。
- **当前状态**：**V0.1 启动前夕 —— T1 脚手架落地尚未执行**；PRD V0.1 已定稿，数据模型已冻结（4 张表），功能范围 11 条 Must 已锁定。
- **V0.1 明确不做**（避免范围蔓延）：多用户 / 注册 / 分享 / 协作、版本历史、回收站 UI、多角色权限矩阵、导入导出界面、看板、公告、反馈、报名审核、短信、成员档案、飞书集成、上线部署。
- **RAG / 向量检索 / AI 问答**放到 **V0.3**（等数据积累起来再加）；V0.1 只解决「存得进、找得到」。
- ⚠️ **V0.1 是新项目从 0 到 1 的第一步，不是 OSC 的延续**：OSC 仅作「工程脚手架参考」（复制结构 → 删业务 → 改名），业务逻辑、权限模型、数据模型全部重新设计，不复用 OSC 的任何业务代码。

### 数据模型（已冻结，V0.1；权威依据见 PRD V0.1）

> 下为速览，字段级约束与索引以 PRD V0.1 为准（单一出处）。

| 表           | 关键字段                                                     | 说明                                            |
| :----------- | :----------------------------------------------------------- | :---------------------------------------------- |
| `note`       | id, title, content, content_format(0=md/1=html), content_text, status(0=inbox/1=archived), pinned, created_at, updated_at, is_deleted | 一条笔记一种格式，新建选定后**不切换**，默认 md |
| `tag`        | id, name, type(0=标签/1=项目), color, sort, created_at, is_deleted | 项目 = `type=1`；表允许多对多，UI 约束项目单选  |
| `note_tag`   | note_id, tag_id（联合主键）                                  | 笔记—标签/项目关联                              |
| `attachment` | id, note_id, file_key, file_name, size, mime, created_at, is_deleted | 附件仅存对象 key，读时拼公开前缀                |

**三条铁律（冻结，改动需用户拍板）**：
1. **一条笔记一种格式** —— 新建时选定、之后不切换、默认 md（否决「双列镜像存 md+html」「只存 md 实时转」）。
2. **项目 = `tag.type=1`** —— 表结构多对多，UI 层约束为单个（详情页项目单选下拉、标签多选；否决「项目独立建表」「note.parent_id 自关联」）。
3. **`content_text` 是搜索专用列** —— md 存原文、html 由后端剥标签得到；**搜索只打 `title` + `content_text`，永不打 `content`**（否则命中 html 标签）。

**排序口径**：`ORDER BY pinned DESC, updated_at DESC`（pinned 明确要求保留）。
**搜索范围（V0.1）**：只搜标题 + 正文；搜标签名留给后续版本。

---

## 二、动手前必读（按顺序，别跳）

1. 本文件 —— 全貌、当前阶段、环境坑、铁律
2. `docs/个人知识库 PKB · 产品需求文档 V0.1.md` —— **权威需求依据**（按功能卡片 P-001~P-011 按需查）
3. `.workbuddy/memory/MEMORY.md` —— 项目长期记忆（技术选型、环境怪癖、各任务点红线）
4. `.workbuddy/memory/YYYY-MM-DD.md` —— 最近工作日志（倒序读最近 1~2 份）
5. `docs/个人知识库 PKB · 开发任务点清单.md` —— **工作台账**：§3 总表看进度、§4 任务点详情（方案/实现/验证）、§6 决策记录（按 D 编号索引）【随开发维护；V0.1 启动前可由 AI 按 OSC 清单结构初始化空模板】
6. OSC 交接文档（只读参考）：`F:\Project\Own Project\New-Management-System\docs\OSC 社团管理系统 · 新对话交接提示词（通用版）.md` —— 仅用于核对「被复用资产」的原始实现（统一返回体 / 三档外壳 / XSS 链路 / MinIO 上传），**不要照搬业务代码**。

> **需求基准**：PRD V0.1 是权威依据；PRD 未覆盖或与实现冲突时，以《开发任务点清单》§6 决策记录为准；若决策也未覆盖，**列出选项让用户拍板，不要擅自决定**。

---

## 三、当前阶段与下一步

> **本节是「本轮做什么」的唯一出处**，每次交接由 AI 更新。

**阶段：V0.1 进行中 —— T4 外壳改造（菜单砍项 + 底部 3 Tab + 登录守卫已就位）**

| 项目                   | 内容                                                         |
| :--------------------- | :----------------------------------------------------------- |
| **已完成**             | ① PRD V0.1（11 条 Must、4 表 + 3 铁律；17 项待拍板已关闭 **8** 项）；② 《开发任务点清单》（T1~T12 转正 + D1~D32 决策）；③ **T1 脚手架落地** ✅；④ **T2 数据层** ✅；⑤ **T3 单账号登录** ✅ |
| **T1 交付要点**        | OSC 迁移 298 文件 + `.git`（98 提交）入 NMS；后端 156→37 文件、`com.pkb`；前端 64→31 文件、i18n 与 `roles.js` 整删；建库 `pkb` + `pkb_app` + 5 表；已推送（`c0455de`/`594982f`/`5e1677a`） |
| **T2 交付要点**        | 4 实体 + 4 Mapper + 3 常量 + `ContentTextBuilder` 收口（12 文件）；**实测抓出 MP 3.5.7 不支持多 `@TableId`**；五表列清单逐一一致；已推送（`691e094`/`08e8aa0`） |
| **T3 交付要点**        | **只改配置 + 验证，生产代码零改动**：`timeout` → **604800（7 天）**、`is-concurrent` → **false（禁多端）**（yml 与 `.env` **两处**都改）；**接口 19/19 + 页面 17/17 全过**；移植出 `pkb-verify` 验证技能（不入仓）；**一条假设被证伪**：T3 阶段无页面调受保护接口 → 40100 端到端验证**推迟到 T5/T6**；测试账号已硬删、`user` 回到 1 行；**未提交** |
| **本轮任务（下一步）** | **T4 外壳改造**：菜单砍项（已砍到 4 项）、底部 3 Tab 改造、登录守卫（**已就位并实测通过**）。⚠️ **开工前必须先出技术方案经用户确认**（铁律 2，涉及外壳公共抽象 + 滚动模型）。⚠️ **首页三列的 3 Tab 属 T9**，T4 只做外壳层 |
| **之后**               | T5 笔记 CRUD → T6 列表+详情（**届时补验 40100 端到端**）→ T7 双格式编辑 → T8 项目/标签 → T9 首页工作台 → T10 搜索 → T11 附件 → T12 联调打磨 |

**开工前必做（环境）** —— 完整清单与命令看「八、启动清单与常用命令」，这里只留要点：
1. MySQL / Redis 是 AUTO_START 服务（开机自启，不用管）；只有 MinIO 需手动起。
2. 常驻服务（MinIO / 后端 / 前端）由用户在会话外手动起，AI 不用会话内后台任务起它们（避免唤醒旧会话，见 §四）。
3. AI 自测只用隔离栈（后端 8090 + 预览站 5180）且当轮收掉。
4. ✅ **目录与 git 已定型**：PKB 工作区 = `Personal-Knowledge-Base`（DSH 绑定，**不可改名**）；OSC 只读档案 = `New-Management-System`（98 提交，只 fetch 不 push）。
5. ⚠️ **`pkb.user` 里已有 1 行真实账号**（用户本人，`18178325352`）——**任何会清空 / 改动 `user` 表的测试都不要做**，也不要再调 `/auth/init-admin`（已初始化会被拒）。
6. ✅ **账号重置流程已实战演练通过**（2026-10-15）：删 user → 清 Redis → 引导页出现 → 恢复账号，全链路实测；
   **解耦核心保证已证明**：删 `user` 行后四张业务表一行未动。步骤见 `README.md`「八、常见问题」，决策见 PRD 附录 **C.4**。

**待用户定夺（开工前）**：
- **Markdown 编辑器选型**：候选 `md-editor-v3`（倾向，Vue3 生态最顺）/ `Vditor` / `CodeMirror + marked`；**T7 开工前必定**（PRD 第十二章 #1）。
- **T5 前需拍板**：PRD 第十二章 **#13**（`content_text` 去标记的具体规则 —— 当前 `ContentTextBuilder.Default` 已可用，但规则本身待定稿）。
- ✅ **T3 的开工阻塞项已全部关闭**：#3（Header key）、#10（重置方式）、**#16（token 策略：7 天 + 禁多端）** 均已拍板。
- 其余待拍板项见 PRD 第十二章（共 **9 项**未关闭，均不阻塞 T3）。

---

## 四、环境与坑

### 0) 先探活，再拉起（沿用 OSC 经验）

⚠️ agent 起的进程「去留不确定」：本轮回复结束可能被回收，也可能一直活着（后端 jar 锁文件会把 `mvn clean package` 顶失败）。
规则：开新一轮先探活（下面命令），不在线就自己拉起；重新构建/启动前先查端口占用并停掉旧进程（尤其 8080），别硬撞。

```powershell
curl.exe -s -o NUL -w "%{http_code}" --noproxy 127.0.0.1 http://127.0.0.1:8080/health
& "C:\Program Files\Redis\redis-cli.exe" -h 127.0.0.1 -p 6379 PING
curl.exe -s -o NUL -w "%{http_code}" --noproxy 127.0.0.1 http://127.0.0.1:9000/minio/health/live
```

### 沿用 OSC 已验证的坑（只列要点，细节见 OSC 交接文档「四、环境与坑」）

- ⚠️ **宿主注入 `SERVER__PORT` 必须清**：在 WorkBuddy 终端起后端前 `Remove-Item env:SERVER__PORT; Remove-Item env:SERVER__HOST`，否则 Spring 宽松绑定拿它抢端口；也可 `--server.port=8080`。
- ⚠️ **PowerShell 5.1 测试脚本一律纯 ASCII**：含 `\"` 的 SQL 别用 PS 字符串拼（会篡改引号），写成 `.sql` 文件再 `source`。
- ⚠️ **页面验证用 Chrome 无头 + CDP 设备模拟**：Windows 上 `--window-size=375` 会被系统最小宽度顶成 504，必须用 `Emulation.setDeviceMetricsOverride` 才是真实移动视口。
- ⚠️ **`el-drawer` / `el-dialog` 关闭是 `v-show`（`display:none`），元素仍在 DOM**：判「已关闭」用 `!!el.offsetParent`，别用 `!!document.querySelector('.el-drawer')`。PKB 笔记详情走 `el-drawer`（桌面），这条直接复用。
- ⚠️ **外壳滚动模型**：`.app-shell` = `height:100% + overflow:hidden`，`.app-main` 是唯一滚动容器；**`min-height:0` 最大的坑** —— flex 子项默认 `min-height:auto`，`.app-body` 与 `.app-main` 两处都必须加，否则内部滚动失效。PKB 首页是「三列各自滚动」，嵌套更深，逐层都要加。用 `height:100%` 而非 `100dvh`。
- ⚠️ **MinIO 启动必须显式带凭据**：`MINIO_ROOT_USER=minioadmin MINIO_ROOT_PASSWORD=minioadmin`，否则报 `Unable to validate credentials inherited from the shell environment`；别用 `| head` 起（管道一关进程被带走），重定向到日志再 `run_in_background`。
- ⚠️ **`git commit` 与 `git push` 必须分两条命令**：命令可能被沙箱升级重试跑两遍，第二次 `git commit` 报 "nothing to commit" 返回 1 → 把 `&&` 链后面的 `git push` 短路掉，看着像提交成功其实没推。收尾一律 `git log --oneline origin/main..HEAD` 核对（空=已推）。

### PKB 新坑补充（本次推理，待 T1~T12 实测回填）

- ⚠️ **`content_text` 同步是数据一致性红线**：保存/更新 `note` 时必须同步算 `content_text`（md 存原文或轻量去标记、html 由后端 jsoup 剥标签得到）。**漏同步 = 搜索漏数据**。建议：在 Service 层统一收口（新增/更新都过同一道「写 content + 算 content_text」），别让 Mapper 直接散落写。`content_text` 建索引只覆盖 `title + content_text`。
- ⚠️ **md 原文做搜索列会命中 markdown 标记**：`#`/`*`/`-` 等符号进 `content_text` 后，搜「项目」可能误召回含 `# 项目` 的笔记。建议落 `content_text` 时对 md 也做**轻量去标记**（剥 `#`、`*`、链接语法、代码围栏），保留纯文本；html 走 jsoup `text()`。具体规则 T5 方案里定。
- ⚠️ **双格式编辑器的 XSS 都要过 DOMPurify**：md 经编译器渲染成 html 后、富文本存库前，两端产物都需用 `DOMPurify`（前端）或 jsoup（后端）再过一遍——md 编译器输出同样可能含注入。**唯一允许 `v-html` 渲染笔记正文的地方要加清洗，且编辑态与展示态都用清洗后内容**。
- ⚠️ **首页三列各自滚动 + AppShell 改造**：复用 OSC 三档外壳，但底部 Tab 改为「最近 / 待整理 / 项目」、菜单砍到 3~4 项。首页三列是 grid/flex 嵌套滚动，`.app-main` 内的每一列容器也要 `min-height:0 + overflow:auto`，**逐层加**，否则只有最外层滚、列内容被裁。
- ⚠️ **单账号 + 引导页**：首启引导创建 1 个账号，强制改密逻辑删除、`constants/roles.js` 整删、守卫只留登录守卫。`user` 表应简化为单账号（去掉 `role/department/duty` 推导），但 `@TableLogic` / Sa-Token 骨架沿用——改表结构前先确认 OSC 的 `user` 表哪些字段可删。
- ⚠️ **`note_tag` 多对多但项目单选**：表层不限制一个 note 关联多个 `type=1` 的 tag，UI 也只给单选下拉；**后端在关联写入时要兜底**（已有关联 `type=1` 时替换而非新增），否则数据层会出现「一个笔记多个项目」的脏数据。
- ⚠️ **附件不止图片**：PKB 附件可能是 pdf/doc/xlsx 等，`ImageValidator` 是图片专用；通用文件上传要放宽魔数校验（按扩展名 + MIME 白名单），复用 MinIO 上传通道但别硬套图片校验。
- ⚠️ **复用资产别顺手删**：`cn.idev.excel`（FastExcel）与 ECharts 封装代码保留不动（未来极可能用上），V0.1 不启用但不删。

### T1 实测回填的坑（2026-10-09，均为真实踩到）

- 🔴 **`.env` 不随迁移走，且会静默覆盖 yml 默认值** —— 最危险的一条。
  `server\.env` 是 `.gitignore` 项，迁移 OSC → PKB 时被排除；于是**写在 `.env.example` 里的新库名根本不生效**，
  应用实际连的是**归档的 `osc` 库**。而 `/health` 会照报 `mysql: UP`，**把真相掩盖掉**。
  - **发现方式**：`/auth/init-status` 返回 `false` 才是对的（`osc.user` 有 93 行，若真连 osc 会返回 `true`）。
  - **规则**：迁移 / 换库 / 换账号后，**必须单独打开 `server\.env` 逐项核对**，不能只看 `application*.yml`；
    `/health` 的 UP **不能证明连对了库**。
- ⚠️ **文件集对比别用 `StartsWith($path)`**：`"$root\.gitignore".StartsWith("$root\.git")` 为真，
  于是 `.gitignore` 被当成 `.git` 的前缀而漏拷。排除判断要用**目录边界**（`StartsWith("$dir\")`）。
- ⚠️ **`npm install` 不会清掉已删依赖的产物**：OSC 的 `dist/` 里残留着旧页面 chunk，构建后仍能看到旧文件名。
  删过依赖或改过 `package.json` 后，**删掉 `dist/` 与 `node_modules` 重建**再判断残留。
- ⚠️ **`MetaObjectHandler` 的填充字段必须与实体字段严格同步**：`strictInsertFill(metaObject, "createdBy", …)`
  在实体没有 `createdBy` 属性时会**抛异常**。删了审计列（`created_by` / `updated_by`）就必须同步删填充逻辑。
- ⚠️ **删了 `ResultCode` 的枚举值会连带打断引用方**：删 `FORBIDDEN` 后 `GlobalExceptionHandler` 编译失败。
  改 `ResultCode` 后必须 `grep` 全项目引用（编译一次是最快的发现手段）。
- ⚠️ **注释里的历史词汇也是残留**：删完代码后，`grep 公告|头像|社团|报名` 仍会命中保留工具类的注释
  （`HtmlSanitizer` / `ImageValidator` / `MinioSupport`）。**注释会误导后来者以为还有那块业务**，一并改掉。

### T2 实测回填的坑（2026-10-15）

- 🔴 **MyBatis-Plus 3.5.7 不允许一个实体有多个 `@TableId`** —— 最响的一条。
  给 `note_tag` 的两个字段都标 `@TableId` 想表达联合主键，结果是
  `MybatisPlusException: @TableId can't more than one in Class`，
  而且**发生在 Mapper 注册阶段 → 整个应用起不来**（不是运行时才炸）。
  - **正解**：MP 没有开箱的复合主键支持，**两个字段都不标 `@TableId`**，
    联合主键约束交给数据库 `PRIMARY KEY (note_id, tag_id)`；重复插入撞主键恰好提供了幂等性。
  - MP 会打一条 WARN：`Not found @TableId annotation, Cannot use Mybatis-Plus 'xxById' Method.`
    —— **这是设计意图不是缺陷**；代价是 `deleteById` / `updateById` 不可用，
    这类表一律用条件 `delete`（`LambdaQueryWrapper`）。
- ⚠️ **MP 的 `delete(wrapper)` 对带 `@TableLogic` 的实体也是「逻辑删除」**，且自动追加 `is_deleted = 0`：
  它**清不掉已经被逻辑删除的行**。测试 / 验证脚本要真正清库必须走**原生 SQL**
  （T2 实测踩到：清理无效，库里留下永久占位行，导致下次运行前置断言失败）。
- ⚠️ **验证脚本别写与「上一步产物」冲突的前置断言**：T2 验证器第 ③ 步为测关联建的笔记尚未删除，
  第 ④ 步却断言「note 表应为空」→ 假失败。**每步自扫门前雪**，不要假设全局空表。
- ✅ **可复用手法：临时 `ApplicationRunner` + 专用 profile 验数据层**
  写一个 `@Component @Profile("t2verify")` 的 `ApplicationRunner`，跑真实库做断言、打印结果、
  自己清理、`System.exit(0)`；跑法 `java -jar … --spring.profiles.active=dev,t2verify`。
  **验完把文件删掉**（不留生产代码）。比写单测快，且验的是真实数据库行为（这次就靠它抓到了多 `@TableId`）。
  - 两个注意：① 控制台中文会乱码 → 加 `-Dfile.encoding=UTF-8`；② 断言失败要打异常栈，别只打一句话。
- ⚠️ **`pkb.user` 里已有真实账号**（用户本人 `18178325352`，BCrypt `$2a$10$`）：
  任何测试都不要清空 / 改动 `user` 表，也不要再调 `/auth/init-admin`（会因已初始化被拒）。

### 账号重置的三个坑（2026-10-15 实测查明，决策见 PRD 附录 C.4 / #10）

> 触发场景：忘记密码要重置、或想「删账号重走引导页」。**三步缺一不可：硬删 user 行 → 清 Redis session → 刷新。**

- 🔴 **坑 1：`user` 实体挂了 `@TableLogic`，删除会变成软删。**
  `entity/User.java` 的 `isDeleted` 上有 `@TableLogic` → `removeById()` / `deleteById()` 执行的是
  `UPDATE user SET is_deleted = 1`，**行还在表里**。所以重置必须用**原生 SQL**：
  ```sql
  DELETE FROM user WHERE id = 1;   -- 原生硬删，不走 MyBatis-Plus
  ```
  - ✅ **一个反直觉的好消息**：引导页的判定条件是 `userMapper.selectCount(null)`，
    而 MP 会**自动追加 `is_deleted = 0`** —— 所以**软删行不会卡住引导页**。
    （「软删会让引导页永远不出现」这个担心在本项目**不成立**；但仍建议硬删，语义更干净。）
  - 判定链：`AuthServiceImpl.initStatus()` → `userMapper.selectCount(null)` → `count > 0` 即 `initialized=true`。
- 🔴 **坑 2：Redis 里的 Sa-Token session 不会跟着 `user` 行走 —— Sa-Token 不查库。**
  删了 user 行，旧 token 在 Redis 里**依然有效** → 浏览器看起来仍是登录态 → **引导页不出现**，流程卡死。
  - **实际 key 前缀（实测，不是推断）**：**`pkb-token:login:token:*`** 与 **`pkb-token:login:session:*`**。
    前缀由 `sa-token.token-name`（= `${SA_TOKEN_NAME:pkb-token}`）决定，
    ⚠️ **不是** `pkb:login:token:*` —— 别被「业务 key 都用 `pkb:`」的直觉带偏。
  - 清理（先 KEYS 看命中，确认后再 DEL）：
    ```powershell
    $cli = "C:\Program Files\Redis\redis-cli.exe"
    & $cli -h 127.0.0.1 -p 6379 KEYS "pkb-token:login:*"
    ```
  - ⚠️ **Redis 里另有大量归档项目遗留的 `osc-token:*` key**（OSC 时期的会话，无害但冗余）。
    清理时**只删 `pkb-token:*`，不要误删 / 误以为它们是 PKB 的**。
- ✅ **坑 3：`user` 表未简化 —— T1 已完成，无需再处理。**
  `user` 已是 **7 列**（`id` / `phone` / `password` / `status` / `created_at` / `updated_at` / `is_deleted`）；
  `constants/roles.js` 整删、`StpInterfaceImpl`（角色推导）已删、强制改密逻辑已删。
  **不要再对 `user` 表做结构变更**（除非有新需求并经用户拍板）。

- ⚠️ **「删除账号 ≠ 删除数据」是本项目的设计事实**：冻结的 4 张业务表**没有任何一张带 `user_id`**
  （也没有指向 `user` 的外键），所以 `DELETE FROM user` 之后笔记 / 标签 / 附件 / 关联**一行都不会动**。
  这是单用户系统的结果 —— **「只有一个人」让「归属」这个概念消失了**。
  → 重置密码**不会丢数据**，可以放心走「删账号重来」。但**验证时仍必须实测核对行数不变**。

### T3 实测回填的坑（2026-10-15）

- ⚠️ **`Start-Process` 起常驻进程会弹出 CMD 窗口且不自动关闭** —— **用户会看到并来问「这是什么」**。
  T3 验证时我用它起了 8090（后端）与 5180（预览站），导致用户屏幕上多了两个空窗口
  （输出被 `-RedirectStandardOutput` 重定向进日志文件，所以窗口是空的、也不会自己退出）。
  - **规则**：AI 自测起常驻进程一律加 **`-WindowStyle Hidden`**（或改用后台任务），**不要让窗口弹到用户屏幕上**。
  - 若已弹出：用完**主动关掉**并告知用户，别让它一直挂着。
- ⚠️ **Redis `TTL` 的回复类型是「整数」（`:604800`），不是「字符串」（`$…`）**：
  用按 `$` 解析的 `redisScalar` 去读 `TTL` 会得 `null`，进而**误报 TTL=0**（T3 第一版就踩了）。
  → 读整数回复用 `/:(-?\d+)/` 提取；写验证脚本时先确认命令的 RESP 回复类型。
- ⚠️ **PKB 登录页的输入框要按 `placeholder` 定位，不能按 `type` 过滤**：
  探针实测顺序是 `[手机号(text), 密码(password), 验证码(text)]` ——
  按 `type !== 'password'` 过滤会**同时命中手机号与验证码两个 text 框**，把手机号当成验证码填（T3 实测踩到）。
- ⚠️ **验证码答案必须「先清空 `pkb:captcha:*` 再让页面取一张」**：
  否则 Redis 里可能同时存在多张（T3 实测命中 9 个），**分不清哪把钥匙属于当前页面** → 取错答案、登录必失败。
- ⚠️ **没有受保护接口的页面，验不了「token 失效自动跳登录」**：
  T3 阶段首页/笔记/项目/标签全是占位页，只调免登录的 `/auth/init-status`，
  所以后端的 `40100` 在前端**无处触发**。**路由守卫只判「本地 token 是否存在」、不打后端**（正确设计）。
  → 该端到端验证**已登记推迟到 T5/T6**（详见《清单》§4 T3 实现结果 ⑤）。

---

## 五、全局铁律

1. **单功能节奏**：一个任务点 = 一次交付。完成即停，等用户审阅确认后再进下一个，不越点开发。
2. **先方案后代码**：涉及技术选型、表结构变更、公共抽象的任务点，先出技术方案经用户确认再动手。
3. **文档策略（单一出处）**：
   - 不新增「按任务点命名」的文档；任务点方案/实现/验证写回《开发任务点清单》对应条目，决策追加 §6。
   - 长期文档固定：PRD V0.1（需求依据）+ 任务点清单（台账与决策）+ 本交接文档（开工与铁律）。
   - **单一出处**：同一事实只写一处，别处只放指针；复制出来的那份一定会漂移。
   - 允许例外：内容「不随任务点推进变化」且「会被反复查阅」时才新增长期文档，并在「七、关键词速查」注册。
4. **实测优于推断**：交付必须附真实跑过的验证记录，不能写"应该可以"。
5. **有疑问先问**：PRD 未覆盖或与实现冲突时，列选项让用户拍板。
6. **参考代码只读**：OSC 仓库仅作参考读取，新代码只写在 PKB 工作区；**不得改动 OSC 原仓库**。
7. **单账号口径（替代 OSC 的权限矩阵）**：PKB 无多角色，`constants/roles.js` 整删；登录守卫只判「是否已登录 / 是否已初始化」，不做角色/能力判定。
8. **`content_text` 与格式铁律（数据层）**：保存笔记必须同步 `content_text`；格式新建选定后不可切换；搜索只打 `title + content_text`，永不打 `content`。
9. **强制改密删除**：单账号引导创建不触发强制改密（无「密码非本人选」场景）；图形验证码保留。
10. **改文档前先定位原文**：改 PRD/清单前先 `grep -n` 读出原文再动手，别凭记忆写 `old_string`（半行锚点会吃掉上一条记录，见 OSC 教训）。
11. **动结构前先做影响面排查**：改外壳滚动模型 / 公共样式 / 路由守卫前，先搜谁会受影响，改完再修更贵。
12. **事实现查不凭记忆**：提交哈希、菜单项数、表行数、端口号一律现查（`git log` / 验证输出 / `SELECT`），不凭记忆写。
13. **改标记 / 枚举 / 公共列前先全项目 grep**：删 `ResultCode` 的枚举值、改实体字段、删工具类，都会连带打断引用方；
    编译一次或 `grep` 一遍是最便宜的发现手段（T1 实测：删 `FORBIDDEN` 直接导致编译失败）。
14. **`.env` 与 `yml` 是两层配置，核对要核到运行时**：`.env` 覆盖 `yml` 默认值，改库名 / 账号 / Header 时
    **两处都要动**，并以运行时行为（如 `/auth/init-status`）作为最终判据，不以配置文本为判据。

---

## 六、协作规则

- **逐点确认**：任务点做完自测（接口 + 页面 + 实测记录）→ 交付附「验证指引」→ 停下等用户审阅。
- **绝不主动提交**：只有用户明确说「**提交 Tx**」才 `git add` + `commit` + `push`，且按功能点拆多个 commit、全英文 title + 中文 body、只 add 明确路径。
- **交付必带验证指引**：命令用 PowerShell 5.1 友好写法（优先原生 `Invoke-RestMethod`；`curl.exe` 只给单行；不要 `curl -H/-d`）。
- **文档实时更新**：每完成一个任务点，同步更新《任务点清单》§3 总表状态 + 该条目实现结果；决策追加 §6。
- 用户是「先审阅 → 再说继续/提交」节奏；禁止越点开发（除非明确说"继续全部"）。

---

## 七、关键词速查

| 关键词                 | 落点                                                         |
| :--------------------- | :----------------------------------------------------------- |
| 权威需求依据           | `docs/个人知识库 PKB · 产品需求文档 V0.1.md`（P-001~P-011 功能卡片） |
| 开发台账 / 进度 / 决策 | `docs/个人知识库 PKB · 开发任务点清单.md`（§3 总表 / §4 任务点详情 / §6 决策，按 D 编号索引） |
| 项目记忆               | `.workbuddy/memory/MEMORY.md` + 最新日志                     |
| 脚手架来源（只读）     | OSC 仓库 `F:\Project\Own Project\New-Management-System`（仅参考，不得改） |
| 后端工程               | `server/`（`com.pkb`，主类 `PkbServerApplication`；应用 `pkb-server`） |
| 前端工程               | `web/`（外壳 `layouts/AppShell.vue` 三档；`PublicLayout` 仅登录/引导用） |
| 数据库                 | MySQL 库 `pkb`（新建，不复用 osc）；应用账号 `pkb_app`（无 DDL 权限，建表用 root） |
| 建表 / 种子脚本        | `server/sql/00~0x`（`01_schema.sql` 无 DROP、幂等）          |
| 数据模型（已冻结）     | 见「一、项目基本盘」速览表 + PRD V0.1；4 表 `note/tag/note_tag/attachment`，3 铁律 |
| 鉴权                   | Sa-Token Header **`pkb-token`**（已定，2026-10-09）；Redis 前缀 `pkb:captcha:*` / `pkb:login:fail:*`；**Sa-Token 自身 key 前缀是 `pkb-token:login:*`**（由 token-name 决定，勿混淆）；前端 localStorage `pkb_token` / `pkb_profile`；登录守卫只判登录态 + 初始化态 |
| token 策略             | **有效期 7 天**（`sa-token.timeout: 604800`）+ **禁止多端登录**（`sa-token.is-concurrent: false`，新登录踢旧）——2026-10-15 拍板（PRD #16），T3 实施 |
| 忘记密码               | **文档化步骤**（PRD #10 / 附录 C.4）：硬删 user 行 → 清 Redis `pkb-token:login:*` → 重走引导页；步骤见 `README.md` 八；演练已通过（2026-10-15） |
| 单账号                 | 引导页首启建 1 账号；`constants/roles.js` 整删；强制改密删除；图形验证码保留 |
| content_text           | 搜索专用列；md 存原文/轻量去标记，html 由 jsoup 剥标签；搜索只打 `title + content_text` |
| 双格式                 | `note.content_format` 0=md/1=html，新建选定不可切换，默认 md；编辑器 T7 选型待定（倾向 md-editor-v3） |
| 项目 = tag.type=1      | 表多对多、UI 项目单选 + 标签多选；后端关联写入兜底只留一个项目 |
| 首页工作台             | 桌面三列（项目/最近/待整理）各自滚动 + 状态条（待整理/已归档/项目/本周+）；手机底部 3 Tab；顶部输入框回车即建 inbox |
| 复用资产               | 统一返回体+全局异常+错误码、Sa-Token 骨架、MinIO 上传+MinioSupport+ImageValidator、富文本+jsoup/DOMPurify 双清洗、Sass 变量体系+三层配色、MyBatis-Plus+@TableLogic、分页/下载/日期 utils |
| 改造复用               | 三档外壳（菜单砍 3~4 项、底部 Tab 改最近/待整理/项目）、路由表+守卫（只留登录守卫）、引导页（首启建单账号） |
| 保留不启用             | FastExcel（`cn.idev.excel`）+ ECharts 封装：代码保留不动，未来可能用上（勿删） |
| 直接删除（OSC 业务）   | 报名/审核/查询/短信/成员档案/反馈/公告/导入导出页面/看板页面/字典业务内容/`constants/roles.js`/飞书相关 |
| 外壳滚动模型           | `.app-shell` = `height:100% + overflow:hidden`；`.app-body` 与 `.app-main` 的 `min-height:0` 不能删；首页三列逐层加 |
| 富文本 XSS 清洗        | 后端 `HtmlSanitizer`（jsoup）+ 前端 `sanitizeHtml.js`（DOMPurify）；md 渲染产物同样要过 DOMPurify |
| 视觉 token             | `styles/index.scss`（主色 `--brand-primary` + EP 派生色一起改）；图表色值单一出处 `constants/palette.js` |
| 附件                   | MinIO bucket `pkb`；库里只存对象 key，读时拼公开前缀；通用文件校验（非图片也要支持） |

---

## 八、启动清单与常用命令

### 8.1 项目启动清单（按顺序；✅ 常驻 / 🔧 手动起）

| #    | 组件                         | 常驻                                | 启动                        | 探活                                                         |
| :--- | :--------------------------- | :---------------------------------- | :-------------------------- | :----------------------------------------------------------- |
| 1    | MySQL 8（库 `pkb`，:3306）   | ✅ Windows 服务 `MySQL80` AUTO_START | （未起）`net start MySQL80` | `mysql.exe --user=root --password=root --skip-column-names "--execute=SELECT 'UP';"` → `UP` |
| 2    | Redis（:6379）               | ✅ Windows 服务 `Redis` AUTO_START   | （未起）`net start Redis`   | `redis-cli.exe -h 127.0.0.1 -p 6379 PING` → `PONG`           |
| 3    | MinIO（:9000，bucket `pkb`） | 🔧 手动                              | 见 8.2                      | `curl.exe -s -o NUL -w "%{http_code}" --noproxy 127.0.0.1 http://127.0.0.1:9000/minio/health/live` → `200` |
| 4    | 后端（dev :8080）            | 🔧 手动                              | 见 8.3                      | `Invoke-RestMethod http://127.0.0.1:8080/health` → `code:200` |
| 5    | 前端（dev :5173）            | 🔧 手动 `npm run dev`                | 见 8.4                      | 浏览器开 http://127.0.0.1:5173                               |

> ⚠️ 分工约定：常驻服务由用户在会话外起（IDEA / 独立窗口 / Windows 服务），不要交给 AI 会话内后台任务（避免唤醒旧会话）。AI 只用隔离栈（后端 8090 + 预览站 5180）且当轮收掉。

### 8.2 MinIO

```powershell
& "E:\Minio\minio\minio.exe" server E:\Minio\pkb-data --address :9000 --console-address :9001
```
- 凭据 `minioadmin/minioadmin`；桶 `pkb` 由应用启动时自动创建并设公开读。
- 别用 `| head` 起；重定向到日志文件再 `run_in_background`。

### 8.3 后端（在 `server/` 目录下执行）

```powershell
Remove-Item env:SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item env:SERVER__HOST -ErrorAction SilentlyContinue
& "E:\MAVEN\apache-maven-3.6.3\bin\mvn.cmd" package -DskipTests
& "C:\Program Files\Java\jdk-17\bin\java.exe" -jar "target\pkb-server-1.0.0.jar"   # dev 8080
```
> AI 自测用 `--server.port=8090` 起隔离栈，不碰 8080。

### 8.4 前端（在 `web/` 目录下执行）

```powershell
npm run dev      # http://127.0.0.1:5173（/api 代理到 8080 并剥前缀）
npm run lint
npm run build
```

### 8.5 探活一条龙

```powershell
curl.exe -s -o NUL -w "minio 9000: %{http_code}`n" --noproxy 127.0.0.1 http://127.0.0.1:9000/minio/health/live
& "C:\Program Files\Redis\redis-cli.exe" -h 127.0.0.1 -p 6379 PING
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=root --skip-column-names "--execute=SELECT 'mysql UP';"
curl.exe -s -o NUL -w "backend 8080: %{http_code}`n" --noproxy 127.0.0.1 http://127.0.0.1:8080/health
```

### 8.6 数据库（root 建表 / 查数据）

```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
& $mysql --user=root --password=root --default-character-set=utf8mb4 --table `
  "--execute=USE pkb; SHOW TABLES;"
```

> 注：本文件**不含「上线部署」章节** —— PKB 为单用户本机/局域网自用，V0.1 不做公网部署、不做多实例；若未来需部署，另立文档。

---

## 九、交接约定（AI 收到「我要开启新对话」时的标准动作）

> 用户换会话继续时，只会说「我要开启新对话」之类。此时 AI 应：

1. **先把本文件更新到最新**（不能省）：
   - 「三、当前阶段与下一步」→ 改写为下一步要做什么（指向任务点 + 环境准备 + 不要做的坑）；
   - 「四、环境与坑」→ 补本次新踩到的坑（尤其 PKB 新坑小节）；
   - 「五、全局铁律」→ 补本次新学铁律；
   - 「七、关键词速查」→ 补新增/变更落点；
   - 顺手核对《任务点清单》§3 总表状态。
2. **在对话里直接输出**下面这段（与本文件顶部逐字一致，整段可复制）：

```text
你好 DS。开始「PKB 个人知识库（Personal Knowledge Base）」V0.1 开发 —— 新对话接续，麻烦先重建上下文。

第一步：完整读取 F:\Project\Own Project\Personal-Knowledge-Base\docs\个人知识库 PKB · 新对话交接提示词（通用版）.md ，
并严格按其中「二、动手前必读（按顺序）」把项目上下文建立起来。

第二步：按该文档「三、当前阶段与下一步」确定本轮要做什么；若那一节指向某个任务点，
就去《开发任务点清单》读该任务点的条目（技术方案 / 实现结果 / 决策 D 编号）。

第三步：先输出你的理解（3~5 条：当前进度、本轮任务、关键红线），再给出实施计划（改动文件、
影响面、怎么自测、怎么验证），等我确认后再动手。

务必遵守该文档的「五、全局铁律」与「六、协作规则」——尤其是：单功能节奏、先方案后代码、
不主动 git 提交（我说「提交Tx」才提交）、交付必须附 PowerShell 5.1 友好的验证指引。

⚠️ 提醒：PKB 是从 0 到 1 的新项目，不是 OSC 的延续；OSC 只作脚手架参考，开发期间不得改动 OSC 原仓库。
```

3. 一句话告诉用户：整段复制即可，本轮任务已在文档「三」写明、无需额外说明。
4. 若本次产出新任务点成果/决策，同时写进「三、当前阶段与下一步」，保证新会话只读这一份 + 按需查清单就能开工。

---

文档已完整输出。落盘后建议放在 `Personal-Knowledge-Base/docs/个人知识库 PKB · 新对话交接提示词（通用版）.md`，与 PRD V0.1 同目录。等目录改名完成、T1 启动前，按文档「二、动手前必读」顺序把上下文建起来即可。需要我顺手把《开发任务点清单》的空模板也产出的话，说一声。