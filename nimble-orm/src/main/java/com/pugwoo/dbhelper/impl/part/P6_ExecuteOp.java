package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.utils.InnerCommonUtils;

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
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }

        log(sql, 0, InnerCommonUtils.newList(paramMap));
        long start = System.currentTimeMillis();

        int rows = namedParameterJdbcTemplate.update(sql, paramMap);

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, 0, InnerCommonUtils.newList(paramMap));
        return rows;
    }

}
