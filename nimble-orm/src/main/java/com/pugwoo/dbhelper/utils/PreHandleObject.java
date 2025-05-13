package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.enums.ValueConditionEnum;
import com.pugwoo.dbhelper.impl.DBHelperContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 预处理对象
 * 
 * @author pugwoo
 * 2017年3月18日 17:30:14
 */
public class PreHandleObject {

	private static final Logger LOGGER = LoggerFactory.getLogger(PreHandleObject.class);

	/**
	 * 插入前预处理字段值
	 */
	public static <T> void preHandleInsert(T t) {
		if(t == null) {
			return;
		}
		List<Field> fields = DOInfoReader.getColumns(t.getClass());
		if(fields.isEmpty()) {
			return;
		}

		Table table = DOInfoReader.getTable(t.getClass());
		Map<Class<?>, Object> defaultValueMap = DBHelperContext.getInsertDefaultValueMap(table.insertDefaultValueMap());
		boolean hasDefaultValueMap = defaultValueMap != null && !defaultValueMap.isEmpty();

		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);

			// 优先级最高的4项，一般只会选择其中之一
			// 这个地方不需要处理turnOff软删除，因为它只是写入时设置默认值
			if(column.softDelete().length == 2
					&& InnerCommonUtils.isNotBlank(column.softDelete()[0])
					&& InnerCommonUtils.isNotBlank(column.softDelete()[1])) {
				Object delete = DOInfoReader.getValue(field, t);
				if(delete == null) {
					DOInfoReader.setValue(field, t, column.softDelete()[0]);
				}
			}
			if(column.setTimeWhenInsert()) {
				if(DOInfoReader.getValue(field, t) == null) {
					setNowDateTime(field, t);
				}
			}
			if(column.setRandomStringWhenInsert()) {
				if(DOInfoReader.getValue(field, t) == null) {
					DOInfoReader.setValue(field, t, 
							UUID.randomUUID().toString().replace("-", "").substring(0, 32));
				}
			}
			if(column.casVersion()) {
				if(DOInfoReader.getValue(field, t) == null) {
					DOInfoReader.setValue(field, t, 1);
				}
			}

			// 优先级较低
			String insertValueScript = column.insertValueScript();
			if(InnerCommonUtils.isNotBlank(insertValueScript)) {
				ValueConditionEnum valueConditionEnum = table.insertValueCondition();
				Object value = DOInfoReader.getValue(field, t);
				if (valueConditionEnum == null || valueConditionEnum == ValueConditionEnum.WHEN_NULL) {
					if (value == null) {
						ScriptUtils.setValueFromScript(t, field, column.ignoreScriptError(), insertValueScript);
					}
				} else if (valueConditionEnum == ValueConditionEnum.WHEN_EMPTY) {
					if (value == null || (value instanceof String && ((String) value).isEmpty())) {
						ScriptUtils.setValueFromScript(t, field, column.ignoreScriptError(), insertValueScript);
					}
				} else if (valueConditionEnum == ValueConditionEnum.WHEN_BLANK) {
					if (value == null || (value instanceof String && InnerCommonUtils.isBlank((String) value))) {
						ScriptUtils.setValueFromScript(t, field, column.ignoreScriptError(), insertValueScript);
					}
				}
			}

			// 优先级最低，类级别指定的默认值；不处理id主键，id主键要么是自增，要么应该由用户明确指定
			if(hasDefaultValueMap && !column.isKey()) {
				Object defaultValue = defaultValueMap.get(field.getType());
				if(defaultValue != null) {
					ValueConditionEnum valueConditionEnum = table.insertValueCondition();
					Object value = DOInfoReader.getValue(field, t);
					if (valueConditionEnum == null || valueConditionEnum == ValueConditionEnum.WHEN_NULL) {
						if (value == null) {
							DOInfoReader.setValue(field, t, defaultValue);
						}
					} else if (valueConditionEnum == ValueConditionEnum.WHEN_EMPTY) {
						if (value == null || (value instanceof String && ((String) value).isEmpty())) {
							DOInfoReader.setValue(field, t, defaultValue);
						}
					} else if (valueConditionEnum == ValueConditionEnum.WHEN_BLANK) {
						if (value == null || (value instanceof String && InnerCommonUtils.isBlank((String) value))) {
							DOInfoReader.setValue(field, t, defaultValue);
						}
					}
				}
			}

			// 优先级最低，自动trim字符串
			if (column.autoTrimString() == 1 || column.autoTrimString() == -1 && table.autoTrimString() == 1) {
				if (String.class.equals(field.getType())) {
					String value = (String) DOInfoReader.getValue(field, t);
					if (value != null) {
						DOInfoReader.setValue(field, t, value.trim());
					}
				}
			}

