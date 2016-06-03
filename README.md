# nimble-orm
一个灵活轻量级的ORM

## 关于数据库字段到Java字段的映射关系
当数据库字段是NULL，而java POJO的字段是基础类型时，会转换成0。
数据库字段不支持enum，推荐使用String来表达。
