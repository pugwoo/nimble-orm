package com.pugwoo.dbhelper.impl.part;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.pugwoo.dbhelper.utils.DOInfoReader;

public abstract class P4_InsertOrUpdateOp extends P3_UpdateOp {

	@Override
	public <T> int insertOrUpdate(T t) {
		if(t == null) {
			return 0;
		}
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		if(isWithKey(t, fields)) {
			return update(t);
		} else {
			return insert(t);
		}
	}
	
	@Override
	public <T> int insertOrUpdateWithNull(T t) {
		if(t == null) {
			return 0;
		}
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		if(isWithKey(t, fields)) {
			return updateWithNull(t);
		} else {
			return insertWithNull(t);
		}
	}
	
	@Override @Transactional
	public <T> int insertOrUpdate(List<T> list) {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		int rows = 0;
		for(T t : list) {
			if(t != null) {
				rows += insertOrUpdate(t);
			}
		}
		return rows;
	}
	
	@Override @Transactional
	public <T> int insertOrUpdateWithNull(List<T> list) {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		int rows = 0;
		for(T t : list) {
			if(t != null) {
				rows += insertOrUpdateWithNull(t);
			}
		}
		return rows;
	}
	
	@Override @Transactional
	public <T> int insertOrUpdateFull(List<T> dbList, List<T> newList) {
		return insertOrUpdateFull(dbList, newList, false);
	}
	
	@Override @Transactional
	public <T> int insertOrUpdateFullWithNull(List<T> dbList, List<T> newList) {
		return insertOrUpdateFull(dbList, newList, true);
	}
	
	private <T> int insertOrUpdateFull(List<T> dbList, List<T> newList, boolean withNull) {
		if(newList == null) {
			return 0;
		}
		if((dbList == null || dbList != null && dbList.isEmpty()) && newList.isEmpty()) {
			return 0; // 不需要处理了
		}
		
		List<Field> fields = DOInfoReader.getColumns(
				dbList != null && !dbList.isEmpty() ? dbList.get(0).getClass()
						: newList.get(0).getClass());
		
		// 1. dbList中有key的，但是newList中没有的，删除掉
		for(T t1 : dbList) {
			if(isWithKey(t1, fields)) {
				boolean isNewExists = false;
				for(T t2 : newList) {
					if(isKeyEquals(t1, t2, fields)) {
						isNewExists = true;
						break;
					}
				}
				if(!isNewExists) {
					deleteByKey(t1);
				}
			}
		}
		
		// 2. insert or update new list
		return withNull ? insertOrUpdateWithNull(newList) : insertOrUpdate(newList);
	}
	
	/**
	 * 判断两个对象的key是否相等,只有都存在所有的key，且key（如多个，则每个）都相等，才返回true
	 * @param t1
	 * @param t2
	 * @param fields
	 * @return
	 */
	private <T> boolean isKeyEquals(T t1, T t2, List<Field> fields) {
		if(t1 == null || t2 == null || fields == null) {
			 return false;
		}
		List<Field> keyFields = DOInfoReader.getKeyColumns(t1.getClass());
		if(keyFields == null || keyFields.isEmpty()) {
			return false;
		}
		for(Field keyField : keyFields) {
			Object key1 = DOInfoReader.getValue(keyField, t1);
			Object key2 = DOInfoReader.getValue(keyField, t2);
			if(key1 == null || key2 == null || !key1.equals(key2)) {
				return false;
			}
		}
		return true;
	}
	
	/**判断对象是否有主键值，必须全部有才返回true*/
	private <T> boolean isWithKey(T t, List<Field> fields) {
		if(t == null || fields == null || fields.isEmpty()) {
			return false;
		}
		
		List<Field> keyFields = DOInfoReader.getKeyColumns(t.getClass());
		if(keyFields.isEmpty()) {
			return false;
		}
		
		for(Field keyField : keyFields) {
			if(DOInfoReader.getValue(keyField, t) == null) {
				return false;
			}
		}
		return true;
	}
}
