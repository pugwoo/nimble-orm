package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.utils.DOInfoReader;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public abstract class P4_InsertOrUpdateOp extends P3_UpdateOp {

	@Override
	public <T> int insertOrUpdate(T t) {
		if(t == null) {
			return 0;
		}

		if(isWithKey(t)) {
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

		if(isWithKey(t)) {
			return updateWithNull(t);
		} else {
			return insertWithNull(t);
		}
	}
	
	@Override
	public <T> int insertOrUpdate(Collection<T> list) {
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

	/**判断对象是否有主键值，必须全部有才返回true*/
	private <T> boolean isWithKey(T t) {
		List<Field> keyFields = DOInfoReader.getKeyColumnsNoThrowsException(t.getClass());
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
