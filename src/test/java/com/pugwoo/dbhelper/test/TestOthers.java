package com.pugwoo.dbhelper.test;

import com.pugwoo.dbhelper.bean.SubQuery;
import com.pugwoo.dbhelper.enums.JoinTypeEnum;
import com.pugwoo.dbhelper.exception.*;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import org.junit.Test;

import java.util.ArrayList;

/**
 * 其它的一些测试，主要为了覆盖代码
 */
public class TestOthers {

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
    }

}
