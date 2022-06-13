package com.pugwoo.dbhelper.sql;

import com.pugwoo.dbhelper.exception.BadSQLSyntaxException;
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

    /**
     * 空的WhereSQL
     */
    public WhereSQL() {
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
            doAddParam(param);
        }
        // 如果condition为空，此时也不应该处理param，不存在condition为空但需要设置参数的情况
    }

    public String getWhereSQL() {
        StringBuilder sql = new StringBuilder();
        if (InnerCommonUtils.isNotBlank(condition)) {
            sql.append("WHERE ").append(condition);
        }
        return sql.toString();
    }

    public Object[] getParams() {
        return params == null ? new Object[0] : params.toArray();
    }

    public WhereSQL not() {
        if (InnerCommonUtils.isBlank(condition)) {
            return this;
        }
        condition = "NOT " + (isAndOrExpression(condition) ? ("(" + condition + ")") : condition);
        isOrExpression = false;
        return this;
    }

    public WhereSQL and(String condition, Object... param) {
        // 如果condition为空，此时也不应该处理param，不存在condition为空但需要设置参数的情况
        if (InnerCommonUtils.isBlank(condition)) {
            return this;
        }

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

        doAddParam(param);
        return this;
    }

    /**
     * 功能同addAnd，注意：只会读取参数whereSQL的条件和参数，因此需要注意whereSQL里【不能】存在order/group by/limit等子句
     */
    public WhereSQL and(WhereSQL whereSQL) {
        return and(whereSQL.condition, whereSQL.params == null ? new Object[0] : whereSQL.params.toArray());
    }

    public WhereSQL or(String condition, Object... param) {
        // 如果condition为空，此时也不应该处理param，不存在condition为空但需要设置参数的情况
        if (InnerCommonUtils.isBlank(condition)) {
            return this;
        }

        this.condition = (this.condition == null ? "" : (this.condition + " OR ")) + condition;
        this.isOrExpression = true;
        doAddParam(param);
        return this;
    }

    /**
     * 功能同addOr，注意：只会读取参数whereSQL的条件和参数，因此需要注意whereSQL里【不能】存在order/group by/limit等子句
     */
    public WhereSQL or(WhereSQL whereSQL) {
        return or(whereSQL.condition, whereSQL.params == null ? new Object[0] : whereSQL.params.toArray());
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

}
