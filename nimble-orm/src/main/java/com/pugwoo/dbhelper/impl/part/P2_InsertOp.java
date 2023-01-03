package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.sql.InsertSQLForBatchDTO;
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
	private void doInterceptBeforeInsertList(Collection<?> list) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue;
			if (list instanceof List) {
				isContinue = interceptor.beforeInsert((List<Object>) list);
			} else {
				isContinue = interceptor.beforeInsert(new ArrayList<>(list));
			}
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
	private void doInterceptAfterInsertList(final Collection<?> list, final int rows) {
		Runnable runnable = () -> {
			for (int i = interceptors.size() - 1; i >= 0; i--) {
				if (list instanceof List) {
					interceptors.get(i).afterInsert((List<Object>)list, rows);
				} else {
					interceptors.get(i).afterInsert(new ArrayList<>(list), rows);
				}
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

		doInterceptBeforeInsertList(list);

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
		/*
		 * 批量插入的主要优化点：
		 * 1）直接使用sql insert values的多个values作为批量插入方式，因为对于null要使用default关键字，
		 *    使用prepare statement的批量插入方式，使用不了default关键字。
		 *    同时，这样做的好处可以不用要求mysql的链接参数加上rewriteBatchedStatements=TRUE
		 * 2）对于批量插入全是null的字段，不需要在insert列里出现，此项节省约10%~50%的插入时间（取决于null值的列的数量）
		 * 3）为了避免sql注入风险，参数还是用?的方式表达，此项对性能的影响比较有限，但安全性足够高，因此不再自己拼凑sql值
		 * 4）对于大量插入，log需要优化，不要打印太多的信息，否则性能会受到较大的影响。
		 */
		if (list == null || list.isEmpty()) {
			return 0;
		}
		SQLAssert.allSameClass(list);
		for (T t : list) {
			PreHandleObject.preHandleInsert(t);
		}
		doInterceptBeforeInsertList(list);

		List<Object> values = new ArrayList<>();
		InsertSQLForBatchDTO sqlDTO = SQLUtils.getInsertSQLForBatch(list, values);
		String sql = addComment(sqlDTO.getSql());
		String sqlForLog = sqlDTO.getSql().substring(0, sqlDTO.getSqlLogEndIndex());
		logForBatchInsert(sqlForLog, list.size(), values.subList(0, sqlDTO.getParamLogEndIndex()));

		long start = System.currentTimeMillis();
		int total = jdbcTemplate.update(sql, values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		long cost = System.currentTimeMillis() - start;
		logSlowForBatch(cost, sqlForLog, list.size());

		doInterceptAfterInsertList(list, total);
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
