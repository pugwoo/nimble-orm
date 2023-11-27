package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.IDBHelperSlowSqlCallback;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * jdbcTemplate原生操作接口封装
 * @author NICK
 */
public abstract class P0_JdbcTemplateOp implements DBHelper, ApplicationContextAware {

	protected static final Logger LOGGER = LoggerFactory.getLogger(SpringJdbcDBHelper.class);

	protected JdbcTemplate jdbcTemplate;
	private DatabaseTypeEnum databaseType; // 数据库类型，从jdbcTemplate的url解析得到；当它为null时，表示未初始化
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	protected long timeoutWarningValve = 1000;
	protected Integer maxPageSize = null; // 每页最大个数，为null表示不限制
	protected int fetchSize = 1000; // Stream流式获取数据的fetchSize大小，默认1000（一般jdbc各数据库驱动的默认值是10，过小了）

	protected ApplicationContext applicationContext;

	protected List<DBHelperInterceptor> interceptors = new ArrayList<>();

	protected Map<FeatureEnum, Boolean> features = new ConcurrentHashMap<FeatureEnum, Boolean>() {{
		put(FeatureEnum.AUTO_SUM_NULL_TO_ZERO, true);
		put(FeatureEnum.LOG_SQL_AT_INFO_LEVEL, false);
		put(FeatureEnum.THROW_EXCEPTION_IF_COLUMN_NOT_EXIST, false);
		put(FeatureEnum.AUTO_ADD_ORDER_FOR_PAGINATION, true);
		put(FeatureEnum.AUTO_EXPLAIN_SLOW_SQL, true);
	}};

	private IDBHelperSlowSqlCallback slowSqlCallback;

	/**
	 * 批量和非批量的log
	 * @param sql 要log的sql
	 * @param batchSize 如果大于0，则是批量log方式
	 * @param args 参数
	 */
	protected void log(String sql, int batchSize, Object args) {
		if (batchSize > 0) { // 批量log
			if (features.get(FeatureEnum.LOG_SQL_AT_INFO_LEVEL)) {
				LOGGER.info("Batch ExecSQL:{}; batch size:{}, first row params:{}",
						sql, batchSize, NimbleOrmJSON.toJson(args));
			} else {
				LOGGER.debug("Batch ExecSQL:{}; batch size:{}, first row params:{}",
						sql, batchSize, NimbleOrmJSON.toJson(args));
			}
		} else {
			if (features.get(FeatureEnum.LOG_SQL_AT_INFO_LEVEL)) {
				LOGGER.info("ExecSQL:{}; params:{}", sql, NimbleOrmJSON.toJson(args));
			} else {
				LOGGER.debug("ExecSQL:{}; params:{}", sql, NimbleOrmJSON.toJson(args));
			}
		}
	}

