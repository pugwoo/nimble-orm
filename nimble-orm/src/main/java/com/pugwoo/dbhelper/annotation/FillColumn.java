package com.pugwoo.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 填充列注解，用于关联查询并注入所需的字段值。
 * 通过refField字段从当前类中检索注解了@Column的字段的值或者是对应字段名称的值，用作查询的参数，
 * 并通过fillScript的脚本来获取要填充值，并填充到该类中。
 * 
 * @author pugwoo
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FillColumn {

    /**
     * 必须，参考字段名称，可以是注解了@Column的字段名或者对应的数据库字段名。
     * 多个字段用逗号隔开，例如："field1,field2"
     * 在脚本中，第一个参数固定叫 refField 或 refField1，剩下的按顺序叫 refField2, refField3, refField4 ...
     */
    String refField();

    /**
     * 必须，用于获取填充值的脚本。
     * 脚本中可以使用参数：refField（或refField1）、refField2、refField3等，对应refField中指定的字段值。
     * 例如："com.xx.xx.ABC.getNameById(refField)"
     */
    String fillScript();

    /**
     * 列注释
     */
    String comment() default "";

    /**
     * 用于控制该FillColumn是否启用的mvel脚本，其中使用变量t表示当前DO类实例。
     * 当为空或返回true时启用该FillColumn属性，当返回false或mvel脚本报错时不启用。
     */
    String conditional() default "";

    /**
     * 当脚本执行出错时，是否忽略该错误，默认忽略并返回null。
     * 如果设置为不忽略，则会在脚本执行出错时，抛出异常ScriptErrorException。
     */
    boolean ignoreScriptError() default true;

}
