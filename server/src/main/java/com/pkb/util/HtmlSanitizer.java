package com.pkb.util;

import com.pkb.config.MinioProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 富文本 XSS 白名单清洗（PRD F-008「内容存储前做 XSS 白名单清洗」）。
 *
 * <p><b>为什么用白名单而不是黑名单</b>：黑名单（屏蔽 {@code <script>}）永远会漏，
 * 浏览器每年都有新的绕过姿势（{@code <svg/onload>}、{@code <img src=x onerror>}、
 * 大小写混淆、HTML 实体编码、{@code javascript:} 伪协议……）。白名单只回答一个问题：
 * 「这个标签 / 属性 / 协议**是不是我认识的**」，不认识的一律丢掉，绕不过去。
 *
 * <p><b>清洗不是"一次就够"</b>：本类在**写入前**（提交入库）与**读取后**（渲染输出）
 * 各调用一次。只清写入端的话，任何绕过应用直连数据库写入的脏数据都会直接进浏览器；
 * 只清读取端的话，库里会长期存着脏 HTML 等着被人翻出来用。
 *
 * <p>除 jsoup 的白名单外，还有三处**属性级加固**（白名单只能限制"有哪些属性"，管不了"属性值长什么样"）：
 * <ol>
 *   <li>{@code style} 只保留排版相关的少量 CSS 属性，且值必须严格匹配正则
 *       （挡掉 {@code url(javascript:...)}、{@code expression()}、{@code \}- 转义绕过等）</li>
 *   <li>{@code img.src} 必须是本站对象存储的地址 —— 否则富文本就成了任意外链 / 内网探测的跳板</li>
 *   <li>{@code a} 强制补 {@code rel="noopener noreferrer"}（防 {@code window.opener} 反向控制），
 *       且 {@code target} 只认 {@code _blank}</li>
 * </ol>
 *
 * <p>图片地址采用「**库里存 key、出口拼地址**」：入库走 {@link #cleanForStore(String)} 把完整地址压成
 * 对象 key（{@code attachments/images/...}），输出走 {@link #cleanForOutput(String)} 再拼回公开前缀 ——
 * 换域名时历史笔记里的图不会集体失效。
 */
@Component
public class HtmlSanitizer {

    /** 标题 / 正文用的摘要长度 */
    private static final int DEFAULT_SUMMARY_LENGTH = 80;

    /**
     * 危险容器：**连同内容一起删掉**。
     *
     * <p>必须在白名单清洗之前显式移除，不能只靠 {@code Safelist.removeTags}：
     * 后者只删标签、保留文本内容，于是 {@code <script>alert(1)</script>} 会留下满屏的
     * "alert(1)" 文本（T12 实测发现的坑，见《开发任务点清单》§6 D83）。
     */
    private static final String DANGEROUS_SELECTOR = String.join(", ",
            "script", "style", "iframe", "frame", "frameset", "object", "embed", "applet",
            "form", "input", "button", "select", "textarea", "option",
            "template", "noscript", "noembed", "noframes",
            "link", "meta", "base", "title", "head");

    /**
     * 需要「拆壳」而不是「连内容删除」的外来命名空间标签。
     *
     * <p>{@code <svg>} / {@code <math>} 是解析黑洞：{@code <p>前</p><svg/onload=x>} 之后的内容
     * 会被浏览器解析成 svg 的子节点。若连内容一起删，后面**正常正文也会陪葬**
     * （从网页复制的带图标内容很容易踩到）。所以这里只拆掉标签本身、保留子节点，
     * 其属性与内部脚本仍会被白名单和步骤①处理。
     */
    private static final String FOREIGN_SELECTOR = "svg, math";

    /** 允许保留的 CSS 属性（只放排版相关，杜绝布局/定位被改坏） */
    private static final Set<String> ALLOWED_STYLE_PROPS =
            Set.of("text-align", "text-indent", "color", "background-color");

    /**
     * 正文内嵌图片的**对象 key** 前缀（PKB：附件统一落在 attachments/ 下）。
     *
     * <p>库里只存 key（{@code attachments/images/202610/xxx.png}），输出时才拼成完整地址 ——
     * 换域名 / 换端口时历史笔记里的图片不会集体失效。
     */
    private static final String IMAGE_KEY_PREFIX = "attachments/images/";

    private static final Pattern IMAGE_KEY = Pattern.compile(
            "^" + Pattern.quote(IMAGE_KEY_PREFIX) + "[A-Za-z0-9][A-Za-z0-9._/-]*\\.(?:jpg|jpeg|png|gif|webp)$");

    private static final Set<String> TEXT_ALIGN_VALUES = Set.of("left", "center", "right", "justify");

    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9a-fA-F]{3,8}$");
    private static final Pattern RGB_COLOR = Pattern.compile("^rgba?\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*(?:,\\s*(?:0|1|0?\\.\\d{1,3})\\s*)?\\)$");
    private static final Pattern LENGTH = Pattern.compile("^\\d{1,4}(?:\\.\\d{1,2})?(?:px|em|rem|%)$");
    private static final Pattern SIZE_ATTR = Pattern.compile("^\\d{1,4}(?:px|%)?$");

    /** 输出设置：不美化（prettyPrint 会插入换行与缩进，破坏 pre / 行内布局） */
    private static final Document.OutputSettings OUTPUT = new Document.OutputSettings()
            .prettyPrint(false)
            .charset(StandardCharsets.UTF_8);

    /** 允许的图片地址前缀（顺序敏感：第一个是当前公开前缀，输出时用它拼地址） */
    private final List<String> allowedImagePrefixes;
    private final Safelist safelist;

    public HtmlSanitizer(MinioProperties properties) {
        // 允许的图片地址前缀：当前公开前缀 + endpoint/bucket（换过 MINIO_PUBLIC_URL 的历史图片也放行）
        Set<String> prefixes = new LinkedHashSet<>();
        prefixes.add(properties.resolvePublicPrefix());
        prefixes.add(stripTrailingSlash(properties.getEndpoint().trim()) + "/" + properties.getBucket());
        this.allowedImagePrefixes = prefixes.stream().filter(StringUtils::hasText).toList();
        this.safelist = buildSafelist();
    }

    // ------------------------------------------------------------
    // 对外方法
    // ------------------------------------------------------------

    /**
     * **入库前**清洗：白名单过滤 + 图片地址归一为对象 key。
     *
     * <p>提交上来的 HTML 里图片是完整地址（编辑器刚上传时拿到的就是地址），
     * 落库前统一压成 key，保证「库里只有 key」这一条不变量。
     */
    public String cleanForStore(String rawHtml) {
        return cleanInternal(rawHtml, true);
    }

    /**
     * **输出前**清洗：白名单过滤 + 图片 key 拼成完整地址。
     *
     * <p>输出端再清一次不是多余：直连数据库写进去的脏数据、或早期未清洗的历史数据，
     * 都会在进浏览器之前被这里拦下。
     */
    public String cleanForOutput(String rawHtml) {
        return cleanInternal(rawHtml, false);
    }

    /** 清洗富文本 HTML（图片按「输出形态」处理：对象 key 展开为完整地址） */
    public String clean(String rawHtml) {
        return cleanInternal(rawHtml, null);
    }

    private String cleanInternal(String rawHtml, Boolean toKey) {
        if (!StringUtils.hasText(rawHtml)) {
            return "";
        }
        // ① 先剥掉危险容器（连同内容）
        Document dirty = Jsoup.parseBodyFragment(rawHtml);
        dirty.outputSettings(OUTPUT);
        dirty.select(DANGEROUS_SELECTOR).remove();
        // ①' svg / math 只拆壳保留子节点（见 FOREIGN_SELECTOR 说明），比连内容删除安全得多
        dirty.select(FOREIGN_SELECTOR).unwrap();
        // ①'' 图片 key 先展开成完整地址：jsoup 的协议白名单只有 http/https，
        //      相对地址（attachments/images/… 这种对象 key）会被当成"非法协议"整张删掉
        //      —— 这正是"库里存 key"必须配一次预展开的原因（OSC T12 实测踩到）
        expandImageKeys(dirty);

        // ② 白名单过滤（标签 / 属性 / 协议）
        String whitelisted = Jsoup.clean(dirty.body().html(), "", safelist, OUTPUT);

        // ③ 属性级加固（白名单管不了属性值）
        Document safe = Jsoup.parseBodyFragment(whitelisted);
        safe.outputSettings(OUTPUT);
        hardenStyles(safe);
        hardenImages(safe, toKey);
        hardenLinks(safe);
        return safe.body().html().trim();
    }

    /** 富文本 → 纯文本摘要（列表页用；HTML 标签不进响应体） */
    public String toSummary(String rawHtml) {
        return toSummary(rawHtml, DEFAULT_SUMMARY_LENGTH);
    }

    public String toSummary(String rawHtml, int maxLength) {
        if (!StringUtils.hasText(rawHtml)) {
            return "";
        }
        String text = Jsoup.parse(rawHtml).text().replaceAll("\\s+", " ").trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "…";
    }

    /**
     * 清洗后的内容是否「有东西可看」。
     *
     * <p>不能只看纯文本：一条只贴了截图、一个字没写的笔记也是合法内容，
     * 只判 text 会把它误判成空内容而拒收。
     */
    public boolean hasVisibleContent(String cleanedHtml) {
        if (!StringUtils.hasText(cleanedHtml)) {
            return false;
        }
        Document doc = Jsoup.parseBodyFragment(cleanedHtml);
        return StringUtils.hasText(doc.text()) || !doc.select("img").isEmpty();
    }

    /**
     * 该地址是否为可用的笔记内嵌图 —— 接受两种形态：
     * <ul>
     *   <li>本站对象存储的完整地址（编辑器刚上传、或用户手工粘贴本站地址）</li>
     *   <li>对象 key（库里存的形态，输出时才展开）</li>
     * </ul>
     * 站外地址一律不接受：否则富文本就成了任意外链 / 内网地址探测的跳板。
     */
    public boolean isAllowedImageUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String value = url.trim();
        if (IMAGE_KEY.matcher(value).matches()) {
            return true;
        }
        return allowedImagePrefixes.stream()
                .anyMatch(prefix -> value.startsWith(prefix + "/") && IMAGE_KEY.matcher(value.substring(prefix.length() + 1)).matches());
    }

    // ------------------------------------------------------------
    // 白名单定义
    // ------------------------------------------------------------

    private Safelist buildSafelist() {
        Safelist list = new Safelist()
                .addTags(
                        // 块级
                        "p", "div", "br", "hr", "blockquote", "pre",
                        "h1", "h2", "h3", "h4", "h5", "h6",
                        // 行内
                        "span", "strong", "b", "em", "i", "u", "s", "del", "ins",
                        "sub", "sup", "mark", "small", "code",
                        // 列表
                        "ul", "ol", "li",
                        // 链接与图片
                        "a", "img",
                        // 表格（技术笔记里贴表格很常见）
                        "table", "thead", "tbody", "tfoot", "tr", "td", "th", "caption",
                        // 图文
                        "figure", "figcaption")
                .addAttributes("a", "href", "title", "target", "rel")
                .addAttributes("img", "src", "alt", "title", "width", "height")
                .addAttributes("ol", "start")
                .addAttributes("td", "colspan", "rowspan")
                .addAttributes("th", "colspan", "rowspan")
                // style 先放行，值在 hardenStyles 里逐条校验
                .addAttributes(":all", "style")
                // 协议白名单：javascript: / data: / vbscript: 在这里就被挡掉
                .addProtocols("a", "href", "http", "https", "mailto", "tel")
                .addProtocols("img", "src", "http", "https")
                // 反向 tabnabbing 防护
                .addEnforcedAttribute("a", "rel", "noopener noreferrer");
        // 明确排除（防止后续误加回来）
        list.removeTags("script", "style", "iframe", "object", "embed", "form", "input", "svg", "math");
        list.removeAttributes(":all", "onclick", "onerror", "onload", "onmouseover", "id", "class");
        return list;
    }

    // ------------------------------------------------------------
    // 属性级加固
    // ------------------------------------------------------------

    /** style 只留排版属性，且值严格校验 —— 白名单只管"有没有 style"，管不了里面写什么 */
    private void hardenStyles(Document doc) {
        for (Element element : doc.select("[style]")) {
            String kept = java.util.Arrays.stream(element.attr("style").split(";"))
                    .map(String::trim)
                    .filter(declaration -> !declaration.isEmpty())
                    .map(declaration -> declaration.split(":", 2))
                    .filter(parts -> parts.length == 2)
                    .filter(parts -> ALLOWED_STYLE_PROPS.contains(parts[0].trim().toLowerCase()))
                    .filter(parts -> isSafeStyleValue(parts[0].trim().toLowerCase(), parts[1].trim()))
                    .map(parts -> parts[0].trim().toLowerCase() + ": " + parts[1].trim())
                    .collect(Collectors.joining("; "));
            if (kept.isEmpty()) {
                element.removeAttr("style");
            } else {
                element.attr("style", kept);
            }
        }
    }

    private boolean isSafeStyleValue(String property, String value) {
        if (value.isEmpty() || value.length() > 64) {
            return false;
        }
        String lower = value.toLowerCase();
        // 任何能"取外部资源"或触发脚本的写法一律拒绝
        if (lower.contains("url(") || lower.contains("expression") || lower.contains("javascript")
                || lower.contains("import") || lower.contains("\\") || lower.contains("<")) {
            return false;
        }
        return switch (property) {
            case "text-align" -> TEXT_ALIGN_VALUES.contains(lower);
            case "text-indent" -> LENGTH.matcher(lower).matches();
            case "color", "background-color" -> HEX_COLOR.matcher(value).matches() || RGB_COLOR.matcher(lower).matches();
            default -> false;
        };
    }

    /**
     * 图片只能是本站对象存储的地址；宽高只留纯数字。
     *
     * @param toKey {@code true} 落库：把完整地址压成对象 key；否则保持完整地址（预展开阶段已拼好）
     */
    private void hardenImages(Document doc, Boolean toKey) {
        for (Element img : doc.select("img")) {
            String src = img.attr("src").trim();
            if (!isAllowedImageUrl(src)) {
                // 外链图片（含内网地址探测）直接摘掉整张图，而不是留个空壳
                img.remove();
                continue;
            }
            if (Boolean.TRUE.equals(toKey)) {
                img.attr("src", toObjectKey(src));
            }
            for (String attr : new String[]{"width", "height"}) {
                if (img.hasAttr(attr) && !SIZE_ATTR.matcher(img.attr(attr).trim()).matches()) {
                    img.removeAttr(attr);
                }
            }
        }
    }

    /** 把对象 key 形态的图片地址预展开成完整地址（必须在白名单清洗之前做） */
    private void expandImageKeys(Document doc) {
        for (Element img : doc.select("img")) {
            String src = img.attr("src").trim();
            if (IMAGE_KEY.matcher(src).matches()) {
                img.attr("src", expandKey(src));
            }
        }
    }

    /** 完整地址 → 对象 key（已是 key 时原样返回） */
    private String toObjectKey(String src) {
        if (IMAGE_KEY.matcher(src).matches()) {
            return src;
        }
        for (String prefix : allowedImagePrefixes) {
            if (src.startsWith(prefix + "/")) {
                return src.substring(prefix.length() + 1);
            }
        }
        return src;
    }

    /** 对象 key → 完整地址（已是地址时原样返回） */
    private String expandKey(String src) {
        if (!IMAGE_KEY.matcher(src).matches()) {
            return src;
        }
        return allowedImagePrefixes.iterator().next() + "/" + src;
    }

    /** 站外链接统一新窗口打开并补 rel（enforced attribute 已兜底，这里把 target 收敛到 _blank） */
    private void hardenLinks(Document doc) {
        for (Element a : doc.select("a")) {
            if (!a.hasAttr("href")) {
                a.removeAttr("target");
                continue;
            }
            a.attr("target", "_blank");
            a.attr("rel", "noopener noreferrer");
        }
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
