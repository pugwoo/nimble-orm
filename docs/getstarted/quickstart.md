# 快速开始

使用nimble-orm需要一个初始化好的JdbcTemplate实例，这里假定读者已经掌握了Spring JdbcTemplate的使用配置，以下示例使用Spring Boot进行。

## 1.引入Maven依赖

修改项目的pom.xml文件，增加以下依赖，要求JDK版本>=1.8：

```xml
<dependency>
    <groupId>com.pugwoo</groupId>
    <artifactId>nimble-orm</artifactId>
    <version>1.8.1</version>
</dependency>
```

## 2.配置DBHelper实例

nimble-orm的操作实例类是com.pugwoo.dbhelper.DBHelper，它需要一个JdbcTemplate实例来初始化。这里假定读者已经准备好了一个MySQL或PostgreSQL数据库，并初始化好了JdbcTemplate实例，假设名称为jdbcTemplate。接下来是初始化DBHelper实例bean：

```java
    @Bean("dbHelper")
    public DBHelper dbHelper(@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SpringJdbcDBHelper(jdbcTemplate);
    }
```

## 3.定义DO类

接下来要读写数据库了，我们准备一张最简单的表(MySQL建表语法为例，其它数据库请相应调整)：

```sql
create table t_user (
    id int primary key auto_increment,
    name varchar(32)
);
```

在nimble-orm中，需要为每一张表创建一个对应的DO类，可以手写也可以使用页面工具 https://dbhelper.pugwoo.com 来生成：

```java
import lombok.Data;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;

@Data
@Table("t_user")
public class UserDO {

    /**通过注解的方式描述该字段是主键，是自增字段*/
    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Integer id;

    @Column(value = "name")
    private String name;

}
```

## 4.插入数据

拿到我们要操作的数据库的dbHelper实例，就可以对该数据库进行插入操作：

```java
    @Autowired
    private DBHelper dbHelper;

    public void insert() {
        UserDO userDO = new UserDO();
        userDO.setName("tom");
        dbHelper.insert(userDO); 
        // 注意：此时userDO的自增id字段已经被ORM自动设置好了
    }
```

## 5.查询数据

同样地，使用dbHelper实例进行查询：

```java
    @Autowired
    private DBHelper dbHelper;

    public void query() {
        UserDO userDO = dbHelper.getOne(UserDO.class, "where name=?", "tom");
        System.out.println(userDO);
    }
```

恭喜你，已经成功地使用了nimble-orm！更多特性等你来发现。
