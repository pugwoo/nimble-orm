package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.DBHelper;
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
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.model.SubQuery;
import com.pugwoo.dbhelper.test.entity.AreaDO;
import com.pugwoo.dbhelper.test.entity.AreaLocationDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.TypesDO;
import com.pugwoo.dbhelper.test.vo.AreaVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

/**
 * 其它的一些测试，主要为了覆盖代码或最佳实践
 */
@ContextConfiguration(locations = "classpath:applicationContext-jdbc.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestOthers {

    @Autowired
    private DBHelper dbHelper;
    
    @Test @Rollback(false)
    public void testRelateComputedColumn() {
        dbHelper.delete(AreaDO.class, "where 1=1");
        dbHelper.delete(AreaLocationDO.class, "where 1=1");

        AreaDO area = new AreaDO();
        area.setLayerCode("CITY");
        area.setAreaCode("SZ");

        dbHelper.insert(area);

        AreaLocationDO areaLocationDO = new AreaLocationDO();
        areaLocationDO.setLayerCode("CITY");
        areaLocationDO.setAreaCode("SZ");
        areaLocationDO.setLongitude(new BigDecimal("120"));
        areaLocationDO.setLatitude(new BigDecimal("22"));

        dbHelper.insert(areaLocationDO);

        AreaVO one = dbHelper.getOne(AreaVO.class);
        assert one.getLocationVO() != null;

    }

    @Test @Rollback(false)
    public void testTypes() {
        TypesDO typesDO = new TypesDO();
        typesDO.setId1(new Random().nextLong());
        typesDO.setId2(new Random().nextLong());
        typesDO.setMyByte(Byte.valueOf("a".getBytes()[0]));
        typesDO.setMyShort(Short.valueOf("11"));
        typesDO.setMyFloat(11.1f);
        typesDO.setMyDouble(22.2);
        typesDO.setMyDecimal(new BigDecimal("11.22"));
        typesDO.setMyDate(new java.sql.Date(new java.util.Date().getTime()));
        typesDO.setMyTime(new java.sql.Time(new java.util.Date().getTime()));
        typesDO.setMyTimestamp(new java.sql.Timestamp(new java.util.Date().getTime()));
        typesDO.setMyMediumint(123456);

        dbHelper.insert(typesDO);
        assert typesDO.getId1() != null;
        assert typesDO.getId2() != null;

        TypesDO types2 = new TypesDO();
        types2.setId1(typesDO.getId1());
        types2.setId2(typesDO.getId2());

        dbHelper.getByKey(types2);
        assert types2.getMyByte().equals(typesDO.getMyByte());
        assert types2.getMyShort().equals(typesDO.getMyShort());
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
    public void testOthers() {

        // SubQuery
        SubQuery subQuery = new SubQuery("field", StudentDO.class, "postsql", "1");
        subQuery.setField("field1");
        assert subQuery.getField().equals("field1");
        subQuery.setClazz(SchoolDO.class);
        assert subQuery.getClazz().equals(SchoolDO.class);
        subQuery.setPostSql("sql");
        assert subQuery.getPostSql().equals("sql");
        subQuery.setArgs(new Object[]{"1", "2", "3"});
        assert subQuery.getArgs().length == 3;

        // JoinTypeEnum
        assert !JoinTypeEnum.JOIN.getName().isEmpty();

        // PageData
        PageData<String> page = new PageData<String>(100, new ArrayList<String>(), 10);
        assert page.getTotalPage() == 10;
        assert page.getPageSize() == 10;
        page = new PageData<String>(9, new ArrayList<String>(), 10);
        assert page.getTotalPage() == 1;
        assert page.getPageSize() == 10;
        page = new PageData<String>(0, new ArrayList<String>(), 10);
        assert page.getTotalPage() == 0;
        assert page.getPageSize() == 10;
        page = new PageData<String>(11, new ArrayList<String>(), 10);
        assert page.getTotalPage() == 2;
        assert page.getPageSize() == 10;
        page = new PageData<String>(11, new ArrayList<String>(), 0);
        assert page.getTotalPage() == 11;

        // exceptions
        Exception cause = new Exception();
        String errorMsg = "err message";

        {
            BadSQLSyntaxException ex = new BadSQLSyntaxException();
            ex = new BadSQLSyntaxException(cause);
            assert ex.getCause().equals(cause);
            ex = new BadSQLSyntaxException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            CasVersionNotMatchException ex = new CasVersionNotMatchException();
            ex = new CasVersionNotMatchException(cause);
            assert ex.getCause().equals(cause);
            ex = new CasVersionNotMatchException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            InvalidParameterException ex = new InvalidParameterException();
            ex = new InvalidParameterException(cause);
            assert ex.getCause().equals(cause);
            ex = new InvalidParameterException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            MustProvideConstructorException ex = new MustProvideConstructorException();
            ex = new MustProvideConstructorException(cause);
            assert ex.getCause().equals(cause);
            ex = new MustProvideConstructorException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NoColumnAnnotationException ex = new NoColumnAnnotationException();
            ex = new NoColumnAnnotationException(cause);
            assert ex.getCause().equals(cause);
            ex = new NoColumnAnnotationException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NoJoinTableMemberException ex = new NoJoinTableMemberException();
            ex = new NoJoinTableMemberException(cause);
            assert ex.getCause().equals(cause);
            ex = new NoJoinTableMemberException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NoKeyColumnAnnotationException ex = new NoKeyColumnAnnotationException();
            ex = new NoKeyColumnAnnotationException(cause);
            assert ex.getCause().equals(cause);
            ex = new NoKeyColumnAnnotationException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NoTableAnnotationException ex = new NoTableAnnotationException();
            ex = new NoTableAnnotationException(cause);
            assert ex.getCause().equals(cause);
            ex = new NoTableAnnotationException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NotAllowQueryException ex = new NotAllowQueryException();
            ex = new NotAllowQueryException(cause);
            assert ex.getCause().equals(cause);
            ex = new NotAllowQueryException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NotOnlyOneKeyColumnException ex = new NotOnlyOneKeyColumnException();
            ex = new NotOnlyOneKeyColumnException(cause);
            assert ex.getCause().equals(cause);
            ex = new NotOnlyOneKeyColumnException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            NullKeyValueException ex = new NullKeyValueException();
            ex = new NullKeyValueException(cause);
            assert ex.getCause().equals(cause);
            ex = new NullKeyValueException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            OnConditionIsNeedException ex = new OnConditionIsNeedException();
            ex = new OnConditionIsNeedException(cause);
            assert ex.getCause().equals(cause);
            ex = new OnConditionIsNeedException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            ParameterSizeNotMatchedException ex = new ParameterSizeNotMatchedException();
            ex = new ParameterSizeNotMatchedException(cause);
            assert ex.getCause().equals(cause);
            ex = new ParameterSizeNotMatchedException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        {
            RowMapperFailException ex = new RowMapperFailException();
            ex = new RowMapperFailException(cause);
            assert ex.getCause().equals(cause);
            ex = new RowMapperFailException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }

        {
            ScriptErrorException ex = new ScriptErrorException();
            ex = new ScriptErrorException(cause);
            assert ex.getCause().equals(cause);
            ex = new ScriptErrorException(errorMsg);
            assert ex.getMessage().equals(errorMsg);
        }
        // test some getter setter

    }

}
