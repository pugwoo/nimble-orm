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


