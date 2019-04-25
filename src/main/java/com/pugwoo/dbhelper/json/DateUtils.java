package com.pugwoo.dbhelper.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {

	/**标准日期时间格式**/
	public final static String FORMAT_STANDARD = "yyyy-MM-dd HH:mm:ss";
	/**日期格式**/
	public final static String FORMAT_DATE = "yyyy-MM-dd";

	@SuppressWarnings("serial")
	public static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
		put("^\\d{6}$", "yyyyMM"); // 201703
		put("^\\d{8}$", "yyyyMMdd"); // 20170306
	    put("^\\d{14}$", "yyyyMMddHHmmss"); // 20170306152356
	    put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss"); // 20170306 152356
	    put("^\\d{4}-\\d{1,2}$", "yyyy-MM"); // 2017-03
	    put("^\\d{4}/\\d{1,2}$", "yyyy/MM"); // 2017/03
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd"); // 2017-03-06
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd"); // 2017/03/06
	    put("^\\d{1,2}:\\d{1,2}:\\d{1,2}$", "HH:mm:ss"); // 16:34:32
	    put("^\\d{1,2}:\\d{1,2}$", "HH:mm"); // 16:34
	    put("^\\d{4}年\\d{1,2}月\\d{1,2}日$", "yyyy年MM月dd日"); // 2017年3月30日
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd HH:mm"); // 2017-03-06 15:23
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "yyyy/MM/dd HH:mm"); // 2017/03/06 15:23
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd HH:mm:ss"); // 2017-03-06 15:23:56
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy/MM/dd HH:mm:ss"); // 2017/03/06 15:23:56
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}$", "yyyy-MM-dd HH:mm:ss.SSS"); // 2017-10-18 16:00:00.000
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}Z$", "yyyy-MM-dd'T'HH:mm:ss.SSSX"); // 2017-10-18T16:00:00.000Z
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}$", "yyyy-MM-dd'T'HH:mm:ss.SSS"); // 2017-10-18T16:00:00.000
	}};

	/**
	 * 自动解析，失败抛出异常
	 * @param date
	 * @return
	 */
	public static Date parseThrowException(String date) throws ParseException {
		if(date == null || date.trim().isEmpty()) {
			return null;
		}
		String pattern = determineDateFormat(date);
		if(pattern == null) {
			throw new ParseException("Unparseable date: \"" + date +
					"\". Supported formats: " + DATE_FORMAT_REGEXPS.values(), -1);
		}
		return new SimpleDateFormat(pattern).parse(date);
	}

	/**
	 * 转换成标准的格式 yyyy-MM-dd HH:mm:ss
	 */
	public static String format(Date date) {
		if(date == null) {
			return "";
		}
		return format(date, FORMAT_STANDARD);
	}
	
	/**
	 * 转换成标准的格式 yyyy-MM-dd
	 */
	public static String formatDate(Date date) {
		if(date == null) {
			return "";
		}
		return format(date, FORMAT_DATE);
	}
	
	public static String format(Date date, String pattern) {
		if(date == null) {
			return "";
		}
		return new SimpleDateFormat(pattern).format(date);
	}
	
	private static String determineDateFormat(String dateString) {
	    for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
	        if (dateString.matches(regexp)) {
	            return DATE_FORMAT_REGEXPS.get(regexp);
	        }
	    }
	    return null; // Unknown format.
	}

}
