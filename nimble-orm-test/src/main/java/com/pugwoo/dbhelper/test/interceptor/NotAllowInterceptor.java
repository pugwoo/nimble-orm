package com.pugwoo.dbhelper.test.interceptor;

import com.pugwoo.dbhelper.DBHelperInterceptor;

import java.util.List;

public class NotAllowInterceptor extends DBHelperInterceptor  {

    @Override
    public boolean beforeSelect(Class<?> clazz, String sql, List<Object> args) {
        return false;
    }

    @Override
    public boolean beforeInsert(List<Object> list) {
        return false;
    }

    @Override
    public boolean beforeUpdate(List<Object> tList, String setSql, List<Object> setSqlArgs) {
        return false;
    }

    @Override
    public boolean beforeUpdateAll(Class<?> clazz, String sql, List<String> customsSets, List<Object> customsParams, List<Object> args) {
        return false;
    }

    @Override
    public boolean beforeDelete(List<Object> tList) {
        return false;
    }
}
