package com.pkb.common;

/**
 * {@code note.content_text} 的生成器 —— **搜索专用文本的单一收口点**。
 *
 * <h3>为什么必须收口（铁律③ + PRD 风险 R1）</h3>
 * <p>{@code content_text} 是搜索专用列，**搜索只打 `title + content_text`，永不打 `content`**
 * （否则会命中 html 标签名与 markdown 符号，产生大量误召回）。
 * 它必须与 {@code content} 同步生成 —— 而**漏同步不会报错，只会让搜索结果静默变少**，
 * 属于「发现得很晚」的一类问题。因此：
 * <ul>
 *   <li>笔记的新增 / 更新**必须**经本接口计算 {@code content_text}；
 *   <li>**禁止**在 Mapper 或调用方散落写这两列。
 * </ul>
 *
 * <h3>格式分流（PRD §6.2）</h3>
 * <table border="1">
 *   <tr><th>content_format</th><th>content_text 来源</th></tr>
 *   <tr><td>0 = md</td><td>原文 → <b>轻量去标记</b>（剥 # / * / - / 链接语法 / 代码围栏，保留纯文本）</td></tr>
 *   <tr><td>1 = html</td><td>原文 → <b>剥标签</b>得到纯文本</td></tr>
 * </table>
 *
 * <h3>实现状态</h3>
 * <p>默认实现 {@link Default} 已可用（html 走 jsoup、md 走保守去标记）。
 * ⚠️ **md 去标记的具体规则仍属 PRD 第十二章 #13（未关闭）**，
 * 归属 **T5** 定稿 —— 本接口的**签名**已冻结，届时只需替换实现，调用方不受影响。
 */
public interface ContentTextBuilder {

    /**
     * 计算 {@code content_text}。
     *
     * @param content       笔记正文原文
     * @param contentFormat 格式：0=md，1=html（null 视为 md）
     * @return 供搜索使用的纯文本；入参为空时返回空串（**不返回 null**，避免调用方判空遗漏）
     */
    String build(String content, Integer contentFormat);

    /**
     * 默认实现：html 用 jsoup 剥标签，md 做保守的轻量去标记。
     *
     * <p>⚠️ md 分支的规则是**保守起步**（只处理最明显的标记），
     * 完整规则待 T5 定稿后替换本类。
     */
    final class Default implements ContentTextBuilder {

        /** 代码围栏（```lang … ```）：T5 定稿前**保留围栏内的内容**，只剥掉围栏行本身 */
        private static final String FENCE_LINE = "(?m)^\\s*```.*$";

        /** ATX 标题标记（# ## ### …）、引用（>）、无序列表（- * +）、有序列表（1.） */
        private static final String LINE_LEADING_MARKERS = "(?m)^\\s*(#{1,6}\\s+|>\\s*|[-*+]\\s+|\\d+[.)]\\s+)";

        /** 行内标记：**粗** __粗__ *斜* _斜_ ~~删~~ `代码` */
        private static final String INLINE_MARKERS = "[*_~`]+";

        /** 图片 ![alt](url) —— 整体丢弃（URL 不是搜索目标） */
        private static final String IMAGE_SYNTAX = "!\\[[^\\]]*\\]\\([^)]*\\)";

        /** 链接 [text](url) —— 保留 text、丢弃 url */
        private static final String LINK_SYNTAX = "\\[([^\\]]*)\\]\\([^)]*\\)";

        /** 水平分割线（--- / *** / ___） */
        private static final String HR_LINE = "(?m)^\\s*([-*_]\\s*){3,}$";

        private static final java.util.regex.Pattern P_FENCE =
                java.util.regex.Pattern.compile(FENCE_LINE);
        private static final java.util.regex.Pattern P_HR =
                java.util.regex.Pattern.compile(HR_LINE);
        private static final java.util.regex.Pattern P_LEADING =
                java.util.regex.Pattern.compile(LINE_LEADING_MARKERS);
        private static final java.util.regex.Pattern P_IMAGE =
                java.util.regex.Pattern.compile(IMAGE_SYNTAX);
        private static final java.util.regex.Pattern P_LINK =
                java.util.regex.Pattern.compile(LINK_SYNTAX);
        private static final java.util.regex.Pattern P_INLINE =
                java.util.regex.Pattern.compile(INLINE_MARKERS);

        /** 连续空白归一为单个空格（含换行，便于搜索与摘要） */
        private static final java.util.regex.Pattern P_BLANK =
                java.util.regex.Pattern.compile("\\s+");

        private final com.pkb.util.HtmlSanitizer htmlSanitizer;

        /**
         * @param htmlSanitizer 复用已有的 jsoup 清洗组件（它已提供 {@code toSummary} 剥标签能力）
         */
        public Default(com.pkb.util.HtmlSanitizer htmlSanitizer) {
            this.htmlSanitizer = htmlSanitizer;
        }

        @Override
        public String build(String content, Integer contentFormat) {
            if (content == null || content.isBlank()) {
                return "";
            }
            if (com.pkb.common.constant.ContentFormat.HTML.getCode() == (contentFormat == null ? 0 : contentFormat)) {
                // html：交给 jsoup 剥标签取全文（复用已有清洗组件，不另造一套）
                return normalize(htmlSanitizer.toPlainText(content));
            }
            return normalize(stripMarkdown(content));
        }

        /**
         * 保守的 md 轻量去标记。
         *
         * <p>⚠️ **规则未定稿**（PRD 第十二章 #13，归 T5）。当前只做「最明显且低风险」的处理，
         * 宁少勿多 —— 过度剥离会把正文真实内容也吃掉，那比误召回更糟。
         */
        private String stripMarkdown(String raw) {
            String text = raw;
            text = P_FENCE.matcher(text).replaceAll("");        // 剥代码围栏行，保留围栏内代码文本
            text = P_HR.matcher(text).replaceAll("");           // 剥水平分割线
            text = P_IMAGE.matcher(text).replaceAll("");         // 图片整体丢弃
            text = P_LINK.matcher(text).replaceAll("$1");        // 链接保留文字
            text = P_LEADING.matcher(text).replaceAll("");       // 剥行首标记（# > - * 1.）
            text = P_INLINE.matcher(text).replaceAll("");        // 剥行内强调标记
            return text;
        }

        /** 空白归一：连续空白（含换行）压成单个空格，并去掉首尾空白 */
        private String normalize(String text) {
            if (text == null || text.isEmpty()) {
                return "";
            }
            return P_BLANK.matcher(text).replaceAll(" ").trim();
        }
    }
}
