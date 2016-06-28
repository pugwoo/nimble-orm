package com.pugwoo.dbhelper.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2015年8月24日 18:37:48
 * 因为jdbcTemplate不支持 in (?)传入list的方式
 * 只支持NamedParameterJdbcTemplate,所以需要把?的方式换成:param的方式
 * 【重要】约定替换后的参数为 paramN， N从1开始
 */
public class NamedParameterUtils {
	
	public static Map<String, Object> transParam(Object... params) {
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
				map.put("param" + (currParamIndex++), param);
			}
		}
		return map;
	}

	/**
	 * 把?变成:paramN的形式，不包括"?"和'?'中的?
	 * paramN的N从1开始
	 * @param sql
	 * @return
	 */
	public static String trans(String sql) {
		if(sql == null || sql.isEmpty()) {
			return "";
		}
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
