2018年5月11日 11:20:44

这篇博客提及了这一场景：
https://blog.csdn.net/syuhaitao/article/details/51727453

这个情况在HikariCP中也出现过（必现），
但tomcat pool和druid对于相同的代码则没有，所以可能是这个原因。

因此，全系统在多sql执行时，都加上了事务。同时，事务@Transactional必须加在public方法上。