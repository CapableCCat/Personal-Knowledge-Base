你好。我要告诉你一件刚发生的事，请你先完整读完、把我们的设想吃透，最后再动手。

【发生了什么】

OSC 社团管理系统（天津中德开源鸿蒙社）已于 2026-09-30 归档。我基于它启动了一个新项目：

- 新项目：PKB（Personal Knowledge Base，个人知识库）
- 定位：自用的「第二大脑」，单用户，本机 / 局域网部署
- 新工作空间：F:\Project\Own Project\Personal-Knowledge-Base
- 起点策略：复制 OSC 仓库 → 删业务 → 改名 → 作为 PKB 起点
  （复用 OSC 的工程结构 / 统一返回体 / 全局异常 / 三档响应式外壳 / 富文本+XSS 链路 /
    MinIO 上传 / Sass 变量体系 / MyBatis-Plus 与 @TableLogic 约定；OSC 的业务代码全删）
- ⚠️ 原 OSC 仓库只读参考，PKB 开发期间不得改动它
- 文档已落盘：docs/个人知识库 PKB · 产品需求文档 V0.1.md（我下面复述的就是它的要点）

【PKB 的设想（这是我们讨论后的最终结论，请照单全收）】

■ 定位与边界

ABC 合并：A 快速记录 + B 项目档案 + C AI 检索，是同一件事的三段，共用一份数据。
核心闭环：写（不管分类）→ 进「待整理」inbox → 清理归类 → 进正式库 archived。
V0.1 只解决「存得进、找得到」；RAG / 向量检索 / AI 问答放 V0.3，等数据积累起来再加。
V0.1 明确不做：多用户 / 注册 / 分享 / 协作、版本历史、回收站 UI、多角色权限矩阵、
导入导出界面、看板、公告、反馈、报名审核、短信、成员档案、飞书集成、上线部署。

■ 数据模型（已冻结，4 张表）

note        id, title, content, content_format(0=md/1=html), content_text,
            status(0=inbox/1=archived), pinned, created_at, updated_at, is_deleted
tag         id, name, type(0=标签/1=项目), color, sort, created_at, is_deleted
note_tag    note_id, tag_id  (联合主键)
attachment  id, note_id, file_key, file_name, size, mime, created_at, is_deleted

三条铁律：
1. 一条笔记一种格式（md 或 html），新建时选定、之后不切换，默认 md。
   —— 讨论时对比过「双列镜像存 md+html」（否决，双份存储双份清洗易漂）
      和「只存 md、富文本实时转」（否决，转换有损，表格/嵌套列表会丢）。
2. 项目 = tag.type=1；表结构允许多对多，UI 层约束为单个（详情页项目是单选下拉，标签多选）。
   —— 讨论时对比过「项目独立建表」（否决，V0.1 用不上项目元信息，易过度设计）
      和「note.parent_id 自关联」（否决，项目既是笔记又是容器，语义混乱）。
3. content_text 是搜索专用列：md 存原文，html 由后端剥标签得到。
   搜索只打 title + content_text，永不打 content（否则会命中 html 标签）。

排序口径：ORDER BY pinned DESC, updated_at DESC。pinned 是我明确要求保留的。
搜索范围：V0.1 只搜标题 + 正文；搜标签名留给后续版本。

■ 首页工作台（形态已定）

桌面（≥1024，复用 AppShell 的 desktop 档）：
  顶部输入框 + 格式切换按钮 + 内联搜索框
  状态条：待整理 12 · 已归档 348 · 项目 7 · 本周 +9（可点，跳对应列表）
  三列：项目 / 最近 / 待整理（三列各自滚动）

手机（<768）：
  顶部输入框常驻 + 搜索图标
  精简状态条（横滑）
  底部 3 Tab：最近 / 待整理 / 项目（切 Tab 只换内容区）

四条硬性交互约定：
1. 顶部输入框回车即建 inbox，不跳页，toast 提示；格式由右侧按钮切换（默认 md）。
2. 打开笔记：桌面 el-drawer（~720px），手机独立全屏路由页 /note/:id；
   两处复用同一份表单组件。
3. 三列各自滚动：外壳 height:100% + overflow:hidden；
   .app-body 与 .app-main 的 min-height:0 不能删（OSC 踩过，删了内部滚动失效）。