	/**
	 * 记录慢sql请求
	 * @param cost 请求耗时，毫秒
	 * @param sql 要记录的sql
	 * @param batchSize 批量大小，如果大于0，则是批量
	 * @param args 参数
	 */
	@SuppressWarnings("unchecked")
	protected void logSlow(long cost, String sql, int batchSize, Object args) {
		if(cost > timeoutWarningValve) {
			if (batchSize > 0) {
				LOGGER.warn("SlowSQL:{}; cost:{}ms, listSize:{}, params:{}", sql, cost,
						batchSize, NimbleOrmJSON.toJson(args));
				try {
					if(slowSqlCallback != null) {
						if (args instanceof List) {
							slowSqlCallback.callback(cost, sql, (List<Object>) args);
						} else if (args instanceof Object[]) {
							slowSqlCallback.callback(cost, sql, Arrays.asList((Object[]) args));
						} else {
							slowSqlCallback.callback(cost, sql, Collections.singletonList(args));
						}
					}
				} catch (Throwable e) {
					LOGGER.error("DBHelperSlowSqlCallback fail, SlowSQL:{}; cost:{}ms, listSize:{}, params:{}",
							sql, cost, batchSize, NimbleOrmJSON.toJson(args), e);
				}
			} else {
				LOGGER.warn("SlowSQL:{}; cost:{}ms, params:{}", sql, cost, NimbleOrmJSON.toJson(args));
				try {
					if(slowSqlCallback != null) {
						if (args instanceof List) {
							slowSqlCallback.callback(cost, sql, (List<Object>) args);
						} else if (args instanceof Object[]) {
							slowSqlCallback.callback(cost, sql, Arrays.asList((Object[]) args));
						} else {
							slowSqlCallback.callback(cost, sql, Collections.singletonList(args));
						}
					}
				} catch (Throwable e) {
					LOGGER.error("DBHelperSlowSqlCallback fail, SlowSQL:{}; cost:{}ms, params:{}",
							sql, cost, NimbleOrmJSON.toJson(args), e);
				}

				// 对于非batch的慢sql，自动explain一下检查是否加了索引
				boolean autoExplainSlowSql = getFeature(FeatureEnum.AUTO_EXPLAIN_SLOW_SQL);
				if (autoExplainSlowSql && getDatabaseType() == DatabaseTypeEnum.MYSQL) {
					try {
						String explainSql = "EXPLAIN " + sql;
						List<Object> explainArgs = new ArrayList<>();
						boolean isMap = false;
						if (args != null) {
							if (args instanceof List) {
								explainArgs = ((List<Object>) args);
							} else if (args instanceof Object[]) {
								explainArgs = Arrays.asList((Object[]) args);
							} else if (args instanceof Map) {
								isMap = true;
							}
						}
						List<Map<String, Object>> explainResult;
						if (!isMap) {
							explainResult = namedParameterJdbcTemplate.queryForList(
									NamedParameterUtils.trans(explainSql, explainArgs),
									NamedParameterUtils.transParam(explainArgs));
						} else {
							explainResult = namedParameterJdbcTemplate.queryForList(explainSql, (Map<String, ?>) args);
						}
						LOGGER.warn("Explain SlowSQL:{}; cost:{}ms, params:{} explain result:{}", sql,
								   cost, NimbleOrmJSON.toJson(args), NimbleOrmJSON.toJson(explainResult));
					} catch (Throwable e) {
						LOGGER.error("SlowSQL explain fail, SlowSQL:{}; cost:{}ms, params:{}",
								sql, cost, NimbleOrmJSON.toJson(args), e);
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

	/**
	 * 使用jdbcTemplate模版执行update，不支持in (?)表达式
	 * @return 实际修改的行数
	 */
	protected int jdbcExecuteUpdate(String sql, Object... args) {
		sql = addComment(sql);
		log(sql, 0, args);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.update(sql, args);// 此处可以用jdbcTemplate，因为没有in (?)表达式
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, 0, args == null ? new ArrayList<>() : Arrays.asList(args));
		return rows;
	}

	/**
	 * 使用namedParameterJdbcTemplate模版执行update，支持in(?)表达式
	 */
	protected int namedJdbcExecuteUpdate(String sql, Object... args) {
		sql = addComment(sql);
		log(sql, 0, args);
		long start = System.currentTimeMillis();
		List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
		if(args != null) {
			argsList.addAll(Arrays.asList(args));
		}
		int rows = namedParameterJdbcTemplate.update(
				NamedParameterUtils.trans(sql, argsList),
				NamedParameterUtils.transParam(argsList)); // 因为有in (?) 所以使用namedParameterJdbcTemplate
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, 0, argsList);
		return rows;
	}


	/**
	 * 使用namedParameterJdbcTemplate模版执行update，支持in(?)表达式
	 * @param logSql 用于日志打印的sql
	 * @param batchSize 批量修改的大小，当它的值大于0时，切换为batchLog方式log
	 * @param logArgs 用于日志打印的参数
	 */
	protected int namedJdbcExecuteUpdateWithLog(String sql, String logSql, int batchSize, List<Object> logArgs,
												Object... args) {
		sql = addComment(sql);
		log(logSql, batchSize, logArgs);
		long start = System.currentTimeMillis();
		List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
		if(args != null) {
			argsList.addAll(Arrays.asList(args));
		}
		int rows = namedParameterJdbcTemplate.update(
				NamedParameterUtils.trans(sql, argsList),
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
		this.databaseType = getDatabaseType(jdbcTemplate);
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
	public void setTimeoutWarningValve(long timeMS) {
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
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
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
	public void setTimeoutWarningCallback(IDBHelperSlowSqlCallback callback) {
		this.slowSqlCallback = callback;
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
}
