package com.pugwoo.dbhelper.test.service;

import com.pugwoo.dbhelper.DBHelperDataService;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WrongDataService implements DBHelperDataService {

    /**
     * 故意返回null，用于测试异常情况
     */
    @Override
    public List<?> get(List<Object> values, RelatedColumn relatedColumn, Class<?> localDOClass, Class<?> remoteDOClass) {
        return null;
    }

}
