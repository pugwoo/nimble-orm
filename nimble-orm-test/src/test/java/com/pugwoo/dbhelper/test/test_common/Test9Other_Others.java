package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.Table;
import com.pugwoo.dbhelper.annotation.WhereColumn;
import com.pugwoo.dbhelper.cache.ClassInfoCache;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.enums.JoinTypeEnum;
import com.pugwoo.dbhelper.exception.BadSQLSyntaxException;
import com.pugwoo.dbhelper.exception.CasVersionNotMatchException;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.exception.MustProvideConstructorException;
import com.pugwoo.dbhelper.exception.NoColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NoJoinTableMemberException;
import com.pugwoo.dbhelper.exception.NoKeyColumnAnnotationException;
import com.pugwoo.dbhelper.exception.NoTableAnnotationException;
import com.pugwoo.dbhelper.exception.NotAllowQueryException;
import com.pugwoo.dbhelper.exception.NotOnlyOneKeyColumnException;
import com.pugwoo.dbhelper.exception.NullKeyValueException;
import com.pugwoo.dbhelper.exception.OnConditionIsNeedException;
import com.pugwoo.dbhelper.exception.ParameterSizeNotMatchedException;
import com.pugwoo.dbhelper.exception.RowMapperFailException;
import com.pugwoo.dbhelper.exception.ScriptErrorException;
import com.pugwoo.dbhelper.json.NimbleOrmDateUtils;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.sql.SQLAssemblyUtils;
import com.pugwoo.dbhelper.sql.WhereSQL;
import com.pugwoo.dbhelper.sql.WhereSQLForNamedParam;
import com.pugwoo.dbhelper.test.entity.AreaDO;
import com.pugwoo.dbhelper.test.entity.AreaLocationDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.TypesDO;
import com.pugwoo.dbhelper.test.service.MyCustomWhereProvider;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.AreaVO;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.TypeAutoCast;
import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.collect.MapUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * 其它的一些测试，主要为了覆盖代码或最佳实践
 */
public abstract class Test9Other_Others {

    public abstract DBHelper getDBHelper();
    
    @Test 
    public void testRelateComputedColumn() {
        getDBHelper().delete(AreaDO.class, "where 1=1");
        getDBHelper().delete(AreaLocationDO.class, "where 1=1");

        AreaDO area = new AreaDO();
        area.setLayerCode("CITY");
        area.setAreaCode("SZ");
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            area.setId(CommonOps.getRandomLong());
        }

        getDBHelper().insert(area);

