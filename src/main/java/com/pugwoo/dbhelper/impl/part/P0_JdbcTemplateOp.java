package com.pugwoo.dbhelper.impl.part;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.impl.SpringJdbcDBHelper;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;

/**
 * jdbcTemplate原生操作接口封装
 * @author NICK
 */
public abstract class P0_JdbcTemplateOp implements DBHelper {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(SpringJdbcDBHelper.class);

	protected JdbcTemplate jdbcTemplate;
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	protected long timeoutWarningValve = 1000;
	
	protected void log(StringBuilder sql) {
		log(sql.toString());
	}
	
	protected void log(String sql) {
		LOGGER.debug("ExecSQL:{}", sql);
	}
	
	protected void logSlow(long cost, StringBuilder sql, List<Object> keyValues) {
		logSlow(cost, sql.toString(), keyValues);
	}
	
	protected void logSlow(long cost, StringBuilder sql, Object keyValue) {
		logSlow(cost, sql.toString(), keyValue);
	}
	
	protected void logSlow(long cost, String sql, List<Object> keyValues) {
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, keyValues);
		}
	}
	
	protected void logSlow(long cost, String sql, Object keyValue) {
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, keyValue);
		}
	}
	
	@Override
	public void rollback() {
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	}
	
	@Override
	public <T> T queryForObject(Class<T> clazz, String sql, Object... args) {
		return namedParameterJdbcTemplate.queryForObject(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(args),
				clazz); // 因为有in (?)所以用namedParameterJdbcTemplate
	}
	
	@Override
	public SqlRowSet queryForRowSet(String sql, Object... args) {
		SqlRowSet sqlRowSet = namedParameterJdbcTemplate.queryForRowSet(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(args)); // 因为有in (?)所以用namedParameterJdbcTemplate
		return sqlRowSet;
	}
	
	@Override
	public Map<String, Object> queryForMap(String sql, Object... args) {
		Map<String, Object> map = namedParameterJdbcTemplate.queryForMap(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(args)); // 因为有in (?)所以用namedParameterJdbcTemplate
		return map;
	}
	
	@Override
	public List<Map<String, Object>> queryForList(String sql, Object... args) {
		List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(args)); // 因为有in (?)所以用namedParameterJdbcTemplate
		return list;
	}
	
	@Override
	public <T> List<T> queryForList(Class<T> clazz, String sql, Object... args) {
		List<T> list = namedParameterJdbcTemplate.queryForList(
				NamedParameterUtils.trans(sql.toString()),
				NamedParameterUtils.transParam(args),
				clazz); // 因为有in (?)所以用namedParameterJdbcTemplate
		return list;
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
		logSlow(cost, sql, args);
		return rows;
	}
	
	/**
	 * 使用namedParameterJdbcTemplate模版执行update，支持in(?)表达式
	 * @param sql
	 * @param args
	 * @return
	 */
	protected int namedJdbcExecuteUpdate(String sql, Object... args) {
		LOGGER.debug("ExecSQL:{}", sql);
		long start = System.currentTimeMillis();
		int rows = namedParameterJdbcTemplate.update(
				NamedParameterUtils.trans(sql),
				NamedParameterUtils.transParam(args)); // 因为有in (?) 所以使用namedParameterJdbcTemplate
		long cost = System.currentTimeMillis() - start;
		if(cost > timeoutWarningValve) {
			LOGGER.warn("SlowSQL:{},cost:{}ms,params:{}", sql, cost, args);
		}
		return rows;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void setTimeoutWarningValve(long timeMS) {
		timeoutWarningValve = timeMS;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return namedParameterJdbcTemplate;
	}

	public long getTimeoutWarningValve() {
		return timeoutWarningValve;
	}

}
