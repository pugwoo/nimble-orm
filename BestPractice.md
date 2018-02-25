# nimbile-orm的最佳实践

## 1. 关于冗余数据的处理

首先，从产品设计上，一个字段是否冗余，取决于业务。两个典型的场景：

1) 电商下单购买商品时，需要冗余当前购买商品的标题、价格、图标等信息。下单成功意味着一张交易合同生成了，这个订单的产品数量、产品价格、优惠金额、总价**不再**受后续商品价格等信息的变化而有变动，它是一个历史数据了。

2) 论坛上用户的帖子和回复，会显示该帖子及回复对应的用户昵称和头像，用户的昵称和头像是不期望冗余的，当用户修改了自己的头像，所有他的发的帖子及回复显示的，就是他新的头像。在主流的知乎、微信朋友圈中都是显示用户最新的头像的。

站在技术开发的角度，必须强调一点，决定是否去冗余一个信息，不要受读取便利性的影响。有时候我们为了展示一个帖子列表，里面有昵称和头像，有些数据库设计就会把这两个信息冗余到帖子表中，使得读出时不用再去查询一遍，但是这样引入的不一致性将很容易导致显示出问题。

接着，讨论下Java Model的常见设计。在Java中，会根据数据库的表设计，创建数据模型类，一般叫model或entity，类名以DO结尾，类成员变量和数据库字段一一对应。这点在dbhelper中的@Table和@Column注解中明显体现。dbhelper是贫血模型，更接近于linux C语言(with-object)的使用习惯。

由于java中的数据对象可能是数据表的扩展或是几个数据表的组合，因此java中常用类名结尾为VO的数据结构类来表达。当VO类是某个DO类的扩展时，由于节省重新定义DO类那么多字段，会用VO继承自DO，并在VO中增加数据成员的方式来做到。举个简单的例子，一个帖子表和用户表，帖子表的user_id字段关联了用户表的id字段。它们的DO类如下：
```java
@Table("t_user")
public class UserDO {
    
    @Column("id")
    private Long id;

    @Column("nickname")
    private String nickname; // 用户昵称，用户头像、积分等信息类似
    
    // getters / setters
}
```

```java
@Table("t_discuss")
public class DiscussDO {
    
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;
    
    @Column("title")
    private String title; // 帖子标题，帖子正文、发布时间等信息类似
    
    // getters / setters
}
```

上面两个表很清楚地记录了帖子内容和帖子由哪个用户发表。在页面显示时，需要把用户的昵称、头像显示在页面上。这种情况下，一般的做法会创建一个帖子VO：
```java
public class DiscussVO extends DiscussDO {

    private UserDO userDO; // 帖子对应的用户信息

}
```

传统的做法会写java代码去查询出userDO再set到discussVO中，在dbhelper中，通过@RelatedColumn注解就可以完成这个事情，代码更少，而且dbhelper做了优化，对于分页一次查十个二十个的情况，如果写for循环去查要查很多次SQL，而dbhelper只需要查一次。写法如下：
```java
public class DiscussVO extends DiscussDO {

    @RelatedColumn(localColumn = "user_id", remoteColumn = "id")
    private UserDO userDO; // 帖子对应的用户信息

}
```

然后将DiscussVO传入到dbhelper的查询接口中即可。这样做已经接近最佳的状态的，但还差一些些。首先，为了查询这些冗余信息，需要新建出一个DiscussVO，导致查询和修改传入的类可能是不同的，类似这样：
```java
dbHelper.getAll(DiscussVO.class);
dbHelper.insert(discussDO); // 实际上传入discussVO实例也是可以的
```

而查询DiscussDO的时候绝大多数情况都是要查出用户信息的。利用dbhelper灵活而语义清晰的注解，我们可以定义一个查询和修改行为不同的DO类，避免创建过多的类，实际的DiscussDO类定义如下：
```java
@Table("t_discuss")
public class DiscussDO {
    
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;
    
    @Column("title")
    private String title;
    
    @RelatedColumn(localColumn = "user_id", remoteColumn = "id") // +
    private UserDO userDO; // +
    
    public String getNickname() {  // + 只提供get，不提供set
        return userDO == null ? "" : userDO.getNickname();
    }
    
    // getters / setters 这里的getter/setter只是DiscussDO对应于数据表字段的
}
```

这样便无需创建VO类，而这个DO类在查询时会查出冗余信息，又不会提供set冗余信息的接口，语义清晰。同时，在所有dbhelper调用中，都是统一地用DiscussDO。DO的读和写分离开了。

