package com.pugwoo.dbhelper.impl.part;


import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.DBHelperSlowSqlCallback;
import com.pugwoo.dbhelper.DBHelperSqlCallback;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.sql.SQLAssemblyUtils;
import com.pugwoo.dbhelper.utils.AnnotationSupportRowMapper;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;
import com.pugwoo.dbhelper.utils.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * jdbcTemplate原生操作接口封装
 * @author NICK
 */
public abstract class P0_JdbcTemplateOp implements DBHelper, ApplicationContextAware {

	protected static final Logger LOGGER = LoggerFactory.getLogger(SpringJdbcDBHelper.class);

	protected JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	/**数据库类型，从jdbcTemplate的url解析得到；当它为null时，表示未初始化*/
	private DatabaseTypeEnum databaseType;
	protected long timeoutWarningValve = 1000;
	/**每页最大个数，为null表示不限制*/
	protected Integer maxPageSize = null;
	/**Stream流式获取数据的fetchSize大小，默认1000（一般jdbc各数据库驱动的默认值是10，过小了）*/
	protected int fetchSize = 1000;

	protected ApplicationContext applicationContext;

	protected List<DBHelperInterceptor> interceptors = new ArrayList<>();

	protected Map<FeatureEnum, Boolean> features = new ConcurrentHashMap<FeatureEnum, Boolean>() {{
		put(FeatureEnum.AUTO_SUM_NULL_TO_ZERO, true);
		put(FeatureEnum.LOG_SQL_AT_INFO_LEVEL, false);
		put(FeatureEnum.THROW_EXCEPTION_IF_COLUMN_NOT_EXIST, false);
		put(FeatureEnum.AUTO_ADD_ORDER_FOR_PAGINATION, true);
		put(FeatureEnum.AUTO_EXPLAIN_SLOW_SQL, true);
		put(FeatureEnum.LAZY_DETECT_DATABASE_TYPE, true);
	}};

	private DBHelperSqlCallback sqlCallback;
	private DBHelperSlowSqlCallback slowSqlCallback;

    /**
     * 批量和非批量的log
     *
     * @param sql 要log的sql
     * @param batchSize 如果大于0，则是批量log方式
     * @param args 参数
     */
    protected void log(String sql, int batchSize, List<Object> args) {
		try {
			if (sqlCallback != null) {
				sqlCallback.beforeExecute(sql, args, batchSize);
			}
		} catch (Throwable e) {
			LOGGER.error("DBHelperSqlCallback call beforeExecute fail, sql:{}; params:{}",
					sql, NimbleOrmJSON.toJsonNoException(args), e);
		}

        if (batchSize > 0) { // 批量log
            if (features.get(FeatureEnum.LOG_SQL_AT_INFO_LEVEL)) {
				if (LOGGER.isInfoEnabled()) {
					String firstCallMethodStr = getFirstCallMethodStr();
					String assembledSql = getAssembledSql(sql, args);
					if (assembledSql != null) {
						LOGGER.info("{} Batch ExecSQL(totalRows:{}):{}",
								firstCallMethodStr, batchSize, assembledSql);
					} else {
						LOGGER.info("{} Batch ExecSQL(totalRows:{}):{}; first row params:{}",
								firstCallMethodStr, batchSize, sql, NimbleOrmJSON.toJsonNoException(args));
					}
				}
            } else {
				if (LOGGER.isDebugEnabled()) {
					String firstCallMethodStr = getFirstCallMethodStr();
					String assembledSql = getAssembledSql(sql, args);
					if (assembledSql != null) {
						LOGGER.debug("{} Batch ExecSQL(totalRows:{}):{}",
								firstCallMethodStr, batchSize, assembledSql);
					} else {
						LOGGER.debug("{} Batch ExecSQL(totalRows:{}):{}; first row params:{}",
								firstCallMethodStr, batchSize, sql, NimbleOrmJSON.toJsonNoException(args));
					}
				}
            }
        } else {
            if (features.get(FeatureEnum.LOG_SQL_AT_INFO_LEVEL)) {
				if (LOGGER.isInfoEnabled()) {
					String firstCallMethodStr = getFirstCallMethodStr();
					String assembledSql = getAssembledSql(sql, args);
					if (assembledSql != null) {
						LOGGER.info("{} ExecSQL:{}", firstCallMethodStr, assembledSql);
					} else {
						LOGGER.info("{} ExecSQL:{}; params:{}", firstCallMethodStr, sql, NimbleOrmJSON.toJsonNoException(args));
					}
				}
            } else {
				if (LOGGER.isDebugEnabled()) {
					String firstCallMethodStr = getFirstCallMethodStr();
					String assembledSql = getAssembledSql(sql, args);
					if (assembledSql != null) {
						LOGGER.debug("{} ExecSQL:{}", firstCallMethodStr, assembledSql);
					} else {
						LOGGER.debug("{} ExecSQL:{}; params:{}", firstCallMethodStr, sql, NimbleOrmJSON.toJsonNoException(args));
					}
				}
			}
		}
	}

