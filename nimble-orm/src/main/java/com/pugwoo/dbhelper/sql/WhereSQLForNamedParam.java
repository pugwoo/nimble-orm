package com.pugwoo.dbhelper.sql;

import com.pugwoo.dbhelper.exception.BadSQLSyntaxException;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 辅助构造where子句及后续子句的工具，用于命名参数
 */
public class WhereSQLForNamedParam {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhereSQLForNamedParam.class);

    private Map<String, Object> paramMap = new HashMap<>();

    private String condition;
    private boolean isOrExpression;

    private List<String> groupBy;

    private String having;

    private List<String> orderBy;

    private Long offset;
    private Long limit;

    /**
     * 空的WhereSQL
     */
    public WhereSQLForNamedParam() {
    }


    /**
     * 复制出一个新的WhereSQL对象，两个对象独立
     */
    public WhereSQLForNamedParam copy() {
        WhereSQLForNamedParam whereSQL = new WhereSQLForNamedParam();
        whereSQL.condition = this.condition;
        whereSQL.isOrExpression = this.isOrExpression;
        whereSQL.paramMap = this.paramMap == null ? null : new HashMap<>(this.paramMap);
        whereSQL.groupBy = this.groupBy == null ? null : new ArrayList<>(this.groupBy);
        whereSQL.having = this.having;
        whereSQL.orderBy = this.orderBy == null ? null : new ArrayList<>(this.orderBy);
        whereSQL.offset = this.offset;
        whereSQL.limit = this.limit;
        return whereSQL;
    }

    /**
     * 使用条件进行初始化，条件即例如 a=:param 这样的表达式，也可以是 a=:param1 or b=:param2 这样的表达式。<br>
     * 当表达式是or表达式时，工具会自动加上括号。
     * @param condition 例如 a=:param 或 a=:param1 or b=:param2，不用加上括号，工具会自动处理
     */
    public WhereSQLForNamedParam(String condition) {
        if (InnerCommonUtils.isNotBlank(condition)) {
            isOrExpression = isOrExpression(condition);
            this.condition = condition;
        }
    }

    /**
     * 使用条件进行初始化，条件即例如 a=:param 这样的表达式，也可以是 a=:param1 or b=:param2 这样的表达式。<br>
     * 当表达式是or表达式时，工具会自动加上括号。
     * @param condition 例如 a=:param 或 a=:param1 or b=:param2，不用加上括号，工具会自动处理
     * @param paramMap 参数
     */
    public WhereSQLForNamedParam(String condition, Map<String, ?> paramMap) {
        if (InnerCommonUtils.isNotBlank(condition)) {
            isOrExpression = isOrExpression(condition);
            this.condition = condition;
        }
        doAddParam(paramMap);
    }

    /**
     * 获得从where开始的SQL子句
     */
    public String getSQL() {
        StringBuilder sql = new StringBuilder();
        if (InnerCommonUtils.isNotBlank(condition)) {
            sql.append(" WHERE ").append(condition);
        }
        getSQLLeft(sql);

        return sql.toString();
    }

    /**
     * 等价于getSQL()
     */
    @Override
    public String toString() {
        return getSQL();
    }

    /**
     * 获得用于where开始的SQL子句，用于追加到一个已有的where sql中，所以如果有where时，会以and开头，并且where条件会自动加上括号
     */
    public String getSQLForWhereAppend() {
        StringBuilder sql = new StringBuilder();
        if (InnerCommonUtils.isNotBlank(condition)) {
            sql.append(" AND ");
            if (isOrExpression) {
                sql.append("(");
            }
            sql.append(condition);
            if (isOrExpression) {
                sql.append(")");
            }
        }
        getSQLLeft(sql);

        return sql.toString();
    }

    private void getSQLLeft(StringBuilder sql) {
        if (InnerCommonUtils.isNotEmpty(groupBy)) {
            sql.append(" GROUP BY ").append(String.join(",", groupBy));
        }
        if (InnerCommonUtils.isNotBlank(having)) {
            sql.append(" HAVING ").append(having);
        }
        if (InnerCommonUtils.isNotEmpty(orderBy)) {
            sql.append(" ORDER BY ").append(String.join(",", orderBy));
        }

        if (limit != null) {
            sql.append(" LIMIT ");
            if (offset != null) {
                sql.append(offset).append(",");
            }
            sql.append(limit);
        }

        sql.append(" "); // 留一个空格，减少和后续sql直接拼接的错误
    }

    /**
     * 获得参数列表
     */
    public Map<String, Object> getParams() {
        return paramMap == null ? new HashMap<>() : paramMap;
    }

    public WhereSQLForNamedParam not() {
        if (InnerCommonUtils.isBlank(condition)) {
            return this;
        }
        condition = "NOT " + (isAndOrExpression(condition) ? ("(" + condition + ")") : condition);
        isOrExpression = false;
        return this;
    }

    public WhereSQLForNamedParam notIf(boolean ifTrue) {
        if (ifTrue) {
            not();
        }
        return this;
    }

    public WhereSQLForNamedParam and(String condition) {
        return and(condition, null);
    }

    public WhereSQLForNamedParam andIf(boolean ifTrue, String condition) {
        if (ifTrue) {
            and(condition);
        }
        return this;
    }

    public WhereSQLForNamedParam and(String condition, Map<String, ?> param) {
        if (InnerCommonUtils.isNotBlank(condition)) {
            // 一共四种组合，目的是最小可能地加括号
            boolean isOrExpression = isOrExpression(condition);
            condition = isOrExpression ? "(" + condition + ")" : condition;
            if (this.isOrExpression) {
                this.condition = "(" + this.condition + ") AND " + condition;
                this.isOrExpression = false;
            } else {
                if (InnerCommonUtils.isBlank(this.condition)) {
                    this.condition = condition;
                    this.isOrExpression = isOrExpression;
                } else {
                    this.condition = this.condition + " AND " + condition;
                    this.isOrExpression = false;
                }
            }
        }

        doAddParam(param);
        return this;
    }

    public WhereSQLForNamedParam andIf(boolean ifTrue, String condition, Map<String, ?> param) {
        if (ifTrue) {
            and(condition, param);
        }
        return this;
    }

    /**
     * 功能同addAnd，注意：只会读取参数whereSQL的条件和参数，因此需要注意whereSQL里【不能】存在order/group by/limit等子句
     */
    public WhereSQLForNamedParam and(WhereSQLForNamedParam whereSQL) {
        if (whereSQL.isNotOnlyHasCondition()) {
            LOGGER.warn("whereSQL has other properties which will be ignored:{}", NimbleOrmJSON.toJson(whereSQL));
        }
        return and(whereSQL.condition, whereSQL.paramMap);
    }

    public WhereSQLForNamedParam andIf(boolean ifTrue, WhereSQLForNamedParam whereSQL) {
        if (ifTrue) {
            and(whereSQL);
        }
        return this;
    }

    public WhereSQLForNamedParam or(String condition, Map<String, ?> param) {
        if (InnerCommonUtils.isNotBlank(condition)) {
            this.condition = (this.condition == null ? "" : (this.condition + " OR ")) + condition;
            this.isOrExpression = true;
        }

        doAddParam(param);
        return this;
    }

    public WhereSQLForNamedParam orIf(boolean ifTrue, String condition, Map<String, ?> param) {
        if (ifTrue) {
            or(condition, param);
        }
        return this;
    }

    /**
     * 功能同addOr，注意：只会读取参数whereSQL的条件和参数，因此需要注意whereSQL里【不能】存在order/group by/limit等子句
     */
    public WhereSQLForNamedParam or(WhereSQLForNamedParam whereSQL) {
        if (whereSQL.isNotOnlyHasCondition()) {
            LOGGER.warn("whereSQL has other properties which will be ignored:{}", NimbleOrmJSON.toJson(whereSQL));
        }
        return or(whereSQL.condition, whereSQL.paramMap);
    }

    public WhereSQLForNamedParam orIf(boolean ifTrue, WhereSQLForNamedParam whereSQL) {
        if (ifTrue) {
            or(whereSQL);
        }
        return this;
    }

    public WhereSQLForNamedParam addGroupByWithParam(String groupColumn, Map<String, ?> paramMap) {
        if (InnerCommonUtils.isNotBlank(groupColumn)) {
            if (this.groupBy == null) {
                this.groupBy = new ArrayList<>();
            }
            this.groupBy.add(groupColumn);
        }

        doAddParam(paramMap);
        return this;
    }

    public WhereSQLForNamedParam addGroupBy(String... groupByColumn) {
        if (groupByColumn != null && groupByColumn.length > 0) {
            if (this.groupBy == null) {
                this.groupBy = new ArrayList<>();
            }
            for (String groupBy : groupByColumn) {
                if (InnerCommonUtils.isNotBlank(groupBy)) {
                    this.groupBy.add(groupBy);
                }
            }
        }

        return this;
    }

    public WhereSQLForNamedParam resetGroupBy() {
        this.groupBy = null;
        return this;
    }

    public WhereSQLForNamedParam having(String having) {
        return having(having, null);
    }

    /**
     * 多次调用时，会覆盖前一次调用设置的值。不需要加HAVING关键字。
     */
    public WhereSQLForNamedParam having(String having, Map<String, ?> paramMap) {
        if (InnerCommonUtils.isNotBlank(this.having)) {
            LOGGER.warn("having sql [{}] will be covered by [{}]", this.having, having);
        }

        this.having = having;
        doAddParam(paramMap);
        return this;
    }

    public WhereSQLForNamedParam addOrderByWithParam(String orderColumn, Map<String, ?> paramMap) {
        if (InnerCommonUtils.isNotBlank(orderColumn)) {
            if (this.orderBy == null) {
                this.orderBy = new ArrayList<>();
            }
            this.orderBy.add(orderColumn);
        }

        doAddParam(paramMap);
        return this;
    }

    public WhereSQLForNamedParam addOrderBy(String... orderByColumn) {
        if (orderByColumn != null && orderByColumn.length > 0) {
            if (this.orderBy == null) {
                this.orderBy = new ArrayList<>();
            }
            for (String orderBy : orderByColumn) {
                if (InnerCommonUtils.isNotBlank(orderBy)) {
                    this.orderBy.add(orderBy);
                }
            }
        }

        return this;
    }

    public WhereSQLForNamedParam resetOrderBy() {
        this.orderBy = null;
        return this;
    }

    public WhereSQLForNamedParam limit(Integer limit) {
        if (limit != null) {
            this.limit = (long) limit;
        }
        this.offset = null;
        return this;
    }

    public WhereSQLForNamedParam limit(Integer offset, Integer limit) {
        if (limit != null) {
            this.limit = (long) limit;
        }
        if (offset != null) {
            this.offset = (long) offset;
        }
        return this;
    }

    public WhereSQLForNamedParam limit(Long limit) {
        this.limit = limit;
        this.offset = null;
        return this;
    }

    public WhereSQLForNamedParam limit(Long offset, Long limit) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    private void doAddParam(Map<String, ?> paramMap) {
        if (paramMap != null) {
            if (this.paramMap == null) {
                this.paramMap = new HashMap<>();
            }
            this.paramMap.putAll(paramMap);
        }
    }

    private boolean isOrExpression(String condition) {
        Expression expression;
        try {
            expression = CCJSqlParserUtil.parseCondExpression(condition);
        } catch (JSQLParserException e) {
            LOGGER.error("parse condition:{} exception", condition, e);
            throw new BadSQLSyntaxException(e);
        }
        return expression instanceof OrExpression;
    }

    private boolean isAndOrExpression(String condition) {
        Expression expression;
        try {
            expression = CCJSqlParserUtil.parseCondExpression(condition);
        } catch (JSQLParserException e) {
            LOGGER.error("parse condition:{} exception", condition, e);
            throw new BadSQLSyntaxException(e);
        }
        return expression instanceof OrExpression || expression instanceof AndExpression;
    }

    private boolean isNotOnlyHasCondition() {
        return InnerCommonUtils.isNotEmpty(groupBy)
                || InnerCommonUtils.isNotBlank(having)
                || InnerCommonUtils.isNotEmpty(orderBy)
                || offset != null || limit != null;
    }

}
