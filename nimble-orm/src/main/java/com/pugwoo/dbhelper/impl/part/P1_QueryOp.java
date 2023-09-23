package com.pugwoo.dbhelper.impl.part;

import com.pugwoo.dbhelper.DBHelperInterceptor;
import com.pugwoo.dbhelper.IDBHelperDataService;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.JoinTable;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.exception.*;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.sql.SQLAssert;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.utils.AnnotationSupportRowMapper;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.NamedParameterUtils;
import net.sf.jsqlparser.JSQLParserException;
import org.mvel2.MVEL;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

public abstract class P1_QueryOp extends P0_JdbcTemplateOp {

    @Override
    public <T> T getByKey(Class<T> clazz, Object keyValue) throws NullKeyValueException, NotOnlyOneKeyColumnException {
        assertNotVirtualTable(clazz);
        if (keyValue == null) {
            throw new NullKeyValueException();
        }
        SQLAssert.onlyOneKeyColumn(clazz);

        String where = SQLUtils.getKeysWhereSQLWithoutSoftDelete(clazz);
        return getOne(clazz, where, keyValue);
    }

    @Override
    public <T> PageData<T> getPage(final Class<T> clazz, int page, int pageSize,
                                   String postSql, Object... args) {
        assertPage(page);
        if (maxPageSize != null && pageSize > maxPageSize) {
            LOGGER.warn("query class:{} pageSize {} is too large, set to maxPageSize {}", clazz, pageSize, maxPageSize);
            pageSize = maxPageSize;
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
        boolean isVirtualTable = DOInfoReader.isVirtualTable(clazz);

        String sql = SQLUtils.getSelectCountSQL(clazz, getDatabaseType()) +
                (isVirtualTable ? "" : SQLUtils.autoSetSoftDeleted("", clazz));
        sql = addComment(sql);

        log(sql, 0, null);
        long start = System.currentTimeMillis();
        Long rows = jdbcTemplate.queryForObject(sql, Long.class);

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, 0, null);
        return rows == null ? 0 : rows;
    }

    // 为了解决group by的计数问题，将计数转换成select count(*) from (子select语句) 的形式
    @Override
    public <T> long getCount(Class<T> clazz, String postSql, Object... args) {
        boolean isVirtualTable = DOInfoReader.isVirtualTable(clazz);

        String sqlSB = "SELECT count(*) FROM ("
                + SQLUtils.getSelectSQL(clazz, false, true, features, postSql, getDatabaseType())
                + (isVirtualTable ? (postSql == null ? "\n" : "\n" + postSql) : SQLUtils.autoSetSoftDeleted(postSql, clazz))
                + ") tff305c6";

        List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
        if (args != null) {
            argsList.addAll(Arrays.asList(args));
        }

        String sql = sqlSB;
        sql = addComment(sql);
        log(sql, 0, argsList);

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
        logSlow(cost, sql, 0, null);
        return rows == null ? 0 : rows;
    }

