package com.pkb.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pkb.common.ContentTextBuilder;
import com.pkb.common.constant.ContentFormat;
import com.pkb.common.constant.NoteStatus;
import com.pkb.common.constant.TagType;
import com.pkb.common.exception.BusinessException;
import com.pkb.common.result.ResultCode;
import com.pkb.dto.NoteAssociationsRequest;
import com.pkb.dto.NoteCreateRequest;
import com.pkb.dto.NoteListItemVO;
import com.pkb.dto.NoteQuery;
import com.pkb.dto.NoteStatsVO;
import com.pkb.dto.NoteUpdateRequest;
import com.pkb.dto.NoteVO;
import com.pkb.dto.PageResult;
import com.pkb.dto.TagBriefVO;
import com.pkb.entity.Note;
import com.pkb.entity.NoteTag;
import com.pkb.entity.Tag;
import com.pkb.mapper.NoteMapper;
import com.pkb.mapper.NoteTagMapper;
import com.pkb.mapper.TagMapper;
import com.pkb.service.NoteService;
import com.pkb.util.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 笔记 CRUD 实现。
 *
 * <p><b>三条铁律的落地位置（本类是最关键的一处）</b>：
 * <ol>
 *   <li>① **格式不可切换**：{@link #update} 里显式拒绝与现值不同的 {@code contentFormat}</li>
 *   <li>② **项目 = tag.type=1**：{@link #applyAssociations} 用「全量替换」实现 ——
 *       先清空该笔记的全部关联再按请求重建，**结构上不存在「第二个项目」的写入路径**
 *       （UI 单选挡不住直接调接口，这里是最后一道兜底，PRD P-006 的 R4）</li>
 *   <li>③ **`content_text` 同步**：<b>只经本类私有的 {@link #applyContentText} 计算</b>，
 *       新增与更新都必须调用它。禁止任何绕过本类的写入路径（漏同步 = 搜索漏数据且不报错）</li>
 * </ol>
 *
 * <p>⚠️ <b>`updated_at` 的坑</b>：归档 / 取回 / 置顶走 {@code update(null, wrapper)}，
 * 此时 MyBatis-Plus 的 {@code MetaObjectHandler} **不会**自动填充（它只在有实体入参时生效），
 * 所以那些地方必须**显式 set updated_at**，否则「最近」列的排序会失真。
 *
 * <p>⚠️ <b>关联读写单独走 `note_tag`</b>：`note_tag` 没有 {@code is_deleted} 列（清单 D28），
 * 一律物理增删；标签被逻辑删除后，`selectBatchIds` 会自动过滤掉它，
 * 于是「删除标签 → 笔记上不再显示该标签」天然成立。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    /** `note.title` 列宽（VARCHAR(255)） */
    private static final int TITLE_MAX = 255;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int PINNED_ON = 1;
    private static final int PINNED_OFF = 0;

    private final NoteMapper noteMapper;
    private final ContentTextBuilder contentTextBuilder;
    private final HtmlSanitizer htmlSanitizer;
    private final TagMapper tagMapper;
    private final NoteTagMapper noteTagMapper;

    // ------------------------------------------------------------
    // 写
    // ------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteVO create(NoteCreateRequest request) {
        int format = resolveFormat(request.contentFormat());

        String title = normalizeTitle(request.title());
        // ---- 铁律①'：html 入库前过 jsoup 白名单（PRD §6.4）----
        // md 是 Markdown 源码，**不能清洗**（洗了会破坏语法），它的清洗发生在「渲染产物」上（前端 DOMPurify）。
        String content = sanitizeForStore(request.content(), format);

        // 标题与正文不能同时为空（P-001：空输入回车不创建）
        // ⚠️ 「正文为空」要按格式判定：html 只贴一张图（没有文字）也是合法内容
        if (!StringUtils.hasText(title) && !hasVisibleContent(content, format)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请填写标题或正文");
        }

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setContentFormat(format);
        note.setStatus(NoteStatus.INBOX.getCode());
        note.setPinned(PINNED_OFF);
        applyContentText(note);

        noteMapper.insert(note);

        // 项目 / 标签：与笔记同一事务（PRD P-003「保存」是一次动作，不能半边成功）
        applyAssociations(note.getId(), request.associations());

        log.info("笔记已创建：id={}, format={}, textLen={}", note.getId(), format,
                note.getContentText() == null ? 0 : note.getContentText().length());
        return toVO(noteMapper.selectById(note.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteVO update(Long id, NoteUpdateRequest request) {
        Note note = requireNote(id);

        // ---- 铁律①：格式创建后不可切换（显式拒绝，不静默忽略）----
        if (request.contentFormat() != null
                && !Objects.equals(request.contentFormat(), note.getContentFormat())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "笔记格式创建后不可修改");
        }

        Note patch = new Note();
        patch.setId(note.getId());

        if (request.title() != null) {
            patch.setTitle(normalizeTitle(request.title()));
        }
        // html 的新正文同样先过 jsoup，再参与 content_text 计算
        String sanitizedContent = null;
        if (request.content() != null) {
            sanitizedContent = sanitizeForStore(request.content(), note.getContentFormat());
            patch.setContent(sanitizedContent);
        }
        if (request.pinned() != null) {
            patch.setPinned(requirePinned(request.pinned()));
        }

        String finalTitle = request.title() != null ? patch.getTitle() : note.getTitle();
        String finalContent = request.content() != null ? sanitizedContent : note.getContent();
        if (!StringUtils.hasText(finalTitle) && !hasVisibleContent(finalContent, note.getContentFormat())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请填写标题或正文");
        }

        // ---- 铁律③：每次都重算 content_text，但**必须用「最终生效的内容」算** ----
        // ⚠️ R1 的真实踩坑点（2026-10-17 实测）：早期版本用 patch.content 去算，
        //    当请求只改 title / pinned、不带 content 时 patch.content 为 null
        //    → build(null) 返回 ""，把库里已有的 content_text **覆盖成空**（搜索静默漏数据）。
        //    正确做法：没传 content 就用库里的旧值 finalContent。
        // ⚠️ T7 补充：html 的 finalContent 必须是**清洗后**的值 —— 顺序反了，
        //    搜索列会把 `<script>` 之类的文本也吃进去（清洗后再算才是「最终存下来的内容」）。
        patch.setContentText(contentTextBuilder.build(finalContent, note.getContentFormat()));

        // updateById 会忽略 null 字段，并触发 MetaObjectHandler 填充 updated_at
        noteMapper.updateById(patch);

        // associations 为 null = 完全不动关联（老调用方不受影响）；非 null = 全量替换
        applyAssociations(note.getId(), request.associations());

        log.info("笔记已更新：id={}", id);
        return toVO(noteMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteVO archive(Long id) {
        return changeStatus(id, NoteStatus.ARCHIVED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteVO unarchive(Long id) {
        return changeStatus(id, NoteStatus.INBOX);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteVO pin(Long id, Integer pinned) {
        Note note = requireNote(id);
        int target = requirePinned(pinned);
        if (Objects.equals(note.getPinned(), target)) {
            // 幂等：状态没变就不写库，避免 updated_at 无意义地跳动（会打乱「最近」列排序）
            return toVO(note);
        }
        noteMapper.update(null, Wrappers.<Note>lambdaUpdate()
                .eq(Note::getId, id)
                .set(Note::getPinned, target)
                /**
                 * ⚠️ **显式把 updated_at 写回原值**，不是写 now，也不是不写：
                 * 表上的 `updated_at` 带 `ON UPDATE CURRENT_TIMESTAMP`，
                 * 只要这一行被 UPDATE 且**没有显式赋值**，MySQL 就会自动把它刷成当前时间；
                 * 而「置顶」不是编辑，把 updated_at 刷掉会让它跑到「最近」列最前面（T6 实测踩到）。
                 */
                .set(Note::getUpdatedAt, note.getUpdatedAt()));
        log.info("笔记置顶状态变更：id={}, pinned={}", id, target);
        return toVO(noteMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireNote(id);
        // @TableLogic → UPDATE note SET is_deleted=1（行仍在库里，PRD P-002 明确 V0.1 无回收站）
        noteMapper.deleteById(id);
        // 关联行一并物理删除：否则「已删笔记的标签计数」会虚高（计数 join note 已过滤，双保险）
        noteTagMapper.delete(Wrappers.<NoteTag>lambdaQuery().eq(NoteTag::getNoteId, id));
        log.info("笔记已逻辑删除：id={}", id);
    }

    // ------------------------------------------------------------
    // 读
    // ------------------------------------------------------------

    @Override
    public NoteVO detail(Long id) {
        return toVO(requireNote(id));
    }

    @Override
    public NoteStatsVO stats() {
        // ⚠️ 四项都依赖 MP 的 @TableLogic 自动追加 is_deleted=0，不要手写 where。
        long inbox = noteMapper.selectCount(Wrappers.<Note>lambdaQuery()
                .eq(Note::getStatus, NoteStatus.INBOX.getCode()));
        long archived = noteMapper.selectCount(Wrappers.<Note>lambdaQuery()
                .eq(Note::getStatus, NoteStatus.ARCHIVED.getCode()));
        long projects = tagMapper.selectCount(Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getType, TagType.PROJECT.getCode()));
        // 「本周」= 本周一 00:00 起算（PRD #11 拍板：周一起算、算本周新建笔记数，不区分状态）
        long weekAdded = noteMapper.selectCount(Wrappers.<Note>lambdaQuery()
                .ge(Note::getCreatedAt, weekStart()));
        return new NoteStatsVO(inbox, archived, projects, weekAdded);
    }

    /** 本周起点（周一 00:00，服务器本地时区）—— `with(MONDAY)` 在 ISO 周里就是本周一 */
    private java.time.LocalDateTime weekStart() {
        return LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
    }

    @Override
    public PageResult<NoteListItemVO> page(NoteQuery query) {
        int pageNo = resolvePage(query == null ? null : query.page());
        int pageSize = resolveSize(query == null ? null : query.size());
        Integer status = resolveStatusFilter(query == null ? null : query.status());

        var wrapper = Wrappers.<Note>lambdaQuery();
        // 列表不传正文：排除 content（LONGTEXT），只留标题/搜索列/元信息
        wrapper.select(Note.class, info -> !"content".equals(info.getColumn()));
        if (status != null) {
            wrapper.eq(Note::getStatus, status);
        }
        // 「只看本周新建」筛选（首页状态条「本周 +N」点进来）
        if (query != null && Boolean.TRUE.equals(query.weekOnly())) {
            wrapper.ge(Note::getCreatedAt, weekStart());
        }

        // ---- 项目 / 标签筛选（T8，PRD P-002）----
        // 先由 note_tag 算出「候选笔记 id 集合」，再用 IN 收窄 —— 不写 join，避免与分页 /
        // 逻辑删除 / 排序兜底混在一起（MP 的 selectPage 对自定义 join 支持很差）。
        Set<Long> candidateIds = resolveFilterNoteIds(
                query == null ? null : query.projectId(),
                query == null ? null : query.tagIds());
        if (candidateIds != null) {
            if (candidateIds.isEmpty()) {
                return PageResult.of(0, pageNo, pageSize, List.of());
            }
            wrapper.in(Note::getId, candidateIds);
        }

        // 排序口径（PRD 冻结）：pinned DESC, updated_at DESC
        // ⚠️ 末尾追加 **id DESC 兜底**：`updated_at` 是 DATETIME（秒级精度），
        //    同一秒内建的笔记时间会完全相同 → 不加兜底键时 MySQL 的返回顺序**不确定**，
        //    列表可能在两次请求间自行跳动（T6 实测踩到）。
        wrapper.orderByDesc(Note::getPinned)
                .orderByDesc(Note::getUpdatedAt)
                .orderByDesc(Note::getId);

        Page<Note> result = noteMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<Note> notes = result.getRecords();

        Map<Long, Associations> assocMap = loadAssociations(notes.stream().map(Note::getId).toList());
        List<NoteListItemVO> records = notes.stream()
                .map(note -> {
                    Associations assoc = assocMap.get(note.getId());
                    return NoteListItemVO.from(note,
                            assoc == null ? null : assoc.project,
                            assoc == null ? List.of() : assoc.tags);
                })
                .toList();
        return PageResult.of(result.getTotal(), pageNo, pageSize, records);
    }

    // ------------------------------------------------------------
    // 关联（项目 / 标签）
    // ------------------------------------------------------------

    /**
     * **全量替换**笔记的项目与标签（T8 的兜底核心）。
     *
     * <p>为什么是「先删光、再按请求重建」：
     * <ul>
     *   <li>项目单选是**铁律②**，但表结构允许多对多 —— 只有全量替换能让
     *       「一条笔记最多一个项目」在**写入路径上**成立，而不是靠调用方自觉；</li>
     *   <li>标签本身就是全量集合，前端一次提交当前状态，语义最直白；</li>
     *   <li>整个过程在 {@code @Transactional} 里，删除与重建不会出现中间态。</li>
     * </ul>
     *
     * @param assoc null = 不动关联；非 null = 以它为准（projectId=null 表示不挂项目）
     */
    private void applyAssociations(Long noteId, NoteAssociationsRequest assoc) {
        if (assoc == null) {
            return;
        }
        Tag project = assoc.projectId() == null
                ? null
                : requireTagOfType(assoc.projectId(), TagType.PROJECT);
        List<Tag> tags = resolveTags(assoc.tagIds());

        // 先清空该笔记的全部关联（note_tag 无 is_deleted，物理删除）
        noteTagMapper.delete(Wrappers.<NoteTag>lambdaQuery().eq(NoteTag::getNoteId, noteId));

        if (project != null) {
            insertLink(noteId, project.getId());
        }
        for (Tag tag : tags) {
            insertLink(noteId, tag.getId());
        }
    }

    /**
     * 把项目 / 标签筛选解析成「候选笔记 id 集合」。
     *
     * @return null = 没有筛选条件（不限制）；空集合 = 条件无命中（调用方直接返回空页）
     */
    private Set<Long> resolveFilterNoteIds(Long projectId, List<Long> tagIds) {
        Set<Long> candidate = null;

        if (projectId != null) {
            Tag project = requireTagOfType(projectId, TagType.PROJECT);
            candidate = new HashSet<>(
                    noteTagMapper.findNoteIdsHavingAllTags(List.of(project.getId()), 1));
        }

        List<Tag> tags = resolveTags(tagIds);
        if (!tags.isEmpty()) {
            List<Long> ids = tags.stream().map(Tag::getId).toList();
            Set<Long> byTags = new HashSet<>(noteTagMapper.findNoteIdsHavingAllTags(ids, ids.size()));
            if (candidate == null) {
                candidate = byTags;
            } else {
                candidate.retainAll(byTags);
            }
        }
        return candidate;
    }

    /** 校验并取出标签列表：必须存在、且全是 `type=0` 的标签（项目不能当标签用） */
    private List<Tag> resolveTags(List<Long> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<Long> ids = raw.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Tag> found = tagMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> tag));
        List<Tag> tags = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Tag tag = found.get(id);
            if (tag == null || !Objects.equals(tag.getType(), TagType.TAG.getCode())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "标签不存在或类型不正确：" + id);
            }
            tags.add(tag);
        }
        return tags;
    }

    /** 取一个指定类型的标签（项目 / 标签校验共用）；类型不符或不存在 → 40000 */
    private Tag requireTagOfType(Long id, TagType type) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null || !Objects.equals(tag.getType(), type.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, type.getLabel() + "不存在或类型不正确");
        }
        return tag;
    }

    private void insertLink(Long noteId, Long tagId) {
        NoteTag link = new NoteTag();
        link.setNoteId(noteId);
        link.setTagId(tagId);
        noteTagMapper.insert(link);
    }

    /**
     * 批量读取若干笔记的关联（列表页用，避免 N+1）。
     *
     * <p>两次查询解决：① `note_tag` 按 noteId IN；② `tag` 按 tagId 批量取
     * （`selectBatchIds` 自带逻辑删除过滤 → 已删标签不会出现在笔记上）。
     */
    private Map<Long, Associations> loadAssociations(List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) {
            return Map.of();
        }
        List<NoteTag> links = noteTagMapper.selectList(
                Wrappers.<NoteTag>lambdaQuery().in(NoteTag::getNoteId, noteIds));
        if (links.isEmpty()) {
            return Map.of();
        }
        Set<Long> tagIds = links.stream().map(NoteTag::getTagId).collect(Collectors.toSet());
        Map<Long, Tag> tags = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> tag));

        Map<Long, Associations> result = new HashMap<>();
        for (NoteTag link : links) {
            Tag tag = tags.get(link.getTagId());
            if (tag == null) {
                continue;
            }
            Associations assoc = result.computeIfAbsent(link.getNoteId(), key -> new Associations());
            if (Objects.equals(tag.getType(), TagType.PROJECT.getCode())) {
                // 表层面可能有多条 type=1（历史脏数据），展示取第一个，避免整页报错
                if (assoc.project == null) {
                    assoc.project = TagBriefVO.from(tag);
                }
            } else {
                assoc.tags.add(TagBriefVO.from(tag));
            }
        }
        result.values().forEach(assoc -> assoc.tags.sort(
                Comparator.comparing(TagBriefVO::name, Comparator.nullsLast(String::compareTo))));
        return result;
    }

    /** 单条笔记 → 出参（含项目与标签） */
    private NoteVO toVO(Note note) {
        if (note == null) {
            return null;
        }
        Associations assoc = loadAssociations(List.of(note.getId())).get(note.getId());
        if (assoc == null) {
            return NoteVO.from(note, null, List.of());
        }
        return NoteVO.from(note, assoc.project == null ? null : assoc.project.id(), assoc.tags);
    }

    /** 关联装配的中间结构（项目最多一个；标签按名称排序） */
    private static final class Associations {
        private TagBriefVO project;
        private final List<TagBriefVO> tags = new ArrayList<>();
    }

    // ------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------

    /** 取笔记；不存在 / 已被逻辑删除 → 40400（@TableLogic 天然过滤已删行） */
    private Note requireNote(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少笔记 id");
        }
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "笔记不存在或已删除");
        }
        return note;
    }

    /**
     * **`content_text` 的唯一计算入口**（铁律③）—— 仅用于「新建」。
     *
     * <p>更新路径**不要**用它：那里要传「最终生效的内容」而不是 patch 里的字段
     * （见 {@link #update} 内联的注释与 R1 踩坑记录）。
     */
    private void applyContentText(Note note) {
        note.setContentText(contentTextBuilder.build(note.getContent(), note.getContentFormat()));
    }

    /**
     * **html 笔记入库前的 jsoup 白名单清洗**（PRD §6.4 / P-004；T7 落地）。
     *
     * <p>为什么清洗放在这一层而不是 Controller：
     * <ul>
     *   <li>它是「写入内容」的唯一收口，和 {@code content_text} 的计算天然同处一室，
     *       顺序也好保证（**先清洗、再算搜索列**）；</li>
     *   <li>将来任何新的写入路径（导入、迁移脚本）只要走 Service 就自动带上清洗。</li>
     * </ul>
     *
     * <p>⚠️ **md 分支必须原样返回**：Markdown 是源码，过 HTML 清洗会破坏语法
     * （比如把 `# 标题` 之外的行内 `<` 转义、把表格竖线搞乱）。md 的 XSS 防护在
     * 「渲染产物」那一端做：前端 DOMPurify（`sanitizeMarkdownHtml`）。
     */
    private String sanitizeForStore(String raw, int format) {
        if (format != ContentFormat.HTML.getCode()) {
            return raw;
        }
        return htmlSanitizer.cleanForStore(raw);
    }

    /**
     * 「正文是否算有内容」—— **必须按格式分流**。
     *
     * <p>md：有可见字符就算（`StringUtils.hasText`）。
     * html：只判文本会误杀「只贴了一张图、一个字没写」的笔记（P-004 允许），
     * 所以交给 {@link HtmlSanitizer#hasVisibleContent(String)}（文本或 img 二者之一）。
     */
    private boolean hasVisibleContent(String content, int format) {
        if (format == ContentFormat.HTML.getCode()) {
            return htmlSanitizer.hasVisibleContent(content);
        }
        return StringUtils.hasText(content);
    }

    /** 状态切换（幂等：已是目标状态就不写库） */
    private NoteVO changeStatus(Long id, NoteStatus target) {
        Note note = requireNote(id);
        if (Objects.equals(note.getStatus(), target.getCode())) {
            return toVO(note);
        }
        noteMapper.update(null, Wrappers.<Note>lambdaUpdate()
                .eq(Note::getId, id)
                .set(Note::getStatus, target.getCode())
                /**
                 * ⚠️ **显式把 updated_at 写回原值**（不是 now，也不是不写）：
                 * 表上的 `updated_at` 带 `ON UPDATE CURRENT_TIMESTAMP` ——
                 * 只要这一行被 UPDATE 且没显式赋值，MySQL 会自动刷成当前时间。
                 * 而「归档 / 取回」是状态流转、不算编辑过，刷掉 updated_at 会让它
                 * 跑到「最近」列最前面（T6 实测踩到；PRD P-005 曾标为待确认，此处定稿）。
                 */
                .set(Note::getUpdatedAt, note.getUpdatedAt()));
        log.info("笔记状态变更：id={}, {} → {}", id, note.getStatus(), target.getCode());
        return toVO(noteMapper.selectById(id));
    }

    /** 标题归一：null → ""；去首尾空白；超 255 截断（PRD P-001 验收要求） */
    private String normalizeTitle(String raw) {
        if (raw == null) {
            return "";
        }
        String title = raw.trim();
        if (title.length() > TITLE_MAX) {
            log.warn("标题超长已截断：原长 {} → {}", title.length(), TITLE_MAX);
            title = title.substring(0, TITLE_MAX);
        }
        return title;
    }

    /** 校验格式：只接受 0/1，null 视为 md（默认） */
    private int resolveFormat(Integer raw) {
        if (raw == null) {
            return ContentFormat.defaultFormat().getCode();
        }
        if (raw != ContentFormat.MD.getCode() && raw != ContentFormat.HTML.getCode()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "笔记格式只能是 0(md) 或 1(html)");
        }
        return raw;
    }

    /** 校验 pinned：只接受 0/1 */
    private int requirePinned(Integer raw) {
        if (raw == null || (raw != PINNED_OFF && raw != PINNED_ON)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "置顶标记只能是 0 或 1");
        }
        return raw;
    }

    private int resolvePage(Integer raw) {
        int page = raw == null ? DEFAULT_PAGE : raw;
        if (page < 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "页码从 1 开始");
        }
        return page;
    }

    private int resolveSize(Integer raw) {
        int size = raw == null ? DEFAULT_SIZE : raw;
        if (size < 1 || size > MAX_SIZE) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "每页条数需在 1~" + MAX_SIZE + " 之间");
        }
        return size;
    }

    /** 状态筛选：null = 全部；只接受 0/1 */
    private Integer resolveStatusFilter(Integer raw) {
        if (raw == null) {
            return null;
        }
        if (raw != NoteStatus.INBOX.getCode() && raw != NoteStatus.ARCHIVED.getCode()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态筛选只能是 0(待整理) 或 1(已归档)");
        }
        return raw;
    }
}
