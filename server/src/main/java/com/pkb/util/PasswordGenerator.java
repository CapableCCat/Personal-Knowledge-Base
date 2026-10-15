package com.pkb.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 随机密码生成器。
 *
 * <p>用途：重置账号密码时生成随机初始密码（V0.1 暂未接改密接口，保留工具备用）。
 * <p>规则：至少各含 1 个字母与 1 个数字；排除易混字符（0 O o 1 l I）。
 */
public final class PasswordGenerator {

    private static final String LETTERS = "abcdefghjkmnpqrstuvwxyzACDEFGHJKLMNPQRSTUVWXYZ";
    private static final String DIGITS = "23456789";
    private static final String ALL = LETTERS + DIGITS;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** 默认长度 8 位 */
    public static String random() {
        return random(8);
    }

    public static String random(int length) {
        if (length < 2) {
            throw new IllegalArgumentException("密码长度至少 2 位");
        }
        List<Character> chars = new ArrayList<>(length);
        chars.add(randomChar(LETTERS));
        chars.add(randomChar(DIGITS));
        while (chars.size() < length) {
            chars.add(randomChar(ALL));
        }
        Collections.shuffle(chars, RANDOM);
        StringBuilder builder = new StringBuilder(length);
        chars.forEach(builder::append);
        return builder.toString();
    }

    private static char randomChar(String source) {
        return source.charAt(RANDOM.nextInt(source.length()));
    }

    private PasswordGenerator() {
    }
}