    /**
     * 记录慢sql请求
     *
     * @param cost 请求耗时，毫秒
     * @param sql 要记录的sql
     * @param batchSize 批量大小，如果大于0，则是批量
     * @param args 参数
     */
    protected void logSlow(long cost, String sql, int batchSize, List<Object> args) {
		try {
			if (sqlCallback != null) {
				sqlCallback.afterExecute(cost, sql, args, batchSize);
			}
		} catch (Throwable e) {
			LOGGER.error("DBHelperSqlCallback call afterExecute fail, sql:{}; params:{}",
					sql, NimbleOrmJSON.toJsonNoException(args), e);
		}

        if (cost > timeoutWarningValve) {
            String firstCallMethodStr = getFirstCallMethodStr();
            if (batchSize > 0) {
				String assembledSql = getAssembledSql(sql, args);
				if (assembledSql != null) {
					LOGGER.warn("{} SlowSQL(cost:{}ms) Batch(totalRows:{}):{}",
							firstCallMethodStr, cost, batchSize, assembledSql);
				} else {
					LOGGER.warn("{} SlowSQL(cost:{}ms) Batch(totalRows:{}):{}, params:{}",
							firstCallMethodStr, cost, batchSize, sql, NimbleOrmJSON.toJsonNoException(args));
				}

                try {
                    if (slowSqlCallback != null) {
						slowSqlCallback.callback(cost, sql, args, batchSize);
                    }
                } catch (Throwable e) {
                    LOGGER.error("DBHelperSlowSqlCallback fail, SlowSQL:{}; cost:{}ms, listSize:{}, params:{}",
                            sql, cost, batchSize, NimbleOrmJSON.toJsonNoException(args), e);
                }
            } else {
				String assembledSql = getAssembledSql(sql, args);
				if (assembledSql != null) {
					LOGGER.warn("{} SlowSQL(cost:{}ms):{}", firstCallMethodStr, cost, assembledSql);
				} else {
					LOGGER.warn("{} SlowSQL(cost:{}ms):{}; params:{}", firstCallMethodStr, cost, sql, NimbleOrmJSON.toJsonNoException(args));
				}

                try {
                    if (slowSqlCallback != null) {
						slowSqlCallback.callback(cost, sql, args, 0);
                    }
                } catch (Throwable e) {
                    LOGGER.error("DBHelperSlowSqlCallback fail, SlowSQL:{}; cost:{}ms, params:{}",
                            sql, cost, NimbleOrmJSON.toJsonNoException(args), e);
                }

				// 对于非batch的慢sql，自动explain一下检查是否加了索引
				boolean autoExplainSlowSql = getFeature(FeatureEnum.AUTO_EXPLAIN_SLOW_SQL);
				if (autoExplainSlowSql && getDatabaseType() == DatabaseTypeEnum.MYSQL) {
					try {
						if (assembledSql != null) {
							String explainSql = "EXPLAIN " + assembledSql;
							List<Map<String, Object>> explainResult = jdbcTemplate.queryForList(explainSql);
							LOGGER.warn("Explain SlowSQL(cost:{}ms):{}; explain result:{}",
									cost, assembledSql, NimbleOrmJSON.toJsonNoException(explainResult));
						}
					} catch (Throwable e) {
						LOGGER.error("SlowSQL explain fail, SlowSQL:{}; cost:{}ms, params:{}",
								sql, cost, NimbleOrmJSON.toJsonNoException(args), e);
					}
				}
			}
		}
	}

	@Override
	public void rollback() {
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	}

