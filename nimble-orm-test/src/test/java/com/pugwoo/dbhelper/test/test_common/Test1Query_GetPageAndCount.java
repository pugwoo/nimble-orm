package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.enums.DatabaseTypeEnum;
import com.pugwoo.dbhelper.exception.InvalidParameterException;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.sql.SQLUtils;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.StudentSchoolJoinVO;
import com.pugwoo.dbhelper.test.vo.StudentVO;
import com.pugwoo.dbhelper.test.vo.StudentVOForGroup;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

public abstract class Test1Query_GetPageAndCount {

    public abstract DBHelper getDBHelper();

    @Test
    public void testGetPage() {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        CommonOps.insertBatch(getDBHelper(),100, prefix);

        // 测试分页获取
        PageData<StudentDO> page1 = getDBHelper().getPage(StudentDO.class, 1, 10);
        assert page1.getTotal() >= 100;
        assert page1.getData().size() == 10;

        page1 = getDBHelper().getPage(StudentDO.class, 2, 10);
        assert page1.getTotal() >= 100;
        assert page1.getData().size() == 10;

        page1 = getDBHelper().getPageWithoutCount(StudentDO.class, 1, 10);
        assert page1.getData().size() == 10;

        page1 = getDBHelper().getPageWithoutCount(StudentDO.class, 2, 10);
        assert page1.getData().size() == 10;

        page1 = getDBHelper().getPage(StudentDO.class, 1, 10, "where name like ?", prefix + "%");
        assert page1.getData().size() == 10;
        assert page1.getTotal() == 100;

        for (StudentDO student : page1.getData()) {
            assert student.getId() != null;
            assert student.getName().startsWith(prefix);
        }

        long total = getDBHelper().getCount(StudentDO.class);
        assert total >= 100;

        total = getDBHelper().getCount(StudentDO.class, "where name like ?", prefix + "%");
        assert total == 100;

        // 测试异常情况，页数<=0
        boolean isThrowException = false;
        try {
            getDBHelper().getPage(StudentDO.class, 0, 10);
        } catch (Exception e) {
            if (e instanceof InvalidParameterException) {
                isThrowException = true;
            }
        }
        assert isThrowException;
    }

    /**测试自动移除limit、自动追加order by*/
    @Test
    public void testGetPageRemoveLimitAddOrder() {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        CommonOps.insertBatch(getDBHelper(), 20, prefix);

        // 这里故意加上limit子句，会被自动清除掉
        PageData<StudentDO> page = getDBHelper().getPage(StudentDO.class, 1, 10,
                "where name like ? limit 4,6", prefix + "%");
        assert page.getData().size() == 10;
        assert page.getTotal() == 20;

        PageData<StudentDO> page2 = getDBHelper().getPage(StudentDO.class, 2, 10,
                "where name like ? limit 4,6", prefix + "%");
        assert page2.getData().size() == 10;
        assert page2.getTotal() == 20;

        Set<String> name = new HashSet<>();
        for (StudentDO stu : page.getData()) {
            name.add(stu.getName());
        }
        for (StudentDO stu : page2.getData()) {
            name.add(stu.getName());
        }
        assert name.size() == 20;

        // ============== 不加group by时自动以id为排序
        System.out.println("=================================");

        page = getDBHelper().getPage(StudentDO.class, 1, 10,
                "where name like ? limit 4,6", prefix + "%");
        assert page.getData().size() == 10;
        assert page.getTotal() == 20;

        page2 = getDBHelper().getPage(StudentDO.class, 2, 10,
                "where name like ? limit 4,6", prefix + "%");
        assert page2.getData().size() == 10;
        assert page2.getTotal() == 20;

        name = new HashSet<>();
        for (StudentDO stu : page.getData()) {
            name.add(stu.getName());
        }
        for (StudentDO stu : page2.getData()) {
            name.add(stu.getName());
        }
        assert name.size() == 20;

        System.out.println("=================================");
        // 如果用户自行执行的order by没有完全包含group by的字段，则有warning 日志
        getDBHelper().getPage(StudentVOForGroup.class, 1, 10,
                "where name like ? group by deleted,name order by name limit 4,6", prefix + "%"); // 看告警
    }

