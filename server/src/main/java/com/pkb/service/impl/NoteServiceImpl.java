package com.pkb.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pkb.common.ContentTextBuilder;
import com.pkb.common.constant.ContentFormat;
import com.pkb.common.constant.NoteStatus;
import com.pkb.common.exception.BusinessException;
import com.pkb.common.result.ResultCode;
import com.pkb.dto.NoteCreateRequest;
import com.pkb.dto.NoteListItemVO;
import com.pkb.dto.NoteQuery;
import com.pkb.dto.NoteUpdateRequest;
import com.pkb.dto.NoteVO;
import com.pkb.dto.PageResult;
import com.pkb.entity.Note;
import com.pkb.mapper.NoteMapper;
import com.pkb.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 笔记 CRUD 实现。
 *
 * <p><b>三条铁律的落地位置（本类是最关键的一处）</b>：
 * <ol>
 *   <li>① **格式不可切换**：{@link #update} 里显式拒绝与现值不同的 {@code contentFormat}</li>
 *   <li>② 项目 = tag.type=1 —— 本任务不涉及（T8）</li>
 *   <li>③ **`content_text` 同步**：<b>只经本类私有的 {@link #applyContentText} 计算</b>，
 *       新增与更新都必须调用它。禁止任何绕过本类的写入路径（漏同步 = 搜索漏数据且不报错）</li>
 * </ol>
 *
 * <p>⚠️ <b>`updated_at` 的坑</b>：归档 / 取回 / 置顶走 {@code update(null, wrapper)}，
 * 此时 MyBatis-Plus 的 {@code MetaObjectHandler} **不会**自动填充（它只在有实体入参时生效），
 * 所以那些地方必须**显式 set updated_at**，否则「最近」列的排序会失真。
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

    // ------------------------------------------------------------
    // 写
    // ------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteVO create(NoteCreateRequest request) {
        int format = resolveFormat(request.contentFormat());

        String title = normalizeTitle(request.title());
        String content = request.content();

        // 标题与正文不能同时为空（P-001：空输入回车不创建）
        if (!StringUtils.hasText(title) && !StringUtils.hasText(content)) {
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
        log.info("笔记已创建：id={}, format={}, textLen={}", note.getId(), format,
                note.getContentText() == null ? 0 : note.getContentText().length());
        return NoteVO.from(note);
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
        if (request.content() != null) {
            patch.setContent(request.content());
        }
        if (request.pinned() != null) {
            patch.setPinned(requirePinned(request.pinned()));
        }

        String finalTitle = request.title() != null ? patch.getTitle() : note.getTitle();
        String finalContent = request.content() != null ? patch.getContent() : note.getContent();
        if (!StringUtils.hasText(finalTitle) && !StringUtils.hasText(finalContent)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请填写标题或正文");
        }

        // ---- 铁律③：每次都重算 content_text，但**必须用「最终生效的内容」算** ----
        // ⚠️ R1 的真实踩坑点（2026-10-17 实测）：早期版本用 patch.content 去算，
        //    当请求只改 title / pinned、不带 content 时 patch.content 为 null
        //    → build(null) 返回 ""，把库里已有的 content_text **覆盖成空**（搜索静默漏数据）。
        //    正确做法：没传 content 就用库里的旧值 finalContent。
        patch.setContentText(contentTextBuilder.build(finalContent, note.getContentFormat()));

        // updateById 会忽略 null 字段，并触发 MetaObjectHandler 填充 updated_at
        noteMapper.updateById(patch);

        log.info("笔记已更新：id={}", id);
        return NoteVO.from(noteMapper.selectById(id));
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
            return NoteVO.from(note);
        }
        noteMapper.update(null, Wrappers.<Note>lambdaUpdate()
                .eq(Note::getId, id)
                .set(Note::getPinned, target)
                // ⚠️ 必须显式 set：无实体入参时 MetaObjectHandler 不填充
                .set(Note::getUpdatedAt, LocalDateTime.now()));
        log.info("笔记置顶状态变更：id={}, pinned={}", id, target);
        return NoteVO.from(noteMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireNote(id);
        // @TableLogic → UPDATE note SET is_deleted=1（行仍在库里，PRD P-002 明确 V0.1 无回收站）
        noteMapper.deleteById(id);
        log.info("笔记已逻辑删除：id={}", id);
    }

    // ------------------------------------------------------------
    // 读
    // ------------------------------------------------------------

    @Override
    public NoteVO detail(Long id) {
        return NoteVO.from(requireNote(id));
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
        // 排序口径（PRD 冻结）：pinned DESC, updated_at DESC
        wrapper.orderByDesc(Note::getPinned).orderByDesc(Note::getUpdatedAt);

        Page<Note> result = noteMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<NoteListItemVO> records = result.getRecords().stream()
                .map(NoteListItemVO::from)
                .toList();
        return PageResult.of(result.getTotal(), pageNo, pageSize, records);
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

    /** 状态切换（幂等：已是目标状态就不写库，避免 updated_at 空跳） */
    private NoteVO changeStatus(Long id, NoteStatus target) {
        Note note = requireNote(id);
        if (Objects.equals(note.getStatus(), target.getCode())) {
            return NoteVO.from(note);
        }
        noteMapper.update(null, Wrappers.<Note>lambdaUpdate()
                .eq(Note::getId, id)
                .set(Note::getStatus, target.getCode())
                // ⚠️ 必须显式 set：无实体入参时 MetaObjectHandler 不填充
                .set(Note::getUpdatedAt, LocalDateTime.now()));
        log.info("笔记状态变更：id={}, {} → {}", id, note.getStatus(), target.getCode());
        return NoteVO.from(noteMapper.selectById(id));
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
