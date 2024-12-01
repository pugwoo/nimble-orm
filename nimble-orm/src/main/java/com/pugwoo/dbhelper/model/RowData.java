package com.pugwoo.dbhelper.model;

import com.pugwoo.dbhelper.utils.TypeAutoCast;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 行数据，该类可以传递到 getRaw 方法的第一个参数。
 * 该类是最接近ResultSet，该类同时可以当做Map.class使用
 */
public final class RowData extends AbstractMap<String, Object> {

    private Map<String, Object> row = new HashMap<>();

    @Override
    @NonNull
    public Set<Entry<String, Object>> entrySet() {
        return row.entrySet();
    }

    public Object put(String key, Object value) {
        return row.put(key, value);
    }

    /**
     * 查询指定列的值，以字符串的形式返回
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     */
    public Object getObject(String columnLabel) {
        return row.get(columnLabel);
    }

    /**
     * 查询指定列的值，以字符串的形式返回
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     */
    public String getString(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), String.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     */
    public Boolean getBoolean(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), Boolean.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     */
    public Byte getByte(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), Byte.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     */
    public Short getShort(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), Short.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     */
    public Integer getInt(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), Integer.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     */
    public Long getLong(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), Long.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public Float getFloat(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), Float.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public Double getDouble(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), Double.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public BigDecimal getBigDecimal(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), BigDecimal.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public byte[] getBytes(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), byte[].class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public java.sql.Date getSqlDate(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), java.sql.Date.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public java.sql.Time getSqlTime(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), java.sql.Time.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public java.sql.Timestamp getSqlTimestamp(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), java.sql.Timestamp.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public Date getDate(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), Date.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public LocalDate getLocalDate(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), LocalDate.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public LocalTime getLocalTime(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), LocalTime.class);
    }

    /**
     * 查询指定列的值
     * @param columnLabel 列名
     * @return 当该列值为null时，返回null
     * */
    public LocalDateTime getLocalDateTime(String columnLabel) {
        return TypeAutoCast.cast(row.get(columnLabel), LocalDateTime.class);
    }

}
