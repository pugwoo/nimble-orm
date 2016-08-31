# nimble-orm

这是一个基于Spring JdbcTemplate的小工具，帮助开发者简单地完成Mysql的增删改查。为什么还需要在众多存在的ORM，如MyBatis/Hibernate的情况下再写一个ORM呢？

1. Hibernate因为比较复杂难以使用好，互联网公司都没有采用，用得多的是MyBatis。而MyBatis的xml文件中，会出现大量相同的列名。增删一个列或修改一个列名，对于xml文件都是很大的改变。有一种方式是用MyBatis的Generator生成xml，但是这样的xml文件如果修改过，下次生成就会覆盖，很容易出错。因此，这种xml方式维护sql，虽然足够灵活，但也非常繁琐。

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

## 关于数据库字段到Java字段的映射关系

强烈建议java POJO不要使用基础类型(如int long)，同时引用类型也不要设置默认值。因为：

1. 无法表达数据库NULL数值。

2. 执行相关updateNotNull操作时会埋坑，有些值会一直被默认值覆盖。

其它：当数据库字段是NULL，而java POJO的字段是基础类型时，会转换成0。数据库字段不支持enum，推荐使用String来表达。

## 一些思考和选择

* 为什么insert默认只插入非null的值，而单独提供一个insertWithNull的方法呢?

> 答：对于null值，如果写入到数据库，那肯定是null，同时无法使用数据库的默认值。因此null值是没有必要默认就写入数据库的。

* 为什么要提供`void rollback()`方法，手工来回滚事务？

> 答：DAO层和service层向上层抛出异常以表达错误，是不太建议的方式。我知道有些建议会推荐使用显式抛异常的方式来表达错误，但上层的处理就会变得麻烦。更好的做法我认为是尽量靠返回值来表示处理的结果，而异常仅用在“无法预测”的不正常情况或者返回值无法表达更多信息的情况下。而@Transational事务回滚，需要抛出RuntimeException，这个行为显然会干扰调用者。所以，为了保证用返回值表达结果，就必须手动回滚事务。

## 注意事项

* 如果参数需要传入的是Object...（例如getAll之类的方法），那么当需要传入若干参数时，强烈使用List来传。因为如果使用Object[]来传，Object...本身就是Object[]类型，当只有单个Object[]的时候，就只会取Object[]的第一个参数作为参数，这样就有错误，而且是语义错误，很隐蔽。有一种hack的方式，但不推荐，在传入Object[]参数后面，再加上一个任意类型的参数，让Java不要认为参数是Object...。

* 参数列表中不能出现null，否则会报org.springframework.dao.InvalidDataAccessApiUsageException: No value supplied for the SQL parameter 'param1': No value registered for key 'param1'