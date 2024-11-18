package com.pugwoo.dbhelper.test.service;

import com.pugwoo.dbhelper.annotation.WhereColumn;
import com.pugwoo.dbhelper.sql.CustomWhereProvider;
import com.pugwoo.dbhelper.sql.WhereSQL;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Service
public class MyCustomWhereProvider implements CustomWhereProvider {

    @Override
    public WhereSQL provide(Object obj, WhereColumn whereColumn, Field field, Method method) {
        Object value = DOInfoReader.getValue(field, obj);
        return new WhereSQL("age >= ?", value);
    }
}
