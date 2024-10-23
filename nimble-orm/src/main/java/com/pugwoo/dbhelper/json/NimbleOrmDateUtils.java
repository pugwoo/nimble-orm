package com.pugwoo.dbhelper.json;

import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NimbleOrmDateUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NimbleOrmDateUtils.class);

	/**标准日期时间格式**/
	public final static String FORMAT_STANDARD = "yyyy-MM-dd HH:mm:ss";
	/**日期格式**/
	public final static String FORMAT_DATE = "yyyy-MM-dd";

	public static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
		put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd HH:mm:ss"); // 2017-03-06 15:23:56
		put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd"); // 2017-03-06

		put("^\\d{6}$", "yyyyMM"); // 201703
		put("^\\d{8}$", "yyyyMMdd"); // 20170306
	    put("^\\d{14}$", "yyyyMMddHHmmss"); // 20170306152356
	    put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss"); // 20170306 152356
	    put("^\\d{4}-\\d{1,2}$", "yyyy-MM"); // 2017-03
	    put("^\\d{4}/\\d{1,2}$", "yyyy/MM"); // 2017/03

	    put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd"); // 2017/03/06
	    put("^\\d{1,2}:\\d{1,2}:\\d{1,2}$", "HH:mm:ss"); // 16:34:32
	    put("^\\d{1,2}:\\d{1,2}$", "HH:mm"); // 16:34
	    put("^\\d{4}年\\d{1,2}月\\d{1,2}日$", "yyyy年MM月dd日"); // 2017年3月30日
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd HH:mm"); // 2017-03-06 15:23
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "yyyy/MM/dd HH:mm"); // 2017/03/06 15:23

	    put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy/MM/dd HH:mm:ss"); // 2017/03/06 15:23:56
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}$", "yyyy-MM-dd HH:mm:ss.SSS"); // 2017-10-18 16:00:00.000
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}Z$", "yyyy-MM-dd'T'HH:mm:ss.SSSX"); // 2017-10-18T16:00:00.000Z
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}$", "yyyy-MM-dd'T'HH:mm:ss.SSS"); // 2017-10-18T16:00:00.000
		put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd'T'HH:mm:ss"); // 2017-10-18T16:00:00
		put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}[+-]{1}\\d{4}$", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"); // 2017-10-18T16:00:00.000+0000
		put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3} [+-]{1}\\d{4}$", "yyyy-MM-dd'T'HH:mm:ss.SSS Z"); // 2017-10-18T16:00:00.000 +0000
	}};

	/**
	 * 自动解析，失败抛出异常
	 * @param date 要解析的日期字符串
	 * @return 解析后的日期
	 */
	public static Date parseThrowException(String date) throws ParseException {
		if(InnerCommonUtils.isBlank(date)) {
			return null;
		}
		date = date.trim();
		String pattern = determineDateFormat(date);
		if(pattern == null) {
			// 检查是否是时间戳
			Date _date = tryParseTimestamp(date);
			if(_date != null) {
				return _date;
			}

			throw new ParseException("cannot parse date: \"" + date +
					"\". Supported formats: " + DATE_FORMAT_REGEXPS.values(), -1);
		}

		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			simpleDateFormat.setLenient(false);
			return simpleDateFormat.parse(date);
		} catch (Exception e) {
			if ("0000-00-00 00:00:00".equals(date) || "0000-00-00".equals(date)) {
				return null;
			}
			throw e;
		}
	}

	public static LocalDateTime toLocalDateTime(Date date) {
		if(date == null) {return null;}
		// java.sql.Date和java.sql.Time不支持date.toInstant()
		if (date instanceof java.sql.Date || date instanceof java.sql.Time) {
			date = new Date(date.getTime());
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	/**失败返回null，不会抛异常*/
	public static LocalDate parseLocalDate(String date) throws ParseException {
		return toLocalDate(parseThrowException(date));
	}

	private static LocalDate toLocalDate(Date date) {
		if(date == null) {return null;}
		// java.sql.Date和java.sql.Time不支持date.toInstant()
		if (date instanceof java.sql.Date || date instanceof java.sql.Time) {
			date = new Date(date.getTime());
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**失败返回null，不会抛异常*/
	public static LocalTime parseLocalTime(String date) throws ParseException {
		return toLocalTime(parseThrowException(date));
	}

	private static LocalTime toLocalTime(Date date) {
		if(date == null) {return null;}
		// java.sql.Date和java.sql.Time不支持date.toInstant()
		if (date instanceof java.sql.Date || date instanceof java.sql.Time) {
			date = new Date(date.getTime());
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
	}

	private static Date tryParseTimestamp(String date) {
		if (!isDigit(date)) {
			return null;
		}
		Long timestamp = parseLong(date);
		if (timestamp == null) {
			return null;
		}

		// 时间戳小于42亿则认为是秒（此时已经是2103-02-04），否则是毫秒
		if(timestamp < 4200000000L) {
			return new Date(timestamp * 1000L);
		} else {
			return new Date(timestamp);
		}
	}

	private static boolean isDigit(String str) {
		if(str == null || str.isEmpty()) {return false;}
		for(char c : str.toCharArray()) {
			if(!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	private static Long parseLong(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof Long) {
			return (Long) obj;
		}
		try {
			return Long.valueOf(obj.toString().trim());
		} catch (Exception e) {
			return null;
		}
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

	/**
	 * 带上毫秒的时间
	 * @param date 要格式化的日期
	 * @return 格式化后的字符串
	 */
	public static String formatWithMs(Date date) {
		return format(date, "yyyy-MM-dd HH:mm:ss.SSS");
	}
	
	private static String determineDateFormat(String dateString) {
	    for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
	        if (dateString.matches(regexp)) {
	            return DATE_FORMAT_REGEXPS.get(regexp);
	        }
	    }
	    return null; // Unknown format.
	}

	// ======================================= 新的LocalDateTime解析器 ===================== START =====================

	public static final Map<String, Boolean> LOCAL_DATE_TIME_IS_DATE = new HashMap<String, Boolean>() {{
		put("^\\d{4}-\\d{1,2}-\\d{1,2}$", true);
	}};

	public static final Map<String, DateTimeFormatter> LOCAL_DATE_TIME_FORMATTER = new LinkedHashMap<String, DateTimeFormatter>() {{

		// 最常用的放前面，提高性能
		put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // 2017-03-06 15:23:56
		put("^\\d{4}-\\d{1,2}-\\d{1,2}$", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // 2017-03-06

		// 只到分钟：2017-03-06 15:23   2017/03/06 15:23  2017-03-06T15:23   2017/03/06T15:23
		DateTimeFormatter formatterMinute = new DateTimeFormatterBuilder()
				.optionalStart().appendPattern("yyyy-MM-dd").optionalEnd()
				.optionalStart().appendPattern("yyyy/MM/dd").optionalEnd()
				.optionalStart().appendLiteral('T').optionalEnd()
				.optionalStart().appendLiteral(' ').optionalEnd()
				.appendPattern("HH:mm").toFormatter();
		put("^\\d{4}(/\\d{1,2}/|-\\d{1,2}-)\\d{1,2}[T ]\\d{1,2}:\\d{1,2}$", formatterMinute);

		// 带毫秒纳秒的时间格式
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.optionalStart().appendPattern("yyyy-MM-dd").optionalEnd()
				.optionalStart().appendPattern("yyyy/MM/dd").optionalEnd()
				.optionalStart().appendPattern("yyyyMMdd").optionalEnd()
				.optionalStart().appendLiteral('T').optionalEnd()
				.optionalStart().appendLiteral(' ').optionalEnd()
				.optionalStart().appendPattern("HH:mm:ss").optionalEnd()
				.optionalStart().appendPattern("HHmmss").optionalEnd()
				.optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd() // 毫秒 纳秒 0-9位
				.optionalStart().appendPattern("XXX").optionalEnd()  // 支持 +00:00 格式
				.optionalStart().appendPattern("xxxx").optionalEnd() // 支持 +0000 格式
				.optionalStart().appendPattern("X").optionalEnd()    // 支持 Z 格式
				.optionalStart().appendPattern(" XXX").optionalEnd()  // 支持 " +00:00" 格式
				.optionalStart().appendPattern(" xxxx").optionalEnd() // 支持 " +0000" 格式
				.toFormatter();
		// 2017-10-18T16:00:00[.纳秒1-9位][+00:00或+0000或Z]      2017-10-18 16:00:00[.纳秒1-9位][+00:00或+0000或Z]
		// 2017/10/18T16:00:00[.纳秒1-9位][+00:00或+0000或Z]      2017/10/18 16:00:00[.纳秒1-9位][+00:00或+0000或Z]
		put("^\\d{4}(/\\d{1,2}/|-\\d{1,2}-)\\d{1,2}[T ]\\d{1,2}:\\d{1,2}:\\d{1,2}(\\.\\d{0,9})?(Z|( ?[+-]\\d{2}:\\d{2})|( ?[+-]\\d{4}))?$", formatter);
        // 20171018T160000[.纳秒1-9位][+00:00或+0000或Z]      20171018 160000[.纳秒1-9位][+00:00或+0000或Z]
		put("^\\d{8}[T ]\\d{6}(\\.\\d{0,9})?(Z|( ?[+-]\\d{2}:\\d{2})|( ?[+-]\\d{4}))?$", formatter);
	}};

	/**解析失败抛异常*/
	public static LocalDateTime parseLocalDateTimeThrowException(String dateString) throws ParseException {
		if (InnerCommonUtils.isBlank(dateString)) {
			return null;
		}
		dateString = dateString.trim();
		for (Map.Entry<String, DateTimeFormatter> formatter : LOCAL_DATE_TIME_FORMATTER.entrySet()) {
			if (dateString.matches(formatter.getKey())) {
				Boolean isDate = LOCAL_DATE_TIME_IS_DATE.get(formatter.getKey());
				if (isDate != null && isDate) {
					dateString = dateString + " 00:00:00";
				}
				return LocalDateTime.parse(dateString, formatter.getValue());
			}
		}
		throw new ParseException("Parse failed. Unsupported pattern:" + dateString, 0);
	}

	/**解析失败抛异常*/
	public static LocalDateTime parseLocalDateTimeThrowException(String dateString, String pattern) throws ParseException {
		if (InnerCommonUtils.isBlank(dateString)) {
			return null;
		}
		return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
	}

	/**解析失败不抛异常，返回null*/
	public static LocalDateTime parseLocalDateTime(String dateString) {
		try {
			return parseLocalDateTimeThrowException(dateString);
		} catch (ParseException e) {
			LOGGER.error("Parse LocalDateTime:{} failed", dateString, e);
			return null;
		}
	}

	/**解析失败不抛异常，返回null*/
	public static LocalDateTime parseLocalDateTime(String dateString, String pattern) {
		try {
			return parseLocalDateTimeThrowException(dateString, pattern);
		} catch (ParseException e) {
			LOGGER.error("Parse LocalDateTime:{} failed", dateString, e);
			return null;
		}
	}

}
