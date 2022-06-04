package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.IDBHelperDataService;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.exception.*;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.AnnotationSupportRowMapper;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;
import net.sf.jsqlparser.JSQLParserException;
import org.springframework.dao.EmptyResultDataAccessException;

import java.lang.reflect.Field;
import java.util.*;

public abstract class P1_QueryOp extends P0_JdbcTemplateOp {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> boolean getByKey(T t) throws NullKeyValueException {
        if (t == null) {
            return false;
        }
        Class<?> clazz = t.getClass();
        StringBuilder sql = new StringBuilder(SQLUtils.getSelectSQL(t.getClass(), false, false, features, null));

        List<Object> keyValues = new ArrayList<>();
        sql.append(SQLUtils.getKeysWhereSQL(t, keyValues));

        try {
            doInterceptBeforeQuery(t.getClass(), sql, keyValues);
            log(sql, keyValues);

            long start = System.currentTimeMillis();
            jdbcTemplate.queryForObject(sql.toString(),
                    new AnnotationSupportRowMapper(this, t.getClass(), t),
                    keyValues.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式

            postHandleRelatedColumn(t);
            logSlow(System.currentTimeMillis() - start, sql.toString(), keyValues);

            t = doInterceptAfterQuery(clazz, t, sql, keyValues);
            return t != null;
        } catch (EmptyResultDataAccessException e) {
            t = doInterceptAfterQuery(clazz, null, sql, keyValues);
            return t != null;
        }
    }

    @Override
    public <T> T getByKey(Class<T> clazz, Object keyValue) throws NullKeyValueException,
            NotOnlyOneKeyColumnException {

        if (keyValue == null) {
            throw new NullKeyValueException();
        }
        SQLAssert.onlyOneKeyColumn(clazz);

        StringBuilder sql = new StringBuilder();
        sql.append(SQLUtils.getSelectSQL(clazz, false, false, features, null));
        sql.append(SQLUtils.getKeysWhereSQL(clazz));

        List<Object> argsList = new ArrayList<>();
        argsList.add(keyValue);

        try {
            doInterceptBeforeQuery(clazz, sql, argsList);
            log(sql, argsList);

            long start = System.currentTimeMillis();
            T t = jdbcTemplate.queryForObject(sql.toString(),
                    new AnnotationSupportRowMapper<>(this, clazz),
                    argsList.toArray()); // 此处可以用jdbcTemplate，因为没有in (?)表达式

            postHandleRelatedColumn(t);

            long cost = System.currentTimeMillis() - start;

            List<Object> args = new ArrayList<>();
            args.add(keyValue);
            logSlow(cost, sql.toString(), args);

            t = doInterceptAfterQuery(clazz, t, sql, argsList);
            return t;
        } catch (EmptyResultDataAccessException e) {
            return doInterceptAfterQuery(clazz, null, sql, argsList);
        }
    }

    @Override
    public <T, K> Map<K, T> getByKeyList(Class<T> clazz, Collection<K> keyValues) {
        if (keyValues == null || keyValues.isEmpty()) {
            return new HashMap<>();
        }

        StringBuilder sql = new StringBuilder();
        sql.append(SQLUtils.getSelectSQL(clazz, false, false, features, null));
        sql.append(SQLUtils.getKeyInWhereSQL(clazz));

        List<Object> argsList = new ArrayList<>();
        argsList.add(keyValues);
        doInterceptBeforeQuery(clazz, sql, argsList);

        log(sql, argsList);

        long start = System.currentTimeMillis();
        List<T> list = namedParameterJdbcTemplate.query(
                NamedParameterUtils.trans(sql.toString(), argsList),
                NamedParameterUtils.transParam(argsList),
                new AnnotationSupportRowMapper<>(this, clazz)); // 因为有in (?)所以用namedParameterJdbcTemplate

        postHandleRelatedColumn(list);
        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql.toString(), argsList);

        list = doInterceptorAfterQueryList(clazz, list, list.size(), sql, argsList);

