# nimble-orm

这是一个基于Spring JdbcTemplate的小工具，帮助开发者简单地完成Mysql的增删改查。为什么还需要在众多存在的ORM，如MyBatis/Hibernate的情况下再写一个ORM呢？

1. Hibernate因为比较复杂难以使用好，互联网公司大都没有采用，用得多的是MyBatis。而MyBatis的xml文件中，会出现大量相同的列名。增删一个列或修改一个列名，对于xml文件都是很大的改变。有一种方式是用MyBatis的Generator生成xml，但是这样的xml文件如果修改过，下次生成就会覆盖，很容易出错。因此，这种xml方式维护sql，虽然足够灵活，但也非常繁琐。

2. MyBatis对null值的处理不够灵活。例如只想更新非null值，或只插入非null值，MyBatis的写法会出现很多判断语句，大量的重复列名出现。

3. MyBatis提供一种定义接口，然后去xml中实现同名标记的sql，就可以暴露为可用DAO的方式。这种方式我并不推荐，在简单的增删改查功能下，这种方式没有问题，因为一个DAO接口对应于sql并不是只有一条，特别是复杂的DAO接口。如果一条SQL就是一个DAO接口的话，那组装多个sql的逻辑就暴露到service层，这样service层会变得复杂。

4. 实现的这个小工具nimble-orm没有完全取代MyBatis的打算，但对于大多数增删改查，可以很简单地完成。特别的，我觉得分布式系统的强一致性，非常合适由MySQL来保证。因此，使用类似update where, insert select where这样的CAS乐观锁写法的SQL，特别常用。

## Get Started 示例

把代码下载后，可以以Maven项目方式导入到Eclipse中，数据库链接配置在`src/test/resources/jdbc.properties`中，数据库测试建表语句在`create-table.sql`中。建好数据库和表之后，就可以执行`TestDBHelper.java`中的例子。

在Java DAO层，一般一个DO对象和一张数据库表是一一对应的。你只需要为DO对象加上一些注解，主要注解哪个表和哪些列，就可以方便地使用DBHelper进行增删改查了。

```java
@Table("t_student")
public class StudentDO extends IdableBaseDO {
		
	@Column("name")
	private String name;
	
	@Column("age")
	private Integer age;
	
	// getters / setters
}
```

项目只需要maven引入：

```xml
<dependency>
    <groupId>com.pugwoo</groupId>
    <artifactId>nimble-orm</artifactId>
    <version>0.1.3</version>
</dependency>
```

同时nimble-orm是基于jdbcTemplate的，需要拿到配置好的jdbcTemplate和namedJdbcTemplate:

```xml
	<!-- 配置JdbcTemplate -->
	<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
		<property name="dataSource" ref="dataSource" />
	</bean>
	
	<!-- 配置namedParameterTemplate -->
	<bean id="namedParameterJdbcTemplate" 
	    class="org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate">
	    <constructor-arg ref="dataSource" />
	</bean>
	
	<bean id="dbHelper" class="com.pugwoo.dbhelper.impl.SpringJdbcDBHelper">
	    <property name="jdbcTemplate" ref="jdbcTemplate" />
	    <property name="namedParameterJdbcTemplate" ref="namedParameterJdbcTemplate" />
	    <property name="timeoutWarningValve" value="1000" /> <!-- 超过1秒的话就告警打log -->
	</bean>
```

## 关于数据库字段到Java字段的映射关系

强烈建议java POJO不要使用基础类型(如int long)，同时引用类型也不要设置默认值。因为：

1. 无法表达数据库NULL数值。

2. 执行相关updateNotNull操作时会埋坑，有些值会一直被默认值覆盖。

其它：

1. 当数据库字段是NULL，而java POJO的字段是基础类型时，会转换成0。数据库字段不支持enum，推荐使用String来表达。

2. 一般来说，Java中的Boolean字段会对应数据库的tinyint(1)字段，和C语言保持一致，对于tinyint(4)，0表示false，非0表示true(包括负数)。当然dbHelper也支持bit的类型。

## 一些思考和选择

* 为什么insert和update默认只插入非null的值，而单独提供一个insertWithNull和updateWithNull的方法呢?

> 答：对于null值，如果写入到数据库，那肯定是null，同时无法使用数据库的默认值。因此null值是没有必要默认就写入数据库的。同理，update也保持一致的做法。

* 为什么要提供`void rollback()`方法，手工来回滚事务？

> 答：DAO层和service层向上层抛出异常以表达错误，是不太建议的方式。我知道有些建议会推荐使用显式抛异常的方式来表达错误，但上层的处理就会变得麻烦。更好的做法我认为是尽量靠返回值来表示处理的结果，而异常仅用在“无法预测”的不正常情况或者返回值无法表达更多信息的情况下。而@Transational事务回滚，需要抛出RuntimeException，这个行为显然会干扰调用者。所以，为了保证用返回值表达结果，就必须手动回滚事务。

* 为什么@Column的注解要显示指定value值，不做自动根据字段名配置映射成驼峰形式或下划线形式？

> 答：`约定优于配置`是一种很常用的指导方向。但需要明确的是，适合约定的内容是什么？它应该是大家乐于接受的，表达明确的，且不易改变的，能支持绝大多数的功能的。例如我们约定linux操作系统的文件夹路径用`/`分隔，就是一个好的约定，在整个linux操作系统内，没有额外的需求需要用另外一个符号来表示。相反的，像webx框架中，约定url到方法的映射关系，可以支持驼峰或下划线的多种混合格式时，就是一个差的约定，例如Java中sayHi的方法，就默认支持url中`say_hi`、`sayHi`，`say_Hi`、`SayHi`等多种不同方式的映射。约定有一个副作用是，失去了追查系统实现的线索，例如你维护一个老系统，前端过来请求`say_Hi`，你怎样快速准确地找到这个url对应的方法？显然你也只能猜测去查找，java中是sayHi，或者say_Hi?。所以Spring MVC明确注解`@RequestMapping`就是一个很不错的方式，它明确了前后端映射的准确对应关系。同样的道理，`@Column`要求明确写value值。

## 注意事项

* 如果参数需要传入的是Object...（例如getAll之类的方法），那么当需要传入若干参数时，强烈使用List来传。因为如果使用Object[]来传，Object...本身就是Object[]类型，当只有单个Object[]的时候，就只会取Object[]的第一个参数作为参数，这样就有错误，而且是语义错误，很隐蔽。有一种hack的方式，但不推荐，在传入Object[]参数后面，再加上一个任意类型的参数，让Java不要认为参数是Object...。

* 参数列表中不能出现null，否则会报org.springframework.dao.InvalidDataAccessApiUsageException: No value supplied for the SQL parameter 'param1': No value registered for key 'param1'

## 未来规划

1. 拦截器设计。

2. Join方式设计。