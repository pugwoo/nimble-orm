package com.pugwoo.dbhelper.impl.part;

import java.util.HashMap;
import java.util.Map;

public class P6_ExecuteOp extends P5_DeleteOp {

    @Override
    public int executeRaw(String sql, Object... args) {
        return namedJdbcExecuteUpdate(sql, args);
    }

    @Override
    public int executeRaw(String sql, Map<String, ?> paramMap) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }
        return namedJdbcExecuteUpdate(sql, paramMap);
    }

}
