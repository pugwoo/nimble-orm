# 基础特性

## 软删除

软删除（Soft Delete）是一种常见的数据管理策略，通过标记字段来表示数据是否被删除，而不是真正从数据库中物理删除数据。nimble-orm 提供了完善的软删除支持，让你无需在业务代码中手动处理软删除逻辑。

##### 假设你已经写了一些代码

假设你已经有了一个学生信息表：

```sql
create table t_student (
  id int primary key auto_increment,
  name varchar(32)
);
```

并且已经把表的增删改查的代码都写完了，类似这样的代码你已经写了很多：

```java
dbHelper.insert(studentDO);
dbHelper.update(studentDO);
dbHelper.delete(studentDO);
dbHelper.getPage(StudentDO.class, "where name=?", queryName);
```

##### 现在你需要软删除功能

假设你现在考虑到t_student表非常重要，你不希望真的物理删除表中的数据，你希望加一个字段deleted，0表示未删除，1表示已删除。于是你给表加了这个字段：

```sql
alter table t_student
  ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0: 未删除；1: 已删除' AFTER id;
```

加完这个字段后，如果没有ORM框架，你必须到处修改SQL：所有查询和update都要加上`where deleted=0`，所有delete要改成`update set deleted=1`。而如果你使用的是nimble-orm，那么你只需要给DO类加上一个字段及注解：

```java
@Column(value = "deleted", softDelete = {"0", "1"})
private Boolean deleted;
```

这样ORM就会自动给所有的查询和update都加上`where deleted=0`，自动将所有delete硬删除改成`update set deleted=1`的方式。我们的代码，除了DO类注解了deleted，其它地方基本上不需要写delete字段。

这也是nimble-orm为何声称是渐进式使用、对重构友好的原因，你不需要在一开始就确定要软删除。