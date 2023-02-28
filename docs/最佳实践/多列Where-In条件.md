### 背景

使用Nimble-ORM可以很方便地在where条件中使用in操作并传入参数，示例代码如下：

```java
List<String> names = new ArrayList();
names.add("tom");
names.add("james");
names.add("neo");

List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where name in (?)", names);
```

如果in操作的是多个字段呢，例如：

```java
dbHelper.getAll(StudentDO.class, "where (name,school) in (?)", 参数); // 那么参数应该怎么传呢？
```

结论是这样传：

```java
List<Object[]> param = new ArrayList();
names.add(new Object[]{"tom", "school1"});
names.add(new Object[]{"james", "school2"});
names.add(new Object[]{"neo", "school3"});

List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where (name,school) in (?)", param);
```