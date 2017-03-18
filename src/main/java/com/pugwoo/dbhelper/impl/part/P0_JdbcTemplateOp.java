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
import com.pugwoo.dbhelper.utils.NamedParameterUtils;

/**
 * jdbcTemplate原生操作接口封装
 * @author NICK
 */
public abstract class P0_JdbcTemplateOp implements DBHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(P0_JdbcTemplateOp.class);

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