    /**测试异常情况 mock SQLUtils.removeLimitAndAddOrder 抛出异常，仍能正常处理*/
    @Test
    public void testRemoveLimitThrowException() {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        CommonOps.insertBatch(getDBHelper(), 20, prefix);

        String where = "where name like ? ";

        try (MockedStatic<SQLUtils> utilities = Mockito.mockStatic(SQLUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utilities.when(() -> SQLUtils.removeLimitAndAddOrder(where, true, StudentDO.class))
                    .thenThrow(new RuntimeException("just test"));

            // 测试异常抛出exception情况下，仍然没有问题
            PageData<StudentDO> page = getDBHelper().getPage(StudentDO.class, 1, 10,
                    where, prefix + "%");
            assert page.getData().size() == 10;
            assert page.getTotal() == 20;
        }
    }

    @Test
    public void testPageDataTransform() {
        CommonOps.insertBatch(getDBHelper(),20);
        PageData<StudentDO> page1 = getDBHelper().getPage(StudentDO.class, 1, 10);
        PageData<StudentVO> page2 = page1.transform(o -> {
            StudentVO studentVO = new StudentVO();
            studentVO.setId(o.getId());
            studentVO.setName(o.getName());
            return studentVO;
        });

        assert page1.getTotal() == page2.getTotal();
        assert page1.getPageSize() == page2.getPageSize();
        assert page1.getData().size() == page2.getData().size();

        for (int i = 0; i < 10; i++) {
            assert page1.getData().get(i).getId().equals(page2.getData().get(i).getId());
            assert Objects.equals(page1.getData().get(i).getName(),
                    page2.getData().get(i).getName());
        }
    }

    /**测试分页最大数限制*/
    @Test
    public void testMaxPageSize() {
        getDBHelper().setMaxPageSize(5);

        CommonOps.insertBatch(getDBHelper(), 10);
        PageData<StudentDO> pageData = getDBHelper().getPage(StudentDO.class, 1, 10);
        assert pageData.getData().size() == 5; // 受限制于maxPageSize

        pageData = getDBHelper().getPageWithoutCount(StudentDO.class, 1, 10);
        assert pageData.getData().size() == 5; // 受限制于maxPageSize

        pageData = getDBHelper().getPageWithoutCount(StudentDO.class, 1, 10, "where 1=1");
        assert pageData.getData().size() == 5; // 受限制于maxPageSize

        getDBHelper().setMaxPageSize(1000000);
    }

    @Test
    public void testCount() {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        long count = getDBHelper().getCount(StudentDO.class, "where name like ?", prefix + "%");
        assert count == 0;

        long beforeInsertCount = getDBHelper().getCount(StudentDO.class);

        List<StudentDO> studentDOS = CommonOps.insertBatch(getDBHelper(), 99, prefix);

        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("sysu");
        if (getDBHelper().getDatabaseType() == DatabaseTypeEnum.CLICKHOUSE) {
            schoolDO.setId(new Random().nextLong());
        }
        getDBHelper().insert(schoolDO);
        assert schoolDO.getId() != null;

        for(StudentDO studentDO : studentDOS) {
            studentDO.setSchoolId(schoolDO.getId());
            getDBHelper().update(studentDO);
        }

        long afterInertCount = getDBHelper().getCount(StudentDO.class);
        assert beforeInsertCount + 99 == afterInertCount;
        afterInertCount = getDBHelper().getCount(StudentDO.class, "where 1=1");
        assert beforeInsertCount + 99 == afterInertCount;

        count = getDBHelper().getCount(StudentDO.class, "where name like ?", prefix +"%");
        assert count == 99;

        assert getDBHelper().getCount(StudentVOForGroup.class, "where name like ? group by deleted,name",
                prefix + "%") == 99;

        count = getDBHelper().getCount(StudentDO.class, "where name not like ? or name is null", prefix+"%");
        assert count == beforeInsertCount;

        List<String> names = new ArrayList<String>();
        names.add(studentDOS.get(0).getName());
        names.add(studentDOS.get(10).getName());
        names.add(studentDOS.get(30).getName());
        count = getDBHelper().getCount(StudentDO.class, "where name in (?)",names);
        assert count == 3;

        PageData<StudentDO> page = getDBHelper().getPage(StudentDO.class, 1, 10, "where name like ?", prefix +"%");
        assert page.getData().size() == 10;
        assert page.getTotal() == 99;

        page = getDBHelper().getPage(StudentDO.class, 1, 10, "where name like ?", prefix +"%");
        assert page.getData().size() == 10;
        assert page.getTotal() == 99;

        page = getDBHelper().getPage(StudentDO.class, 1, 10,
                "where name not like ? or name is null", prefix +"%");
        assert page.getData().size() == Math.min(10, beforeInsertCount);
        assert page.getTotal() == beforeInsertCount;

        page = getDBHelper().getPage(StudentDO.class, 1, 10, "where name in (?)", names);
        assert page.getData().size() == 3;
        assert page.getTotal() == 3;

        page = getDBHelper().getPage(StudentDO.class, 1, 2, "where name in (?)", names);
        assert page.getData().size() == 2;
        assert page.getTotal() == 3;

        PageData<StudentVOForGroup>page2 = getDBHelper().getPage(StudentVOForGroup.class, 1, 2,
                "where name in (?) group by deleted,name", names);
        assert page2.getData().size() == 2;
        assert page2.getTotal() == 3;

        page = getDBHelper().getPage(StudentDO.class, 1, 1000000); // 设置了maxPage=1000000
        System.out.println("page.getData().size():" + page.getData().size() + ",afterInertCount:" + afterInertCount);
        assert page.getData().size() == Math.min(afterInertCount, 1000000);
        assert page.getTotal() == afterInertCount;

        count = getDBHelper().getCount(StudentSchoolJoinVO.class);
        assert count == afterInertCount;
        count = getDBHelper().getCount(StudentSchoolJoinVO.class, "where 1=1");
        assert count == afterInertCount;
        count = getDBHelper().getCount(StudentSchoolJoinVO.class, "where t1.name like ?", prefix +"%");
        assert count == 99;
        count = getDBHelper().getCount(StudentSchoolJoinVO.class, "where t1.name like ? group by t1.name", prefix +"%");
        assert count == 99;

        PageData<StudentSchoolJoinVO> page3 = getDBHelper().getPage(StudentSchoolJoinVO.class, 1, 10);
        assert page3.getData().size() == 10;
        assert page3.getTotal() == afterInertCount;

        page3 = getDBHelper().getPage(StudentSchoolJoinVO.class, 1, 10000);
        assert page3.getData().size() == Math.min(10000, afterInertCount);
        assert page3.getTotal() == afterInertCount;
    }

}