    @Override
    public <T> PageData<T> getPageWithoutCount(Class<T> clazz, int page, int pageSize,
                                               String postSql, Object... args) {
        assertPage(page);
        if (maxPageSize != null && pageSize > maxPageSize) {
            LOGGER.warn("query class:{} pageSize {} is too large, set to maxPageSize {}", clazz, pageSize, maxPageSize);
            pageSize = maxPageSize;
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
    public <T> Stream<T> getAllForStream(Class<T> clazz) {
        return getAllForStream(clazz, "");
    }

    @Override
    public <T> Stream<T> getAllForStream(Class<T> clazz, String postSql, Object... args) {
        jdbcTemplate.setFetchSize(fetchSize);

        StringBuilder sqlSB = new StringBuilder();
        sqlSB.append(SQLUtils.getSelectSQL(clazz, false, false, features, postSql, getDatabaseType()));
        sqlSB.append(SQLUtils.autoSetSoftDeleted(postSql, clazz));

        List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
        if (args != null) {
            argsList.addAll(Arrays.asList(args));
        }

        doInterceptBeforeQuery(clazz, sqlSB, argsList);

        String sql = sqlSB.toString();
        sql = addComment(sql);
        log(sql, 0, argsList);

        long start = System.currentTimeMillis();

        Stream<T> list;

        AnnotationSupportRowMapper<T> mapper = new AnnotationSupportRowMapper<>(this, clazz);
        if (argsList.isEmpty()) {
            list = jdbcTemplate.queryForStream(sql, mapper);
        } else {
            // 因为有in (?)所以用namedParameterJdbcTemplate
            list = namedParameterJdbcTemplate.queryForStream(
                    NamedParameterUtils.trans(sql, argsList),
                    NamedParameterUtils.transParam(argsList),
                    mapper);
        }

        Stream<T> result;
        List<Field> relatedColumns = DOInfoReader.getRelatedColumns(clazz);
        if (!relatedColumns.isEmpty()) {
            result = InnerCommonUtils.partition(list, fetchSize)
                    .peek(this::handleRelatedColumn)
                    .flatMap(Collection::stream);
        } else {
            result = list;
        }

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, 0, argsList);

        // stream方式不支持doInterceptorAfterQueryList
        return result;
    }

    @Override
    public <T> List<T> getAll(final Class<T> clazz, String postSql, Object... args) {
        return _getPage(clazz, true,false, false, null, null, postSql, args).getData();
    }

    @Override
    public <T> List<T> getAllKey(Class<T> clazz, String postSql, Object... args) {
        assertNotVirtualTable(clazz);

        return _getPage(clazz, true, true, false, null, null, postSql, args).getData();
    }

    @Override
    public <T> T getOne(Class<T> clazz) {
        List<T> list = _getPage(clazz, true, false, false, 0, 1, null).getData();
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    @Override
    public <T> T getOne(Class<T> clazz, String postSql, Object... args) {
        List<T> list = _getPage(clazz, true, false, false,
                0, 1, postSql, args).getData();
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    @Override
    public <T> List<T> getRaw(Class<T> clazz, String sql, Map<String, ?> args) {
        return getRawByNamedParam(clazz, sql, args);
    }

    @Override
    public <T> Stream<T> getRawForStream(Class<T> clazz, String sql, Map<String, ?> args) {
        return getRawByNamedParamForStream(clazz, sql, args);
    }

    private <T> Stream<T> getRawByNamedParamForStream(Class<T> clazz, String sql, Map<String, ?> args) {
        jdbcTemplate.setFetchSize(fetchSize);

        List<Object> forIntercept = new ArrayList<>();
        if (args != null) {
            forIntercept.add(args);
        }
        doInterceptBeforeQuery(clazz, sql, forIntercept);

        sql = addComment(sql);
        log(sql, 0, forIntercept);
        long start = System.currentTimeMillis();


        Stream<T> stream;
        if (args == null || args.isEmpty()) {
            stream = namedParameterJdbcTemplate.queryForStream(sql, new HashMap<>(),
                    new AnnotationSupportRowMapper<>(this, clazz, false));
        } else {
            stream = namedParameterJdbcTemplate.queryForStream(sql, args,
                    new AnnotationSupportRowMapper<>(this, clazz, false));
        }

        Stream<T> result;
        List<Field> relatedColumns = DOInfoReader.getRelatedColumns(clazz);
        if (!relatedColumns.isEmpty()) {
            result = InnerCommonUtils.partition(stream, fetchSize)
                    .peek(this::handleRelatedColumn)
                    .flatMap(Collection::stream);
        } else {
            result = stream;
        }

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, 0, forIntercept);

        // stream方式不支持doInterceptorAfterQueryList
        return result;
    }

    private <T> List<T> getRawByNamedParam(Class<T> clazz, String sql, Map<String, ?> args) {
        List<Object> forIntercept = new ArrayList<>();
        if (args != null) {
            forIntercept.add(args);
        }
        doInterceptBeforeQuery(clazz, sql, forIntercept);

        sql = addComment(sql);
        log(sql, 0, forIntercept);
        long start = System.currentTimeMillis();

        List<T> list;
        if (args == null || args.isEmpty()) {
            list = namedParameterJdbcTemplate.query(sql,
                    new AnnotationSupportRowMapper<>(this, clazz, false));
        } else {
            list = namedParameterJdbcTemplate.query(sql, args,
                    new AnnotationSupportRowMapper<>(this, clazz, false));
        }

        handleRelatedColumn(list);

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, 0, forIntercept);

        doInterceptorAfterQueryList(clazz, list, -1, sql, forIntercept);

        return list;
    }

    @Override
    public <T> Stream<T> getRawForStream(Class<T> clazz, String sql, Object... args) {
        jdbcTemplate.setFetchSize(fetchSize);

        List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
        if (args != null) {
            argsList.addAll(Arrays.asList(args));
        }
        doInterceptBeforeQuery(clazz, sql, argsList);

        sql = addComment(sql);
        log(sql, 0, argsList);

        long start = System.currentTimeMillis();
        Stream<T> stream;
        if (argsList.isEmpty()) {
            stream = namedParameterJdbcTemplate.queryForStream(sql, new HashMap<>(),
                    new AnnotationSupportRowMapper<>(this, clazz, false));
        } else {
            stream = namedParameterJdbcTemplate.queryForStream(
                    NamedParameterUtils.trans(sql, argsList),
                    NamedParameterUtils.transParam(argsList),
                    new AnnotationSupportRowMapper<>(this, clazz, false));
        }

        Stream<T> result;
        List<Field> relatedColumns = DOInfoReader.getRelatedColumns(clazz);
        if (!relatedColumns.isEmpty()) {
            result = InnerCommonUtils.partition(stream, fetchSize)
                    .peek(this::handleRelatedColumn)
                    .flatMap(Collection::stream);
        } else {
            result = stream;
        }

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, 0, argsList);

        // stream方式不支持doInterceptorAfterQueryList
        return result;
    }

