## RelatedColumn多字段关联

正常情况下RelatedColumn只能处理一张表的A字段，关联到另一张表的B字段这样的等值关联。

然而有些情况下，由于设计不佳或历史原因等情况，会出现多字段关联的需求。

举个例子，假设有一个区域表，区域的唯一标识可以由层级layer_code和区域代号area_code组成，表结构如下：

```sql
CREATE TABLE `t_area` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `layer_code` varchar(20) DEFAULT NULL,
  `area_code` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

数据示例如下：

|  id   | layer_code  | area_code | 说明 |
|  ----  | ----  | ---- | ---- |
| 1  | COUNTRY | CN | 这个表示中国 | 
| 2  | CITY | SZ | 这个表示深圳城市 |

然后另外有一张表，它存放了区域的经纬度信息：

```sql
CREATE TABLE `t_area_location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `layer_code` varchar(20) DEFAULT NULL,
  `area_code` varchar(20) DEFAULT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

这张表的id是自增id，没有被外部引用。这张表也是使用层级layer_code和区域代号area_code来唯一标识区域。

详见AreaVO.java和AreaLocationVO.java的做法。