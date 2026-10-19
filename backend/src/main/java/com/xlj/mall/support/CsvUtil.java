package com.xlj.mall.support;

import java.util.ArrayList;
import java.util.List;

/**
 * 极简 CSV 读写工具（支持双引号转义、字段内逗号与换行），用于导入模板下载与批量导入。
 */
public final class CsvUtil {

    private CsvUtil() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    public static String build(List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(escape(row[i]));
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }

    public static List<String[]> parse(String text) {
        List<String[]> rows = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return rows;
        }
        String content = text.startsWith("\uFEFF") ? text.substring(1) : text;
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
                continue;
            }
            switch (c) {
                case '"' -> inQuotes = true;
                case ',' -> {
                    fields.add(field.toString().trim());
                    field.setLength(0);
                }
                case '\r' -> {
                    // 等待 \n
                }
                case '\n' -> {
                    fields.add(field.toString().trim());
                    field.setLength(0);
                    if (!fields.stream().allMatch(String::isEmpty)) {
                        rows.add(fields.toArray(String[]::new));
                    }
                    fields.clear();
                }
                default -> field.append(c);
            }
        }
        if (field.length() > 0 || !fields.isEmpty()) {
            fields.add(field.toString().trim());
            if (!fields.stream().allMatch(String::isEmpty)) {
                rows.add(fields.toArray(String[]::new));
            }
        }
        return rows;
    }
}
