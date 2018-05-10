package com.pugwoo.dbhelper.sql;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;

/**
 * 修复jsqlparser对于AND OR嵌套的优先级问题：
 * 当AND里面有OR时，OR应该加上()
 * 
 * @author pugwoo
 * 2017年3月16日 23:04:34
 */
public class FixedAndExpression extends AndExpression {

	public FixedAndExpression(Expression leftExpression, Expression rightExpression) {
		super(leftExpression, rightExpression);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(isNot() ? "NOT " : "");
		
		if(getLeftExpression() instanceof OrExpression) {
			sb.append("(").append(getLeftExpression()).append(")");
		} else {
			sb.append(getLeftExpression());
		}
		
		sb.append(" ").append(getStringExpression()).append(" ");
		
		if(getRightExpression() instanceof OrExpression) {
			sb.append("(").append(getRightExpression()).append(")");
		} else {
			sb.append(getRightExpression());
		}
		
		return sb.toString().trim().isEmpty() ? " " : "("+sb.toString()+")";
	}
	
}
