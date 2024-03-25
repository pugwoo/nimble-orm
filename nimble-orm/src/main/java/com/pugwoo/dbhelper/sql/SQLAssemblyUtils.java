package com.pugwoo.dbhelper.sql;

import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 负责将带占位符的sql和参数组合起来，形成真正可执行的sql
 */
public class SQLAssemblyUtils {

    public static String assembleSql(String sql, Object... params) {
        if (params == null) {
            return sql;
        }
        if (params.length == 1 && params[0] instanceof Map) {
            return assembleNamedParameterSql(sql, (Map<String, Object>) params[0]);
        }

        List<Object> paramsAsList = Arrays.asList(params);
        String namedSql = NamedParameterUtils.trans(sql, paramsAsList);
        Map<String, Object> namedParams = NamedParameterUtils.transParam(paramsAsList);
        return assembleNamedParameterSql(namedSql, namedParams);
    }

    public static String assembleSql(String sql, Map<String, Object> params) {
        return assembleNamedParameterSql(sql, params);
    }

    private static String assembleNamedParameterSql(String sql, Map<String, Object> params) {
        if (params == null) {
            return sql;
        }

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            // 根据参数的值来确定替换的最终样子
            Object value = entry.getValue();
            String valueAsStr = "";
            if (value instanceof List) {
                // 对于多列in的情况，List中的元素是Object[]类型
                if (!((List<?>) value).isEmpty() && ((List<?>) value).get(0) instanceof Object[]) {
                    valueAsStr = InnerCommonUtils.join(",",
                            InnerCommonUtils.transform((List<?>) value, o -> {
                                Object[] arr = (Object[]) o;
                                return "(" + InnerCommonUtils.join(",",
                                        InnerCommonUtils.transform(arr, SQLAssemblyUtils::objectToString)) + ")";
                            }));
                } else {
                    valueAsStr = InnerCommonUtils.join(",",
                            InnerCommonUtils.transform((List<?>) value, SQLAssemblyUtils::objectToString));
                }
            } else {
                valueAsStr = objectToString(value);
            }

            sql = sql.replace(":" + entry.getKey(), valueAsStr);
        }
        return sql;
    }

    /**
     * 将参数对象转换为字符串，兼容参数是Number、Boolean、String类型，不含List类型
     */
    private static String objectToString(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else {
            return "'" + value.toString().replace("'", "''") + "'"; // SQL中单引号转义是2个单引号
        }
    }
}
