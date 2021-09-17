package com.pugwoo.dbhelper.impl;

import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.impl.part.P5_DeleteOp;

/**
 * 2015年1月12日 16:41:03 数据库操作封装：增删改查
 * @author pugwoo
 */
public class SpringJdbcDBHelper extends P5_DeleteOp {
	
	// 实现分别安排在impl.part包下几个文件中

    public SpringJdbcDBHelper() {

        // 初始化特性值
        turnOnFeature(FeatureEnum.AUTO_SUM_COALESCE_TO_ZERO);

    }

}
