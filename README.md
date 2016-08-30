# nimble-orm

## 关于数据库字段到Java字段的映射关系
强烈建议java POJO不要使用基础类型，同时引用类型也不要设置默认值。因为：一来无法表达数据库NULL数值，二来updateNotNull会埋坑。

当数据库字段是NULL，而java POJO的字段是基础类型时，会转换成0。数据库字段不支持enum，推荐使用String来表达。

## 注意事项

如果参数列表需要传入的是Object...（例如getAll之类的方法），那么当需要传入一组集合时，强烈使用List来传，因为使用Object[]来传的话，Object...本身就是Object[]类型，但只有单个Object[]的话，就只会取Object[]的第一个参数作为参数，这样就导致错误。有一种hack的方式是，传入Object[]参数后面，再加上一个任意类型的参数，让Java不要认为参数是Object...即可。

参数列表中不能出现null，否则会报org.springframework.dao.InvalidDataAccessApiUsageException: No value supplied for the SQL parameter 'param1': No value registered for key 'param1'

## 一些思考和选择

 - 为什么insert默认只插入非null的值，而单独提供一个insertWithNull的方法呢?

    答：对于null值，如果写入写入到数据库，那肯定是null，同时无法使用数据库的默认值。因此null值是没有必要通过指定来写入数据库的。

