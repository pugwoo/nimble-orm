package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.PreHandleObject;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class P2_InsertOp extends P1_QueryOp {
	
	//////// 拦截器 BEGIN
	private void doInterceptBeforeInsert(Object t) {
		List<Object> list = new ArrayList<>();
		list.add(t);
		doInterceptBeforeInsertList(list);
	}
	private void doInterceptBeforeInsertList(List<Object> list) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue = interceptor.beforeInsert(list);
			if (!isContinue) {
				throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	
	private void doInterceptAfterInsert(Object t, int rows) {
		List<Object> list = new ArrayList<>();
		list.add(t);
		doInterceptAfterInsertList(list, rows);
	}
	private void doInterceptAfterInsertList(final List<Object> list, final int rows) {
		Runnable runnable = () -> {
			for (int i = interceptors.size() - 1; i >= 0; i--) {
				interceptors.get(i).afterInsert(list, rows);
			}
		};
		if(!executeAfterCommit(runnable)) {
			runnable.run();
		}
	}
	/////// 拦截器 END

	@Override
	public <T> int insert(T t) {
		return insert(t, false, true);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int insert(Collection<?> list) {
		if(list == null || list.isEmpty()) {
			return 0;
		}

		if (list instanceof List) {
			doInterceptBeforeInsertList((List<Object>) list);
		} else {
			doInterceptBeforeInsertList(new ArrayList<>(list));
		}

		int sum = 0;
		for(Object obj : list) {
			sum += insert(obj, false, false);
		}

		if (list instanceof List) {
			doInterceptAfterInsertList((List<Object>)list, sum);
		} else {
			doInterceptAfterInsertList(new ArrayList<>(list), sum);
		}

		return sum;
	}

	@Override
	public <T> int insertBatchWithoutReturnId(Collection<T> list) {
		if(list == null || list.isEmpty()) {
			return 0;
		}
		SQLAssert.allSameClass(list);

		for (T t : list) {
			PreHandleObject.preHandleInsert(t);
		}

		if (list instanceof List) {
			doInterceptBeforeInsertList((List<Object>) list);
		} else {
			doInterceptBeforeInsertList(new ArrayList<>(list));
		}

		List<Object[]> values = new ArrayList<>();
		String sql = SQLUtils.getInsertSQLForBatch(list, values);
		sql = addComment(sql);
		log(sql, values);

		final long start = System.currentTimeMillis();

		int[] rows = jdbcTemplate.batchUpdate(sql, values);

		int total = 0;
		for (int row : rows) {
			int result = row;
			if (row == -2) {
				result = 1; // -2 means Statement.SUCCESS_NO_INFO
			} else if (row < 0) {
				result = 0; // not success
			}
			total += result;
		}

		long cost = System.currentTimeMillis() - start;
		logSlowForBatch(cost, sql, list.size());

		if (list instanceof List) {
			doInterceptAfterInsertList((List<Object>)list, total);
		} else {
			doInterceptAfterInsertList(new ArrayList<>(list), total);
		}

		return total;
	}

	@Override
	public <T> int insertWithNull(T t) {
		return insert(t, true, true);
	}
	
	private <T> int insert(T t, boolean isWithNullValue, boolean withInterceptor) {
		PreHandleObject.preHandleInsert(t);
		
		final List<Object> values = new ArrayList<>();
		
		if(withInterceptor) {
			doInterceptBeforeInsert(t);
		}
		
		String sql1 = SQLUtils.getInsertSQL(t, values, isWithNullValue);
		final String sql = addComment(sql1);
		log(sql, values);
		
		final long start = System.currentTimeMillis();

		int rows;
		Field autoIncrementField = DOInfoReader.getAutoIncrementField(t.getClass());
		if (autoIncrementField != null) {
			GeneratedKeyHolder holder = new GeneratedKeyHolder();
			rows = jdbcTemplate.update(con -> {
				PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				for (int i = 0; i < values.size(); i++) {
					statement.setObject(i + 1, values.get(i));
				}
				return statement;
			}, holder);

			if(rows > 0) {
				Number key = holder.getKey();
				if (key == null) {
					DOInfoReader.setValue(autoIncrementField, t, null);
				} else {
					long primaryKey = key.longValue();
					DOInfoReader.setValue(autoIncrementField, t, primaryKey);
				}
			}

		} else {
			rows = jdbcTemplate.update(sql, values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		}

		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, values);
		
		if(withInterceptor) {
			doInterceptAfterInsert(t, rows);
		}
		return rows;
	}

}
