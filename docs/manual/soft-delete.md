# 软删除

软删除（Soft Delete）是一种常见的数据管理策略，通过标记字段来表示数据是否被删除，而不是真正从数据库中物理删除数据。nimble-orm 提供了完善的软删除支持，让你无需在业务代码中手动处理软删除逻辑。

## 基本用法

### 1. 定义软删除字段

在 DO 类中使用 `@Column` 注解的 `softDelete` 属性来标记软删除字段：

```java
@Column(value = "deleted", softDelete = {"0", "1"})
private Boolean deleted;
```

**注解说明：**
- `softDelete` 必须提供一个包含两个值的数组
- 第一个值：未删除标记（例如：`"0"` 或 `"false"`）
- 第二个值：已删除标记（例如：`"1"` 或 `"true"`）
- 每个 DO 类最多只能有一个 `softDelete` 字段

### 2. 字符串类型的软删除字段

如果软删除字段是字符串类型，需要在值外面加上单引号：

```java
@Column(value = "deleted", softDelete = {"'NO'", "'YES'"})
private String deleted;
```

## 软删除工作原理

一旦配置了软删除字段，nimble-orm 会自动处理所有增删改查操作：

### 删除操作

调用 `delete` 方法时，会自动执行 UPDATE 操作而不是 DELETE：

```java
// 这不会真正删除数据，而是将 deleted 字段设置为 1
dbHelper.delete(studentDO);
// 等价于执行：UPDATE t_student SET deleted=1 WHERE id=?
// 批量删除也同理
```

### 查询操作

所有查询方法会自动过滤掉已软删除的数据：

```java
// 自动添加 WHERE deleted=0 条件
StudentDO student = dbHelper.getByKey(StudentDO.class, 1L);
// 自动添加 WHERE deleted=0 条件
PageData<StudentDO> page = dbHelper.getPage(StudentDO.class, 1, 10);
// 自动添加 WHERE deleted=0 条件
List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where age > ?", 18);
```

你完全不需要在代码中手动添加 `deleted=0` 条件。

## 硬删除

如果需要真正物理删除数据，使用 `deleteHard` 方法：

```java
// 物理删除单条记录
dbHelper.deleteHard(studentDO);
// 物理删除多条记录
dbHelper.deleteHard(studentList);
// 条件物理删除
dbHelper.deleteHard(StudentDO.class, "where age > ?", 18);
```

## 第二种软删除方式

nimble-orm 支持另外一种软删除方式，将软删除的数据自动备份到另一张表中，通过 `@Table` 注解的 `softDeleteTable` 属性配置：

```java
@Table(value = "t_student", softDeleteTable = "t_student_del")
public class StudentDO extends IdableSoftDeleteBaseDO {
    // ...
}
```

**工作原理：**
1. 执行软删除时，会先将数据插入到 `t_student_del` 表
2. 然后在原表 `t_student` 中的数据物理删除。
3. 软删除表的结构必须和原表一致

**跨数据库支持：**

如果软删除表在不同的数据库中，可以指定 DBHelper Bean：

```java
@Table(value = "t_student",
       softDeleteTable = "t_student_del",
       softDeleteDBHelperBean = "archiveDBHelper")
public class StudentDO extends IdableSoftDeleteBaseDO {
    // ...
}
```

**查询软删除表中的数据：**

```java
// 使用 withTableNames 临时切换表名
Map<Class<?>, String> tableNames = new HashMap<>();
tableNames.put(StudentDO.class, "t_student_del");

DBHelper.withTableNames(tableNames, () -> {
    StudentDO deletedStudent = dbHelper.getOne(StudentDO.class, "where id=?", studentId);
    // 这里查询的是 t_student_del 表
});
```

## 自动设置删除时间

配合 `setTimeWhenDelete` 属性，可以在软删除时自动记录删除时间：

```java
@Column(value = "delete_time", setTimeWhenDelete = true)
private LocalDateTime deleteTime;
```

支持的时间类型：
- `java.util.Date` - 精度：秒
- `java.sql.Date` - 精度：秒
- `java.sql.Timestamp` - 精度：毫秒
- `java.sql.Time` - 精度：秒
- `java.time.LocalDateTime` - 精度：秒
- `java.time.LocalDate` - 精度：天
- `java.time.LocalTime` - 精度：秒
- `java.util.Calendar` - 精度：秒
- `java.time.Instant` - 精度：秒
- `java.time.ZonedDateTime` - 精度：秒

## 删除时执行脚本

使用 `deleteValueScript` 可以在删除时自动执行脚本并将脚本返回值作为字段值进行设置：

```java
@Column(value = "name", deleteValueScript = "'deleted_' + t.name")
private String name;
```

删除时，name 字段会被设置为 `"deleted_" + 原name值`。deleteValueScript是一段mvel脚本，你也可以用它来调用一个静态方法。

## 注意事项

1. **唯一性约束**：软删除字段必须提供恰好两个值，否则注解无效
2. **字段数量**：每个 DO 类最多只能有一个软删除字段
3. **虚拟表限制**：使用 `virtualTableSQL` 的虚拟表不支持自动处理软删除标记，此时完全由人工控制虚拟表的SQL。
4. **性能考虑**：同时使用拦截器和 `deleteValueScript`时，软删除会先查询再批量删除，性能可能较差

### 最佳实践

1. **统一基类**：建议创建一个包含软删除字段的基类，让需要软删除的 DO 继承它：

```java
@Data
public class IdableSoftDeleteBaseDO {
    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

    @Column(value = "deleted", softDelete = {"false", "true"})
    private Boolean deleted;
}
```

2. **数据库设计**：在数据库中为 `deleted` 字段添加索引，提高查询性能

```sql
CREATE INDEX idx_deleted ON t_student(deleted);
```

3. **数据归档**：定期将软删除表中的数据归档到历史库，避免数据量过大