## 2. 在IDE中重构代码

DBHelper的设计初衷，就是为了适应互联网应用表结构经常变化的特点，以弥补mybatis的不足。

在DBHelper中，所有的表操作都会使用dbhelper工具来操作，且每个操作都和某个表有直接关联，即类DO名会体现在dbhelper接口上。

我们按照表结构的重构类型来分类：

1. 修改表名: DO的@Table注解需要修改，一般只需要修改这里。但是不排除表名在其它sql的where子句被使用，所以需要全文搜索原表名，如果有则修改。

2. 修改字段名: 一种很常用的场景，一般我们重命名字段是因为字段名称不符合规范或语义，可能多个表都有资格字段名称需要修改。我们可以简单地搜索代码中这个字段名称，**以字段为单位进行修改**，多个表一起修改，以完全避免上述可能改漏的情况。注意最好忽略大小写。正则表达式为：`".*字段名.*"` 或 `dbhelper[\S\s]*".*字段名.*"`

```
DO的@Column注解需要修改，此外表名经常被用在查询条件中，因此要用IDE正则搜索`dbhelper[\S\s]*DO类名[\S\s]*".*字段名.*"`，找到后确认是否是真的需要修改。但是这种方式也确实可能漏掉，因为dbhelper操作的参数，可能从一个更大的数据中去get或者是在其它DO的子查询语句中，搜索`dbhelper[\S\s]*".*字段名.*"`，并没有把类名暴露在代码中。所以，代码规范上，建议一个表的操作被封装在单个或尽量少的ServiceImpl中，不能暴露到Web层。
```

3. 修改字段类型: 由于dbhelper是自动适配类型的，所以只需要修改DO @Column注解的java字段对应的类型。

4. 增加字段: 由于新字段并不会在代码被使用，所以新增字段是最简单的，常常也是最常见的，只需要DO类加上字段(含getter/setter)并注解上@Column即可。

5. 删除字段: 类似修改字段名，除了@Column字段删除掉，还需要IDE正则搜索字段在其它where子句被使用的地方。

## 3. 树型结构的表达

有好些数据结构是树型的。例如商品的类目，每个类目下面有子类目，子类目下面也有子类目，理论上可以有无限多层。例如文件夹，文件夹里有文件和文件夹，也是可以无限多层的。又例如微商的邀请链，每个人可以邀请别人加入，别人又可以继续邀请他人加入。

下面就以商品类目为例，表达无穷多层的树型数据表结构不难：

```sql
-- t_category
id int, -- id主键
name varchar(128), -- 类目名称
parent_id int, -- 指向父类目id，如果为null，则表示根目录
```

对于树形表的修改：增删改，即按照树节点指针的方式，修改其parent_id即可。对于读取来说，有两种情况：

1. 从某一个类目开始，一路查询其parent_id，把该类目的所有父类目全部查询出来。
2. 从某一个类目开始，一路查询其子类目，子类目的子类目，将所有子类目查找出来。

上面两种情况，如果手动使用代码和sql来查询的话，需要写while循环，特别是第2种情况中的一对多查询，代码会非常复杂且查询次数随着层数呈指数增长O(2^n)。如果使用dbhelper，那么while循环不需要自己写了，查询次数也可以降低到层数的线性次数O(n)。

对于情况1，定义CategoryVO extends CategoryDO:

```java
   @RelatedColumn(localColumn = "parent_id", remoteColum = "id")
   private CategoryVO parentCategoryVO;
```

对于情况2，定义CategoryVO extends CategoryDO:

```java
   @RelatedColumn(localColumn = "id", remoteColum = "parent_id")
   private List<CategoryVO> subCategoryVOs;
```

然后使用dbhelper的getOne或getAll或getPage接口都可以，是不是超级简单性能又好？

## 4. 关于@RelatedColumn可能导致的查询死循环

使用@RelatedColumn是要特别注意查询死循环的情况，在TestDeadLoop测试用例中，演示了查询死循环的一个例子。

查询死循环可以通过合理的设计来规避，实际项目中还是很少出现查询死循环的。查询死循环并不会一直真的死循环查询下去，而会抛出堆栈Overflow异常，从而让理论上的死循环中断，但还是会导致过多的数据库查询的发生。

设计规范建议：DO类和数据库表一一对应。VO类继承自DO类，并避免VO类之间的无结束条件的循环引用。
