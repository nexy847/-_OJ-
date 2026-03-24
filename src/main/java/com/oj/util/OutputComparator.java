package com.oj.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OutputComparator {
    private OutputComparator() {}

    public static boolean equalsNormalized(Path actualPath, Path expectedPath) throws IOException {
        String actual = normalize(Files.readString(actualPath, StandardCharsets.UTF_8));
        String expected = normalize(Files.readString(expectedPath, StandardCharsets.UTF_8));
        return actual.equals(expected);
    }

    private static String normalize(String value) {
        //win用\r\n换行 linux用\n 强制换为\n 此行为适应docker的linux系统
        String normalized = value.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        int end = lines.length;
        while (end > 0 && lines[end - 1].trim().isEmpty()) {//忽略文件末尾的空行（比如写完代码总是打个回车）
            end--;
        }
        for (int i = 0; i < end; i++) {
            sb.append(lines[i].replaceAll("\\s+$", ""));//忽略行末空格
            if (i < end - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
