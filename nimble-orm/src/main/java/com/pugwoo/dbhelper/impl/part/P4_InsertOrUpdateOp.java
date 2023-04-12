package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class P4_InsertOrUpdateOp extends P3_UpdateOp {

	@Override
	public <T> int insertOrUpdate(T t) {
		if (t == null) {
			return 0;
		}

		if (isWithKey(t)) {
			return update(t);
		} else {
			return insert(t);
		}
	}
	
	@Override
	public <T> int insertOrUpdateWithNull(T t) {
		if (t == null) {
			return 0;
		}

		if (isWithKey(t)) {
			return updateWithNull(t);
		} else {
			return insertWithNull(t);
		}
	}
	
	@Override
	public <T> int insertOrUpdate(Collection<T> list) {
		if (InnerCommonUtils.isEmpty(list)) {
			return 0;
		}

		// 将insert和update拆开进行调用，更好的利用批量能力
		List<T> toInsert = new ArrayList<>();
		List<T> toUpdate = new ArrayList<>();

		for(T t : list) {
			if(t != null) {
				if(isWithKey(t)) {
					toUpdate.add(t);
				} else {
					toInsert.add(t);
				}
			}
		}

		int rows = 0;
		rows += insert(toInsert);
		rows += update(toUpdate);
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
