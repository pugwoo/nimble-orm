package com.pugwoo.dbhelper.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 内部常用工具类
 */
public class InnerCommonUtils {

    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * filter一个list
     */
    public static <T> List<T> filter(List<T> list, Predicate<? super T> predicate) {
        if(list == null) {
            return new ArrayList<>();
        }
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * filter一个数组
     */
    public static <T> List<T> filter(T[] array, Predicate<? super T> predicate) {
        if (array == null || array.length == 0) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>();
        for (T t : array) {
            if (predicate.test(t)) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 判断给定的数组是否非空
     */
    public static boolean isNotEmpty(String[] strings) {
        return strings != null && strings.length > 0;
    }

    /**
     * 检查strings中是否包含有checkStr字符串
     */
    public static boolean isContains(String checkStr, String[] strings) {
        if (!isNotEmpty(strings)) {
            return false;
        }

        boolean isContain = false;
        for (String str : strings) {
            if (Objects.equals(str, checkStr)) {
                isContain = true;
                break;
            }
        }

        return isContain;
    }

    /**
     * 将字符串str按间隔符sep分隔，返回分隔后的字符串
     * @param str 字符串
     * @param sep 间隔符
     * @return 会自动过滤掉空白(blank)的字符串；并且会自动trim()
     */
    public static List<String> split(String str, String sep) {
        if (str == null || str.isEmpty()) {
            return new ArrayList<>();
        }

        String[] splits = str.split(sep);
        List<String> result = new ArrayList<>();
        for (String s : splits) {
            if (InnerCommonUtils.isNotBlank(s)) {
                result.add(s.trim());
            }
        }
        return result;
    }

    public static String firstLetterUpperCase(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        String firstLetter = str.substring(0, 1).toUpperCase();
        return firstLetter + str.substring(1);
    }

}
