package com.pugwoo.dbhelper.utils;

import java.util.HashMap;
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
