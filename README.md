# nimble-orm

这是一个基于Spring JdbcTemplate的小工具，帮助开发者简单地完成Mysql(其它数据库是否可用未测试过)的增删改查。为什么还需要在众多存在的ORM，如MyBatis/Hibernate的情况下再写一个ORM呢？

1. Hibernate因为比较复杂难以使用好，互联网公司大都没有采用，用得多的是MyBatis。而MyBatis的xml文件中，会出现大量相同的列名。增删一个列或修改一个列名，对于xml文件都是很大的变动。有一种方式是用MyBatis的Generator生成xml，但是这样的xml文件如果修改过，下次生成就会覆盖，很容易出错。因此，这种xml方式维护sql，虽然足够灵活，但也非常繁琐。

2. MyBatis对null值的处理不够灵活。例如只想更新非null值，或只插入非null值，MyBatis的写法会出现很多判断语句，大量的重复列名出现。

3. MyBatis提供一种定义接口，然后去xml中实现同名标记的sql，就可以暴露为可用DAO的方式。这种方式我并不推荐，在简单的增删改查功能下，这种方式没有问题，因为一个DAO接口对应于sql并不是只有一条，特别是复杂的DAO接口。如果一条SQL就是一个DAO接口的话，那组装多个sql的逻辑就暴露到service层，这样service层会变得复杂。

4. 实现的这个小工具nimble-orm没有完全取代MyBatis的打算，但对于大多数增删改查，可以很简单地完成。特别的，我觉得分布式系统的强一致性，非常合适由MySQL来保证。因此，使用类似update where, insert select where这样的CAS乐观锁写法的SQL，特别常用。

# 使用nimble-orm的优势

1. **为互联网频繁的表变动而生。** 表名、字段名，仅在代码中出现一次。修改表名只需要改一处地方，修改字段名，仅需改字段注解一次及where子句中涉及的字段名。增加字段只需增加一个表成员。修改量相比MyBatis大大减少。

2. **实用主义者，注重简单实用的接口。** 分页接口、软删除标记位全面支持、数据库乐观锁CAS写法、事务手动回滚、支持SOA远程方式的跨数据库关联查询等。定义完DO之后，无需增加额外配置，调用接口即可。

3. **贫血模型，纯粹的POJO。**  不需要继承指定类或实现接口，纯粹的POJO，适合于各种序列化场景。喜欢Spring就会喜欢nimble-orm。

4. **没有潜规则约定，一切注解配置，老项目迁移成本极低。** 不会约定类成员变量和表字段名的关系，全部需要通过配置指定，老项目不规则的表名字段名，不会影响新代码的命名。强制指定配置，这种“麻烦”会收获到后续代码运维上的很多便利。欠下的技术债总是要还的，何不一开始就描述清楚点？而xml或其它文件格式写sql，导致几个文件来回切换的编码，一个修改另外一个忘记修改就出错的情况，不再需要了，代码内聚，让阅读和维护更简单。

## Get Started 示例

把代码下载后，可以以Maven项目方式导入到IDE中，数据库链接配置在`src/test/resources/jdbc.properties`中，数据库测试建表语句在`create-table.sql`中。建好数据库和表之后，就可以执行`TestDBHelper.java`中的例子。

在Java DAO层，一般一个DO对象和一张数据库表是一一对应的。你只需要为DO对象加上一些注解，主要注解哪个表和哪些列，就可以方便地使用DBHelper进行增删改查了。

```java
// 这个一个标准的POJO，除了注解，没有引入任何nimble-orm的东西，不要求继承或实现任何东西。
@Table("t_student")
public class StudentDO extends IdableSoftDeleteBaseDO { // 这里用不用继承都是可以的，用继承可以框定一些规范（在业务系统中，有些每个表都有的公共字段可以放在父类中）

/** IdableSoftDeleteBaseDO也是单纯的POJO，无需继承或实现任何东西。其内容是：
  @Column(value = "id", isKey = true, isAutoIncrement = true)
  private Long id;
  
  // 软删除标记为，0 未删除，1已删除
  @Column(value = "deleted", softDelete = {"0", "1"})
  private Boolean deleted;
  
  @Column(value = "create_time", setTimeWhenInsert = true)
  private Date createTime;
  
  @Column(value = "update_time", setTimeWhenUpdate = true, setTimeWhenInsert = true)
  private Date updateTime;
*/
		
	@Column("name")
	private String name;
	
	@Column("age")
	private Integer age;
	
	// 标准的 getters / setters，如果提供，nimble-orm就会用；如果没提供，就会直接反射设置字段值
}
```