4. 首页只承载「看一眼」，全量列表在各自列表页。

■ 功能范围（V0.1 共 11 条 Must）

P-001 快速记录  P-002 笔记列表  P-003 笔记详情/编辑  P-004 双格式编辑
P-005 待整理闭环  P-006 项目管理  P-007 标签管理  P-008 搜索
P-009 附件  P-010 首页工作台  P-011 单账号登录

单账号登录：复用 Sa-Token，首启引导创建 1 个账号；强制改密逻辑删掉；图形验证码保留。
constants/roles.js 那套能力函数整删。

■ 复用 / 改造 / 删除

直接复用：统一返回体+全局异常+错误码、Sa-Token 骨架、MinIO 上传+MinioSupport+ImageValidator、
富文本编辑器+jsoup/DOMPurify 双清洗、Sass 变量体系+三层配色、MyBatis-Plus+@TableLogic 约定、
分页/下载/日期 utils。

改造后复用：三档响应式外壳（菜单砍到 3~4 项，底部 Tab 改为最近/待整理/项目）、
路由表+守卫（权限判定整删，只留登录守卫）、引导页（改为首启建单账号）。

直接删除：报名 / 审核 / 查询 / 短信 / 成员档案 / 反馈 / 公告 / 导入导出页面 / 看板页面 /
字典业务内容 / constants/roles.js / 飞书相关。

■ 保留但 V0.1 不启用的资产

Excel 导入导出（FastExcel cn.idev.excel）与 ECharts 封装：代码保留不动，未来极可能用上。
这是我明确拍板的，不要顺手删。

■ 实施顺序（草案，正式编号待定）

T1 脚手架落地（复制 OSC → 删业务 → 改名 com.pkb → 跑通空壳）
T2 数据层（4 张表 + 实体 + Mapper）
T3 单账号登录（引导页 + Sa-Token）
T4 外壳改造（菜单砍项 + 底部 Tab 改造 + 登录守卫）
T5 笔记 CRUD（后端，含 content_text 同步 / 归档 / 置顶）
T6 笔记列表+详情（前端，drawer / 全屏页 + 表单组件）
T7 双格式编辑（md 编辑器 + 富文本复用 + 后端分流清洗）
T8 项目/标签（CRUD + 关联 + 项目卡片计数）
T9 首页工作台
T10 搜索
T11 附件
T12 联调打磨

■ 待定项（尚未拍板）

- Markdown 编辑器选型：候选 md-editor-v3 / Vditor / CodeMirror+marked；
  倾向 md-editor-v3（Vue 3 生态最顺），开工前定。
- 任务点正式编号：由《开发任务点清单》维护。

【现在请你做的事】

你手上有 OSC 的完整项目上下文（.workbuddy/memory/、docs/ 下的 PRD / 开发任务点清单 /
新对话交接提示词 / 战略说明）。

请结合「OSC 项目的现状」和「上面 PKB 的全部设想」，产出一份《PKB 新对话交接提示词（通用版）》，
供我以后开新对话延续 PKB 开发用。

要求：
- 直接输出完整文档内容，我复制落盘，不要反问、不要只给目录
- 结构参考 OSC 那份交接文档（你手上就有），至少包含：
  开场提示词 / 项目基本盘 / 动手前必读 / 当前阶段与下一步 / 环境与坑 / 全局铁律 /
  协作规则 / 关键词速查 / 启动清单与常用命令 / 交接约定
- 环境与坑一节：把 OSC 已验证的坑搬过去（宿主注入 SERVER__PORT、PowerShell 5.1 纯 ASCII、
  Chrome 无头 CDP、el-drawer v-show 判 offsetParent、flex min-height:0、MinIO 显式凭据、
  git commit 与 push 分两条命令 等），写「沿用 OSC 经验」即可，不必重复解释；
  如你认为 PKB 会有新坑，另起一小节补充
- 全局铁律沿用 OSC 的：单功能节奏、先方案后代码、单一出处、实测优于推断、不主动 git 提交
- 不要写「上线部署」相关章节（PKB 自用，暂不需要）
- 明确标注：V0.1 是新项目从 0 到 1 的第一步，不是 OSC 的延续，OSC 只作脚手架参考
- 单一出处原则：同一事实只写一处，别处只放指针