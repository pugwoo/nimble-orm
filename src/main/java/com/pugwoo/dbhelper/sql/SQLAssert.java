package com.pugwoo.dbhelper.sql;

import java.lang.reflect.Field;
import java.util.List;

import com.pugwoo.dbhelper.exception.NotOnlyOneKeyColumnException;
import com.pugwoo.dbhelper.utils.DOInfoReader;

public class SQLAssert {

	public static void onlyOneKeyColumn(Class<?> clazz) {
		
		List<Field> keyFields = DOInfoReader.getKeyColumns(clazz);

		if (keyFields.size() != 1) {
			throw new NotOnlyOneKeyColumnException(
					"must have only one key column, actually has "
							+ keyFields.size() + " key columns");
		}
		
	}
	
}
