package com.pugwoo.dbhelper.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * SQL解析工具类
 * 
 * @author pugwoo
 * 2017年3月16日 23:02:47
 */
public class SQLUtils {

	/**
	 * 往where sql里面插入AND关系的表达式。
	 * 
	 * 例如：whereSql为 where a>3 or a!=2 limit 1
	 *      condExpress为 deleted=0
	 * 那么返回：where deleted=0 and (a>3 or a!=2) limit 1
	 * 
	 * @param whereSql 从where起的sql子句，如果有where必须带上where关键字。
	 * @param condExpression 例如a=?  不带where或and关键字。
	 * @return 注意返回字符串前面没有空格
	 * @throws JSQLParserException 
	 */
	public static String insertWhereAndExpression(String whereSql, String condExpression) 
			throws JSQLParserException {
		
		if(condExpression == null || condExpression.trim().isEmpty()) {
			return whereSql == null ? "" : whereSql;
		}
		if(whereSql == null || whereSql.trim().isEmpty()) {
			return "WHERE " + condExpression;
		}
		
		whereSql = whereSql.trim();
		if(!whereSql.toUpperCase().startsWith("WHERE ")) {
			return "WHERE " + condExpression + " " + whereSql;
		}
		
		
		String selectSql = "select * from dual "; // 辅助where sql解析用
		Statement statement = CCJSqlParserUtil.parse(selectSql + whereSql);
		Select selectStatement = (Select) statement;
		PlainSelect plainSelect = (PlainSelect)selectStatement.getSelectBody();
		
		Expression ce = CCJSqlParserUtil.parseCondExpression(condExpression);
		Expression oldWhere = plainSelect.getWhere();
		Expression newWhere = new FixedAndExpression(ce, oldWhere);
		plainSelect.setWhere(newWhere);
		
		return plainSelect.toString().substring(selectSql.length());
	}
		
}
