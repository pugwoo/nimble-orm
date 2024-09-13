package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.exception.NotAllowModifyException;
import com.pugwoo.dbhelper.json.NimbleOrmDateUtils;
import com.pugwoo.dbhelper.sql.InsertSQLForBatchDTO;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.Generated;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.PreHandleObject;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class P2_InsertOp extends P1_QueryOp {
	
	//////// 拦截器 BEGIN
	private void doInterceptBeforeInsert(Object t) {
		List<Object> list = new ArrayList<>();
		list.add(t);
		doInterceptBeforeInsertList(list);
	}

	@SuppressWarnings("unchecked")
	private void doInterceptBeforeInsertList(Collection<?> list) {
		for (DBHelperInterceptor interceptor : interceptors) {
			boolean isContinue;
			if (list instanceof List) {
				isContinue = interceptor.beforeInsert((List<Object>) list);
			} else {
				isContinue = interceptor.beforeInsert(new ArrayList<>(list));
			}
			if (!isContinue) {
				throw new NotAllowModifyException("interceptor class:" + interceptor.getClass());
			}
		}
	}
	
	private void doInterceptAfterInsert(Object t, int rows) {
		List<Object> list = new ArrayList<>();
		list.add(t);
		doInterceptAfterInsertList(list, rows);
	}
	@SuppressWarnings("unchecked")
	private void doInterceptAfterInsertList(final Collection<?> list, final int rows) {
		if (InnerCommonUtils.isEmpty(interceptors)) {
			return; // 内部实现尽量不调用executeAfterCommit
		}
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
	
	@Override
	public int insert(Collection<?> list) {
		list = InnerCommonUtils.filterNonNull(list);
		if (InnerCommonUtils.isEmpty(list)) {
			return 0;
		}

		doInterceptBeforeInsertList(list);

		// 判断是否可以转成批量：所有的类相同，且插入主键有值
		boolean canBatchInsert = false;
		boolean isSameClass = SQLAssert.isAllSameClass(list);
		if (isSameClass && isAllHaveKeyValue(list)) {
			canBatchInsert = true;
		}

		int sum = 0;
		if (canBatchInsert) {
			sum = insertBatchWithoutReturnId(list, false);
		} else {
			for(Object obj : list) {
				sum += insert(obj, false, false);
			}
		}

		doInterceptAfterInsertList(list, sum);
		return sum;
	}

	@Override
	public <T> int insertBatchWithoutReturnId(Collection<T> list) {
		return insertBatchWithoutReturnId(list, true);
	}

	private <T> int insertBatchWithoutReturnId(Collection<T> list, boolean withInterceptor) {
		/*
		 * 批量插入的主要优化点：
		 * 1）直接使用sql insert values的多个values作为批量插入方式，因为对于null要使用default关键字，
		 *    使用prepare statement的批量插入方式，使用不了default关键字。
		 *    同时，这样做的好处可以不用要求mysql的链接参数加上rewriteBatchedStatements=TRUE
		 * 2）对于批量插入全是null的字段，不需要在insert列里出现，此项节省约10%~50%的插入时间（取决于null值的列的数量）
		 * 3）为了避免sql注入风险，参数还是用?的方式表达，此项对性能的影响比较有限，但安全性足够高，因此不再自己拼凑sql值
		 * 4）对于大量插入，log需要优化，不要打印太多的信息，否则性能会受到较大的影响。
		 *
		 * 说明：还有一种方式是多条insert values的代码通过批量的方式提交给数据库执行，这种方式不是真的批量，性能很差。
		 */
		list = InnerCommonUtils.filterNonNull(list);
		if (InnerCommonUtils.isEmpty(list)) {
			return 0;
		}

		SQLAssert.allSameClass(list);
		for (T t : list) {
			PreHandleObject.preHandleInsert(t);
		}
		if (withInterceptor) {
			doInterceptBeforeInsertList(list);
		}

		int total;
		DatabaseTypeEnum databaseType = getDatabaseType();
		/*
		  特别说明，clickhouse的批量插入不推荐使用insert into values()()()方式，原因：
		  1）这种方式不被clickhouse官方推荐
		  2）这种方式的性能不佳，插入速度不及jdbcTemplate.batchUpdate方式的1%
		  3）这种方式返回的affected不准确，不管是clickhouse 0.3或0.4的驱动，都不准确
		  <br>
		  但是，由于clickhouse的驱动不同版本差异比较大，主要差异为：
		  1）对于DDL非null字段，Java侧插入null值时，0.3版本不会报错，可以成功正确插入；而0.4版本会报错，因此对于非null字段，请确保Java侧的DO属性值不为null
		  2）对于clickhouse的UInt8类型，低版本可能无法用false来插入，请改成0
		 */
		if (databaseType == DatabaseTypeEnum.CLICKHOUSE) {
			List<Object[]> values = new ArrayList<>();
			String sql = SQLUtils.getInsertSQLForBatchForJDBCTemplate(getDatabaseType(), list, values);
			total = insertBatchJDBCTemplateMode(sql, values);
		} else {
			total = insertBatchDefaultModeWithAutoSplit(databaseType, list);
		}

		if (withInterceptor) {
			doInterceptAfterInsertList(list, total);
		}
		return total;
	}

	/**支持自动降级处理mysql max package太小的限制*/
	private <T> int insertBatchDefaultModeWithAutoSplit(DatabaseTypeEnum databaseType, Collection<T> list) {
		try {
			List<Object> values = new ArrayList<>();
			InsertSQLForBatchDTO sqlDTO = SQLUtils.getInsertSQLForBatch(databaseType, list, values);
            return insertBatchDefaultMode(sqlDTO, values, list.size());
		} catch (Exception e) {
			if (e.getCause() != null && "PacketTooBigException".equals(e.getCause().getClass().getSimpleName())) {
				// 对于因为mysql max package allowed限制的情况，降级为拆分并插入，给出error log
				LOGGER.error("Batch insert failed. Now splitting into smaller lists and retrying. You might consider increasing the max_allowed_packet or reducing the list size.", e);
				// 什么每批100个？因为降级后的首要任务是插入成功，而不再是性能；mysql 5.6/5.7默认4M，100个一批可以支持最大每条40K，覆盖绝大部分场景
				List<List<T>> partition = InnerCommonUtils.partition(list, 100);
				int total = 0;
				for (List<T> l : partition) {
					List<Object> values = new ArrayList<>();
					InsertSQLForBatchDTO sqlDTO = SQLUtils.getInsertSQLForBatch(databaseType, l, values);
					total += insertBatchDefaultMode(sqlDTO, values, l.size());
				}
				return total;
			} else {
				throw e;
			}
		}
	}

	private int insertBatchDefaultMode(InsertSQLForBatchDTO sqlDTO, List<Object> values, int listSize) {
		String sql = addComment(sqlDTO.getSql());
		String sqlForLog = sqlDTO.getSql().substring(0, sqlDTO.getSqlLogEndIndex());
		List<Object> paramForLog = values.subList(0, sqlDTO.getParamLogEndIndex());
		log(sqlForLog, listSize, paramForLog);

		long start = System.currentTimeMillis();
		int total = jdbcTemplate.update(sql, values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sqlForLog, listSize, paramForLog);
		return total;
	}

	private int insertBatchJDBCTemplateMode(String sql, List<Object[]> values) {
		sql = addComment(sql);
		List<Object> paramForLog = values.isEmpty() ? null : InnerCommonUtils.arrayToList(values.get(0));
		log(sql, values.size(), paramForLog);
		String sqlForLog = sql;

		long start = System.currentTimeMillis();
		int total = 0;
		int[] rows = jdbcTemplate.batchUpdate(sql, values);
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
		logSlow(cost, sqlForLog, values.size(), paramForLog);
		return total;
	}

	@Override
	public int insertBatchWithoutReturnId(String tableName, Collection<Map<String, Object>> list) {
		list = InnerCommonUtils.filterNonNull(list);
		if (InnerCommonUtils.isEmpty(list)) {
			return 0;
		}

		DatabaseTypeEnum databaseType = getDatabaseType();
		int total;
		if (databaseType == DatabaseTypeEnum.CLICKHOUSE) {
			List<Object[]> values = new ArrayList<>();
			String sql = SQLUtils.getInsertSQLForBatchForJDBCTemplate(databaseType, tableName, list, values);
			total = insertBatchJDBCTemplateMode(sql, values);
		} else {
			List<Object> values = new ArrayList<>();
			InsertSQLForBatchDTO sqlDTO = SQLUtils.getInsertSQLForBatch(databaseType, tableName, list, values);
			total = insertBatchDefaultMode(sqlDTO, values, list.size());
		}

		//	doInterceptAfterInsertList(list, total); // 不支持拦截器

		return total;
	}

	@Override
	public int insertBatchWithoutReturnId(String tableName, List<String> cols, Collection<Object[]> values) {
		List<Object[]> values2 = (List<Object[]>) InnerCommonUtils.filterNonNull(values);
		if (InnerCommonUtils.isEmpty(values2)) {
			return 0;
		}

		DatabaseTypeEnum databaseType = getDatabaseType();
		int total;
		if (databaseType == DatabaseTypeEnum.CLICKHOUSE) {
			String sql = SQLUtils.getInsertSQLForBatchForJDBCTemplate(databaseType, tableName, cols);
			total = insertBatchJDBCTemplateMode(sql, values2);
		} else {
			List<Object> values3 = new ArrayList<>();
			InsertSQLForBatchDTO sqlDTO = SQLUtils.getInsertSQLForBatch(databaseType, tableName, cols, values2, values3);
			total = insertBatchDefaultMode(sqlDTO, values3, values2.size());
		}

		// doInterceptAfterInsertList(values, total); // 不支持拦截器

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
		
		String sql1 = SQLUtils.getInsertSQL(getDatabaseType(), t, values, isWithNullValue);
		final String sql = addComment(sql1);
		log(sql, 0, values);
		
		final long start = System.currentTimeMillis();

		DatabaseTypeEnum databaseType = getDatabaseType();

		int rows;
		Field autoIncrementField = DOInfoReader.getAutoIncrementField(t.getClass());
		// 有自增注解且该字段值为null时才回查
		if (autoIncrementField != null && DOInfoReader.getValue(autoIncrementField, t) == null) {
			GeneratedKeyHolder holder = new GeneratedKeyHolder();
			rows = jdbcTemplate.update(con -> {
				PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				for (int i = 0; i < values.size(); i++) {
					if (databaseType == DatabaseTypeEnum.POSTGRESQL) {
						Object value = values.get(i);
						if (value != null) {
							// postgresql不支持java.util.Date，需要转换
							if (value instanceof java.util.Date) {
								value = NimbleOrmDateUtils.toLocalDateTime((java.util.Date) value);
							}
						}
						statement.setObject(i + 1, value);
					} else {
						statement.setObject(i + 1, values.get(i));
					}
				}
				return statement;
			}, holder);

			if(rows > 0) {
				if (databaseType == DatabaseTypeEnum.POSTGRESQL) {
					// 对于postgresql，会返回整个列的所有字段，所以这里需要取出来
					Map<String, Object> map = holder.getKeys();
					if (map != null) {
						Object key = map.get(autoIncrementField.getAnnotation(Column.class).value());
						logIfNull(key, autoIncrementField);
						if (key != null) {
							DOInfoReader.setValue(autoIncrementField, t, key);
						}
					}
				} else {
					Number key = holder.getKey();
					logIfNull(key, autoIncrementField);
					if (key != null)  {
						long primaryKey = key.longValue();
						DOInfoReader.setValue(autoIncrementField, t, primaryKey);
					}
				}
			}

		} else {
			rows = jdbcTemplate.update(sql, values.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式
		}

		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, 0, values);
		
		if(withInterceptor) {
			doInterceptAfterInsert(t, rows);
		}
		return rows;
	}

	/**抽取出来是不对这个log进行覆盖率统计*/
	@Generated
	private void logIfNull(Object key, Field autoIncrementField) {
		// 对于key为null的属于异常情况，仅log，不进行任何处理
		if (key == null) {
			LOGGER.error("get auto increment field:{} return null", autoIncrementField);
		}
	}

	/**
	 * 判断list里的元素是否【没有主键】或者【有主键且有值】，调用该方法之前需要确定list都是相同的class
	 */
	private boolean isAllHaveKeyValue(Collection<?> list) {
		Class<?> clazz = list.iterator().next().getClass();
		List<Field> fields = DOInfoReader.getKeyColumnsNoThrowsException(clazz);
		if (fields.isEmpty()) {
			return true; // 没有key也认为是
		}

		for (Object obj : list) {
			for (Field field : fields) {
				if (DOInfoReader.getValue(field, obj) == null) {
					return false;
				}
			}
		}

		return true;
	}
}
