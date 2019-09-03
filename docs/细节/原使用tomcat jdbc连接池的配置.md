```xml
    <bean id="dataSource" class="org.apache.tomcat.jdbc.pool.DataSource">
        <property name="driverClassName" value="${jdbc.driverClassName}" />
		<property name="url" value="${jdbc.url}" />
		<property name="username" value="${jdbc.username}" />
		<property name="password" value="${jdbc.password}" />
		<property name="initialSize" value="10" />
		<property name="maxActive" value="20" /> <!-- 最大连接数 -->
		<property name="validationQuery" value="SELECT 1" />
		<property name="testWhileIdle" value="true" /> <!-- 检查空闲链接 -->
		<property name="testOnBorrow" value="false" /> <!-- 借出链接不检查，否则过于影响性能，除非数据库经常重启 -->
		<property name="testOnReturn" value="false" /> <!-- 归还链接不检查 -->
		<!-- 每timeBetweenEvictionRunsMillis毫秒检查空闲超过minEvictableIdleTimeMillis毫秒的连接，mysql默认空闲时间是8小时，因此minEvictableIdleTimeMillis要小于8小时 -->
	    <property name="minEvictableIdleTimeMillis" value="21600000" /> <!-- 6小时，默认30分钟 -->
	    <property name="timeBetweenEvictionRunsMillis" value="7200000" /> <!-- 2小时 -->
	</bean>
```