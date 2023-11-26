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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 辅助构造where子句及后续子句的工具
 */
public class WhereSQL {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhereSQL.class);

    private String condition;
    private boolean isOrExpression;
    private List<Object> params;

    private List<String> groupBy;
    private List<Object> groupByParams;

    private String having;
    private List<Object> havingByParams;

    private List<String> orderBy;
    private List<Object> orderByParams;

    private Long offset;
    private Long limit;

    /**
     * 空的WhereSQL
     */
    public WhereSQL() {
    }


    /**
     * 复制出一个新的WhereSQL对象，两个对象独立
     */
    public WhereSQL copy() {
        WhereSQL whereSQL = new WhereSQL();
        whereSQL.condition = this.condition;
        whereSQL.isOrExpression = this.isOrExpression;
        whereSQL.params = this.params == null ? null : new ArrayList<>(this.params);
        whereSQL.groupBy = this.groupBy == null ? null : new ArrayList<>(this.groupBy);
        whereSQL.groupByParams = this.groupByParams == null ? null : new ArrayList<>(this.groupByParams);
        whereSQL.having = this.having;
        whereSQL.havingByParams = this.havingByParams == null ? null : new ArrayList<>(this.havingByParams);
        whereSQL.orderBy = this.orderBy == null ? null : new ArrayList<>(this.orderBy);
        whereSQL.orderByParams = this.orderByParams == null ? null : new ArrayList<>(this.orderByParams);
        whereSQL.offset = this.offset;
        whereSQL.limit = this.limit;
        return whereSQL;
    }

    /**
     * 使用条件进行初始化，条件即例如 a=? 这样的表达式，也可以是 a=? or b=? 这样的表达式。<br>
     * 当表达式是or表达式时，工具会自动加上括号。
     * @param condition 例如 a=? 或 a=? or b=?，不用加上括号，工具会自动处理
     * @param param 参数
     */
    public WhereSQL(String condition, Object... param) {
        if (InnerCommonUtils.isNotBlank(condition)) {
            isOrExpression = isOrExpression(condition);
            this.condition = condition;
        }
        doAddParam(param);
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
            sql.append(limit);
            if (offset != null) {
                sql.append(" OFFSET ").append(offset);
            }
        }

        sql.append(" "); // 留一个空格，减少和后续sql直接拼接的错误
    }

    /**
     * 获得参数列表
     */
    public Object[] getParams() {
        List<Object> result = new ArrayList<>();

        if (params != null) {
            result.addAll(params);
        }
        if (groupByParams != null) {
            result.addAll(groupByParams);
        }
        if (havingByParams != null) {
            result.addAll(havingByParams);
        }
        if (orderByParams != null) {
            result.addAll(orderByParams);
        }

        return result.toArray();
    }

    public WhereSQL not() {
        if (InnerCommonUtils.isBlank(condition)) {
            return this;
        }
        condition = "NOT " + (isAndOrExpression(condition) ? ("(" + condition + ")") : condition);
        isOrExpression = false;
        return this;
    }

    /**
     * 只有当ifTrue为true时，才会执行not
     */
    public WhereSQL notIf(boolean ifTrue) {
        if (ifTrue) {
            not();
        }
        return this;
    }

    public WhereSQL and(String condition, Object... param) {
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

    public WhereSQL andIf(boolean ifTrue, String condition, Object... param) {
        if (ifTrue) {
            and(condition, param);
        }
        return this;
    }

    /**
     * 功能同addAnd，注意：只会读取参数whereSQL的条件和参数，因此需要注意whereSQL里【不能】存在order/group by/limit等子句
     */
    public WhereSQL and(WhereSQL whereSQL) {
        if (whereSQL.isNotOnlyHasCondition()) {
            LOGGER.warn("whereSQL has other properties which will be ignored:{}", NimbleOrmJSON.toJson(whereSQL));
        }
        return and(whereSQL.condition, whereSQL.params == null ? new Object[0] : whereSQL.params.toArray());
    }

    public WhereSQL andIf(boolean ifTrue, WhereSQL whereSQL) {
        if (ifTrue) {
            and(whereSQL);
        }
        return this;
    }

    public WhereSQL or(String condition, Object... param) {
        if (InnerCommonUtils.isNotBlank(condition)) {
            this.condition = (this.condition == null ? "" : (this.condition + " OR ")) + condition;
            this.isOrExpression = true;
        }

        doAddParam(param);
        return this;
    }

    public WhereSQL orIf(boolean ifTrue, String condition, Object... param) {
        if (ifTrue) {
            or(condition, param);
        }
        return this;
    }

    /**
     * 功能同addOr，注意：只会读取参数whereSQL的条件和参数，因此需要注意whereSQL里【不能】存在order/group by/limit等子句
     */
    public WhereSQL or(WhereSQL whereSQL) {
        if (whereSQL.isNotOnlyHasCondition()) {
            LOGGER.warn("whereSQL has other properties which will be ignored:{}", NimbleOrmJSON.toJson(whereSQL));
        }
        return or(whereSQL.condition, whereSQL.params == null ? new Object[0] : whereSQL.params.toArray());
    }

    public WhereSQL orIf(boolean ifTrue, WhereSQL whereSQL) {
        if (ifTrue) {
            or(whereSQL);
        }
        return this;
    }

    public WhereSQL addGroupByWithParam(String groupColumn, Object... params) {
        if (InnerCommonUtils.isNotBlank(groupColumn)) {
            if (this.groupBy == null) {
                this.groupBy = new ArrayList<>();
            }
            this.groupBy.add(groupColumn);
        }

        if (params != null && params.length > 0) {
            if (this.groupByParams == null) {
                this.groupByParams = new ArrayList<>();
            }
            this.groupByParams.addAll(Arrays.asList(params));
        }

        return this;
    }

    public WhereSQL addGroupBy(String... groupByColumn) {
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

    public WhereSQL resetGroupBy() {
        this.groupBy = null;
        this.groupByParams = null;
        return this;
    }

    /**
     * 多次调用时，会覆盖前一次调用设置的值。不需要加HAVING关键字。
     */
    public WhereSQL having(String having, Object... params) {
        if (InnerCommonUtils.isNotBlank(this.having) || InnerCommonUtils.isNotEmpty(this.havingByParams)) {
            LOGGER.warn("having sql [{}] will be covered by [{}]", this.having, having);
        }

        this.having = having;
        if (params != null && params.length > 0) {
            this.havingByParams = new ArrayList<>(Arrays.asList(params));
        } else {
            this.havingByParams = null;
        }
        return this;
    }

    public WhereSQL addOrderByWithParam(String orderColumn, Object... params) {
        if (InnerCommonUtils.isNotBlank(orderColumn)) {
            if (this.orderBy == null) {
                this.orderBy = new ArrayList<>();
            }
            this.orderBy.add(orderColumn);
        }

        if (params != null && params.length > 0) {
            if (this.orderByParams == null) {
                this.orderByParams = new ArrayList<>();
            }
            this.orderByParams.addAll(Arrays.asList(params));
        }

        return this;
    }

    public WhereSQL addOrderBy(String... orderByColumn) {
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

    public WhereSQL resetOrderBy() {
        this.orderBy = null;
        this.orderByParams = null;
        return this;
    }

    public WhereSQL limit(Integer limit) {
        if (limit != null) {
            this.limit = (long) limit;
        }
        this.offset = null;
        return this;
    }

    public WhereSQL limit(Integer offset, Integer limit) {
        if (limit != null) {
            this.limit = (long) limit;
        }
        if (offset != null) {
            this.offset = (long) offset;
        }
        return this;
    }

    public WhereSQL limit(Long limit) {
        this.limit = limit;
        this.offset = null;
        return this;
    }

    public WhereSQL limit(Long offset, Long limit) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    private void doAddParam(Object[] param) {
        if (param != null && param.length > 0) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }
            this.params.addAll(Arrays.asList(param));
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
        return InnerCommonUtils.isNotEmpty(groupBy) || InnerCommonUtils.isNotEmpty(groupByParams)
                || InnerCommonUtils.isNotBlank(having) || InnerCommonUtils.isNotEmpty(havingByParams)
                || InnerCommonUtils.isNotEmpty(orderBy) || InnerCommonUtils.isNotEmpty(orderByParams)
                || offset != null || limit != null;

    }

}
