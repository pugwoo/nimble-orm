package com.pugwoo.dbhelper.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

	// 常用格式 //
	
	/**标准日期时间格式**/
	public final static String FORMAT_STANDARD = "yyyy-MM-dd HH:mm:ss";
	/**MySQL日期时间格式**/
	public final static String FORMAT_MYSQL_DATETIME = "yyyy-MM-dd HH:mm:ss.SSS";
	/**中文日期格式**/
	public final static String FORMAT_CHINESE_DATE = "yyyy年MM月dd日";
	/**日期格式**/
	public final static String FORMAT_DATE = "yyyy-MM-dd";
	/**时间格式**/
	public final static String FORMAT_TIME = "HH:mm:ss";
	
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
	 * @param field 对应于Calendar定义的域
	 */
	public static Date addTime(Date date, int field, int num) {
		if(date == null) {return null;}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(field, num);
		return cal.getTime();
	}
	
	/**
	 * 自动解析，不会抛出异常
	 * @param date
	 * @return
	 */
	public static Date parse(String date) {
		if(date == null || date.trim().isEmpty()) {
			return null;
		}
		String pattern = determineDateFormat(date);
		if(pattern == null) {
			LOGGER.error("date parse, pattern not support, date:{}", date);
			return null;
		}
		try {
			return new SimpleDateFormat(pattern).parse(date);
		} catch (ParseException e) {
			LOGGER.error("parse date error, date:{}", date, e);
			return null;
		}
	}
	
	/**
	 * 解析日期，不会抛出异常，如果解析失败，打log并返回null
	 * @param date
	 * @param pattern 日期格式pattern
	 */
	public static Date parse(String date, String pattern) {
		if(date == null || date.trim().isEmpty()) {
			return null;
		}
		try {
			return new SimpleDateFormat(pattern).parse(date);
		} catch (ParseException e) {
			LOGGER.error("parse date error, date:{}", date, e);
			return null;
		}
	}
	
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
	 * 解析日期，失败抛出异常
	 * @param date
	 * @return
	 */
	public static Date parseThrowException(String date, String pattern) throws ParseException {
		if(date == null || date.trim().isEmpty()) {
			return null;
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
		return new SimpleDateFormat(FORMAT_STANDARD).format(date);
	}
	
	/**
	 * 转换成标准的格式 yyyy-MM-dd
	 */
	public static String formatDate(Date date) {
		if(date == null) {
			return "";
		}
		return new SimpleDateFormat(FORMAT_DATE).format(date);
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
	
	// ======================================
	
	/**
	 * 计算两个日期的天数差，不足一天的不算。
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int diffDays(Date date1, Date date2) {
		if(date1 == null || date2 == null) {
			return 0;
		}
		return (int) (Math.abs(date1.getTime() - date2.getTime()) / (24 * 3600 * 1000));
	}
	
	/**
	 * 计算两个日期的年份差，主要用于计算年龄。不足一年的不计，一年按365天计，不考虑闰年。
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int diffYears(Date date1, Date date2) {
		return diffDays(date1, date2) / 365;
	}
	
	/**
	 * 显示日期date到现在的时间差的字符串友好形式:
	 * 1. 10秒内，显示刚刚
	 * 2. 60秒内，显示xx秒前
	 * 3. 3600秒内，显示xx分钟前
	 * 4. 3600*24秒内，显示xx小时前
	 * 5. 3600*24*10秒内，显示xx天前
	 * 6. 其它显示真实日期，格式yyyy-MM-dd HH:mm
	 * @param date
	 * @return
	 */
    public static String getIntervalToNow(Date date) {
        if(date == null){
            return "";
        }
        String interval = "";
        long seconds = (System.currentTimeMillis() - date.getTime()) / 1000;
        if(seconds >= 0) {
            if (seconds < 10) {
                interval = "刚刚";
            } else if (seconds < 60) {
            	interval = seconds + "秒前";
            } else if (seconds < 3600) {
            	interval = (seconds / 60) + "分钟前";
            } else if (seconds < 3600 * 24) {
            	interval = (seconds / 3600) + "小时前";
            } else if (seconds < 3600 * 24 * 10) {
            	interval = (seconds / 3600 / 24) + "天前";
            } else {
            	interval = format(date, "yyyy-MM-dd HH:mm");
            }
        } else {
        	interval = format(date, "yyyy-MM-dd HH:mm");
        }
        return interval;
    }

}
