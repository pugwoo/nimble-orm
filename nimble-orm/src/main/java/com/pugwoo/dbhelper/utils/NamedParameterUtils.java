package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.json.NimbleOrmJSON;
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
				
				// 转换后，对于param是空的List或Set，则List或Set插入null，此动作时防止SQL出错，同时保证查询结果准确
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
	 * 计算sql中?的个数，不包含'?'中的?和注释中的?
	 */
	public static int getQuestionMarkCount(String sql) {
		if(sql == null || sql.isEmpty()) {
			return 0;
		}
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
				currParamIndex++;
				continue;
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

        return currParamIndex - 1;
	}

	/**
	 * 把?变成:paramN的形式，不包括'?'中的?
	 * paramN的N从1开始
	 * @param args 该参数可能被修改，当该参数是null或空时，返回原sql，等价于不处理
	 */
	public static String trans(String sql, List<Object> args) {
		if(sql == null || sql.isEmpty()) {
			if (args != null && !args.isEmpty()) {
				LOGGER.error("SQL args not matched, provide args count:{}, expected:{}, SQL:{}, params:{}",
						args.size(), 0, sql, NimbleOrmJSON.toJsonNoException(args));
			}
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

		int paramCount = currParamIndex - 1;
        if (paramCount != args.size()) {
			LOGGER.error("SQL args not matched, provide args count:{}, expected:{}, SQL:{}, params:{}",
					args.size(), paramCount, sql, NimbleOrmJSON.toJsonNoException(args));
			// 尝试将args里的list转换成数组，仅为防御性处理，不建议用户这样使用
			if (paramCount > 1 && args.size() == 1 && args.get(0) instanceof List<?>) {
				LOGGER.error("SQL args is a List, please convert to array, provide args {}, SQL:{}. "
						+ " NimbleOrm will automatically convert it for you, although it is not recommended.",
						NimbleOrmJSON.toJsonNoException(args), sql);
				List<?> list = (List<?>) args.get(0);
				args.clear();
				args.addAll(list);
			}
			// 如果sql参数只有一个(?)，同时args大于1个【这种不处理，这个误处理的几率大】
		}

		return sb.toString();
	}

	/**
	 * 对于param是空的List或Set，则List或Set插入null，此动作时防止SQL出错，同时保证查询结果准确
	 */
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
