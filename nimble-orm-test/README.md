## FAQ

1. 怎样在IDEA显示单元测试覆盖率情况？

答：由于源码模块和单元测试模块在不同的module中，因此执行单元测试时，需要Edit Configuration，在打开的执行配置中，选择Modify options，去掉勾选`Code Coverage` - `Specify classes and packages`。这样就可以显示源码模块的覆盖率情况了。