	@Override
	public boolean executeAfterCommit(final Runnable runnable) {
		if(runnable == null) {
            return false;
        }
		if(!TransactionSynchronizationManager.isActualTransactionActive()) {
            return false;
        }
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				runnable.run();
			}
		});
		return true;
	}

	protected <T> List<T> namedJdbcQuery(String sql, List<Object> argsList, AnnotationSupportRowMapper<T> mapper) {
		sql = addComment(sql);
		log(sql, 0, argsList);
		long start = System.currentTimeMillis();
		List<T> list = namedParameterJdbcTemplate.query(NamedParameterUtils.trans(sql, argsList),
				NamedParameterUtils.transParam(argsList), mapper);
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, 0, argsList);
		return list;
	}

	protected <T> List<T> namedJdbcQuery(String sql, Map<String, ?> argsMap, RowMapper<T> mapper) {
		List<Object> argsList = InnerCommonUtils.newList(argsMap);
		sql = addComment(sql);
		log(sql, 0, argsList);
		long start = System.currentTimeMillis();
		NamedParameterUtils.preHandleParams(argsMap);
		List<T> list = namedParameterJdbcTemplate.query(sql, argsMap, mapper);
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, 0, argsList);
		return list;
	}

	protected <T> T namedJdbcQueryForObject(Class<T> clazz, String sql, List<Object> argsList) {
		sql = addComment(sql);
		log(sql, 0, argsList);
		long start = System.currentTimeMillis();
		T t = namedParameterJdbcTemplate.queryForObject(NamedParameterUtils.trans(sql, argsList),
				NamedParameterUtils.transParam(argsList), clazz);
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, 0, argsList);
		return t;
	}

	protected <T> Stream<T> namedJdbcQueryForStream(String sql, List<Object> argsList, AnnotationSupportRowMapper<T> mapper) {
		sql = addComment(sql);
		log(sql, 0, argsList);
		long start = System.currentTimeMillis();
		jdbcTemplate.setFetchSize(fetchSize);
		Stream<T> list = namedParameterJdbcTemplate.queryForStream(NamedParameterUtils.trans(sql, argsList),
				NamedParameterUtils.transParam(argsList), mapper);
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, 0, argsList);
		return list;
	}

	protected <T> Stream<T> namedJdbcQueryForStream(String sql, Map<String, ?> argsMap, RowMapper<T> mapper) {
		List<Object> argsList = InnerCommonUtils.newList(argsMap);
		sql = addComment(sql);
		log(sql, 0, argsList);
		long start = System.currentTimeMillis();
		NamedParameterUtils.preHandleParams(argsMap);
		jdbcTemplate.setFetchSize(fetchSize);
		Stream<T> stream = namedParameterJdbcTemplate.queryForStream(sql, argsMap, mapper);
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, 0, argsList);
		return stream;
	}

	/**
	 * 使用namedParameterJdbcTemplate模版执行update，支持in(?)表达式
	 */
	protected int namedJdbcExecuteUpdate(String sql, Object... args) {
		sql = addComment(sql);
		List<Object> argsList = InnerCommonUtils.arrayToList(args);
		log(sql, 0, argsList);
		long start = System.currentTimeMillis();
		int rows = namedParameterJdbcTemplate.update(NamedParameterUtils.trans(sql, argsList),
				NamedParameterUtils.transParam(argsList)); // 因为有in (?) 所以使用namedParameterJdbcTemplate
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, 0, argsList);
		return rows;
	}

	protected int namedJdbcExecuteUpdate(String sql, Map<String, ?> argsMap) {
		sql = addComment(sql);
		log(sql, 0, InnerCommonUtils.newList(argsMap));
		long start = System.currentTimeMillis();
		int rows = namedParameterJdbcTemplate.update(sql, argsMap);
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, 0, InnerCommonUtils.newList(argsMap));
		return rows;
	}

	/**
	 * 使用namedParameterJdbcTemplate模版执行update，支持in(?)表达式
	 * @param logSql 用于日志打印的sql
	 * @param batchSize 批量修改的大小，当它的值大于0时，切换为batchLog方式log
	 * @param logArgs 用于日志打印的参数
	 */
	protected int namedJdbcExecuteUpdate(String sql, String logSql, int batchSize, List<Object> logArgs,
										 Object... args) {
		sql = addComment(sql);
		log(logSql, batchSize, logArgs);
		long start = System.currentTimeMillis();
		List<Object> argsList = InnerCommonUtils.arrayToList(args);
		int rows = namedParameterJdbcTemplate.update(NamedParameterUtils.trans(sql, argsList),
				NamedParameterUtils.transParam(argsList)); // 因为有in (?) 所以使用namedParameterJdbcTemplate
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, logSql, batchSize, logArgs);
		return rows;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		if (jdbcTemplate == null) {
			return;
		}
		this.jdbcTemplate = jdbcTemplate;
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		if (!getFeature(FeatureEnum.LAZY_DETECT_DATABASE_TYPE)) {
			this.databaseType = getDatabaseType(jdbcTemplate);
		}
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate, DatabaseTypeEnum databaseType) {
		if (databaseType == null) {
			setJdbcTemplate(jdbcTemplate);
		} else {
			if (jdbcTemplate == null) {
				return;
			}
			this.jdbcTemplate = jdbcTemplate;
			this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
			this.databaseType = databaseType;
		}
	}

	/**
	 * 可选，非必须
	 * since 1.2
	 */
	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return this.namedParameterJdbcTemplate;
	}

	@Override
	public void setSlowSqlWarningValve(long timeMS) {
		timeoutWarningValve = timeMS;
	}

	@Override
	public void setMaxPageSize(int maxPageSize) {
		this.maxPageSize = maxPageSize;
	}

	@Override
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	@Override
	public void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		SpringContext.setApplicationContext(applicationContext);
	}

	@Override
	public void setInterceptors(List<DBHelperInterceptor> interceptors) {
		if(interceptors != null) {
			// 移除为null的值
			for(DBHelperInterceptor interceptor : interceptors) {
				if(interceptor != null) {
					this.interceptors.add(interceptor);
				}
			}
		}
	}

	@Override
	public void setSlowSqlWarningCallback(DBHelperSlowSqlCallback callback) {
		this.slowSqlCallback = callback;
	}

	@Override
	public void setSqlCallback(DBHelperSqlCallback callback) {
		this.sqlCallback = callback;
	}

	@Override
	public void turnOnFeature(FeatureEnum featureEnum) {
		features.put(featureEnum, true);
	}

	@Override
	public void turnOffFeature(FeatureEnum featureEnum) {
		features.put(featureEnum, false);
	}

	public boolean getFeature(FeatureEnum featureEnum) {
		Boolean enabled = features.get(featureEnum);
		return enabled != null && enabled;
	}

	/**
	 * 给sql加上注释，返回加完注释之后的sql
	 * @param sql not null
	 */
	protected String addComment(String sql) {
		String globalComment = DBHelperContext.getGlobalComment();
		if (InnerCommonUtils.isNotBlank(globalComment)) {
			sql = "/*" + globalComment + "*/" + sql;
		}
		String comment = DBHelperContext.getThreadLocalComment();
		if (InnerCommonUtils.isNotBlank(comment)) {
			sql = "/*" + comment + "*/" + sql;
		}
		return sql;
	}

	private DatabaseTypeEnum getDatabaseType(JdbcTemplate jdbcTemplate) {
		DataSource dataSource = jdbcTemplate.getDataSource();
		if (dataSource == null) {
			LOGGER.error("fail to get database type from jdbc url, dataSource is null, jdbcTemplate:{}, will try later", jdbcTemplate);
			return null;
		}

		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			String url = connection.getMetaData().getURL();
			String type = url.split(":")[1];
			return DatabaseTypeEnum.getByJdbcProtocol(type);
		} catch (Exception e) {
			LOGGER.error("fail to get database type from jdbc url, jdbcTemplate:{}, will try later", jdbcTemplate, e);
			return null;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					LOGGER.error("fail to close connection, jdbcTemplate:{}, ignored", jdbcTemplate, e);
				}
			}
		}
	}

	@Override
	public DatabaseTypeEnum getDatabaseType() {
		if (databaseType == null) {
			databaseType = getDatabaseType(jdbcTemplate); // 尝试再次获取
		}
		return databaseType;
	}

	private String getFirstCallMethodStr() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement st : stackTrace) {
			String className = st.getClassName();
			if (className.startsWith("java.") || className.startsWith("org.springframework.")) {
				continue;
			}
			if (className.startsWith("com.pugwoo.dbhelper.") && !className.startsWith("com.pugwoo.dbhelper.test")) {
				continue;
			}
			return "(" + st.getFileName() + ":" + st.getLineNumber() + ")";
		}
		return "";
	}

	/**当处理失败时返回null*/
	private String getAssembledSql(String sql, List<Object> params) {
		try {
			if (params == null) {
				return sql;
			}
			return SQLAssemblyUtils.assembleSql(sql, params.toArray());
		} catch (Exception e) {
			LOGGER.error("fail to assemble sql, sql:{}, params:{}", sql, NimbleOrmJSON.toJsonNoException(params), e);
			return null;
		}
	}
}