    @Override
    public <T> List<T> getRaw(Class<T> clazz, String sql, Object... args) {
        List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
        if (args != null) {
            argsList.addAll(Arrays.asList(args));
        }
        doInterceptBeforeQuery(clazz, sql, argsList);

        sql = addComment(sql);
        log(sql, 0, argsList);

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

        handleRelatedColumn(list);

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, 0, argsList);


        doInterceptorAfterQueryList(clazz, list, -1, sql, argsList);

        return list;
    }

    @Override
    public <T> T getRawOne(Class<T> clazz, String sql, Object... args) {
        List<T> raw = getRaw(clazz, sql, args);
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        return raw.get(0);
    }

    @Override
    public <T> T getRawOne(Class<T> clazz, String sql, Map<String, ?> args) {
        List<T> raw = getRaw(clazz, sql, args);
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        return raw.get(0);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> List<T> getByExample(T t, int limit) {
        assertNotVirtualTable(t.getClass());

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
        boolean isVirtualTable = DOInfoReader.isVirtualTable(clazz);

        StringBuilder sqlSB = new StringBuilder();
        sqlSB.append(SQLUtils.getSelectSQL(clazz, selectOnlyKey, false, features, postSql, getDatabaseType()));
        // 当limit不为null时，分页由orm内部控制，此时postSql不应该包含limit子句，这里尝试去除
        if (limit != null && !isVirtualTable) {
            try {
                boolean autoAddOrderForPagination = getFeature(FeatureEnum.AUTO_ADD_ORDER_FOR_PAGINATION);
                postSql = SQLUtils.removeLimitAndAddOrder(postSql, autoAddOrderForPagination, clazz);
            } catch (Exception e) {
                LOGGER.error("removeLimitAndAddOrder fail for class:{}, postSql:{}",
                        clazz, postSql, e);
            }
        }
        sqlSB.append(isVirtualTable ? (postSql == null ? "\n" : "\n" + postSql) : SQLUtils.autoSetSoftDeleted(postSql, clazz));
        sqlSB.append(SQLUtils.genLimitSQL(offset, limit));

        List<Object> argsList = new ArrayList<>(); // 不要直接用Arrays.asList，它不支持clear方法
        if (args != null) {
            argsList.addAll(Arrays.asList(args));
        }

        if (!selectOnlyKey) {
            doInterceptBeforeQuery(clazz, sqlSB, argsList);
        }

        String sql = sqlSB.toString();
        sql = addComment(sql);
        log(sql, 0, argsList);

        long start = System.currentTimeMillis();
        List<T> list;
        if (argsList.isEmpty()) {
            list = jdbcTemplate.query(sql,
                    new AnnotationSupportRowMapper<>(this, clazz, selectOnlyKey));
        } else {
            if (isUseNamedTemplate) {
                // 因为有in (?)所以用namedParameterJdbcTemplate
                list = namedParameterJdbcTemplate.query(
                        NamedParameterUtils.trans(sql, argsList),
                        NamedParameterUtils.transParam(argsList),
                        new AnnotationSupportRowMapper<>(this, clazz, selectOnlyKey));
            } else {
                list = jdbcTemplate.query(sql,
                        new AnnotationSupportRowMapper<>(this, clazz, selectOnlyKey), argsList.toArray());
            }
        }

        long total = -1; // -1 表示没有查询总数，未知
        if (withCount) {
            // 如果offset为0且查询的list小于limit数量，说明总数就这么多了，不需要再查总数了
            if(offset != null && offset == 0 && limit != null && list.size() < limit) {
                total = list.size();
            } else {
                total = getCount(clazz, postSql, args);
            }
        }

        if (!selectOnlyKey) {
            handleRelatedColumn(list);
        }

        long cost = System.currentTimeMillis() - start;
        logSlow(cost, sql, 0, argsList);

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
        return getOne(clazz, postSql, args) != null;
    }

    @Override
    public <T> boolean isExistAtLeast(int atLeastCounts, Class<T> clazz,
                                      String postSql, Object... args) {
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
        postHandleRelatedColumnSingle(t);
    }

    @Override
    public <T> void handleRelatedColumn(T t, String... relatedColumnProperties) {
        postHandleRelatedColumnSingle(t, relatedColumnProperties);
    }

    @Override
    public <T> void handleRelatedColumn(List<T> list) {
        postHandleRelatedColumn(list, false);
    }

    @Override
    public <T> void handleRelatedColumn(List<T> list, String... relatedColumnProperties) {
        postHandleRelatedColumn(list, false, relatedColumnProperties);
    }

    /**单个关联*/
    private <T> void postHandleRelatedColumnSingle(T t, String... relatedColumnProperties) {
        if (t == null) {
            return;
        }
        List<T> list = new ArrayList<>();
        list.add(t);

        postHandleRelatedColumn(list, false, relatedColumnProperties);
    }

    private <T> List<T> filterRelatedColumnConditional(List<T> tList, String conditional, Field field) {
        if (InnerCommonUtils.isBlank(conditional)) {
            return tList;
        }

        List<T> result = new ArrayList<>();
        for (T t : tList) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("t", t);
            try {
                Object value = MVEL.eval(conditional, vars);
                if (value == null) {
                    LOGGER.error("execute conditional return null, script:{}, t:{}",
                            conditional, NimbleOrmJSON.toJson(t));
                } else {
                    if (value instanceof Boolean) {
                        if ((Boolean) value) {
                            result.add(t);
                        } else {
                            if (field.getType() == List.class) {
                                DOInfoReader.setValue(field, t, new ArrayList<>());
                            }
                        }
                    } else {
                        LOGGER.error("execute conditional return is not instance of Boolean, script:{}, t:{}",
                                conditional, NimbleOrmJSON.toJson(t));
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("execute script fail: {}, t:{}", conditional, NimbleOrmJSON.toJson(t), e);
            }
        }

        return result;
    }

    /**批量关联，要求批量操作的都是相同的类*/
    private <T> void postHandleRelatedColumn(List<T> tList, boolean isFromJoin, String... relatedColumnProperties) {
        if (tList == null || tList.isEmpty()) {
            return;
        }
        Class<?> clazz = getElementClass(tList);
        if (clazz == null) {
            return;
        }

        JoinTable joinTable = DOInfoReader.getJoinTable(clazz);
        if (joinTable != null && !isFromJoin) { // 处理join的方式
            SQLAssert.allSameClass(tList);

            List<Object> list1 = new ArrayList<>();
            List<Object> list2 = new ArrayList<>();

            Field joinLeftTableField = DOInfoReader.getJoinLeftTable(clazz);
            Field joinRightTableField = DOInfoReader.getJoinRightTable(clazz);
            for (T t : tList) {
                Object obj1 = DOInfoReader.getValue(joinLeftTableField, t);
                if (obj1 != null) {
                    list1.add(obj1);
                }
                Object obj2 = DOInfoReader.getValue(joinRightTableField, t);
                if (obj2 != null) {
                    list2.add(obj2);
                }
            }

            postHandleRelatedColumn(list1, false);
            postHandleRelatedColumn(list2, false);
            postHandleRelatedColumn(tList, true);
            return;
        }

        List<Field> relatedColumns = DOInfoReader.getRelatedColumns(clazz);
        if (!relatedColumns.isEmpty()) { // 只有有relatedColumn才进行判断是否全是相同的类型
            SQLAssert.allSameClass(tList);
        } else {
            return; // 不需要处理了
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
            // 根据conditional判断该RelatedColumn是否进行处理
            List<T> tListFiltered = filterRelatedColumnConditional(tList, column.conditional(), field);

            if (InnerCommonUtils.isBlank(column.localColumn())) {
                throw new RelatedColumnFieldNotFoundException("field:" + field.getName() + " localColumn is blank");
            }
            if (InnerCommonUtils.isBlank(column.remoteColumn())) {
                throw new RelatedColumnFieldNotFoundException("field:" + field.getName() + " remoteColumn is blank");
            }

            List<DOInfoReader.RelatedField> localField = DOInfoReader.getFieldByDBField(clazz, column.localColumn(), field);

            // 批量查询数据库，提高效率的关键
            Class<?> remoteDOClass;
            if (field.getType() == List.class) {
                remoteDOClass = DOInfoReader.getGenericFieldType(field);
            } else {
                remoteDOClass = field.getType();
            }

            List<DOInfoReader.RelatedField> remoteField = DOInfoReader.getFieldByDBField(remoteDOClass, column.remoteColumn(), field);

            Set<Object> values = new HashSet<>(); // 用于去重，同样适用于ArrayList
            for (T t : tListFiltered) {
                Object value = DOInfoReader.getValueForRelatedColumn(localField, t);
                if (value != null) {
                    values.add(value);
                }
            }

            if (values.isEmpty()) {
                // 不需要查询数据库，但是对List的，设置空List，确保list不会是null
                if (field.getType() == List.class) {
                    for (T t : tListFiltered) {
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
                String whereColumn = getWhereColumnForRelated(remoteField);
                // 这里不能用DBHelper是因为拦截器会被重复触发；其次也必要，另外的DBHelper的实现也重新实现这个逻辑
                P1_QueryOp _dbHelper = this;
                if (InnerCommonUtils.isNotBlank(column.dbHelperBean())) {
                    String beanName = column.dbHelperBean().trim();
                    Object bean = applicationContext.getBean(beanName);
                    if (!(bean instanceof P1_QueryOp)) {
                        throw new SpringBeanNotMatchException("cannot find DBHelper bean: " + beanName
                                 + " or it is not type of SpringJdbcDBHelper");
                    } else {
                        _dbHelper = (P1_QueryOp) bean;
                    }
                }

                if (InnerCommonUtils.isBlank(column.extraWhere())) {
                    String inExpr = whereColumn + " in " + buildQuestionMark(values);
                    relateValues = _dbHelper.getAllForRelatedColumn(remoteDOClass, "where " + inExpr, values);
                } else {
                    // 如果extraWhere包含limit子句，那么只能降级为逐个处理，否则可以用批量处理的方式提高性能
                    try {
                        if (SQLUtils.isContainsLimit(column.extraWhere())) {
                            String eqExpr = whereColumn + "=?";
                            String where = SQLUtils.insertWhereAndExpression(column.extraWhere(), eqExpr);
                            relateValues = _dbHelper.getAllForRelatedColumnBySingleValue(remoteDOClass, where, values);
                        } else {
                            String inExpr = whereColumn + " in " + buildQuestionMark(values);
                            String where = SQLUtils.insertWhereAndExpression(column.extraWhere(), inExpr);
                            relateValues = _dbHelper.getAllForRelatedColumn(remoteDOClass, where, values);
                        }
                    } catch (JSQLParserException e) {
                        LOGGER.error("wrong RelatedColumn extraWhere:{}, ignore extraWhere", column.extraWhere());
                        throw new BadSQLSyntaxException(e);
                    }
                }
            }
            if (relateValues == null) {
                relateValues = new ArrayList<>();
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
                for (T t : tListFiltered) {
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
                for (T t : tListFiltered) {
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

    /**获得用于查询remoteColumn的列，如果多个列时用加上()*/
    private String getWhereColumnForRelated(List<DOInfoReader.RelatedField> remoteField) {
        boolean isSingleColumn = remoteField.size() == 1;
        StringBuilder sb = new StringBuilder(isSingleColumn ? "" : "(");
        boolean isFirst = true;
        for (DOInfoReader.RelatedField remoteF : remoteField) {
            if (!isFirst) {
                sb.append(",");
            }
            isFirst = false;

            Column remoteColumn = remoteF.field.getAnnotation(Column.class);
            if (InnerCommonUtils.isBlank(remoteColumn.computed())) {
                sb.append(remoteF.fieldPrefix).append(SQLUtils.getColumnName(remoteColumn.value()));
            } else {
                // 对于有remoteF.fieldPrefix的，由于计算列是用户自己写的，所以需要自己确保有fieldPrefix
                sb.append(SQLUtils.getComputedColumn(remoteColumn, features));
            }
        }
        sb.append(isSingleColumn ? "" : ")");
        return sb.toString();
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

    private void assertNotVirtualTable(Class<?> clazz) {
        boolean isVirtualTable = DOInfoReader.isVirtualTable(clazz);
        if (isVirtualTable) {
            throw new NotAllowQueryException("Virtual table is not supported");
        }
    }

    private void assertPage(int page) {
        if (page < 1) {
            throw new InvalidParameterException("[page] must greater than 0");
        }
    }
}
