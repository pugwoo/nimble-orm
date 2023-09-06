package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.cache.ClassInfoCache;
import com.pugwoo.dbhelper.enums.JoinTypeEnum;
import com.pugwoo.dbhelper.exception.*;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.sql.WhereSQL;
import com.pugwoo.dbhelper.sql.WhereSQLForNamedParam;
import com.pugwoo.dbhelper.test.entity.AreaDO;
import com.pugwoo.dbhelper.test.entity.AreaLocationDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.TypesDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.AreaVO;
import com.pugwoo.dbhelper.utils.DOInfoReader;
import com.pugwoo.dbhelper.utils.InnerCommonUtils;
import com.pugwoo.dbhelper.utils.TypeAutoCast;
import com.pugwoo.wooutils.collect.MapUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

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

        getDBHelper().insert(area);

        AreaLocationDO areaLocationDO = new AreaLocationDO();
        areaLocationDO.setLayerCode("CITY");
        areaLocationDO.setAreaCode("SZ");
        areaLocationDO.setLongitude(new BigDecimal("120"));
        areaLocationDO.setLatitude(new BigDecimal("22"));

        getDBHelper().insert(areaLocationDO);

        AreaVO one = getDBHelper().getOne(AreaVO.class);
        assert one.getLocationVO() != null;

    }

    @Test 
    public void testTypes() {
        TypesDO typesDO = new TypesDO();
        typesDO.setId1(new Random().nextLong());
        typesDO.setId2(new Random().nextLong());
        typesDO.setMyByte("a".getBytes()[0]);
        typesDO.setS(Short.valueOf("11"));
        typesDO.setMyFloat(11.1f);
        typesDO.setMyDouble(22.2);
        typesDO.setMyDecimal(new BigDecimal("11.22"));
        typesDO.setMyDate(new java.sql.Date(new Date().getTime()));
        typesDO.setMyTime(new java.sql.Time(new Date().getTime()));
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

        whereSQL.addGroupBy("age");
        assert  getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == 1;

        whereSQL.resetGroupBy();
        whereSQL.addGroupBy("id", "name");
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.resetGroupBy();
        whereSQL.addGroupByWithParam("id + ?", 1); // 重复group id效果一样
        whereSQL.having("count(*) = ?", 0);
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == 0;

        whereSQL.having("count(*) > ?", 0);
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.having("count(*) > 0");
        assert getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.resetOrderBy();
        whereSQL.addOrderBy("name desc", "age");
        all = getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams());
        assert all.size() == num1 + num2;
        for (int i = 0; i < all.size() - 1; i++) {
            assert all.get(i).getName().compareTo(all.get(i + 1).getName()) > 0;
        }

        whereSQL.resetOrderBy();
        whereSQL.addOrderByWithParam("concat(name,?) desc", "abc");
        all = getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams());
        assert all.size() == num1 + num2;
        for (int i = 0; i < all.size() - 1; i++) {
            assert all.get(i).getName().compareTo(all.get(i + 1).getName()) > 0;
        }

        // test limit

        whereSQL.limit(5);

        all = getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams());
        assert all.size() == 5;

        whereSQL.limit(3, 3);
        all = getDBHelper().getAll(StudentDO.class, whereSQL.getSQL(), whereSQL.getParams());
        assert all.size() == 3;
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
    public void testWhereSQLForNamedParam() {
        int num1 = 3 + new Random().nextInt(5);
        String prefix1 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        List<StudentDO> student1 = CommonOps.insertBatch(getDBHelper(), num1, prefix1);

        int num2 = 3 + new Random().nextInt(5);
        String prefix2 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        List<StudentDO> student2 = CommonOps.insertBatch(getDBHelper(), num2, prefix2);

        WhereSQLForNamedParam whereSQL = new WhereSQLForNamedParam("deleted=0 and name like :name",
                MapUtils.of("name", prefix1 + "%"));
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams()).size() == num1;

        whereSQL = new WhereSQLForNamedParam("deleted=0").and("name like :name", MapUtils.of("name", prefix1 + "%")); // 等价写法
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams()).size() == num1;

        whereSQL.and(new WhereSQLForNamedParam()); // 加一个空的，等于没有任何约束
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams()).size() == num1;

        whereSQL.or(new WhereSQLForNamedParam("deleted=0 and name like :name2", MapUtils.of("name2", prefix2 + "%")));
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.not().and("deleted=0");
        int size = getDBHelper().getRawOne(Integer.class, "select count(*) from t_student "+ whereSQL.getSQL(), whereSQL.getParams());
        long size2 = getDBHelper().getCount(StudentDO.class, "where name is not null") - (num1 + num2);
        assert size == size2;

        whereSQL.not().and("deleted=0");
        assert getDBHelper().getRawOne(Integer.class, "select count(*) from t_student " + whereSQL.getSQL(),
                whereSQL.getParams()) == num1 + num2;

        whereSQL.and("1=:one", MapUtils.of("one", 1)).and("deleted=0");
        assert getDBHelper().getRawOne(Integer.class, "select count(*) from t_student " + whereSQL.getSQL(), whereSQL.getParams()) == num1 + num2;

        whereSQL = whereSQL.copy();
        assert getDBHelper().getRawOne(Integer.class, "select count(*) from t_student " + whereSQL.getSQL(), whereSQL.getParams()) == num1 + num2;

        // 测试空的not，等于没有约束
        assert getDBHelper().getAll(StudentDO.class).size() ==
                getDBHelper().getRawOne(Integer.class, "select count(*) from t_student " + new WhereSQL().not().and("deleted=0").getSQL());

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

        whereSQL.addGroupBy("age");
        assert  getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams()).size() == 1;

        whereSQL.resetGroupBy();
        whereSQL.addGroupBy("id", "name");
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " +whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.resetGroupBy();
        whereSQL.addGroupByWithParam("id + :one", MapUtils.of("one",1)); // 重复group id效果一样
        whereSQL.having("count(*) = :zero", MapUtils.of("zero",0));
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " +whereSQL.getSQL(), whereSQL.getParams()).size() == 0;

        whereSQL.having("count(*) > :zero", MapUtils.of("zero",0));
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " +whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.having("count(*) > 0");
        assert getDBHelper().getRaw(StudentDO.class, "select * from t_student " +whereSQL.getSQL(), whereSQL.getParams()).size() == num1 + num2;

        whereSQL.resetOrderBy();
        whereSQL.addOrderBy("name desc", "age");
        all = getDBHelper().getRaw(StudentDO.class, "select * from t_student " +whereSQL.getSQL(), whereSQL.getParams());
        assert all.size() == num1 + num2;
        for (int i = 0; i < all.size() - 1; i++) {
            assert all.get(i).getName().compareTo(all.get(i + 1).getName()) > 0;
        }

        whereSQL.resetOrderBy();
        whereSQL.addOrderByWithParam("concat(name,:abcname) desc", MapUtils.of("abcname","abc"));
        all = getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams());
        assert all.size() == num1 + num2;
        for (int i = 0; i < all.size() - 1; i++) {
            assert all.get(i).getName().compareTo(all.get(i + 1).getName()) > 0;
        }

        // test limit

        whereSQL.limit(5);

        all = getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams());
        assert all.size() == 5;

        whereSQL.limit(3, 3);
        all = getDBHelper().getRaw(StudentDO.class, "select * from t_student " + whereSQL.getSQL(), whereSQL.getParams());
        assert all.size() == 3;
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

}
