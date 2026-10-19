package com.pkb.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pkb.common.constant.TagType;
import com.pkb.common.exception.BusinessException;
import com.pkb.common.result.ResultCode;
import com.pkb.dto.TagCountRow;
import com.pkb.dto.TagCreateRequest;
import com.pkb.dto.TagSortRequest;
import com.pkb.dto.TagUpdateRequest;
import com.pkb.dto.TagVO;
import com.pkb.entity.NoteTag;
import com.pkb.entity.Tag;
import com.pkb.mapper.NoteTagMapper;
import com.pkb.mapper.TagMapper;
import com.pkb.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 标签 / 项目管理实现（P-006 / P-007）。
 *
 * <p><b>三条落地点：</b>
 * <ol>
 *   <li>**名称查重**（清单 D26）：库层无唯一索引，本层查「同类型 + 同名 + 未删除」；
 *       改名时排除自己，否则原样保存会被自己挡下。</li>
 *   <li>**删除只解关联**：先物理删 `note_tag`（该表无 `is_deleted`），再逻辑删 tag；
 *       笔记一行不动 —— PRD P-006「删除项目时其下笔记不删除」。</li>
 *   <li>**计数口径**：全部状态（含待整理），见 PRD #12 拍板；一次分组查询拿全表，避免 N+1。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    /** `tag.name` 列宽（VARCHAR(64)） */
    private static final int NAME_MAX = 64;

    /** 排序步长：新建排到末尾、重排时按 0/10/20… 落库，留出手工插值的余地 */
    private static final int SORT_STEP = 10;

    /** 颜色格式：`#RGB` 或 `#RRGGBB`（可带 alpha 的 8 位也放行，与前端取色器一致） */
    private static final Pattern COLOR = Pattern.compile("^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$");

    private final TagMapper tagMapper;
    private final NoteTagMapper noteTagMapper;

    // ------------------------------------------------------------
    // 读
    // ------------------------------------------------------------

    @Override
    public List<TagVO> list(Integer type) {
        var wrapper = Wrappers.<Tag>lambdaQuery();
        if (type != null) {
            wrapper.eq(Tag::getType, requireType(type));
        }
        // 排序口径：先类型（标签在前、项目在后），再 sort 升序，最后 id 兜底
        wrapper.orderByAsc(Tag::getType).orderByAsc(Tag::getSort).orderByAsc(Tag::getId);

        Map<Long, Long> counts = countsByTag();
        return tagMapper.selectList(wrapper).stream()
                .map(tag -> toVO(tag, counts))
                .toList();
    }

    // ------------------------------------------------------------
    // 写
    // ------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagVO create(TagCreateRequest request) {
        int type = requireType(request.type());
        String name = normalizeName(request.name());
        requireNameUnique(type, name, null);

        Tag tag = new Tag();
        tag.setName(name);
        tag.setType(type);
        tag.setColor(normalizeColor(request.color()));
        tag.setSort(request.sort() == null ? nextSort(type) : request.sort());

        tagMapper.insert(tag);
        log.info("标签已创建：id={}, type={}, name={}", tag.getId(), type, name);
        return toVO(tagMapper.selectById(tag.getId()), countsByTag());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagVO update(Long id, TagUpdateRequest request) {
        Tag tag = requireTag(id);

        String name = null;
        if (request.name() != null) {
            name = normalizeName(request.name());
            requireNameUnique(tag.getType(), name, id);
        }
        // ⚠️ 用 wrapper 显式 set，而不是把字段塞进实体：
        //    实体走 updateById 时 null 会被忽略 → 「把颜色清空」永远清不掉。
        var update = Wrappers.<Tag>lambdaUpdate().eq(Tag::getId, id);
        if (name != null) {
            update.set(Tag::getName, name);
        }
        if (request.color() != null) {
            update.set(Tag::getColor, normalizeColor(request.color()));
        }
        if (request.sort() != null) {
            update.set(Tag::getSort, request.sort());
        }
        tagMapper.update(null, update);

        log.info("标签已更新：id={}, name={}", id, name == null ? tag.getName() : name);
        return toVO(tagMapper.selectById(id), countsByTag());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        Tag tag = requireTag(id);

        long affected = noteTagMapper.selectCount(
                Wrappers.<NoteTag>lambdaQuery().eq(NoteTag::getTagId, id));
        // 关联表没有 is_deleted 列（清单 D28）→ 物理删除
        noteTagMapper.delete(Wrappers.<NoteTag>lambdaQuery().eq(NoteTag::getTagId, id));
        // tag 本身是逻辑删除（@TableLogic）
        tagMapper.deleteById(id);

        log.info("标签已删除：id={}, type={}, 解除关联 {} 条", id, tag.getType(), affected);
        return (int) affected;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TagVO> sort(TagSortRequest request) {
        int type = requireType(request.type());
        List<Long> ids = request.ids() == null ? List.of() : request.ids();

        // 要求「该类型下的全部 id，一个不多一个不少」：
        // 只给部分 id 的话，没提交的条目会保留旧 sort，与新区间交错，排序结果不可预期。
        Set<Long> actual = tagMapper.selectList(Wrappers.<Tag>lambdaQuery().eq(Tag::getType, type))
                .stream().map(Tag::getId).collect(Collectors.toSet());
        if (ids.size() != actual.size() || !new HashSet<>(ids).equals(actual)) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "排序列表必须是该类型下的全部条目（数量或内容不一致）");
        }

        for (int i = 0; i < ids.size(); i++) {
            tagMapper.update(null, Wrappers.<Tag>lambdaUpdate()
                    .eq(Tag::getId, ids.get(i))
                    .set(Tag::getSort, i * SORT_STEP));
        }
        log.info("标签已重排：type={}, 共 {} 项", type, ids.size());
        return list(type);
    }

    // ------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------

    /** 一次分组查询拿「标签 → 笔记数」，避免管理页逐条 COUNT */
    private Map<Long, Long> countsByTag() {
        return noteTagMapper.countNotesByTag().stream()
                .filter(row -> row.tagId() != null)
                .collect(Collectors.toMap(TagCountRow::tagId, TagCountRow::total, (a, b) -> a));
    }

    private TagVO toVO(Tag tag, Map<Long, Long> counts) {
        if (tag == null) {
            return null;
        }
        return new TagVO(tag.getId(), tag.getName(), tag.getType(), tag.getColor(), tag.getSort(),
                counts.getOrDefault(tag.getId(), 0L), tag.getCreatedAt());
    }

    /** 取标签；不存在 / 已逻辑删除 → 40400 */
    private Tag requireTag(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少标签 id");
        }
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在或已删除");
        }
        return tag;
    }

    /** 类型只接受 0/1；刻意不给默认值（避免把项目误建成标签） */
    private int requireType(Integer raw) {
        if (raw == null || (raw != TagType.TAG.getCode() && raw != TagType.PROJECT.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "类型只能是 0(标签) 或 1(项目)");
        }
        return raw;
    }

    private String normalizeName(String raw) {
        String name = raw == null ? "" : raw.trim();
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "名称不能为空");
        }
        if (name.length() > NAME_MAX) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "名称最长 " + NAME_MAX + " 个字符");
        }
        return name;
    }

    /** 同类型内查重（清单 D26）：excludeId 用于「改名时排除自己」 */
    private void requireNameUnique(int type, String name, Long excludeId) {
        var wrapper = Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getType, type)
                .eq(Tag::getName, name);
        if (excludeId != null) {
            wrapper.ne(Tag::getId, excludeId);
        }
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "同名" + TagType.of(type).getLabel() + "已存在");
        }
    }

    /** 颜色归一：空 / null → null（表示「不设颜色」）；否则补 `#` 并校验格式 */
    private String normalizeColor(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String color = raw.trim();
        if (!color.startsWith("#")) {
            color = "#" + color;
        }
        if (!COLOR.matcher(color).matches()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "颜色格式应为 #RRGGBB");
        }
        return color;
    }

    /** 新建时的默认排序：排在同类型末尾 */
    private int nextSort(int type) {
        return tagMapper.selectList(Wrappers.<Tag>lambdaQuery().eq(Tag::getType, type)).stream()
                .map(Tag::getSort)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .orElse(0) + SORT_STEP;
    }
}