        AreaLocationDO areaLocationDO = new AreaLocationDO();
        areaLocationDO.setLayerCode("CITY");
        areaLocationDO.setAreaCode("SZ");
        areaLocationDO.setLongitude(new BigDecimal("120"));
        areaLocationDO.setLatitude(new BigDecimal("22"));
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            areaLocationDO.setId(CommonOps.getRandomLong());
        }

        getDBHelper().insert(areaLocationDO);

        AreaVO one = getDBHelper().getOne(AreaVO.class);
        assert one.getLocationVO() != null;

    }

    @Test 
    public void testTypes() {
        TypesDO typesDO = new TypesDO();
        typesDO.setId1(CommonOps.getRandomLong());
        typesDO.setId2(CommonOps.getRandomLong());
        typesDO.setMyByte("a".getBytes()[0]);
        typesDO.setS(Short.valueOf("11"));
        typesDO.setMyFloat(11.1f);
        typesDO.setMyDouble(22.2);
        typesDO.setMyDecimal(new BigDecimal("11.22"));
        typesDO.setMyDate(new java.sql.Date(new Date().getTime()));

        // clickhouse不支持单独的时间类型(即没有日期的时间)
        if (getDBHelper().getDatabaseType() != DatabaseTypeEnum.CLICKHOUSE) {
            typesDO.setMyTime(new java.sql.Time(new Date().getTime()));
        }

        typesDO.setMyTimestamp(new java.sql.Timestamp(new Date().getTime()));
        typesDO.setMyMediumint(123456);

        getDBHelper().insert(typesDO);
        assert typesDO.getId1() != null;
        assert typesDO.getId2() != null;

        TypesDO types2 = getDBHelper().getOne(TypesDO.class, "where id1=? and id2=?", typesDO.getId1(), typesDO.getId2());
        assert types2.getMyByte().equals(typesDO.getMyByte());
        assert types2.getS().equals(typesDO.getS());
        assert types2.getMyFloat().equals(typesDO.getMyFloat());
        assert types2.getMyDouble().equals(typesDO.getMyDouble());
        assert types2.getMyDecimal().equals(typesDO.getMyDecimal());
        assert types2.getMyMediumint().equals(typesDO.getMyMediumint());
        // 日期的手工比对过了，数据库存的是0时区的值，记得
        System.out.println(types2.getMyDate());
        System.out.println(types2.getMyTime());
        System.out.println(types2.getMyTimestamp());
    }

    @Test
    public void testOthers() throws NoSuchFieldException, IOException {
        // JoinTypeEnum
        assert !JoinTypeEnum.JOIN.getName().isEmpty();

        // PageData
        PageData<String> page = new PageData<>(100, new ArrayList<>(), 10);
        assert page.getTotalPage() == 10;
        assert page.getPageSize() == 10;
        page = new PageData<>(9, new ArrayList<>(), 10);
        assert page.getTotalPage() == 1;
        assert page.getPageSize() == 10;
        page = new PageData<>(0, new ArrayList<>(), 10);
        assert page.getTotalPage() == 0;
        assert page.getPageSize() == 10;
        page = new PageData<>(11, new ArrayList<>(), 10);
        assert page.getTotalPage() == 2;
        assert page.getPageSize() == 10;
        page = new PageData<>(11, new ArrayList<>(), 0);
        assert page.getTotalPage() == 11;

        // exceptions
        Exception cause = new Exception();
        String errorMsg = "err message";

        {
            BadSQLSyntaxException ex = new BadSQLSyntaxException(); // for test
            ex = new BadSQLSyntaxException(cause);
            assert ex.getCause().equals(cause);
            ex = new BadSQLSyntaxException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            CasVersionNotMatchException ex = new CasVersionNotMatchException(); // for test
            ex = new CasVersionNotMatchException(cause);
            assert ex.getCause().equals(cause);
            ex = new CasVersionNotMatchException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            InvalidParameterException ex = new InvalidParameterException(); // for test
            ex = new InvalidParameterException(cause);
            assert ex.getCause().equals(cause);
            ex = new InvalidParameterException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            MustProvideConstructorException ex = new MustProvideConstructorException();  // for test
            ex = new MustProvideConstructorException(cause);
            assert ex.getCause().equals(cause);
            ex = new MustProvideConstructorException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NoColumnAnnotationException ex = new NoColumnAnnotationException();  // for test
            ex = new NoColumnAnnotationException(cause);
            assert ex.getCause().equals(cause);
            ex = new NoColumnAnnotationException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NoJoinTableMemberException ex = new NoJoinTableMemberException();  // for test
            ex = new NoJoinTableMemberException(cause);
            assert ex.getCause().equals(cause);
            ex = new NoJoinTableMemberException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NoKeyColumnAnnotationException ex = new NoKeyColumnAnnotationException();  // for test
            ex = new NoKeyColumnAnnotationException(cause);
            assert ex.getCause().equals(cause);
            ex = new NoKeyColumnAnnotationException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NoTableAnnotationException ex = new NoTableAnnotationException();  // for test
            ex = new NoTableAnnotationException(cause);
            assert ex.getCause().equals(cause);
            ex = new NoTableAnnotationException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NotAllowQueryException ex = new NotAllowQueryException();  // for test
            ex = new NotAllowQueryException(cause);
            assert ex.getCause().equals(cause);
            ex = new NotAllowQueryException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NotOnlyOneKeyColumnException ex = new NotOnlyOneKeyColumnException(); // for test
            ex = new NotOnlyOneKeyColumnException(cause);
            assert ex.getCause().equals(cause);
            ex = new NotOnlyOneKeyColumnException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NullKeyValueException ex = new NullKeyValueException();  // for test
            ex = new NullKeyValueException(cause);
            assert ex.getCause().equals(cause);
            ex = new NullKeyValueException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            OnConditionIsNeedException ex = new OnConditionIsNeedException(); // for test
            ex = new OnConditionIsNeedException(cause);
            assert ex.getCause().equals(cause);
            ex = new OnConditionIsNeedException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            ParameterSizeNotMatchedException ex = new ParameterSizeNotMatchedException();  // for test
            ex = new ParameterSizeNotMatchedException(cause);
            assert ex.getCause().equals(cause);
            ex = new ParameterSizeNotMatchedException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            RowMapperFailException ex = new RowMapperFailException(); // for test
            ex = new RowMapperFailException(cause);
            assert ex.getCause().equals(cause);
            ex = new RowMapperFailException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }

        {
            ScriptErrorException ex = new ScriptErrorException(); // for test
            ex = new ScriptErrorException(cause);
            assert ex.getCause().equals(cause);
            ex = new ScriptErrorException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }

        // test some getter setter

        // 允许getter和setter为null
        Field name = SomeDO.class.getDeclaredField("name");
        assert ClassInfoCache.getFieldGetMethod(name) == null;
        assert ClassInfoCache.getFieldSetMethod(name) == null;
        assert ClassInfoCache.getFieldGetMethod(name) == null; // 走cache
        assert ClassInfoCache.getFieldSetMethod(name) == null; // 走cache

        SomeDO someDO = new SomeDO();
        someDO.name = "helloname";
        assert DOInfoReader.getValue(name, someDO).equals("helloname");

        // utils
        assert InnerCommonUtils.isEmpty(new ArrayList<>());
        List<String> list = null;
        assert InnerCommonUtils.filter(list, o -> true).isEmpty();

        assert !InnerCommonUtils.isContains("hello", null);
        assert !InnerCommonUtils.isContains("hello", new String[0]);

    }

    public static class SomeDO {
        private String name;
    }

    @Data
    @Table(value = "t_student")
    public static class StudentForGroupDO {
        @Column(value = "id1", computed = "max(id)")
        private Long id;
        @Column(value = "name1", computed = "max(name)")
        private String name;
        @Column(value = "age1", computed = "max(age)")
        private Integer age;
    }

    @Test
    public void testWhereSQL() {
        int num1 = 3 + new Random().nextInt(5);
        String prefix1 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        List<StudentDO> student1 = CommonOps.insertBatch(getDBHelper(), num1, prefix1);

        int num2 = 3 + new Random().nextInt(5);
        String prefix2 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        List<StudentDO> student2 = CommonOps.insertBatch(getDBHelper(), num2, prefix2);

        WhereSQL whereSQL = new WhereSQL("name like ?", prefix1 + "%");
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1;

        whereSQL = new WhereSQL().and("name like ?", prefix1 + "%"); // 等价写法
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1;

        whereSQL.and(new WhereSQL()); // 加一个空的，等于没有任何约束
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1;

        whereSQL.or(new WhereSQL("name like ?", prefix2 + "%"));
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.not();
        int size = getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size();
        long size2 = getDBHelper().getCount(StudentDO.class, "where name is not null") - (num1 + num2);
        assert size == size2;

        whereSQL.not();
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.and("1=?", 1);
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL = whereSQL.copy();
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        // 测试空的not，等于没有约束
        assert getDBHelper().getAll(StudentDO.class).size() ==
                getDBHelper().getAll(StudentDO.class, new WhereSQL().not().getSQL()).size();

        // =============================== 重新new WhereSQL

        whereSQL = new WhereSQL("name like ? or name like ?", prefix1 + "%", prefix2 + "%");
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.and("1=1");
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.addOrderBy("id desc");
        List<StudentDO> all = getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams());
        assert all.size() == num1 + num2;
        for (int i = 0; i < all.size() - 1; i++) {
            assert all.get(i).getId() > all.get(i + 1).getId();
        }

        whereSQL.resetOrderBy();
        whereSQL.addGroupBy("age");
        assert  getDBHelper().getAll(StudentForGroupDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == 1;

        whereSQL.resetGroupBy();
        whereSQL.addGroupBy("id", "name");
        assert getDBHelper().getAll(StudentForGroupDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.resetGroupBy();
        whereSQL.addGroupByWithParam("id + ?", 1); // 重复group id效果一样
        whereSQL.having("count(*) = ?", 0);
        assert getDBHelper().getAll(StudentForGroupDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == 0;

        whereSQL.having("count(*) > ?", 0);
        assert getDBHelper().getAll(StudentForGroupDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.having("count(*) > 0");
        assert getDBHelper().getAll(StudentForGroupDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.resetOrderBy();
        whereSQL.addOrderBy("max(name) desc", "max(age)");
        List<StudentForGroupDO> all2 = getDBHelper().getAll(StudentForGroupDO.class, whereSQL.getSQL(), whereSQL.getParams());
        assert all2.size() == num1 + num2;
        for (int i = 0; i < all2.size() - 1; i++) {
            assert all2.get(i).getName().compareTo(all2.get(i + 1).getName()) > 0;
        }

        whereSQL.resetOrderBy();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.POSTGRESQL) {
            whereSQL.addOrderByWithParam("concat(max(name),?::text) desc", "abc"); // pg识别不了这个参数的类型，需要加::text
        } else {
            whereSQL.addOrderByWithParam("concat(max(name),?) desc", "abc");
        }
        all2 = getDBHelper().getAll(StudentForGroupDO.class, whereSQL.getSQL(), whereSQL.getParams());
        assert all2.size() == num1 + num2;
        for (int i = 0; i < all2.size() - 1; i++) {
            assert all2.get(i).getName().compareTo(all2.get(i + 1).getName()) > 0;
        }

        // test limit

        whereSQL.limit(5);

        all2 = getDBHelper().getAll(StudentForGroupDO.class, whereSQL.getSQL(), whereSQL.getParams());
        assert all2.size() == 5;

        whereSQL.limit(3, 3);
        all2 = getDBHelper().getAll(StudentForGroupDO.class, whereSQL.getSQL(), whereSQL.getParams());
        assert all2.size() == 3;
    }

    @Data
    public static class QueryStudentReq {
        @WhereColumn("name = ?")
        private String name;
        // @WhereColumn("age >= ?")
        @WhereColumn(value = "", customWhereProvider = MyCustomWhereProvider.class) // 使用自定义提供的条件
        private Integer atLeastAge;
        @WhereColumn("age <= ?")
        private Integer maxAge;

        @WhereColumn(value = "school_id=? and school_id=?", orGroupName = "schoolId") // 故意写2遍?
        private Long schoolId1;

        private Long schoolId2;

        @WhereColumn(value = "school_id=?", orGroupName = "schoolId")
        public Long getSchoolId2() {
            return schoolId2;
        }

        @WhereColumn(value = "")
        public WhereSQL getSomething() {
            WhereSQL whereSQL = new WhereSQL();
            whereSQL.and("province=?", "gd");
            return whereSQL;
        }
    }

    @Test
    public void testWhereSQLAnnotation() {

        QueryStudentReq req = new QueryStudentReq();
        req.setName("tom");
        req.setAtLeastAge(16);
        // maxAge不设置，会自动被忽略
        req.setSchoolId1(1L);
        req.setSchoolId2(2L);

        WhereSQL whereSQL = WhereSQL.buildFromAnnotation(req);
        String sql = whereSQL.getSQL().trim();
        System.out.println(sql);
        assert sql.equalsIgnoreCase("WHERE name = ? AND age >= ? AND province=? AND (school_id=? and school_id=? OR school_id=?)");
        assert whereSQL.getParams().length == 6;
        assert whereSQL.getParams()[0].equals("tom");
        assert whereSQL.getParams()[1].equals(16);
        assert whereSQL.getParams()[2].equals("gd");
        assert whereSQL.getParams()[3].equals(1L);
        assert whereSQL.getParams()[4].equals(1L);
        assert whereSQL.getParams()[5].equals(2L);
    }

    @Test
    public void testWhereSQLAppendAnd() {
        WhereSQL whereSQL = new WhereSQL();
        whereSQL.and("name='nick'");
        whereSQL.and("age=18");
        System.out.println(whereSQL.getSQLForWhereAppend());
        assert whereSQL.getSQLForWhereAppend().equals(" AND name='nick' AND age=18 ");

        whereSQL = new WhereSQL();
        whereSQL.or("name='nick'");
        whereSQL.or("age=18");
        System.out.println(whereSQL.getSQLForWhereAppend());
        assert whereSQL.getSQLForWhereAppend().equals(" AND (name='nick' OR age=18) ");
    }

    @Test
    public void testGetRawWithInNamedParam() {
        List<StudentDO> studentDOS = CommonOps.insertBatch(getDBHelper(), 10);
        List<Long> studentIds = ListUtils.transform(studentDOS, o -> o.getId());

        Map<String, Object> params = new HashMap<>();
        params.put("studentIds", studentIds);

        List<StudentDO> raw = getDBHelper().getRaw(StudentDO.class, "select *from t_student where id in (:studentIds)", params);
        assert raw.size() == 10;

        ListUtils.sortAscNullLast(studentIds, o -> o);
        List<Long> studentIds2 = ListUtils.transform(raw, o -> o.getId());
        ListUtils.sortAscNullLast(studentIds2, o -> o);

        assert Objects.equals(studentIds, studentIds2);
    }

    @Test
    public void testWhereSQLForNamedParam() {
        int num1 = 3 + new Random().nextInt(5);
        String prefix1 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        List<StudentDO> student1 = CommonOps.insertBatch(getDBHelper(), num1, prefix1);

        int num2 = 3 + new Random().nextInt(5);
        String prefix2 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        List<StudentDO> student2 = CommonOps.insertBatch(getDBHelper(), num2, prefix2);

        WhereSQLForNamedParam whereSQL = new WhereSQLForNamedParam("deleted=false and name like :name",
                MapUtils.of("name", prefix1 + "%"));
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams()).size() == num1;

        whereSQL = new WhereSQLForNamedParam("deleted=false").and("name like :name", MapUtils.of("name", prefix1 + "%")); // 等价写法
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams()).size() == num1;

        whereSQL.and(new WhereSQLForNamedParam()); // 加一个空的，等于没有任何约束
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams()).size() == num1;

        whereSQL.or(new WhereSQLForNamedParam("deleted=false and name like :name2", MapUtils.of("name2", prefix2 + "%")));
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.not().and("deleted=false");
        int size = getDBHelper().getRawOne(Integer.class, "select count(*) from t_student "+ whereSQL.getSQL(), whereSQL.getParams());
        long size2 = getDBHelper().getCount(StudentDO.class, "where name is not null") - (num1 + num2);
        assert size == size2;

        whereSQL.not().and("deleted=false");
        assert getDBHelper().getRawOne(Integer.class, "select count(*) from t_student " + whereSQL.getSQL(),
                whereSQL.getParams()) == num1 + num2;

        whereSQL.and("1=:one", MapUtils.of("one", 1)).and("deleted=false");
        assert getDBHelper().getRawOne(Integer.class, "select count(*) from t_student " + whereSQL.getSQL(), whereSQL.getParams()) == num1 + num2;

        whereSQL = whereSQL.copy();
        assert getDBHelper().getRawOne(Integer.class, "select count(*) from t_student " + whereSQL.getSQL(), whereSQL.getParams()) == num1 + num2;

        // 测试空的not，等于没有约束
        assert getDBHelper().getAll(StudentDO.class).size() ==
                getDBHelper().getRawOne(Integer.class, "select count(*) from t_student " + new WhereSQL().not().and("deleted=false").getSQL());

        // =============================== 重新new WhereSQL

        whereSQL = new WhereSQLForNamedParam("name like :name1 or name like :name2",
                MapUtils.of("name1", prefix1 + "%", "name2", prefix2 + "%"));
        assert getDBHelper().getRawOne(Integer.class, "select count(*) from t_student " + whereSQL.getSQL(), whereSQL.getParams()) == num1 + num2;

        whereSQL.and("1=1");
        assert getDBHelper().getRawOne(Integer.class, "select count(*) from t_student " + whereSQL.getSQL(), whereSQL.getParams()) == num1 + num2;

        whereSQL.addOrderBy("id desc");
        List<StudentDO> all = getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(),
                whereSQL.getParams());
        assert all.size() == num1 + num2;
        for (int i = 0; i < all.size() - 1; i++) {
            assert all.get(i).getId() > all.get(i + 1).getId();
        }

        whereSQL.resetOrderBy();
        whereSQL.addGroupBy("age");
        assert  getDBHelper().getRaw(StudentForGroupDO.class,
                "select max(id) as id1, max(name) as name1, max(age) as age1 from t_student " + whereSQL.getSQL(), whereSQL.getParams()).size() == 1;

        whereSQL.resetGroupBy();
        whereSQL.addGroupBy("id", "name");
        assert getDBHelper().getRaw(StudentForGroupDO.class,
                "select max(id) as id1, max(name) as name1, max(age) as age1 from t_student " +
                        whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.resetGroupBy();
        whereSQL.addGroupByWithParam("id + :one", MapUtils.of("one",1)); // 重复group id效果一样
        whereSQL.having("count(*) = :zero", MapUtils.of("zero",0));
        assert getDBHelper().getRaw(StudentForGroupDO.class,
                "select max(id) as id1, max(name) as name1, max(age) as age1 from t_student " +whereSQL.getSQL(), whereSQL.getParams()).isEmpty();

        whereSQL.having("count(*) > :zero", MapUtils.of("zero",0));
        assert getDBHelper().getRaw(StudentForGroupDO.class,
                "select max(id) as id1, max(name) as name1, max(age) as age1 from t_student " +whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.having("count(*) > 0");
        assert getDBHelper().getRaw(StudentForGroupDO.class,
                "select max(id) as id1, max(name) as name1, max(age) as age1 from t_student " +whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.resetOrderBy();
        whereSQL.addOrderBy("max(name) desc", "max(age)");
        List<StudentForGroupDO> all2 = getDBHelper().getRaw(StudentForGroupDO.class,
                "select max(id) as id1, max(name) as name1, max(age) as age1 from t_student " +whereSQL.getSQL(), whereSQL.getParams());
        assert all2.size() == num1 + num2;
        for (int i = 0; i < all2.size() - 1; i++) {
            assert all2.get(i).getName().compareTo(all2.get(i + 1).getName()) > 0;
        }

        whereSQL.resetOrderBy();
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.POSTGRESQL) {
            // pg识别不了这个参数的类型，需要加::text
            whereSQL.addOrderByWithParam("concat(max(name),:abcname::text) desc", MapUtils.of("abcname","abc"));
        } else {
            whereSQL.addOrderByWithParam("concat(max(name),:abcname) desc", MapUtils.of("abcname","abc"));
        }
        all2 = getDBHelper().getRaw(StudentForGroupDO.class,
                "select max(id) as id1, max(name) as name1, max(age) as age1 from t_student " + whereSQL.getSQL(), whereSQL.getParams());
        assert all2.size() == num1 + num2;
        for (int i = 0; i < all2.size() - 1; i++) {
            assert all2.get(i).getName().compareTo(all2.get(i + 1).getName()) > 0;
        }

        // test limit

        whereSQL.limit(5);

        all2 = getDBHelper().getRaw(StudentForGroupDO.class,
                "select max(id) as id1, max(name) as name1, max(age) as age1 from t_student " + whereSQL.getSQL(), whereSQL.getParams());
        assert all2.size() == 5;

        whereSQL.limit(3, 3);
        all2 = getDBHelper().getRaw(StudentForGroupDO.class,
                "select max(id) as id1, max(name) as name1, max(age) as age1 from t_student " + whereSQL.getSQL(), whereSQL.getParams());
        assert all2.size() == 3;
    }

    @Test
    public void testTypeAutoCast() {
        assert TypeAutoCast.cast(1, Integer.class) == 1;
        assert TypeAutoCast.cast(1, Long.class) == 1L;
        assert TypeAutoCast.cast("97", Byte.class).equals(Byte.valueOf("97"));

        assert TypeAutoCast.cast(null, Short.class) == null;
        assert TypeAutoCast.cast(33, Short.class) == 33;
        assert TypeAutoCast.cast((short)33, Short.class) == 33;
        assert TypeAutoCast.cast("33", Short.class) == 33;

        assert TypeAutoCast.cast("hello", String.class).equals("hello");
        assert TypeAutoCast.cast("123.4", BigDecimal.class).compareTo(new BigDecimal("123.4")) == 0;

        assert TypeAutoCast.cast(new Date(), java.sql.Date.class) instanceof java.sql.Date;
        assert TypeAutoCast.cast(new Date(), java.sql.Time.class) instanceof java.sql.Time;

    }

    @Test
    public void testAssembleWhereSql() {
        // 1. 占位符测试
        {
            WhereSQL whereSQL = new WhereSQL();
            whereSQL.and("name=?", "nick").and("age=?", 18);

            String sql = SQLAssemblyUtils.assembleWhereSql(whereSQL);
            assert sql.equals(" WHERE name='nick' AND age=18 ");
        }

        // 2. 命名参数测试
        {
            WhereSQLForNamedParam whereSQL = new WhereSQLForNamedParam();
            whereSQL.and("name=:name", MapUtils.of("name", "nick"))
                    .and("age=:age", MapUtils.of("age", 18));

            String sql = SQLAssemblyUtils.assembleWhereSql(whereSQL);
            assert sql.equals(" WHERE name='nick' AND age=18 ");
        }

    }


    @Test
    public void testAssembleSql() {
        // 1. 问号占位符测试
        {
            String sql = "select * from t_student where name=? and age=?";
            String finalSql = SQLAssemblyUtils.assembleSql(sql, "nick", 18);
            assert "select * from t_student where name='nick' and age=18".equals(finalSql);
        }

        {
            String sql = "select * from t_student where name in (?) and age between ? and ?";
            String finalSql = SQLAssemblyUtils.assembleSql(sql, ListUtils.newList("nick1", "nick2", "nick3"),
                    30, 80);
            assert "select * from t_student where name in ('nick1','nick2','nick3') and age between 30 and 80".equals(finalSql);
        }

        {
            String sql = "select * from t_student where (name,age) in (?) and age between ? and ?";
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[]{"nick1", 18});
            params.add(new Object[]{"nick2", 19});
            String finalSql = SQLAssemblyUtils.assembleSql(sql, params, 30, 80);
            assert "select * from t_student where (name,age) in (('nick1',18),('nick2',19)) and age between 30 and 80".equals(finalSql);
        }

        // 2. 命名参数测试
        {
            String sql = "select * from t_student where name=:name and age=:age";
            String finalSql = SQLAssemblyUtils.assembleSql(sql, MapUtils.of("name", "nick", "age", 18));
            System.out.println(finalSql);
            assert "select * from t_student where name='nick' and age=18".equals(finalSql);
        }

        {
            String sql = "select * from t_student where name in (:names) and age between :min and :max";
            String finalSql = SQLAssemblyUtils.assembleSql(sql,
                    MapUtils.of("names", ListUtils.newList("nick1", "nick2", "nick3"),
                    "min", 30, "max", 80));
            assert "select * from t_student where name in ('nick1','nick2','nick3') and age between 30 and 80".equals(finalSql);
        }

        {
            String sql = "select * from t_student where (name,age) in (:nameAndAges) and age between :min and :max";
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[]{"nick1", 18});
            params.add(new Object[]{"nick2", 19});
            String finalSql = SQLAssemblyUtils.assembleSql(sql, MapUtils.of("nameAndAges", params,
                    "min", 30, "max", 80));
            assert "select * from t_student where (name,age) in (('nick1',18),('nick2',19)) and age between 30 and 80".equals(finalSql);
        }

    }

    /**
     * 这是一个比较异常的场景，传入参数不是数组，而是list
     */
    @Test
    public void testPassParamAsList() {
        StudentDO student1 = CommonOps.insertOne(getDBHelper());
        StudentDO student2 = CommonOps.insertOne(getDBHelper());

        List<Object> params = new ArrayList<>();
        params.add(student1.getId());
        params.add(student2.getId());

        List<StudentDO> all = getDBHelper().getAll(StudentDO.class, "where id=? or id=?", params);// 正常应该是传入params.toArray()
        assert all.size() == 2;
        assert all.get(0).getId().equals(student1.getId())
                 || all.get(1).getId().equals(student1.getId());
        assert all.get(0).getId().equals(student2.getId())
                 || all.get(1).getId().equals(student2.getId());
        assert !all.get(0).getId().equals(all.get(1).getId());
    }


    private static List<String> getDiffFmtMonth(String stdDateStr) {
        List<String> tmp = new ArrayList<>();
        tmp.add(stdDateStr);
        tmp.add(stdDateStr.replace("-", "/"));
        tmp.add(stdDateStr.replaceFirst("-", "年") + "月");

        List<String> result = new ArrayList<>();
        result.addAll(tmp);
        result.addAll(ListUtils.transform(tmp, o -> o.replace("03", "3")));

        return result;
    }

    private static List<String> getDiffFmtDate(String stdDateStr) {
        List<String> tmp = new ArrayList<>();
        tmp.add(stdDateStr);
        tmp.add(stdDateStr.replace("-", "/"));
        tmp.add(stdDateStr.replaceFirst("-", "年").replaceFirst("-", "月") + "日");

        List<String> result = new ArrayList<>();
        result.addAll(tmp);
        result.addAll(ListUtils.transform(tmp, o -> o.replace("03", "3")));
        result.addAll(ListUtils.transform(tmp, o -> o.replace("04", "4")));
        result.addAll(ListUtils.transform(tmp, o -> o.replace("03", "3").replace("04", "4")));

        return result;
    }

    private static List<String> getDiffFmtOnlyTime(String stdDateStr, boolean withSecond) {
        List<String> tmp = new ArrayList<>();
        tmp.add(stdDateStr);
        tmp.add(stdDateStr.replace("05", "5"));

        List<String> tmp2 = new ArrayList<>();
        tmp2.addAll(tmp);
        tmp2.addAll(ListUtils.transform(tmp, o -> o.replace("06", "6")));

        List<String> tmp3 = new ArrayList<>();
        tmp3.addAll(tmp2);
        tmp3.addAll(ListUtils.transform(tmp2, o -> o.replace("07", "7")));

        List<String> result = new ArrayList<>();
        if (!withSecond) {
            result.addAll(tmp3);
            result.addAll(ListUtils.transform(tmp3, o -> o + "Z"));
        } else {
            result.addAll(tmp3);
            result.addAll(ListUtils.transform(tmp3, o -> o + "Z"));
            result.addAll(ListUtils.transform(tmp3, o -> o + "+0000"));
            result.addAll(ListUtils.transform(tmp3, o -> o + "+00"));
            result.addAll(ListUtils.transform(tmp3, o -> o + "+00:00"));
            result.addAll(ListUtils.transform(tmp3, o -> o + " +0000"));
            result.addAll(ListUtils.transform(tmp3, o -> o + " +00:00"));
        }

        return result;
    }

    private static List<String> getDiffFmtFullMinute(String stdDateStr) {
        List<String> tmp = new ArrayList<>();
        tmp.add(stdDateStr);
        tmp.add(stdDateStr.replace(" ", "T"));
        tmp.add(stdDateStr.replace("-", "/"));
        tmp.add(stdDateStr.replace(" ", "T").replace("-", "/"));

        List<String> tmp2 = new ArrayList<>();
        tmp2.addAll(tmp);
        tmp2.addAll(ListUtils.transform(tmp, o -> o.replace("03", "3")));
        tmp2.addAll(ListUtils.transform(tmp, o -> o.replace("04", "4")));
        tmp2.addAll(ListUtils.transform(tmp, o -> o.replace("03", "3").replace("04", "4")));

        List<String> result = new ArrayList<>();
        result.addAll(tmp2);
        result.addAll(ListUtils.transform(tmp2, o -> o.replace("05", "5")));
        result.addAll(ListUtils.transform(tmp2, o -> o.replace("06", "6")));
        result.addAll(ListUtils.transform(tmp2, o -> o.replace("05", "5").replace("06", "6")));

        return result;
    }

    private static List<String> getDiffFmtFullNanos(String stdDateStr) {

        List<String> tmp = new ArrayList<>();
        tmp.add(stdDateStr);
        tmp.add(stdDateStr.replace(" ", "T"));

        List<String> tmp2 = new ArrayList<>();
        tmp2.addAll(tmp);
        tmp2.addAll(ListUtils.transform(tmp, o -> o.replace("-", "/")));
        tmp2.addAll(ListUtils.transform(tmp, o -> o.replace("-", "")));

        List<String> tmp3 = new ArrayList<>();
        tmp3.addAll(tmp2);
        tmp3.addAll(ListUtils.transform(tmp2, o -> o.replace(":", "")));

        // 只有-才替换日期
        List<String> tmp4 = new ArrayList<>(tmp3);
        List<String> tmpDate = ListUtils.filter(tmp3, o -> o.contains("-"));
        tmp4.addAll(ListUtils.transform(tmpDate, o -> o.replace("03", "3")));
        tmp4.addAll(ListUtils.transform(tmpDate, o -> o.replace("04", "4")));
        tmp4.addAll(ListUtils.transform(tmpDate, o -> o.replace("03", "3").replace("04", "4")));

        // 只有有:才替换时间
        List<String> tmp5 = new ArrayList<>(tmp4);
        List<String> tmpTime = ListUtils.filter(tmp4, o -> o.contains(":"));
        tmp5.addAll(ListUtils.transform(tmpTime, o -> o.replace("05", "5")));
        tmp5.addAll(ListUtils.transform(tmpTime, o -> o.replace("06", "6")));
        tmp5.addAll(ListUtils.transform(tmpTime, o -> o.replace("07", "7")));
        tmp5.addAll(ListUtils.transform(tmpTime, o -> o.replace("05", "5").replace("06", "6")));
        tmp5.addAll(ListUtils.transform(tmpTime, o -> o.replace("05", "5").replace("07", "7")));
        tmp5.addAll(ListUtils.transform(tmpTime, o -> o.replace("06", "6").replace("07", "7")));
        tmp5.addAll(ListUtils.transform(tmpTime, o -> o.replace("05", "5").replace("06", "6").replace("07", "7")));

        List<String> result = new ArrayList<>();
        result.addAll(tmp5);
        result.addAll(ListUtils.transform(tmp5, o -> o + "Z"));
        result.addAll(ListUtils.transform(tmp5, o -> o + "+0000"));
        result.addAll(ListUtils.transform(tmp5, o -> o + "+00"));
        result.addAll(ListUtils.transform(tmp5, o -> o + "+00:00"));
        result.addAll(ListUtils.transform(tmp5, o -> o + " +0000"));
        result.addAll(ListUtils.transform(tmp5, o -> o + " +00:00"));

        return result;
    }

    private static LocalDateTime ldt(String str) {
        return NimbleOrmDateUtils.parseLocalDateTime(str);
    }

    private static LocalDate ld(String str) {
        return NimbleOrmDateUtils.parseLocalDate(str);
    }

    private static LocalTime lt(String str) {
        return NimbleOrmDateUtils.parseLocalTime(str);
    }

    /**
     * 测试解析日期相关
     */
    @Test
    public void testParseDate() {

        // ============== LocalDateTime =================
        {
            // 只有日期，到月份
            LocalDateTime localDateTimeMonth = LocalDateTime.of(2024, 3, 1, 0, 0);
            getDiffFmtMonth("2024-03").forEach(str -> {assert localDateTimeMonth.equals(ldt(str));});
            assert localDateTimeMonth.equals(ldt("202403"));

            // 只有日期，到日期
            LocalDateTime localDateTimeDate = LocalDateTime.of(2024, 3, 4, 0, 0);
            getDiffFmtDate("2024-03-04").forEach(str -> {assert localDateTimeDate.equals(ldt(str));});
            assert localDateTimeDate.equals(ldt("20240304"));

            // 只有时间，到分钟
            LocalDateTime localDateTimeOnlyTime = LocalDateTime.of(0, 1, 1, 5, 6);
            getDiffFmtOnlyTime("05:06", false).forEach(str -> {assert localDateTimeOnlyTime.equals(ldt(str));});

            // 只有时间，到秒
            LocalDateTime localDateTimeOnlyTime2 = LocalDateTime.of(0, 1, 1, 5, 6, 7, 0);
            getDiffFmtOnlyTime("05:06:07", true).forEach(str -> {assert localDateTimeOnlyTime2.equals(ldt(str));});
            getDiffFmtOnlyTime("05:06:07.000",true).forEach(str -> {assert localDateTimeOnlyTime2.equals(ldt(str));});
            getDiffFmtOnlyTime("05:06:07.0000000",true).forEach(str -> {assert localDateTimeOnlyTime2.equals(ldt(str));});
            getDiffFmtOnlyTime("05:06:07.", true).forEach(str -> {assert localDateTimeOnlyTime2.equals(ldt(str));});
            getDiffFmtOnlyTime("05:06:07.000000000", true).forEach(str -> {assert localDateTimeOnlyTime2.equals(ldt(str));});

            // 只有时间，带毫秒纳秒
            LocalDateTime localDateTimeOnlyTime3 = LocalDateTime.of(0, 1, 1, 5, 6, 7, 123456700);
            getDiffFmtOnlyTime("05:06:07.1234567", true).forEach(str -> {assert localDateTimeOnlyTime3.equals(ldt(str));});

            LocalDateTime localDateTimeOnlyTime4 = LocalDateTime.of(0, 1, 1, 5, 6, 7, 120000000);
            getDiffFmtOnlyTime("05:06:07.12", true).forEach(str -> {assert localDateTimeOnlyTime4.equals(ldt(str));});
            getDiffFmtOnlyTime("05:06:07.120",true).forEach(str -> {assert localDateTimeOnlyTime4.equals(ldt(str));});

            // 日期+时间，到分钟
            LocalDateTime dateTimeMinute = LocalDateTime.of(2024, 3, 4, 5, 6);
            getDiffFmtFullMinute("2024-03-04 05:06").forEach(str -> {assert dateTimeMinute.equals(ldt(str));});

            // 日期+时间，到秒，可选带毫秒纳秒
            LocalDateTime dateTime3 = LocalDateTime.of(2024, 3, 4, 5, 6, 7, 0);
            getDiffFmtFullNanos("2024-03-04 05:06:07").forEach(str -> {assert dateTime3.equals(ldt(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.000").forEach(str -> {assert dateTime3.equals(ldt(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.0000000").forEach(str -> {assert dateTime3.equals(ldt(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.").forEach(str -> {assert dateTime3.equals(ldt(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.000000000").forEach(str -> {assert dateTime3.equals(ldt(str));});
            assert dateTime3.equals(ldt("20240304050607"));

            LocalDateTime dateTime1 = LocalDateTime.of(2024, 3, 4, 5, 6, 7, 123456700);
            getDiffFmtFullNanos("2024-03-04 05:06:07.1234567").forEach(str -> {assert dateTime1.equals(ldt(str));});

            LocalDateTime dateTime2 = LocalDateTime.of(2024, 3, 4, 5, 6, 7, 120000000);
            getDiffFmtFullNanos("2024-03-04 05:06:07.12").forEach(str -> {assert dateTime2.equals(ldt(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.120").forEach(str -> {assert dateTime2.equals(ldt(str));});
        }

        // ============== LocalDate =================
        {
            // 只有日期，到月份
            LocalDate localDateTimeMonth = LocalDate.of(2024, 3, 1);
            getDiffFmtMonth("2024-03").forEach(str -> {assert localDateTimeMonth.equals(ld(str));});
            assert localDateTimeMonth.equals(ld("202403"));

            // 只有日期，到日期
            LocalDate localDateTimeDate = LocalDate.of(2024, 3, 4);
            getDiffFmtDate("2024-03-04").forEach(str -> {assert localDateTimeDate.equals(ld(str));});
            assert localDateTimeDate.equals(ld("20240304"));

            // 日期+时间，到分钟
            LocalDate dateTimeMinute = LocalDate.of(2024, 3, 4);
            getDiffFmtFullMinute("2024-03-04 05:06").forEach(str -> {assert dateTimeMinute.equals(ld(str));});

            // 日期+时间，到秒，可选带毫秒纳秒
            LocalDate dateTime3 = LocalDate.of(2024, 3, 4);
            getDiffFmtFullNanos("2024-03-04 05:06:07").forEach(str -> {assert dateTime3.equals(ld(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.000").forEach(str -> {assert dateTime3.equals(ld(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.0000000").forEach(str -> {assert dateTime3.equals(ld(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.").forEach(str -> {assert dateTime3.equals(ld(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.000000000").forEach(str -> {assert dateTime3.equals(ld(str));});
            assert dateTime3.equals(ld("20240304050607"));

            LocalDate dateTime1 = LocalDate.of(2024, 3, 4);
            getDiffFmtFullNanos("2024-03-04 05:06:07.1234567").forEach(str -> {assert dateTime1.equals(ld(str));});

            LocalDate dateTime2 = LocalDate.of(2024, 3, 4);
            getDiffFmtFullNanos("2024-03-04 05:06:07.12").forEach(str -> {assert dateTime2.equals(ld(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.120").forEach(str -> {assert dateTime2.equals(ld(str));});
        }

        // ============== LocalTime =================
        {
            // 只有时间，到分钟
            LocalTime localDateTimeOnlyTime = LocalTime.of(5, 6);
            getDiffFmtOnlyTime("05:06", false).forEach(str -> {assert localDateTimeOnlyTime.equals(lt(str));});

            // 只有时间，到秒
            LocalTime localDateTimeOnlyTime2 = LocalTime.of(5, 6, 7, 0);
            getDiffFmtOnlyTime("05:06:07", true).forEach(str -> {assert localDateTimeOnlyTime2.equals(lt(str));});
            getDiffFmtOnlyTime("05:06:07.000",true).forEach(str -> {assert localDateTimeOnlyTime2.equals(lt(str));});
            getDiffFmtOnlyTime("05:06:07.0000000",true).forEach(str -> {assert localDateTimeOnlyTime2.equals(lt(str));});
            getDiffFmtOnlyTime("05:06:07.", true).forEach(str -> {assert localDateTimeOnlyTime2.equals(lt(str));});
            getDiffFmtOnlyTime("05:06:07.000000000", true).forEach(str -> {assert localDateTimeOnlyTime2.equals(lt(str));});

            // 只有时间，带毫秒纳秒
            LocalTime localDateTimeOnlyTime3 = LocalTime.of(5, 6, 7, 123456700);
            getDiffFmtOnlyTime("05:06:07.1234567", true).forEach(str -> {assert localDateTimeOnlyTime3.equals(lt(str));});

            LocalTime localDateTimeOnlyTime4 = LocalTime.of(5, 6, 7, 120000000);
            getDiffFmtOnlyTime("05:06:07.12", true).forEach(str -> {assert localDateTimeOnlyTime4.equals(lt(str));});
            getDiffFmtOnlyTime("05:06:07.120",true).forEach(str -> {assert localDateTimeOnlyTime4.equals(lt(str));});

            // 日期+时间，到分钟
            LocalTime dateTimeMinute = LocalTime.of(5, 6);
            getDiffFmtFullMinute("2024-03-04 05:06").forEach(str -> {assert dateTimeMinute.equals(lt(str));});

            // 日期+时间，到秒，可选带毫秒纳秒
            LocalTime dateTime3 = LocalTime.of(5, 6, 7, 0);
            getDiffFmtFullNanos("2024-03-04 05:06:07").forEach(str -> {assert dateTime3.equals(lt(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.000").forEach(str -> {assert dateTime3.equals(lt(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.0000000").forEach(str -> {assert dateTime3.equals(lt(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.").forEach(str -> {assert dateTime3.equals(lt(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.000000000").forEach(str -> {assert dateTime3.equals(lt(str));});
            assert dateTime3.equals(lt("20240304050607"));

            LocalTime dateTime1 = LocalTime.of(5, 6, 7, 123456700);
            getDiffFmtFullNanos("2024-03-04 05:06:07.1234567").forEach(str -> {assert dateTime1.equals(lt(str));});

            LocalTime dateTime2 = LocalTime.of(5, 6, 7, 120000000);
            getDiffFmtFullNanos("2024-03-04 05:06:07.12").forEach(str -> {assert dateTime2.equals(lt(str));});
            getDiffFmtFullNanos("2024-03-04 05:06:07.120").forEach(str -> {assert dateTime2.equals(lt(str));});
        }

    }

}
