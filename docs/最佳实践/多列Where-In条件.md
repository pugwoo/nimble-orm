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
param.add(new Object[]{"tom", "school1"});
param.add(new Object[]{"james", "school2"});
param.add(new Object[]{"neo", "school3"});

List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where (name,school) in (?)", param);
```

说明：对于单列的in，可以传空List，但是对于多列的in，必须传入非空List，值为:
    
```java
    List<Object[]> param = new ArrayList();
    param.add(new Object[]{null, null}); // 根据实际的列数来加对应个数的null值
```