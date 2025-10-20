# 新增和更新时间

nimble-orm 支持在插入和更新时自动为时间字段赋值，无需手写代码。通过在 DO 字段上使用 @Column 的 setTimeWhenInsert 与 setTimeWhenUpdate 属性，即可实现“创建时间”和“更新时间”的自动填充。

## 基本用法

在 DO 类中为时间字段添加注解：

```java
@Data
public class StudentDO {
    @Column(value = "id", isKey = true, isAutoIncrement = true)
    private Long id;

    // 新增时自动设置当前时间（仅当原值为 null）
    @Column(value = "create_time", setTimeWhenInsert = true)
    private LocalDateTime createTime;

    // 更新时总是设置为当前时间；软删除时设置为删除时间
    @Column(value = "update_time", setTimeWhenUpdate = true)
    private LocalDateTime updateTime;
}
```

## 属性语义

### setTimeWhenInsert

- 作用：插入时，当字段原值为 null，自动设置为当前时间
- 常见用途：create_time、gmt_create
- 支持的时间类型及精度：
  - java.util.Date（秒）
  - java.sql.Date（秒）
  - java.sql.Timestamp（毫秒）
  - java.sql.Time（秒）
  - java.time.LocalDateTime（秒）
  - java.time.LocalDate（天）
  - java.time.LocalTime（秒）
  - java.util.Calendar（秒）
  - java.time.Instant（秒）
  - java.time.ZonedDateTime（秒）

### setTimeWhenUpdate

- 作用：更新时，无论字段原值是否为 null，都会自动设置为当前时间
- 常见用途：update_time、gmt_modified
- 与软删除的配合：当使用软删除时，该字段会在删除操作中被设置为“删除发生的时间”
- 支持的时间类型及精度同上

## 设置删除时间

若启用了软删除，[软删除手册](/manual/soft-delete) 中说明了删除时也可记录时间。你可以为“删除时间”单独添加一个字段：

```java
@Column(value = "delete_time", setTimeWhenDelete = true)
private LocalDateTime deleteTime;
```