项目只需要maven引入：

```xml
<dependency>
    <groupId>com.pugwoo</groupId>
    <artifactId>nimble-orm</artifactId>
    <version>0.4.0</version>
</dependency>
```

同时nimble-orm是基于jdbcTemplate的，nimble-orm需要拿到配置好的jdbcTemplate和namedJdbcTemplate，dataSource请自行提供，不作限制:

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

请确保项目是使用Spring和SpringJDBC并有引入下面两个maven依赖，版本支持2.x、3.x、4.x：

```xml
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-jdbc</artifactId>
		</dependency>
		
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context-support</artifactId>
		</dependency>
```

## 一些高级好玩特性介绍（部分）

**数据库用到软删除标记，每次都要写where deleted=0 ?**

在nimble-orm中，你只需要为软删除字段注解上：

```java
  /**软删除标记，false未删除，true已删除*/
  @Column(value = "deleted", softDelete = {"0", "1"})
  private Boolean deleted;
```

然后增删改查，nimble-orm的接口都会自动处理好软删除的事。例如调用dbHelper.delete方法，会自动update软删除位为1，不会真的删除；例如调用dbHelper.getPage时，会自动给where条件加上deleted=0条件，不会把软删除的查出来。除了DO注解外，你完全不需要在代码中出现deleted字样。

**兑奖记录表，每个手机号只出现一次**

如果你在写一个兑奖逻辑，先查询数据表A里面有没有手机号，如果没有则插入一条并发奖。伪代码：

```java
boolean haveGot = db.getByPhone(phone); // 查询手机号是否存在
if(!haveGot) {
   db.insertPhone(phone); // 插入手机号
   sendGift(); // 发奖
}
```

这样的逻辑在高并发下会有问题，表现为数据库有多条相同手机号的记录并发了多次奖。有一种解决方式是给phone字段加上唯一索引，使得插入时如果已经存在就插入不进去。但是这种方式并不推荐，应用的逻辑正确性不应该依赖于数据库的索引，规范上我们认为索引仅用于性能优化，不应该同时保证业务逻辑正确性。

如果没有数据库的唯一索引，同时又是分布式系统，要使用线程同步来实现也不容易。可以使用Redis的CAS功能来实现高并发，但引入redis并要求redis保存该持久化数据，也不太好。

nimble-orm推荐的做法是，使用mysql的insert select from where not exists 的写法，保证插入数据库有且只有一条：

```java
int row = dbHelper.insertWhereNotExist(XXXDO.class, "where phone=?", phone);
if(row > 0) {
   sendGift(); // 发奖
}
```

**关联查询**

假设一个场景，StudentDO学生表，有表字段school_id，外键关联SchoolDO表。如果没有类似@OneToOne之类的关联查询，那么需要手动写代码查两次：先查StudentDO拿到school_id，再通过school_id去查SchoolDO表，拿到之后设置到studentDO中。

这个动作可以通过注解让nimble-orm来做，由于我们约定DO是和数据库表一一对应，所以这个关联的类应该叫StudentVO，它继承自StudentDO：

```java
public class StudentVO extends StudentDO {
  
  // 关联查询时，请务必确保关联字段在Java是相同类型，否则java的equals方法会判断为不相等。
  // 为了解决这种情况，dbhelper采用一种折中方案，当类型不同时，都转化成string进行判断，同时给出WARN日志
  @RelatedColumn(value = "school_id", remoteColumn = "id")
  private SchoolDO schoolDO;

}
```

这样就可以了，查询时接口都一模一样，只是把StudentDO.class的地方换成StudentVO.class即可。

**Join查询**

其实和关联查询相似，但有些表有where查询条件时，必须得用join来查询。nimble-orm采用一种巧妙的方式，在不新增接口的情况下，满足了这个需求，目前只支持2个表关联，更多的表关联应该尽量避免。

同样的，定义个VO，使用查询接口一模一样：

```java
@JoinTable(joinType = JoinTypeEnum.LEFT_JOIN, on = "t1.school_id=t2.id")
public class StudentSchoolJoinVO {

  @JoinLeftTable
  private StudentDO studentDO;
  
  @JoinRightTable
  private SchoolDO schoolDO;
  
｝
```

上面定义的StudentSchoolJoinVO即表达了`select t1.*,t2.* from t_student t1 left join t_school t2 on t1.school_id=t2.id where t1.deleted=0 and (t2.deleted=0 or t2.deleted is null)`的基本语句。

## 关于数据库字段到Java字段的映射关系