			// 优先级最低，truncate string should be last
			if (column.maxStringLength() >= 0) {
				if (String.class.equals(field.getType())) {
					String value = (String) DOInfoReader.getValue(field, t);
					if (value != null && value.length() > column.maxStringLength()) {
						String newValue = value.substring(0, column.maxStringLength());
						DOInfoReader.setValue(field, t, newValue);
						LOGGER.warn("truncate class:{} field:{} value:{} to maxStringLength:{} newValue:{}",
								t.getClass().getName(), field.getName(), value, column.maxStringLength(), newValue);
					}
				}
			}
		}
	}

    public static <T> void preHandleDelete(T t) {
        if(t == null) {
            return;
        }

        List<Field> notKeyFields = DOInfoReader.getNotKeyColumns(t.getClass());
        for(Field field : notKeyFields) {
            Column column = field.getAnnotation(Column.class);
            if(InnerCommonUtils.isNotBlank(column.deleteValueScript())) {
                ScriptUtils.setValueFromScript(t, field, column.ignoreScriptError(), column.deleteValueScript());
            }
        }
    }

	public static <T> void preHandleDeleteList(Collection<T> tList) {
		if (tList == null) {
			return;
		}
		for(T t : tList) {
			preHandleDelete(t);
		}
	}
	
	public static <T> void preHandleUpdate(T t) {
		if(t == null) {
			return;
		}
		
		List<Field> notKeyFields = DOInfoReader.getNotKeyColumns(t.getClass());

		Table table = DOInfoReader.getTable(t.getClass());

		for(Field field : notKeyFields) {
			Column column = field.getAnnotation(Column.class);
			
			if(column.setTimeWhenUpdate()) {
				setNowDateTime(field, t);
			}

			// 这个地方不用处理turnOff软删除，因为它只是更新时自动设置软删除的值
			if(column.softDelete().length == 2
					&& InnerCommonUtils.isNotBlank(column.softDelete()[0])
					&& InnerCommonUtils.isNotBlank(column.softDelete()[1])) {
				Object delete = DOInfoReader.getValue(field, t);
				if(delete == null) {
					DOInfoReader.setValue(field, t, column.softDelete()[0]);
				}
			}

            if(InnerCommonUtils.isNotBlank(column.updateValueScript())) {
                ScriptUtils.setValueFromScript(t, field, column.ignoreScriptError(), column.updateValueScript());
            }

			// 优先级最低，自动trim字符串
			if (column.autoTrimString() == 1 || column.autoTrimString() == -1 && table.autoTrimString() == 1) {
				if (String.class.equals(field.getType())) {
					String value = (String) DOInfoReader.getValue(field, t);
					if (value != null) {
						DOInfoReader.setValue(field, t, value.trim());
					}
				}
			}

            // truncate string should be last
			if (column.maxStringLength() >= 0) {
				if (String.class.equals(field.getType())) {
					String value = (String) DOInfoReader.getValue(field, t);
					if (value != null && value.length() > column.maxStringLength()) {
						String newValue = value.substring(0, column.maxStringLength());
						DOInfoReader.setValue(field, t, newValue);
						LOGGER.warn("truncate class:{} field:{} value:{} to maxStringLength:{} newValue:{}",
								t.getClass().getName(), field.getName(), value, column.maxStringLength(), newValue);
					}
				}
			}
		}
	}

	/**
	 * 返回对应的类型的时间日期字符串，返回null表示不支持该clazz
	 */
	public static String getNowDateTime(Class<?> clazz) {
		if (clazz == Date.class || clazz == LocalDateTime.class || clazz == java.sql.Date.class
		    || clazz == Calendar.class || clazz == Instant.class || clazz == ZonedDateTime.class) {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		} else if (clazz == LocalDate.class) {
			return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		} else if (clazz == Time.class || clazz == LocalTime.class) {
			return new SimpleDateFormat("HH:mm:ss").format(new Date());
		} else if (clazz == Timestamp.class) {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		} else {
			LOGGER.error("fail to get now date time for class:{}", clazz);
			return null;
		}
	}

	private static void setNowDateTime(Field field, Object obj) {
		Class<?> clazz = field.getType();
		Object value = null;
		if (clazz == Date.class) {
			value = new Date(System.currentTimeMillis() / 1000 * 1000);
		} else if (clazz == LocalDateTime.class) {
			value = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		} else if (clazz == LocalDate.class) {
			value = LocalDate.now();
		} else if (clazz == java.sql.Date.class) {
			value = new java.sql.Date(System.currentTimeMillis() / 1000 * 1000);
		} else if (clazz == Timestamp.class) {
			value = new Timestamp(System.currentTimeMillis());
		} else if (clazz == LocalTime.class) {
			value = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
		} else if (clazz == Time.class) {
			value = new Time(System.currentTimeMillis() / 1000 * 1000);
		} else if (clazz == Calendar.class) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis() / 1000 * 1000);
			value = cal;
		} else if (clazz == Instant.class) {
			value = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		} else if (clazz == ZonedDateTime.class) {
			value = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		} else {
			LOGGER.error("fail to set now date time for class:{}", clazz);
		}

		if (value != null) {
			DOInfoReader.setValue(field, obj, value);
		}
	}
	
}
