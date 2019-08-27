package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.IDBHelperSlowSqlCallback;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * jdbcTemplate原生操作接口封装
 * @author NICK
 */
public abstract class P0_JdbcTemplateOp implements DBHelper, ApplicationContextAware {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(SpringJdbcDBHelper.class);

	protected JdbcTemplate jdbcTemplate;
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	protected long timeoutWarningValve = 1000;
	protected Integer maxPageSize = null; // 每页最大个数，为null表示不限制
	
	protected ApplicationContext applicationContext;
	
	protected List<DBHelperInterceptor> interceptors = new ArrayList<DBHelperInterceptor>();
	
	private IDBHelperSlowSqlCallback slowSqlCallback;
	
	protected void log(StringBuilder sql) {
		log(sql.toString());
	}
	
	protected void log(String sql) {
		LOGGER.debug("ExecSQL:{}", sql);
	}
	
	protected void logSlow(long cost, String sql, List<Object> keyValues) {
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, keyValues);
			try {
				if(slowSqlCallback != null) {
					slowSqlCallback.callback(cost, sql, keyValues);
				}
			} catch (Throwable e) {
				LOGGER.error("DBHelperSlowSqlCallback fail, SlowSQL:{},cost:{}ms,params:{}",
						sql, cost, keyValues, e);
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
			public void suspend() {
			}
			@Override
			public void resume() {
			}
			@Override
			public void flush() {
			}
			@Override
			public void beforeCompletion() {
			}
			@Override
			public void beforeCommit(boolean readOnly) {
			}
			@Override
			public void afterCompletion(int status) {
			}
			@Override
			public void afterCommit() {
				runnable.run();
			}
		});
		return true;
	}

	/**
	 * 使用jdbcTemplate模版执行update，不支持in (?)表达式 
	 * @param sql
	 * @param args
	 * @return 实际修改的行数
	 */
	protected int jdbcExecuteUpdate(String sql, Object... args) {
		log(sql);
		long start = System.currentTimeMillis();
		int rows = jdbcTemplate.update(sql.toString(), args);// 此处可以用jdbcTemplate，因为没有in (?)表达式
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, args == null ? new ArrayList<Object>() : Arrays.asList(args));
		return rows;
	}
	
	/**
	 * 使用namedParameterJdbcTemplate模版执行update，支持in(?)表达式
	 * @param sql
	 * @param args
	 * @return
	 */
	protected int namedJdbcExecuteUpdate(String sql, Object... args) {
		log(sql);
		long start = System.currentTimeMillis();
		List<Object> argsList = new ArrayList<Object>(); // 不要直接用Arrays.asList，它不支持clear方法
		if(args != null) {
			argsList.addAll(Arrays.asList(args));
		}
		int rows = namedParameterJdbcTemplate.update(
				NamedParameterUtils.trans(sql, argsList),
				NamedParameterUtils.transParam(argsList)); // 因为有in (?) 所以使用namedParameterJdbcTemplate
		long cost = System.currentTimeMillis() - start;
		logSlow(cost, sql, argsList);
		return rows;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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
	public <T> void setTableName(Class<T> clazz, String tableName) {
		DBHelperContext.setTableName(clazz, tableName);
	}

	@Override
	public void resetTableNames() {
		DBHelperContext.resetTableName();
	}
}