强烈建议java POJO不要使用基础类型(如int long)，同时引用类型也不要设置默认值。该建议同样适用于MyBatis，因为：

1. 无法表达数据库NULL数值。

2. 执行相关updateNotNull操作时会埋坑，有些值会一直被默认值覆盖。

其它：

1. 当数据库字段是NULL，而java POJO的字段是基础类型时，会转换成0。数据库字段不支持enum，推荐使用String来表达。

2. 一般来说，Java中的Boolean字段会对应数据库的tinyint(1)字段，和C语言保持一致，对于tinyint(4)，0表示false，非0表示true(包括负数)。当然dbHelper也支持mysql bit的类型。

## 一些思考和选择

* 为什么insert和update默认只插入非null的值，而单独提供一个insertWithNull和updateWithNull的方法呢?

> 答：对于null值，如果写入到数据库，那肯定是null，同时无法使用数据库的默认值。因此null值是没有必要默认就写入数据库的。同理，update也保持一致的做法。

* 为什么要提供`void rollback()`方法，手工来回滚事务？

> 答：DAO层和service层向上层抛出异常以表达错误，是不太建议的方式。我知道有些建议会推荐使用显式抛异常的方式来表达错误，但上层的处理就会变得麻烦。更好的做法我认为是尽量靠返回值来表示处理的结果，而异常仅用在“无法预测”的不正常情况或者返回值无法表达更多信息的情况下。而@Transational事务回滚，需要抛出RuntimeException，这个行为显然会干扰调用者。所以，为了保证用返回值表达结果，就必须手动回滚事务。

* 为什么@Column的注解要显示指定value值，不做自动根据字段名配置映射成驼峰形式或下划线形式？

> 答：`约定优于配置`是一种很常用的指导方向。但需要明确的是，适合约定的内容是什么？它应该是大家乐于接受的，表达明确的，且不易改变的，能支持绝大多数的功能的。例如我们约定linux操作系统的文件夹路径用`/`分隔，就是一个好的约定，在整个linux操作系统内，没有额外的需求需要用另外一个符号来表示。相反的，像webx框架中，约定url到方法的映射关系，可以支持驼峰或下划线的多种混合格式时，就是一个差的约定，例如Java中sayHi的方法，就默认支持url中`say_hi`、`sayHi`，`say_Hi`、`SayHi`等多种不同方式的映射。约定有一个副作用是，失去了追查系统实现的线索，例如你维护一个老系统，前端过来请求`say_Hi`，你怎样快速准确地找到这个url对应的方法？显然你也只能猜测去查找，java中是sayHi，或者say_Hi?。所以Spring MVC明确注解`@RequestMapping`就是一个很不错的方式，它明确了前后端映射的准确对应关系。同样的道理，`@Column`要求明确写value值。

* 为什么查询参数当SQL写为`a=?`且传入的参数值是null时，不自动转换为`a is null`?

> 答：首先，在实际开发过程中，传入查询参数明确为null是极少见的情况，更多的情况是参数有异常（例如参数名称写错导致其值为null，或者前端忘记传递过来导致其值为null）且没有过滤导致。其次，null是有歧义的、不够明确的值，在查询语句中，它即可以表示不需要查询该字段，也可以表示为查询该字段为null的值。在这种有歧义的情况下，最好的设计就是让框架把这种更有可能是人为失误的情况报出异常（通常可以在测试阶段发现），而非自动转换为`a is null`来隐藏一个问题，埋下一个大坑。

## 注意事项

* 如果需要传入的参数是Object...（例如getAll之类的方法），那么当单个参数需要传入多个值时，强烈使用List来传。因为如果使用Object[]来传，Object...本身就是Object[]类型，当只有单个Object[]的时候，就只会取Object[]的第一个参数作为参数，这样就有错误，而且是语义错误，很隐蔽。有一种hack的方式，但不推荐，在传入Object[]参数后面，再加上一个任意类型的参数，让Java不要认为参数是Object...。

* 参数列表中不能出现null，否则会报`org.springframework.dao.InvalidDataAccessApiUsageException: No value supplied for the SQL parameter 'param1': No value registered for key 'param1'`

* 目前发现HikariCP数据库连接池在高并发时，getPage获取分页总数时，会有返回1的异常数据。使用tomcat-jdbc或duird则没有问题。因此不推荐使用HikariCP，它在实现高性能的同时肯定突破了某些规范。

## 未来规划

1. 拦截器设计。

2. Join方式设计。(0.3.0+ 已实现)

3. 分表支持

## QQ群：178709063 欢迎使用
