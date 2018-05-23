package com.pugwoo.dbhelper.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pugwoo.dbhelper.bean.SubQuery;
import com.pugwoo.dbhelper.exception.ParameterSizeNotMatchedException;
import com.pugwoo.dbhelper.sql.SQLUtils;

/**
 * 2015年8月24日 18:37:48
 * 因为jdbcTemplate不支持 in (?)传入list的方式
 * 只支持NamedParameterJdbcTemplate,所以需要把?的方式换成:param的方式
 * 【重要】约定替换后的参数为 paramN， N从1开始
 */
public class NamedParameterUtils {
	
	// sha256 from Pugwoo Chia's nimble-ORM
	private final static String magicForEmptyCollection = 
			"450DB9DF910D25F80428D4A9BAB4FA36F45D0A15F0AC5B83AFC389D386F1AE9C";
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> transParam(List<Object> params) {
		Map<String, Object> map = new HashMap<String, Object>();
		if(params != null) {
			int currParamIndex = 1;
			for(Object param : params) {
				// 如果参数是数组，同时【不是】byte[]，则转换成List
				if(param != null && param.getClass().isArray()) {
					List<Object> p = new ArrayList<Object>();
					if(param instanceof char[]) {
						for(char c : (char[]) param) {
							p.add(new Character(c));
						}
						param = p;
					} else if(param instanceof short[]) {
						for(short s : (short[]) param) {
							p.add(new Short(s));
						}
						param = p;
					} else if(param instanceof int[]) {
						for(int i : (int[]) param) {
							p.add(new Integer(i));
						}
						param = p;
					} else if(param instanceof long[]) {
						for(long l : (long[]) param) {
							p.add(new Long(l));
						}
						param = p;
					} else if(param instanceof float[]) {
						for(float f : (float[]) param) {
							p.add(new Float(f));
						}
						param = p;
					} else if(param instanceof double[]) {
						for(double d : (double[]) param) {
							p.add(new Double(d));
						}
						param = p;
					} else if(param instanceof Object[]) {
						for(Object o : (Object[]) param) {
							p.add(o);
						}
						param = p;
					}
				}
				
				// 转换后，对于param是空的List或Set，则List或Set插入一个很长的不可能被用户撞上的值
				if(param instanceof List<?>) {
					if(((List<?>) param).isEmpty()) {
						((List<Object>) param).add((Object)magicForEmptyCollection);
					}
				} else if (param instanceof Set<?>) {
					if(((Set<?>) param).isEmpty()) {
						((Set<Object>) param).add((Object)magicForEmptyCollection);
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
	 * @param sql
	 * @param args
	 * @return
	 */
	private static String _expandParam(String sql, List<Object> args) {
		StringBuilder sb = new StringBuilder();
		boolean isInStr = false;
		boolean isPreSlash = false;
		char strQuota = 0;
		int currParamIndex = 0;
		List<Object> newArgs = new ArrayList<Object>();
		for(int i = 0; i < sql.length(); i++) {
			char ch = sql.charAt(i);
			
			if(ch == '?' && !isInStr) {
				if(args.size() <= currParamIndex) {
					throw new ParameterSizeNotMatchedException(sql);
				}
				Object arg = args.get(currParamIndex);
				if(arg != null && arg instanceof SubQuery) {
					List<Object> values = new ArrayList<Object>();
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
			
			if(ch == '\'' || ch == '"') {
				if(!isInStr) {
					isInStr = true;
					strQuota = ch;
				} else {
					if(strQuota == ch && !isPreSlash) {
						isInStr = false;
						strQuota = 0;
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
	 * 把?变成:paramN的形式，不包括"?"和'?'中的?
	 * paramN的N从1开始
	 * @param sql
	 * @param args将会操作参数列表
	 * @return
	 */
	public static String trans(String sql, List<Object> args) {
		if(sql == null || sql.isEmpty()) {
			return "";
		}
		
		sql = expandParam(sql, args);
		
		StringBuilder sb = new StringBuilder();
		boolean isInStr = false;
		boolean isPreSlash = false;
		char strQuota = 0;
		int currParamIndex = 1;
		for(int i = 0; i < sql.length(); i++) {
			char ch = sql.charAt(i);
			
			if(ch == '?' && !isInStr) {
				sb.append(":param").append(currParamIndex++);
				continue;
			} else {
				sb.append(ch);
			}
			
			if(ch == '\'' || ch == '"') {
				if(!isInStr) {
					isInStr = true;
					strQuota = ch;
				} else {
					if(strQuota == ch && !isPreSlash) {
						isInStr = false;
						strQuota = 0;
					}
				}
			}
			
			isPreSlash = ch == '\\';
		}
		return sb.toString();
	}
	
}
