package com.pugwoo.dbhelper.utils;

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
				
				// 转换后，对于param是空的List或Set，则List或Set插入null，此动作时防止SQL出错
				if(param instanceof List<?>) {
					if(((List<?>) param).isEmpty()) {
						List<String> list = new ArrayList<>(1);
						list.add(null);
						param = list;
					}
				} else if (param instanceof Set<?>) {
					if(((Set<?>) param).isEmpty()) {
						Set<String> set = new HashSet<>(1);
						set.add(null);
						param = set;
					}
				}
				
				map.put("param" + (currParamIndex++), param);
			}
		}
		return map;
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
		StringBuilder sb = new StringBuilder();

		boolean isInStr = false;
		boolean isInComment = false;
		int commentType = 0; // 0:无注释，1:单行注释，2:多行注释

		boolean isPreBackSlash = false;
		boolean isPreHyphen = false;
		boolean isPreSlash = false;
		boolean isPreAsterisk = false;

		int currParamIndex = 1;
		for(char ch : sql.toCharArray()) {
			if(ch == '?' && !isInStr && !isInComment) {
				sb.append(":param").append(currParamIndex++);
				continue;
			} else {
				sb.append(ch);
			}

			if (!isInComment) { // 只有不在注释中，字符串才可能出现
				if(ch == '\'') {
					if(!isInStr) {
						isInStr = true;
					} else {
						if(!isPreBackSlash) {
							isInStr = false;
						}
					}
				}
			}

			if (!isInStr) { // 只有不在字符串中，注释才可能出现
				if (!isInComment) {
					if (isPreHyphen && ch == '-') {
						isInComment = true;
						commentType = 1;
					} else if (isPreSlash && ch == '*') {
						isInComment = true;
						commentType = 2;
					}
				} else {
					if (commentType == 1 && ch == '\n') {
						isInComment = false;
						commentType = 0;
					} else if (commentType == 2 && isPreAsterisk && ch == '/') {
						isInComment = false;
						commentType = 0;
					}
				}
			}

			isPreBackSlash = ch == '\\';
			isPreHyphen = ch == '-';
			isPreSlash = ch == '/';
			isPreAsterisk = ch == '*';
		}

        if (currParamIndex - 1 != args.size()) {
			LOGGER.error("SQL args not matched, provide args count:{}, expected:{}, SQL:{}",
					args.size(), currParamIndex - 1, sql);
		}

		return sb.toString();
	}

	public static void preHandleParams(Map<String, ?> params) {
		if (params == null) {
			return;
		}

		for (Map.Entry<String, ?> entry : params.entrySet()) {
			if(entry.getValue() instanceof List<?>) {
				if(((List<?>) entry.getValue()).isEmpty()) {
					List<String> list = new ArrayList<>(1);
					list.add(null);
					((Map<String, Object>) params).put(entry.getKey(), list);
				}
			} else if (entry.getValue() instanceof Set<?>) {
				if(((Set<?>) entry.getValue()).isEmpty()) {
					Set<String> set = new HashSet<>(1);
					set.add(null);
					((Map<String, Object>) params).put(entry.getKey(), set);
				}
			}
		}
	}

}
