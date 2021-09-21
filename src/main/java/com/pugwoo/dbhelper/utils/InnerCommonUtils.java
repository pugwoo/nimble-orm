package com.pugwoo.dbhelper.utils;

import java.util.Objects;

/**
 * 内部常用工具类
 */
public class InnerCommonUtils {

    /**
     * 判断给定的数组是否非空
     */
    public static boolean isNotEmpty(String[] strs) {
        return strs != null && strs.length > 0;
    }

    /**
     * 检查strs中是否包含有checkStr字符串
     */
    public static boolean isContains(String checkStr, String[] strs) {
        if (!isNotEmpty(strs)) {
            return false;
        }

        boolean isContain = false;
        for (String str : strs) {
            if (Objects.equals(str, checkStr)) {
                isContain = true;
                break;
            }
        }

        return isContain;
    }

}
