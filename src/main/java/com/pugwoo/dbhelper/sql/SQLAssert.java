package com.pugwoo.dbhelper.sql;

import java.lang.reflect.Field;
import java.util.List;

import com.pugwoo.dbhelper.exception.InvalidParameterException;
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
	
	public static <T> void allSameClass(List<T> list) throws InvalidParameterException {
		Class<?> clazz = null;
		for(T t : list) {
			if(clazz == null) {
				clazz = t.getClass();
			} else {
				if (!clazz.equals(t.getClass())) {
					throw new InvalidParameterException(
							"list elements must be same class");
				}
			}
		}
	}
	
}
