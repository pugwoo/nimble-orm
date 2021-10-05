package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.MustProvideConstructorException;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.PreHandleObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class P5_DeleteOp extends P4_InsertOrUpdateOp {
	
	/////// 拦截器
	protected void doInterceptBeforeDelete(List<Object> tList) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeDelete(tList);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}

	protected void doInterceptAfterDelete(final List<Object> tList, final int rows) {
		Runnable runnable = () -> {
			for (int i = interceptors.size() - 1; i >= 0; i--) {
				interceptors.get(i).afterDelete(tList, rows);
			}
		};
		if(!executeAfterCommit(runnable)) {
			runnable.run();
		}
	}

	///////////

	@Override
	public <T> int deleteByKey(T t) throws NullKeyValueException {
		PreHandleObject.preHandleDelete(t);

		Field softDelete = DOInfoReader.getSoftDeleteColumn(t.getClass());
		
		List<Object> values = new ArrayList<>();

		List<Object> tList = new ArrayList<>();
		tList.add(t);
		doInterceptBeforeDelete(tList);
		
		String sql;
		
		if(softDelete == null) { // 物理删除
			sql = SQLUtils.getDeleteSQL(t, values);
		} else { // 软删除
			// 对于软删除，当有拦截器时，可能使用者会修改数据以记录删除时间或删除人信息等，此时要先update该条数据
			if(interceptors != null && !interceptors.isEmpty()) {
				updateForDelete(t);
			}
			Column softDeleteColumn = softDelete.getAnnotation(Column.class);
			sql = SQLUtils.getSoftDeleteSQL(t, softDeleteColumn, values);
		}
		
		int rows = jdbcExecuteUpdate(sql, values.toArray()); // 不会有in(?)表达式

		doInterceptAfterDelete(tList, rows);
		
		return rows;
	}
	
	@Override
	public <T> int deleteByKey(List<T> list) throws NullKeyValueException {
		if(list == null || list.isEmpty()) {
            return 0;
        }

		boolean batchDelete = false; // 当所有list都是一个类型，且key是1个，且没有用到deleteValueScript时
		Field keyField = null;
		if(SQLAssert.isAllSameClass(list)) {
			List<Field> keyFields = DOInfoReader.getKeyColumns(list.get(0).getClass());
			if(keyFields.size() == 1) {
				keyField = keyFields.get(0);

				boolean isUseDeleteValueScript = false;
                List<Field> fields = DOInfoReader.getColumns(list.get(0).getClass());
                for(Field field : fields) {
                    Column column = field.getAnnotation(Column.class);
                    String deleteValueScript = column.deleteValueScript().trim();
                    if(!deleteValueScript.isEmpty()) {
                        isUseDeleteValueScript = true;
                        break;
                    }
                }

                if(!isUseDeleteValueScript) {
                    batchDelete = true;
                }
			}
		}
		
		if(batchDelete) {

            for(T t : list) {
                PreHandleObject.preHandleDelete(t);
            }

			Class<?> clazz = list.get(0).getClass();
			List<Object> keys = new ArrayList<>();
			for(T t : list) {
				Object key = DOInfoReader.getValue(keyField, t);
				if(key != null) {
                    keys.add(key);
                }
			}

			List<Object> listTmp = new ArrayList<>(list);
			doInterceptBeforeDelete(listTmp);
			
			Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除
			
			String sql;
			String where = "where `" + keyField.getAnnotation(Column.class).value() + "` in (?)";
			if(softDelete == null) { // 物理删除
				sql = SQLUtils.getCustomDeleteSQL(clazz, where);
			} else { // 软删除
				sql = SQLUtils.getCustomSoftDeleteSQL(clazz, where, softDelete);
			}
			
			int rows = namedJdbcExecuteUpdate(sql, keys);
			
			doInterceptAfterDelete(listTmp, rows);
			return rows;
		} else {
			int rows = 0;
			for(T t : list) {
				rows += deleteByKey(t); // deleteByKey中已经做了preHandleDelete
			}
			return rows;
		}
	}
		
	@Override
	public <T> int deleteByKey(Class<T> clazz, Object keyValue) 
			throws NullKeyValueException, MustProvideConstructorException {
		if(keyValue == null) {
			throw new NullKeyValueException();
		}

		Field keyField = DOInfoReader.getOneKeyColumn(clazz);
		
		try {
			T t = clazz.newInstance();
			DOInfoReader.setValue(keyField, t, keyValue);
			return deleteByKey(t);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new MustProvideConstructorException();
		}
	}
	
	@Override
	public <T> int delete(Class<T> clazz, String postSql, Object... args) {
		if(postSql != null) {postSql = postSql.replace('\t', ' ');}
		if(postSql == null || postSql.trim().isEmpty()) { // warning: very dangerous
			// 不支持缺省条件来删除。如果需要全表删除，请明确传入where 1=1
			throw new InvalidParameterException("delete postSql is blank. it's very dangerous"); 
		}

		Field softDelete = DOInfoReader.getSoftDeleteColumn(clazz); // 支持软删除
		String sql;
		
		if(interceptors == null || interceptors.isEmpty()) { // 没有配置拦截器，则直接删除
			if(softDelete == null) { // 物理删除
				sql = SQLUtils.getCustomDeleteSQL(clazz, postSql);
			} else { // 软删除
				sql = SQLUtils.getCustomSoftDeleteSQL(clazz, postSql, softDelete);
			}
			return namedJdbcExecuteUpdate(sql, args);
		} else { // 配置了拦截器，则先查出key，再删除
			List<T> allKey = getAllKey(clazz, postSql, args);
			return deleteByKey(allKey);
		}
	}

	////////
	private <T> void updateForDelete(T t) throws NullKeyValueException {

		// 这个if是肯定不会进去的，因为updateForDelete只对软删除有用，此时非key column肯定不会为空
		//if(DOInfoReader.getNotKeyColumns(t.getClass()).isEmpty()) {
		//	return 0; // not need to update
		//}

		List<Object> tList = new ArrayList<>();
		tList.add(t);

		List<Object> values = new ArrayList<>();
		String sql = SQLUtils.getUpdateSQL(t, values, false, null);

		if (sql != null) {
			namedJdbcExecuteUpdate(sql, values.toArray());
		}

	}
}
