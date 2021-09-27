package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.model.SubQuery;
import com.pugwoo.dbhelper.exception.ParameterSizeNotMatchedException;
import com.pugwoo.dbhelper.sql.SQLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 2015年8月24日 18:37:48
 * 因为jdbcTemplate不支持 in (?)传入list的方式
 * 只支持NamedParameterJdbcTemplate,所以需要把?的方式换成:param的方式
 * 【重要】约定替换后的参数为 paramN， N从1开始
 */
public class NamedParameterUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NamedParameterUtils.class);
	
	// sha256 from Pugwoo Chia's nimble-ORM
	private final static String MAGIC_FOR_EMPTY_COLLECTION =
			"450DB9DF910D25F80428D4A9BAB4FA36F45D0A15F0AC5B83AFC389D386F1AE9C";
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> transParam(List<Object> params) {
		Map<String, Object> map = new HashMap<>();
		if(params != null) {
			int currParamIndex = 1;
			for(Object param : params) {
				// 如果参数是数组，同时【不是】byte[]，则转换成List
				if(param != null && param.getClass().isArray()) {
					List<Object> p = new ArrayList<>();
					if(param instanceof char[]) {
						for(char c : (char[]) param) {
							p.add(c);
						}
						param = p;
					} else if(param instanceof short[]) {
						for(short s : (short[]) param) {
							p.add(s);
						}
						param = p;
					} else if(param instanceof int[]) {
						for(int i : (int[]) param) {
							p.add(i);
						}
						param = p;
					} else if(param instanceof long[]) {
						for(long l : (long[]) param) {
							p.add(l);
						}
						param = p;
					} else if(param instanceof float[]) {
						for(float f : (float[]) param) {
							p.add(f);
						}
						param = p;
					} else if(param instanceof double[]) {
						for(double d : (double[]) param) {
							p.add(d);
						}
						param = p;
					} else if(param instanceof Object[]) {
						p.addAll(Arrays.asList((Object[]) param));
						param = p;
					}
				}
				
				// 转换后，对于param是空的List或Set，则List或Set插入一个很长的不可能被用户撞上的值
				if(param instanceof List<?>) {
					if(((List<?>) param).isEmpty()) {
						((List<Object>) param).add(MAGIC_FOR_EMPTY_COLLECTION);
					}
				} else if (param instanceof Set<?>) {
					if(((Set<?>) param).isEmpty()) {
						((Set<Object>) param).add(MAGIC_FOR_EMPTY_COLLECTION);
					}
				}
				
				map.put("param" + (currParamIndex++), param);
			}
		}
		return map;
	}
	
	private static String expandParam(String sql, List<Object> args) {
		if(args == null || args.isEmpty()) {
			return sql;
		}
		while(true) {
			boolean isExistSubQuery = false;
			for(Object arg : args) {
				if(arg instanceof SubQuery) {
					isExistSubQuery = true;
					break;
				}
			}
			if(isExistSubQuery) {
				sql = _expandParam(sql, args);
			} else {
				break;
			}
		}
		return sql;
	}
	
	/**
	 * 展开参数，主要是处理SubQuery参数
	 */
	private static String _expandParam(String sql, List<Object> args) {
		StringBuilder sb = new StringBuilder();
		boolean isInStr = false;
		boolean isPreSlash = false;
		int currParamIndex = 0;
		List<Object> newArgs = new ArrayList<>();
		for(char ch : sql.toCharArray()/*int i = 0; i < sql.length(); i++*/) {
			//char ch = sql.charAt(i);
			
			if(ch == '?' && !isInStr) {
				if(args.size() <= currParamIndex) {
					throw new ParameterSizeNotMatchedException(sql);
				}
				Object arg = args.get(currParamIndex);
				if(arg instanceof SubQuery) {
					List<Object> values = new ArrayList<>();
					sb.append(SQLUtils.expandSubQuery((SubQuery)arg, values));
					newArgs.addAll(values);
				} else {
					sb.append("?");
					newArgs.add(arg);
				}
				currParamIndex++;
				continue;
			} else {
				sb.append(ch);
			}
			
			if(ch == '\'') {
				if(!isInStr) {
					isInStr = true;
				} else {
					if(!isPreSlash) {
						isInStr = false;
					}
				}
			}
			
			isPreSlash = ch == '\\';
		}
		
		args.clear(); // 清空原数组并插入新数组
		args.addAll(newArgs);
		return sb.toString();
	}

	/**
	 * 把?变成:paramN的形式，不包括'?'中的?
	 * paramN的N从1开始
	 * @param args 将会操作参数列表
	 */
	public static String trans(String sql, List<Object> args) {
		if(sql == null || sql.isEmpty()) {
			return "";
		}

		String originSql = sql;
		
		sql = expandParam(sql, args);
		
		StringBuilder sb = new StringBuilder();
		boolean isInStr = false;
		boolean isPreSlash = false;
		int currParamIndex = 1;
		for(char ch : sql.toCharArray()/*int i = 0; i < sql.length(); i++*/) {
			//char ch = sql.charAt(i);
			
			if(ch == '?' && !isInStr) {
				sb.append(":param").append(currParamIndex++);
				continue;
			} else {
				sb.append(ch);
			}
			
			if(ch == '\'') {
				if(!isInStr) {
					isInStr = true;
				} else {
					if(!isPreSlash) {
						isInStr = false;
					}
				}
			}
			
			isPreSlash = ch == '\\';
		}

        if (currParamIndex - 1 != args.size()) {
			LOGGER.error("SQL args not matched, provide args count:{}, expected:{}, SQL:{}",
					args.size(), currParamIndex - 1, originSql);
		}

		return sb.toString();
	}
	
}
