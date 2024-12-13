package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.exception.EnumNotSupportedException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ValidateUtils {

    /**
     * 校验参数是否包含枚举类型，如果包含，则抛出异常
     * @param arg 需要检查的参数
     */
    public static void assertNoEnumArgs(Object arg) {
        if (arg == null || arg instanceof byte[]) {
            return;
        }
        if (arg instanceof Enum) {
            throw new EnumNotSupportedException("sql parameters contains enum value:" + arg);
        }
        if (arg instanceof List) {
            for (Object o : (List<?>) arg) {
                assertNoEnumArgs(o);
            }
        } else if (arg instanceof Set) {
            for (Object o : (Set<?>) arg) {
                assertNoEnumArgs(o);
            }
        } else if (arg.getClass().isArray()) {
            Class<?> componentType = arg.getClass().getComponentType();
            if (componentType.isEnum()) {
                throw new EnumNotSupportedException("sql parameters contains enum value:" + arg);
            }
        } else if (arg instanceof Map) {
            for (Object o : ((Map<?, ?>) arg).values()) {
                assertNoEnumArgs(o);
            }
        }
    }

}