        // 转换to map
        if (list == null || list.isEmpty()) {
            return new HashMap<>();
        }
        Field keyField = DOInfoReader.getOneKeyColumn(clazz);
        Map<K, T> map = new LinkedHashMap<>();
        for (K key : keyValues) {
            if (key == null) {
                continue;
            }
            for (T t : list) {
                Object k = DOInfoReader.getValue(keyField, t);
                if (key.equals(k)) {
                    map.put(key, t);
                    break;
                }
            }
        }
        return map;
    }

    @Override
    public <T> PageData<T> getPage(final Class<T> clazz, int page, int pageSize,
                                   String postSql, Object... args) {
        if (page < 1) {
            throw new InvalidParameterException("[page] must greater than 0");
        }
        if (maxPageSize != null && pageSize > maxPageSize) {
            pageSize = maxPageSize;
        }

        if (postSql != null) {
            postSql = postSql.replace('\t', ' ');
        }
        int offset = (page - 1) * pageSize;
        return _getPage(clazz, true,false, true, offset, pageSize, postSql, args);
    }

    @Override
    public <T> PageData<T> getPage(final Class<T> clazz, int page, int pageSize) {
        return getPage(clazz, page, pageSize, null);
    }

    @Override
    public <T> long getCount(Class<T> clazz) {
        StringBuilder sql = new StringBuilder();
        sql.append(SQLUtils.getSelectCountSQL(clazz));
        sql.append(SQLUtils.autoSetSoftDeleted("", clazz));

        log(sql, null);
        long start = System.currentTimeMillis();
        Long rows = jdbcTemplate.queryForObject(sql.toString(), Long.class);

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql.toString(), null);
        return rows == null ? 0 : rows;
    }

    // 为了解决group by的计数问题，将计数转换成select count(*) from (子select语句) 的形式
    @Override
    public <T> long getCount(Class<T> clazz, String postSql, Object... args) {
        if (postSql != null) {
            postSql = postSql.replace('\t', ' ');
        }

        StringBuilder sql = new StringBuilder("SELECT count(*) FROM (");

        sql.append(SQLUtils.getSelectSQL(clazz, false, true, features, postSql));
        sql.append(SQLUtils.autoSetSoftDeleted(postSql, clazz));

        sql.append(") tff305c6");

        List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
        if (args != null) {
            argsList.addAll(Arrays.asList(args));
        }

        log(sql, argsList);

        long start = System.currentTimeMillis();

        Long rows;
        if (argsList.isEmpty()) {
            rows = namedParameterJdbcTemplate.queryForObject(sql.toString(), new HashMap<>(),
                    Long.class); // 因为有in (?)所以用namedParameterJdbcTemplate
        } else {
            rows = namedParameterJdbcTemplate.queryForObject(
                    NamedParameterUtils.trans(sql.toString(), argsList),
                    NamedParameterUtils.transParam(argsList),
                    Long.class); // 因为有in (?)所以用namedParameterJdbcTemplate
        }

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql.toString(), null);
        return rows == null ? 0 : rows;
    }

    @Override
    public <T> PageData<T> getPageWithoutCount(Class<T> clazz, int page, int pageSize,
                                               String postSql, Object... args) {
        if (page < 1) {
            throw new InvalidParameterException("[page] must greater than 0");
        }
        if (maxPageSize != null && pageSize > maxPageSize) {
            pageSize = maxPageSize;
        }

        if (postSql != null) {
            postSql = postSql.replace('\t', ' ');
        }
        int offset = (page - 1) * pageSize;
        return _getPage(clazz, true, false, false, offset, pageSize, postSql, args);
    }

    @Override
    public <T> PageData<T> getPageWithoutCount(final Class<T> clazz, int page, int pageSize) {
        return getPageWithoutCount(clazz, page, pageSize, null);
    }

    @Override
    public <T> List<T> getAll(final Class<T> clazz) {
        return _getPage(clazz, true, false, false, null, null, null).getData();
    }

    @Override
    public <T> List<T> getAll(final Class<T> clazz, String postSql, Object... args) {
        if (postSql != null) {
            postSql = postSql.replace('\t', ' ');
        }
        return _getPage(clazz, true,false, false, null, null, postSql, args).getData();
    }

    @Override
    public <T> List<T> getAllKey(Class<T> clazz, String postSql, Object... args) {
        if (postSql != null) {
            postSql = postSql.replace('\t', ' ');
        }
        return _getPage(clazz, true, true, false, null, null, postSql, args).getData();
    }

    @Override
    public <T> T getOne(Class<T> clazz) {
        List<T> list = _getPage(clazz, true, false, false, 0, 1, null).getData();
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    @Override
    public <T> T getOne(Class<T> clazz, String postSql, Object... args) {
        if (postSql != null) {
            postSql = postSql.replace('\t', ' ');
        }
        List<T> list = _getPage(clazz, true, false, false,
                0, 1, postSql, args).getData();
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    @Override
    public <T> List<T> getRaw(Class<T> clazz, String sql, Map<String, Object> args) {
        return getRawByNamedParam(clazz, sql, args);
    }

    private <T> List<T> getRawByNamedParam(Class<T> clazz, String sql, Map<String, Object> args) {
        List<Object> forIntercept = new ArrayList<>();
        if (args != null) {
            forIntercept.add(args);
        }

        doInterceptBeforeQuery(clazz, sql, forIntercept);

        log(sql, forIntercept);
        long start = System.currentTimeMillis();

        List<T> list;
        if (args == null || args.isEmpty()) {
            list = namedParameterJdbcTemplate.query(sql,
                    new AnnotationSupportRowMapper<>(this, clazz, false));
        } else {
            list = namedParameterJdbcTemplate.query(sql, args,
                    new AnnotationSupportRowMapper<>(this, clazz, false));
        }

        postHandleRelatedColumn(list);

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, forIntercept);

        doInterceptorAfterQueryList(clazz, list, -1, sql, forIntercept);

        return list;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> List<T> getRaw(Class<T> clazz, String sql, Object... args) {
        // 解决如果java选择错重载的问题
        if (args != null && args.length == 1 && args[0] instanceof Map) {
            return getRawByNamedParam(clazz, sql, (Map<String, Object>)(args[0]));
        }

        List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
        if (args != null) {
            argsList.addAll(Arrays.asList(args));
        }

        doInterceptBeforeQuery(clazz, sql, argsList);

        log(sql, argsList);

        long start = System.currentTimeMillis();
        List<T> list;
        if (argsList.isEmpty()) {
            list = namedParameterJdbcTemplate.query(sql,
                    new AnnotationSupportRowMapper<>(this, clazz, false));
        } else {
            list = namedParameterJdbcTemplate.query(
                    NamedParameterUtils.trans(sql, argsList),
                    NamedParameterUtils.transParam(argsList),
                    new AnnotationSupportRowMapper<>(this, clazz, false));
        }

        postHandleRelatedColumn(list);

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, argsList);


        doInterceptorAfterQueryList(clazz, list, -1, sql, argsList);

        return list;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> List<T> getByExample(T t, int limit) {
        Map<Field, String> filed2column = new HashMap<>();
        List<Field> declaredFields = DOInfoReader.getColumns(t.getClass());
        for (Field declaredField : declaredFields) {
            Column annotation = declaredField.getAnnotation(Column.class);
            if (annotation != null) {
                filed2column.put(declaredField, annotation.value());
            }
        }

        List<String> cols = new ArrayList<>();
        List<Object> args = new ArrayList<>();

        for (Map.Entry<Field, String> filed : filed2column.entrySet()) {
            Object value = DOInfoReader.getValue(filed.getKey(), t);
            if (value != null) {
                cols.add(filed.getValue());
                args.add(value);
            }
        }

        StringBuilder sql = new StringBuilder();
        if (!cols.isEmpty()) {
            sql.append(" WHERE ");
        }

        int size = cols.size();
        for (int i = 0; i < size; i++) {
            sql.append(SQLUtils.getColumnName(cols.get(i))).append("=?");
            if (i != size - 1) {
                sql.append(" AND ");
            }
        }

        return (List<T>) _getPage(t.getClass(), true, false,
                false, null, limit, sql.toString(), args.toArray()).getData();
    }

    @Override
    public long getRawCount(String sql, Map<String, Object> args) {
        return getRowCountByNamedParam(sql, args);
    }

    private long getRowCountByNamedParam(String sql, Map<String, Object> args) {
        log(sql, args);

        long start = System.currentTimeMillis();

        Long rows;
        if (args == null || args.isEmpty()) {
            rows = namedParameterJdbcTemplate.queryForObject(sql, new HashMap<>(),
                    Long.class); // 因为有in (?)所以用namedParameterJdbcTemplate
        } else {
            rows = namedParameterJdbcTemplate.queryForObject(
                    sql, args, Long.class); // 因为有in (?)所以用namedParameterJdbcTemplate
        }

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, null);
        return rows == null ? 0 : rows;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public long getRawCount(String sql, Object... args) {
        // 解决如果java选择错重载的问题
        if (args != null && args.length == 1 && args[0] instanceof Map) {
            return getRowCountByNamedParam(sql, (Map<String, Object>)(args[0]));
        }

        List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
        if (args != null) {
            argsList.addAll(Arrays.asList(args));
        }

        log(sql, argsList);

        long start = System.currentTimeMillis();

        Long rows;
        if (argsList.isEmpty()) {
            rows = namedParameterJdbcTemplate.queryForObject(sql, new HashMap<>(),
                    Long.class); // 因为有in (?)所以用namedParameterJdbcTemplate
        } else {
            rows = namedParameterJdbcTemplate.queryForObject(
                    NamedParameterUtils.trans(sql, argsList),
                    NamedParameterUtils.transParam(argsList),
                    Long.class); // 因为有in (?)所以用namedParameterJdbcTemplate
        }

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, null);
        return rows == null ? 0 : rows;
    }

    /**
     * 查询列表
     *
     * @param clazz 注解了@Table的类
     * @param selectOnlyKey 是否只查询主键，只查询主键时，拦截器不进行拦截，RelatedColumn也不处理
     * @param withCount 是否计算总数
     * @param offset 从0开始，null时不生效；当offset不为null时，要求limit存在
     * @param limit null时不生效
     * @param postSql sql的where/group/order等sql语句
     * @param args 参数
     */
    private <T> PageData<T> _getPage(Class<T> clazz, boolean isUseNamedTemplate,
                                     boolean selectOnlyKey, boolean withCount,
                                     Integer offset, Integer limit,
                                     String postSql, Object... args) {

        StringBuilder sql = new StringBuilder();
        sql.append(SQLUtils.getSelectSQL(clazz, selectOnlyKey, false, features, postSql));
        // 当limit不为null时，分页由orm内部控制，此时postSql不应该包含limit子句，这里尝试去除
        if (limit != null) {
            try {
                boolean autoAddOrderForPagination = getFeature(FeatureEnum.AUTO_ADD_ORDER_FOR_PAGINATION);
                postSql = SQLUtils.removeLimitAndAddOrder(postSql, autoAddOrderForPagination, clazz);
            } catch (Exception e) {
                LOGGER.error("removeLimitAndAddOrder fail for class:{}, postSql:{}",
                        clazz, postSql, e);
            }
        }
        sql.append(SQLUtils.autoSetSoftDeleted(postSql, clazz));
        sql.append(SQLUtils.genLimitSQL(offset, limit));

        List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
        if (args != null) {
            argsList.addAll(Arrays.asList(args));
        }

        if (!selectOnlyKey) {
            doInterceptBeforeQuery(clazz, sql, argsList);
        }

        log(sql, argsList);

        long start = System.currentTimeMillis();
        List<T> list;
        if (argsList.isEmpty()) {
            if (isUseNamedTemplate) {
                // 因为有in (?)所以用namedParameterJdbcTemplate
                list = namedParameterJdbcTemplate.query(sql.toString(),
                        new AnnotationSupportRowMapper<>(this, clazz, selectOnlyKey));
            } else {
                list = jdbcTemplate.query(sql.toString(),
                        new AnnotationSupportRowMapper<>(this, clazz, selectOnlyKey));
            }
        } else {
            if (isUseNamedTemplate) {
                // 因为有in (?)所以用namedParameterJdbcTemplate
                list = namedParameterJdbcTemplate.query(
                        NamedParameterUtils.trans(sql.toString(), argsList),
                        NamedParameterUtils.transParam(argsList),
                        new AnnotationSupportRowMapper<>(this, clazz, selectOnlyKey));
            } else {
                list = jdbcTemplate.query(sql.toString(),
                        new AnnotationSupportRowMapper<>(this, clazz, selectOnlyKey), argsList.toArray());
            }
        }

        long total = -1; // -1 表示没有查询总数，未知
        if (withCount) {
            // 如果offset为0且查询的list小于limit数量，说明总数就这么多了，不需要再查总数了
            if(offset != null && offset == 0 && limit != null && list.size() < limit) {
                total = list.size();
            } else {
                if(postSql == null) {
                    total = getCount(clazz);
                } else {
                    total = getCount(clazz, postSql, args);
                }
            }
        }

        if (!selectOnlyKey) {
            postHandleRelatedColumn(list);
        }

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql.toString(), argsList);

        if (!selectOnlyKey) {
            doInterceptorAfterQueryList(clazz, list, total, sql, argsList);
        }

        PageData<T> pageData = new PageData<>();
        pageData.setData(list);
        pageData.setTotal(total);
        if (limit != null) {
            pageData.setPageSize(limit);
        }

        return pageData;
    }

    @Override
    public <T> boolean isExist(Class<T> clazz, String postSql, Object... args) {
        if (postSql != null) {
            postSql = postSql.replace('\t', ' ');
        }
        return getOne(clazz, postSql, args) != null;
    }

    @Override
    public <T> boolean isExistAtLeast(int atLeastCounts, Class<T> clazz,
                                      String postSql, Object... args) {
        if (postSql != null) {
            postSql = postSql.replace('\t', ' ');
        }
        if (atLeastCounts == 1) {
            return isExist(clazz, postSql, args);
        }
        return getCount(clazz, postSql, args) >= atLeastCounts;
    }

    //////////////////// 拦截器封装方法

    private void doInterceptBeforeQuery(Class<?> clazz, StringBuilder sql, List<Object> args) {
        doInterceptBeforeQuery(clazz, sql.toString(), args);
    }

    private void doInterceptBeforeQuery(Class<?> clazz, String sql, List<Object> args) {
        for (DBHelperInterceptor interceptor : interceptors) {
            boolean isContinue = interceptor.beforeSelect(clazz, sql, args);
            if (!isContinue) {
                throw new NotAllowQueryException("interceptor class:" + interceptor.getClass());
            }
        }
    }

    /**t为null表示没有记录，因此等价于空list*/
    private <T> T doInterceptAfterQuery(Class<?> clazz, T t, StringBuilder sql, List<Object> args) {
        List<T> list = new ArrayList<>();
        if (t != null) {
            list.add(t);
        }
        list = doInterceptorAfterQueryList(clazz, list, 1, sql, args);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    private <T> List<T> doInterceptorAfterQueryList(Class<?> clazz, List<T> list, long total,
                                                    StringBuilder sql, List<Object> args) {
        return doInterceptorAfterQueryList(clazz, list, total, sql.toString(), args);
    }

    private <T> List<T> doInterceptorAfterQueryList(Class<?> clazz, List<T> list, long total,
                                                    String sql, List<Object> args) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            list = interceptors.get(i).afterSelect(clazz, sql, args, list, total);
        }
        return list;
    }

    // ======================= 处理 RelatedColumn数据 ========================

    @Override
    public <T> void handleRelatedColumn(T t) {
        postHandleRelatedColumn(t);
    }

    @Override
    public <T> void handleRelatedColumn(T t, String... relatedColumnProperties) {
        postHandleRelatedColumn(t, relatedColumnProperties);
    }

    @Override
    public <T> void handleRelatedColumn(List<T> list) {
        postHandleRelatedColumn(list);
    }

    @Override
    public <T> void handleRelatedColumn(List<T> list, String... relatedColumnProperties) {
        postHandleRelatedColumn(list, relatedColumnProperties);
    }

    /**单个关联*/
    private <T> void postHandleRelatedColumn(T t, String... relatedColumnProperties) {
        if (t == null) {
            return;
        }
        List<T> list = new ArrayList<>();
        list.add(t);

        postHandleRelatedColumn(list, relatedColumnProperties);
    }

    /**批量关联，要求批量操作的都是相同的类*/
    private <T> void postHandleRelatedColumn(List<T> tList, String... relatedColumnProperties) {
        if (tList == null || tList.isEmpty()) {
            return;
        }
        Class<?> clazz = getElementClass(tList);
        if (clazz == null) {
            return;
        }

        JoinTable joinTable = DOInfoReader.getJoinTable(clazz);
        if (joinTable != null) { // 处理join的方式
            SQLAssert.allSameClass(tList);

            List<Object> list1 = new ArrayList<>();
            List<Object> list2 = new ArrayList<>();

            Field joinLeftTableFiled = DOInfoReader.getJoinLeftTable(clazz);
            Field joinRightTableFiled = DOInfoReader.getJoinRightTable(clazz);
            for (T t : tList) {
                Object obj1 = DOInfoReader.getValue(joinLeftTableFiled, t);
                if (obj1 != null) {
                    list1.add(obj1);
                }
                Object obj2 = DOInfoReader.getValue(joinRightTableFiled, t);
                if (obj2 != null) {
                    list2.add(obj2);
                }
            }

            postHandleRelatedColumn(list1);
            postHandleRelatedColumn(list2);
            return;
        }

        List<Field> relatedColumns = DOInfoReader.getRelatedColumns(clazz);
        if (!relatedColumns.isEmpty()) { // 只有有relatedColumn才进行判断是否全是相同的类型
            SQLAssert.allSameClass(tList);
        }

        for (Field field : relatedColumns) {

            // 只处理指定的field
            if (InnerCommonUtils.isNotEmpty(relatedColumnProperties)) {
                boolean isContain = InnerCommonUtils.isContains(field.getName(), relatedColumnProperties);
                if (!isContain) {
                    continue;
                }
            }

            RelatedColumn column = field.getAnnotation(RelatedColumn.class);
            if (InnerCommonUtils.isBlank(column.localColumn())) {
                LOGGER.error("relatedColumn value is empty, field:{}", field);
                continue;
            }
            if (InnerCommonUtils.isBlank(column.remoteColumn())) {
                LOGGER.error("remoteColumn value is empty, field:{}", field);
                continue;
            }

            List<Field> localField = DOInfoReader.getFieldByDBField(clazz, column.localColumn());
            if (localField.isEmpty()) {
                LOGGER.error("cannot find all localField, db column name:{}", column.localColumn());
                continue;
            }

            // 批量查询数据库，提高效率的关键
            Class<?> remoteDOClass;
            if (field.getType() == List.class) {
                remoteDOClass = DOInfoReader.getGenericFieldType(field);
            } else {
                remoteDOClass = field.getType();
            }

            List<Field> remoteField = DOInfoReader.getFieldByDBField(remoteDOClass, column.remoteColumn());
            if (remoteField.isEmpty()) {
                LOGGER.error("cannot find remoteField,db column name:{}", column.remoteColumn());
                continue;
            }

            Set<Object> values = new HashSet<>(); // 用于去重，同样适用于ArrayList
            for (T t : tList) {
                Object value = DOInfoReader.getValueForRelatedColumn(localField, t);
                if (value != null) {
                    values.add(value);
                }
            }

            if (values.isEmpty()) {
                // 不需要查询数据库，但是对List的，设置空List，确保list不会是null
                if (field.getType() == List.class) {
                    for (T t : tList) {
                        DOInfoReader.setValue(field, t, new ArrayList<>());
                    }
                }
                continue;
            }

            List<?> relateValues;
            if (column.dataService() != void.class &&
                    IDBHelperDataService.class.isAssignableFrom(column.dataService())) {
                IDBHelperDataService dataService = (IDBHelperDataService)
                        applicationContext.getBean(column.dataService());
                List<Object> valuesList = new ArrayList<>(values);
                relateValues = dataService.get(valuesList, column, clazz, remoteDOClass);
            } else {
                String whereColumn;
                boolean isSingleColumn = remoteField.size() == 1;
                StringBuilder sb = new StringBuilder(isSingleColumn ? "" : "(");
                boolean isFirst = true;
                for (Field remoteF : remoteField) {
                    if (!isFirst) {
                        sb.append(",");
                    }
                    isFirst = false;

                    Column remoteColumn = remoteF.getAnnotation(Column.class);
                    if (InnerCommonUtils.isBlank(remoteColumn.computed())) {
                        sb.append(SQLUtils.getColumnName(remoteColumn.value()));
                    } else {
                        sb.append(SQLUtils.getComputedColumn(remoteColumn, features));
                    }
                }
                sb.append(isSingleColumn ? "" : ")");
                whereColumn = sb.toString();

                if (InnerCommonUtils.isBlank(column.extraWhere())) {
                    String inExpr = whereColumn + " in " + buildQuestionMark(values);
                    relateValues = getAllForRelatedColumn(remoteDOClass, "where " + inExpr, values);
                } else {
                    // 如果extraWhere包含limit子句，那么只能降级为逐个处理，否则可以用批量处理的方式提高性能
                    if (SQLUtils.isContainsLimit(column.extraWhere())) {
                        try {
                            String eqExpr = whereColumn + "=?";
                            String where = SQLUtils.insertWhereAndExpression(column.extraWhere(), eqExpr);
                            relateValues = getAllForRelatedColumnBySingleValue(remoteDOClass, where, values);
                        } catch (JSQLParserException e) {
                            LOGGER.error("wrong RelatedColumn extraWhere:{}, ignore extraWhere", column.extraWhere());
                            throw new BadSQLSyntaxException(e);
                        }
                    } else {
                        try {
                            String inExpr = whereColumn + " in " + buildQuestionMark(values);
                            String where = SQLUtils.insertWhereAndExpression(column.extraWhere(), inExpr);
                            relateValues = getAllForRelatedColumn(remoteDOClass, where, values);
                        } catch (JSQLParserException e) {
                            LOGGER.error("wrong RelatedColumn extraWhere:{}, ignore extraWhere", column.extraWhere());
                            throw new BadSQLSyntaxException(e);
                        }
                    }
                }
            }

            if (field.getType() == List.class) {
                Map<Object, List<Object>> mapRemoteValues = new HashMap<>();
                Map<String, List<Object>> mapRemoteValuesString = new HashMap<>();
                for (Object obj : relateValues) {
                    Object oRemoteValue = DOInfoReader.getValueForRelatedColumn(remoteField, obj);
                    if (oRemoteValue == null) {continue;}

                    List<Object> oRemoteValueList = mapRemoteValues.computeIfAbsent(
                            oRemoteValue, k -> new ArrayList<>());
                    oRemoteValueList.add(obj);

                    List<Object> oRemoteValueListString = mapRemoteValuesString.computeIfAbsent(
                            oRemoteValue.toString(), k -> new ArrayList<>());
                    oRemoteValueListString.add(obj);
                }
                for (T t : tList) {
                    List<Object> valueList = new ArrayList<>();
                    Object oLocalValue = DOInfoReader.getValueForRelatedColumn(localField, t);
                    if (oLocalValue != null) {
                        List<Object> objRemoteList = mapRemoteValues.get(oLocalValue);
                        if (objRemoteList != null) {
                            valueList = objRemoteList;
                        } else {
                            List<Object> objRemoteStringList = mapRemoteValuesString.get(oLocalValue.toString());
                            if (objRemoteStringList != null) {
                                LOGGER.error("@RelatedColumn fields local:{},remote:{} is different classes. Use String compare.",
                                        localField, remoteField);
                                valueList = objRemoteStringList;
                            }
                        }
                    }
                    if (valueList.isEmpty()) { // 没有匹配数据时，当原字段有值，则不修改原来的值
                        if (DOInfoReader.getValue(field, t) == null) {
                            DOInfoReader.setValue(field, t, valueList);
                        }
                    } else {
                        DOInfoReader.setValue(field, t, valueList);
                    }
                }
            } else {
                Map<Object, Object> mapRemoteValues = new HashMap<>();
                Map<String, Object> mapRemoteValuesString = new HashMap<>();
                for (Object obj : relateValues) {
                    Object oRemoteValue = DOInfoReader.getValueForRelatedColumn(remoteField, obj);
                    if (oRemoteValue != null && !mapRemoteValues.containsKey(oRemoteValue)) {
                        mapRemoteValues.put(oRemoteValue, obj);
                        mapRemoteValuesString.put(oRemoteValue.toString(), obj);
                    }
                }
                for (T t : tList) {
                    Object oLocalValue = DOInfoReader.getValueForRelatedColumn(localField, t);
                    if (oLocalValue == null) {continue;}
                    
                    Object objRemote = mapRemoteValues.get(oLocalValue);
                    if (objRemote != null) {
                        DOInfoReader.setValue(field, t, objRemote);
                        continue;
                    }
                    Object objRemoteString = mapRemoteValuesString.get(oLocalValue.toString());
                    if (objRemoteString != null) {
                        LOGGER.error("@RelatedColumn fields local:{},remote:{} are different classes. Use String compare.",
                                localField, remoteField);
                        DOInfoReader.setValue(field, t, objRemoteString);
                    }
                }
            }
        }
    }

    private String buildQuestionMark(Set<Object> values) {
        StringBuilder sb = new StringBuilder("(");
        boolean isFirst = true;
        for (Object obj : values) {
            if (!isFirst) {
                sb.append(",");
            }
            isFirst = false;

            if (obj instanceof List) {
                sb.append("(");
                int size = ((List<?>)obj).size();
                for (int i = 0; i < size; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("?");
                }
                sb.append(")");
            } else {
                sb.append("?");
            }

        }
        sb.append(")");
        return sb.toString();
    }

    private <T> List<T> getAllForRelatedColumn(final Class<T> clazz, String postSql, Set<Object> values) {
        if (postSql != null) {
            postSql = postSql.replace('\t', ' ');
        }

        List<Object> param = new ArrayList<>();
        for (Object obj : values) {
            if (obj instanceof List) {
                param.addAll((List<?>) obj);
            } else {
                param.add(obj);
            }
        }

        return _getPage(clazz, false, false,
                false, null, null, postSql, param.toArray()).getData();
    }

    private <T> List<T> getAllForRelatedColumnBySingleValue(final Class<T> clazz, String postSql, Set<Object> values) {
        if (postSql != null) {
            postSql = postSql.replace('\t', ' ');
        }

        List<T> result = new ArrayList<>();

        for (Object value : values) {
            List<Object> param = new ArrayList<>();
            param.add(value);

            List<T> results = _getPage(clazz, false, false,
                    false, null, null, postSql, param.toArray()).getData();
            result.addAll(results);
        }

        return result;
    }

    private Class<?> getElementClass(List<?> tList) {
        for (Object obj : tList) {
            if (obj != null) {
                return obj.getClass();
            }
        }
        return null;
    }

}
