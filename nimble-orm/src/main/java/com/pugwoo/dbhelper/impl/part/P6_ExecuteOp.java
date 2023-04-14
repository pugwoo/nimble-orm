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
        sql = addComment(sql);
        log(sql, 0, paramMap);
        long start = System.currentTimeMillis();

        int rows;
        if (paramMap == null) {
            rows = namedParameterJdbcTemplate.update(sql, new HashMap<>());
        } else {
            rows = namedParameterJdbcTemplate.update(sql, paramMap);
        }

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, 0, paramMap);
        return rows;
    }

}
