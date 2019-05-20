package com.pugwoo.dbhelper.utils;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.exception.ScriptErrorException;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
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
		
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			
			if(column.softDelete() != null && column.softDelete().length == 2
					&& !column.softDelete()[0].trim().isEmpty()
					&& !column.softDelete()[1].trim().isEmpty()) {
				Object delete = DOInfoReader.getValue(field, t);
				if(delete == null) {
					DOInfoReader.setValue(field, t, column.softDelete()[0]);
				}
			}
			
			if(column.setTimeWhenInsert() && Date.class.isAssignableFrom(field.getType())) {
				if(DOInfoReader.getValue(field, t) == null) {
					DOInfoReader.setValue(field, t, new Date());
				}
			}
			
			if(column.insertDefault() != null && !column.insertDefault().isEmpty()) {
				if(DOInfoReader.getValue(field, t) == null) {
					DOInfoReader.setValue(field, t, column.insertDefault());
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

			String insertValueScript = column.insertValueScript().trim();
			if(!insertValueScript.isEmpty()) {
				Map<String, Object> vars = new HashMap<String, Object>();
				vars.put("t", t);
				try {
					Object value = MVEL.eval(insertValueScript, vars);
					DOInfoReader.setValue(field, t, value);
				} catch (Throwable e) {
					LOGGER.error("execute script fail: {}", insertValueScript, e);
					if(!column.ignoreScriptError()) {
						throw new ScriptErrorException(e);
					}
				}
			}

		}
	}
	
	public static <T> void preHandleUpdate(T t) {
		if(t == null) {
			return;
		}
		
		List<Field> notKeyFields = DOInfoReader.getNotKeyColumns(t.getClass());
		
		for(Field field : notKeyFields) {
			Column column = field.getAnnotation(Column.class);
			
			if(column.setTimeWhenUpdate() && Date.class.isAssignableFrom(field.getType())) {
				DOInfoReader.setValue(field, t, new Date());
			}
			
			if(column.softDelete() != null && column.softDelete().length == 2
					&& !column.softDelete()[0].trim().isEmpty()
					&& !column.softDelete()[1].trim().isEmpty()) {
				Object delete = DOInfoReader.getValue(field, t);
				if(delete == null) {
					DOInfoReader.setValue(field, t, column.softDelete()[0]);
				}
			}
		}
	}
	
}
