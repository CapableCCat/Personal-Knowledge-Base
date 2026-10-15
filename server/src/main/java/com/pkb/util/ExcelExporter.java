package com.pkb.util;

import cn.idev.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 导出工具 —— 🔒 **保留不启用**（用户拍板勿删，见清单 D18；V0.2 导出笔记时启用）。
 *
 * <p>两件事：
 * <ol>
 *   <li>把一批行写成 xlsx 字节（列头由行模型的 {@code @ExcelProperty} 决定）</li>
 *   <li>把字节写进响应，**并正确设置中文文件名**</li>
 * </ol>
 *
 * <p>关于第 2 点的坑：文件名需支持中文（未来导出笔记时形如 {@code 笔记导出_20261009.xlsx}），
 * 而 HTTP 头是 ASCII 的 —— 直接塞中文会乱码。按 RFC 5987 要写成
 * {@code filename*=UTF-8''<percent-encoded>}，同时保留一个 ASCII 的 {@code filename=} 给老浏览器兜底。
 */
public final class ExcelExporter {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * 写出 xlsx 字节。
     *
     * @param sheetName sheet 名
     * @param headClass 行模型类（列头取自它的 {@code @ExcelProperty}）
     * @param rows      数据行
     */
    public static byte[] toXlsx(String sheetName, Class<?> headClass, List<?> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, headClass).sheet(sheetName).doWrite(rows);
        return out.toByteArray();
    }

    /**
     * 把 xlsx 写进响应（带中文文件名）。
     *
     * @param filename      展示给用户的文件名（可含中文），如 {@code 笔记导出_20261009.xlsx}
     * @param asciiFallback 老浏览器用的纯 ASCII 文件名，如 {@code notes_20261009.xlsx}
     */
    public static void writeToResponse(HttpServletResponse response, String filename,
                                      String asciiFallback, byte[] data) throws IOException {
        // URLEncoder 会把空格编成 '+'，而 RFC 5987 里必须是 %20
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType(XLSX_CONTENT_TYPE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encoded);
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
    }

    private ExcelExporter() {
    }
}
