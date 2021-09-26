package com.pugwoo.dbhelper.sql;

import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.NotOnlyOneKeyColumnException;
import com.pugwoo.dbhelper.utils.DOInfoReader;

import java.lang.reflect.Field;
import java.util.List;

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
		if(!isAllSameClass(list)) {
			throw new InvalidParameterException("list elements must be same class");
		}
	}
	
	public static <T> boolean isAllSameClass(List<T> list) {
		if(list == null || list.isEmpty()) {
            return true;
        }
		Class<?> clazz = null;
		for(T t : list) {
			if (t == null) {
				continue;
			}
			if(clazz == null) {
				clazz = t.getClass();
			} else {
				if (clazz != t.getClass()) {
					return false;
				}
			}
		}
		return true;
	}
	
}
