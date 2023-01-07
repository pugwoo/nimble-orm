package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.IDBHelperSlowSqlCallback;
import com.pugwoo.dbhelper.enums.FeatureEnum;
import com.pugwoo.dbhelper.json.NimbleOrmJSON;
import com.pugwoo.dbhelper.model.PageData;
import com.pugwoo.dbhelper.model.SubQuery;
import com.pugwoo.dbhelper.test.entity.*;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.*;
import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.collect.MapUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 测试读操作相关
 */
@SpringBootTest
public class TestDBHelper_query {

    @Autowired
    private DBHelper dbHelper;

    @Test 
    public void testSameTableNameAs() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        StudentCalVO db = dbHelper.getOne(StudentCalVO.class, "where id=?", studentDO.getId());
        assert db != null;
        assert db.getNameWithHi() != null && db.getNameWithHi().endsWith("hi");
    }

    @Test
    public void testRelatedColumnWithLimit() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);

        CourseDO courseDO1 = new CourseDO();
        courseDO1.setName("math");
        courseDO1.setStudentId(studentDO.getId());
        courseDO1.setIsMain(true); // math是主课程
        dbHelper.insert(courseDO1);

        CourseDO courseDO2 = new CourseDO();
        courseDO2.setName("eng");
        courseDO2.setStudentId(studentDO.getId());
        courseDO2.setIsMain(true); // eng是主课程
        dbHelper.insert(courseDO2);

        CourseDO courseDO3 = new CourseDO();
        courseDO3.setName("chinese");
        courseDO3.setStudentId(studentDO.getId());
        courseDO3.setIsMain(true); // chinese是主课程
        dbHelper.insert(courseDO3);

        StudentDO studentDO2 = CommonOps.insertOne(dbHelper);

        List<StudentLimitVO> all = dbHelper.getAll(StudentLimitVO.class, "where id=? or id=?",
                studentDO.getId(), studentDO2.getId());
        for (StudentLimitVO a : all) {
            if (a.getId().equals(studentDO.getId())) {
                assert a.getMainCourses().size() == 2;
                assert a.getMainCourses().get(0).getIsMain();
                assert a.getMainCourses().get(1).getIsMain();
            }
            if (a.getId().equals(studentDO2.getId())) {
                assert a.getMainCourses().isEmpty();
            }
        }
    }

    @Test
    public void testRelatedColumnConditional() {
        // 构造数据：
        // 课程1 学生1 主课程
        // 课程1 学生2 非主课程
        // 课程2 学生1 非主课程
        // 课程2 学生2 主课程
        StudentDO student1 = CommonOps.insertOne(dbHelper);
        StudentDO student2 = CommonOps.insertOne(dbHelper);

        String course1 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String course2 = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        CourseDO courseDO = new CourseDO();
        courseDO.setName(course1);
        courseDO.setStudentId(student1.getId());
        courseDO.setIsMain(true);
        dbHelper.insert(courseDO);
        Long id1 = courseDO.getId();

        courseDO = new CourseDO();
        courseDO.setName(course1);
        courseDO.setStudentId(student2.getId());
        courseDO.setIsMain(false);
        dbHelper.insert(courseDO);
        Long id2 = courseDO.getId();

        courseDO = new CourseDO();
        courseDO.setName(course2);
        courseDO.setStudentId(student1.getId());
        courseDO.setIsMain(false);
        dbHelper.insert(courseDO);
        Long id3 = courseDO.getId();

        courseDO = new CourseDO();
        courseDO.setName(course2);
        courseDO.setStudentId(student2.getId());
        courseDO.setIsMain(true);
        dbHelper.insert(courseDO);
        Long id4 = courseDO.getId();

        CourseVO courseVO = dbHelper.getOne(CourseVO.class, "where id=?", id1);
        assert courseVO.getMainCourseStudents().size() == 1;
        assert courseVO.getMainCourseStudents().get(0).getId().equals(student1.getId());
        assert courseVO.getMainCourseStudent().getId().equals(student1.getId());

        courseVO = dbHelper.getOne(CourseVO.class, "where id=?", id2);
        assert courseVO.getMainCourseStudents().isEmpty();
        assert courseVO.getMainCourseStudent() == null;

        courseVO = dbHelper.getOne(CourseVO.class, "where id=?", id3);
        assert courseVO.getMainCourseStudents().isEmpty();
        assert courseVO.getMainCourseStudent() == null;

        courseVO = dbHelper.getOne(CourseVO.class, "where id=?", id4);
        assert courseVO.getMainCourseStudents().size() == 1;
        assert courseVO.getMainCourseStudents().get(0).getId().equals(student2.getId());
        assert courseVO.getMainCourseStudent().getId().equals(student2.getId());

    }

    @Test 
    public void testRelatedColumn() {

        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("sysu");
        dbHelper.insert(schoolDO);

        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        studentDO.setSchoolId(schoolDO.getId());
        dbHelper.update(studentDO);

        CourseDO courseDO1 = new CourseDO();
        courseDO1.setName("math");
        courseDO1.setStudentId(studentDO.getId());
        courseDO1.setIsMain(true); // math是主课程
        dbHelper.insert(courseDO1);

        CourseDO courseDO2 = new CourseDO();
        courseDO2.setName("eng");
        courseDO2.setStudentId(studentDO.getId());
        dbHelper.insert(courseDO2);

        StudentDO studentDO2  = CommonOps.insertOne(dbHelper);
        studentDO2.setSchoolId(schoolDO.getId());
        dbHelper.update(studentDO2);

        CourseDO courseDO3 = new CourseDO();
        courseDO3.setName("math");
        courseDO3.setStudentId(studentDO2.getId());
        courseDO3.setIsMain(true); // math是主课程
        dbHelper.insert(courseDO3);

        CourseDO courseDO4 = new CourseDO();
        courseDO4.setName("chinese");
        courseDO4.setStudentId(studentDO2.getId());
        dbHelper.insert(courseDO4);

        /////////////////// 下面是查询 ///////////////////

        StudentVO studentVO1 = dbHelper.getByKey(StudentVO.class, studentDO.getId());
        assert studentVO1 != null;
        assert studentVO1.getSchoolDO() != null;
        assert studentVO1.getSchoolDO().getId().equals(studentVO1.getSchoolId());
        assert studentVO1.getCourses() != null;
        assert studentVO1.getCourses().size() == 2;
        assert studentVO1.getCourses().get(0).getId().equals(courseDO1.getId())
                || studentVO1.getCourses().get(0).getId().equals(courseDO2.getId());
        assert studentVO1.getMainCourses().size() == 1 &&
                studentVO1.getMainCourses().get(0).getName().equals("math"); // math是主课程
        assert studentVO1.getNameWithHi().equals(studentVO1.getName() + "hi"); // 测试计算列

        // == handleRelatedColumn test
        StudentVOForHandleRelatedColumnOnly studentVO2 = new StudentVOForHandleRelatedColumnOnly();
        studentVO2.setId(studentDO.getId());
        studentVO2.setSchoolId(studentDO.getSchoolId());
        dbHelper.handleRelatedColumn(studentVO2);
        assert studentVO2 != null;
        assert studentVO2.getSchoolDO() != null;
        assert studentVO2.getSchoolDO().getId().equals(studentVO2.getSchoolId());
        assert studentVO2.getCourses() != null;
        assert studentVO2.getCourses().size() == 2;
        assert studentVO2.getCourses().get(0).getId().equals(courseDO1.getId())
                || studentVO2.getCourses().get(0).getId().equals(courseDO2.getId());
        assert studentVO2.getMainCourses().size() == 1 &&
                studentVO2.getMainCourses().get(0).getName().equals("math"); // math是主课程

        studentVO2 = new StudentVOForHandleRelatedColumnOnly();
        studentVO2.setId(studentDO.getId());
        studentVO2.setSchoolId(studentDO.getSchoolId());
        dbHelper.handleRelatedColumn(studentVO2, "courses", "schoolDO"); // 指定要的RelatedColumn
        assert studentVO2 != null;
        assert studentVO2.getSchoolDO() != null;
        assert studentVO2.getSchoolDO().getId().equals(studentVO2.getSchoolId());
        assert studentVO2.getCourses() != null;
        assert studentVO2.getCourses().size() == 2;
        assert studentVO2.getCourses().get(0).getId().equals(courseDO1.getId())
                || studentVO2.getCourses().get(0).getId().equals(courseDO2.getId());
        assert studentVO2.getMainCourses() == null;

        // END

        List<Long> ids = new ArrayList<Long>();
        ids.add(studentDO.getId());
        ids.add(studentDO2.getId());
        List<StudentVO> studentVOs = dbHelper.getAll(StudentVO.class,
                "where id in (?)", ids);
        assert studentVOs.size() == 2;
        for(StudentVO sVO : studentVOs) {
            assert sVO != null;
            assert sVO.getSchoolDO() != null;
            assert sVO.getSchoolDO().getId().equals(sVO.getSchoolId());
            assert sVO.getCourses() != null;
            assert sVO.getCourses().size() == 2;
            assert sVO.getMainCourses().size() == 1 &&
                    studentVO1.getMainCourses().get(0).getName().equals("math"); // math是主课程

            if(sVO.getId().equals(studentDO2.getId())) {
                assert 
                        sVO.getCourses().get(0).getId().equals(courseDO3.getId())
                                || sVO.getCourses().get(1).getId().equals(courseDO4.getId());
            }

            assert sVO.getNameWithHi().equals(sVO.getName() + "hi"); // 测试计算列
        }

        // 测试innerClass
        SchoolWithInnerClassVO schoolVO = dbHelper.getByKey(SchoolWithInnerClassVO.class, schoolDO.getId());
        assert schoolVO != null && schoolVO.getId().equals(schoolDO.getId());
        assert schoolVO.getStudents().size() == 2;
        for(SchoolWithInnerClassVO.StudentVO s : schoolVO.getStudents()) {
            assert s != null && s.getId() != null && s.getCourses().size() == 2;
        }
    }

    @Test 
    public void testGetByKey() {
        Long id = CommonOps.insertOne(dbHelper).getId();

        StudentDO studentDO = new StudentDO();
        studentDO.setId(id);

        assert dbHelper.getByKey(studentDO);

        StudentDO student2 = dbHelper.getByKey(StudentDO.class, id);
        assert student2 != null;

        // student的时分秒不能全为0
        Date createTime = student2.getCreateTime();
        assert createTime != null;
        assert !(createTime.getHours() == 0 && createTime.getMinutes() == 0 && createTime.getSeconds() == 0);

        // student的时间搓在当前时间10秒以内才算合格
        assert System.currentTimeMillis() - createTime.getTime() < 10000;
    }

    @Test 
    public void testGetByKeyList() {
        List<Long> ids = new ArrayList<Long>();
        ids.add(CommonOps.insertOne(dbHelper).getId());
        ids.add(CommonOps.insertOne(dbHelper).getId());
        ids.add(CommonOps.insertOne(dbHelper).getId());
        Map<Long, StudentDO> map = dbHelper.getByKeyList(StudentDO.class, ids);

        assert 3 == map.size();
        for(int i = 0; i < 3; i++) {
            assert map.get(ids.get(i)).getId().equals(ids.get(i));
        }

        // 测试一下用Set来查询
        Set<Long> idsSet = new HashSet<>(ids);
        map = dbHelper.getByKeyList(StudentDO.class, idsSet);

        assert 3 == map.size();
        for(int i = 0; i < 3; i++) {
            assert map.get(ids.get(i)).getId().equals(ids.get(i));
        }


        List<StudentDO> allKey = dbHelper.getAllKey(StudentDO.class, "where 1=1");
        assert allKey.size() >= 3;
    }

    @Test 
    public void testExists() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        assert dbHelper.isExist(StudentDO.class, null);
        assert dbHelper.isExist(StudentDO.class, "where id=?", studentDO.getId());
        assert dbHelper.isExistAtLeast(1, StudentDO.class,
                "where id=?", studentDO.getId());

        assert !dbHelper.isExistAtLeast(2, StudentDO.class,
                "where id=?", studentDO.getId());
    }

    @Test 
    public void testGetList() {
        // 测试获取全部
        List<StudentDO> list = dbHelper.getAll(StudentDO.class);
        System.out.println("total:" + list.size());
        for(StudentDO studentDO : list) {
            System.out.println(studentDO);
        }

        System.out.println("===============================");

        // 测试获取有条件的查询
        Long[] ids = new Long[3];
        ids[0] = 2L;
        ids[1] = 4L;
        ids[2] = 6L;
        //List<StudentDO> list2 = dbHelper.getAll(StudentDO.class, "where id in (?)",
        //		ids); // 这样是错误的范例，getAll只会取ids的第一个参数传入in (?)中
        List<StudentDO> list2 = dbHelper.getAll(StudentDO.class, "where id in (?)",
                ids, 1); // 这是一种hack的写法，后面带上的参数1，可以让Java把ids当作单个参数处理
        System.out.println("total:" + list2.size());
        for(StudentDO studentDO : list2) {
            System.out.println(studentDO);
        }

        System.out.println("===============================");
    }

    @Test 
    public void testGetPage() {
        CommonOps.insertBatch(dbHelper,100);

        // 测试分页获取
        PageData<StudentDO> page1 = dbHelper.getPage(StudentDO.class, 1, 10);
        assert page1.getTotal() >= 100;
        assert page1.getData().size() == 10;

        page1 = dbHelper.getPage(StudentDO.class, 2, 10);
        assert page1.getTotal() >= 100;
        assert page1.getData().size() == 10;

        page1 = dbHelper.getPageWithoutCount(StudentDO.class, 1, 10);
        assert page1.getData().size() == 10;

        page1 = dbHelper.getPageWithoutCount(StudentDO.class, 2, 10);
        assert page1.getData().size() == 10;

        long total = dbHelper.getCount(StudentDO.class);
        assert total >= 100;

        total = dbHelper.getCount(StudentDO.class, "where name like ?", "nick%");
        assert total >= 100;
    }

    @Test
    public void testGetPageRemoteLimitAddOrder() {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        CommonOps.insertBatch(dbHelper, 20, prefix);

        // 这里故意加上limit子句，会被自动清除掉
        PageData<StudentDO> page = dbHelper.getPage(StudentDO.class, 1, 10,
                "where name like ? group by name, age limit 4,6", prefix + "%");
        assert page.getData().size() == 10;
        assert page.getTotal() == 20;

        PageData<StudentDO> page2 = dbHelper.getPage(StudentDO.class, 2, 10,
                "where name like ? group by name, age limit 4,6", prefix + "%");
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

        page = dbHelper.getPage(StudentDO.class, 1, 10,
                "where name like ? limit 4,6", prefix + "%");
        assert page.getData().size() == 10;
        assert page.getTotal() == 20;

        page2 = dbHelper.getPage(StudentDO.class, 2, 10,
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
        page = dbHelper.getPage(StudentDO.class, 1, 10,
                "where name like ? group by name,age order by name limit 4,6", prefix + "%"); // 看告警
    }

    @Test 
    public void testPageDataTransform() {
        CommonOps.insertBatch(dbHelper,20);
        PageData<StudentDO> page1 = dbHelper.getPage(StudentDO.class, 1, 10);
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

    @Test 
    public void testGetByExample() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);

        StudentDO example = new StudentDO();
        example.setName(studentDO.getName());

        List<StudentDO> byExample = dbHelper.getByExample(example, 10);
        assert byExample.size() == 1;
        assert byExample.get(0).getId().equals(studentDO.getId());
        assert byExample.get(0).getName().equals(studentDO.getName());

        example.setIntro(studentDO.getIntro());
        byExample = dbHelper.getByExample(example, 10);
        assert byExample.size() == 1;
        assert byExample.get(0).getId().equals(studentDO.getId());
        assert byExample.get(0).getName().equals(studentDO.getName());
    }

    @Test 
    public void testGetByArray() {
        // 但是这种写法容易有歧义，推荐传入List参数值
        List<StudentDO> list = dbHelper.getAll(StudentDO.class, "where id in (?)", new long[]{50,51,52});
        System.out.println(list.size());
        list = dbHelper.getAll(StudentDO.class, "where id in (?)", new int[]{50,51,52});
        System.out.println(list.size());
        list = dbHelper.getAll(StudentDO.class, "where id in (?)", new short[]{50,51,52});
        System.out.println(list.size());
        list = dbHelper.getAll(StudentDO.class, "where id in (?)", new char[]{50,51,52});
        System.out.println(list.size());
        list = dbHelper.getAll(StudentDO.class, "where id in (?)", new float[]{50,51,52});
        System.out.println(list.size());
        list = dbHelper.getAll(StudentDO.class, "where id in (?)", new double[]{50,51,52});
        System.out.println(list.size());

        // 测试空list或空set
        list = dbHelper.getAll(StudentDO.class, "where id in (?)", new ArrayList<Long>());
        assert list.isEmpty();
        list = dbHelper.getAll(StudentDO.class, "where id in (?)", new HashSet<Long>());
        assert list.isEmpty();
    }

    @Test
    public void testGetJoin() {
        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("sysu");
        dbHelper.insert(schoolDO);

        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        studentDO.setSchoolId(schoolDO.getId());
        dbHelper.update(studentDO);

        StudentDO studentDO2 = CommonOps.insertOne(dbHelper);
        studentDO2.setSchoolId(schoolDO.getId());
        dbHelper.update(studentDO2);

        PageData<StudentSchoolJoinVO> pageData = dbHelper.getPage(StudentSchoolJoinVO.class, 1, 10);
        assert pageData.getData().size() > 0;
        for(StudentSchoolJoinVO vo : pageData.getData()) {
            assert vo.getStudentDO() != null;
        }

        List<StudentSchoolJoinVO> all = dbHelper.getAll(StudentSchoolJoinVO.class, "where t1.id in (?)",
                ListUtils.newList(studentDO.getId(), studentDO2.getId()));
        assert all.size() == 2;
        assert all.get(0).getSchoolDO2().getId().equals(schoolDO.getId());
        assert all.get(1).getSchoolDO2().getId().equals(schoolDO.getId());
        assert all.get(0).getSchoolDO().getId().equals(schoolDO.getId());
        assert all.get(1).getSchoolDO().getId().equals(schoolDO.getId());
        assert all.get(0).getSchoolDO3().getId().equals(schoolDO.getId());
        assert all.get(1).getSchoolDO3().getId().equals(schoolDO.getId());
        assert all.get(0).getVo2().getSchoolDO().getId().equals(schoolDO.getId());
        assert all.get(1).getVo2().getSchoolDO().getId().equals(schoolDO.getId());
        assert all.get(0).getVo3().getSchoolDO().getId().equals(schoolDO.getId());
        assert all.get(1).getVo3().getSchoolDO().getId().equals(schoolDO.getId());

        pageData = dbHelper.getPage(StudentSchoolJoinVO.class, 1, 10,
                "where t1.name like ?", "nick%");
        assert pageData.getData().size() > 0;
        for(StudentSchoolJoinVO vo : pageData.getData()) {
            assert vo.getStudentDO() != null;
        }

        long total = dbHelper.getCount(StudentSchoolJoinVO.class);
        assert total > 0;
        total = dbHelper.getCount(StudentSchoolJoinVO.class, "where t1.name like ?", "nick%");
        assert total > 0;

        // right join test
        PageData<StudentSchoolJoinVO2> pageData2 = dbHelper.getPage(StudentSchoolJoinVO2.class, 1, 10);
        assert pageData2.getData().size() > 0;
        for(StudentSchoolJoinVO2 vo : pageData2.getData()) {
            assert vo.getStudentDO() != null;
        }

    }

    @Test 
    public void testDateTime() {

        dbHelper.turnOnFeature(FeatureEnum.LOG_SQL_AT_INFO_LEVEL);

        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        dbHelper.deleteByKey(studentDO);

        StudentWithLocalDateTimeDO one = dbHelper.getOne(StudentWithLocalDateTimeDO.class,
                "where id=?", studentDO.getId());

        assert one.getCreateTime() != null;
        assert one.getUpdateTime() != null;
        assert one.getDeleteTime() != null;
    }

    @Test 
    public void testCount() {

        dbHelper.delete(StudentDO.class, "where 1=1");
        long count = dbHelper.getCount(StudentDO.class);
        assert count == 0;

        dbHelper.delete(SchoolDO.class, "where 1=1");
        count = dbHelper.getCount(SchoolDO.class);
        assert count == 0;

        List<StudentDO> studentDOS = CommonOps.insertBatch(dbHelper, 99);

        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("sysu");
        dbHelper.insert(schoolDO);
        assert schoolDO.getId() != null;

        for(StudentDO studentDO : studentDOS) {
            studentDO.setSchoolId(schoolDO.getId());
            dbHelper.update(studentDO);
        }

        count = dbHelper.getCount(StudentDO.class);
        assert count == 99;
        count = dbHelper.getCount(StudentDO.class, "where 1=1");
        assert count == 99;
        count = dbHelper.getCount(StudentDO.class, "where name like ?", "nick%");
        assert count == 99;
        count = dbHelper.getCount(StudentDO.class, "where name like ? group by name", "nick%");
        assert count == 99;

        count = dbHelper.getCount(StudentDO.class, "where name not like ? group by name", "nick%");
        assert count == 0;

        List<String> names = new ArrayList<String>();
        names.add(studentDOS.get(0).getName());
        names.add(studentDOS.get(10).getName());
        names.add(studentDOS.get(30).getName());
        count = dbHelper.getCount(StudentDO.class, "where name in (?)",names);
        assert count == 3;

        PageData<StudentDO> page = dbHelper.getPage(StudentDO.class, 1, 10);
        assert page.getData().size() == 10;
        assert page.getTotal() == 99;

        page = dbHelper.getPage(StudentDO.class, 1, 10, "where name like ?", "nick%");
        assert page.getData().size() == 10;
        assert page.getTotal() == 99;

        page = dbHelper.getPage(StudentDO.class, 1, 10, "where name not like ?", "nick%");
        assert page.getData().size() == 0;
        assert page.getTotal() == 0;

        page = dbHelper.getPage(StudentDO.class, 1, 10, "where name in (?)", names);
        assert page.getData().size() == 3;
        assert page.getTotal() == 3;

        page = dbHelper.getPage(StudentDO.class, 1, 2, "where name in (?)", names);
        assert page.getData().size() == 2;
        assert page.getTotal() == 3;

        page = dbHelper.getPage(StudentDO.class, 1, 2, "where name in (?) group by name", names);
        assert page.getData().size() == 2;
        assert page.getTotal() == 3;


        page = dbHelper.getPage(StudentDO.class, 1, 100);
        assert page.getData().size() == 99;
        assert page.getTotal() == 99;

        count = dbHelper.getCount(StudentSchoolJoinVO.class);
        assert count == 99;
        count = dbHelper.getCount(StudentSchoolJoinVO.class, "where 1=1");
        assert count == 99;
        count = dbHelper.getCount(StudentSchoolJoinVO.class, "where t1.name like ?", "nick%");
        assert count == 99;
        count = dbHelper.getCount(StudentSchoolJoinVO.class, "where t1.name like ? group by t1.name", "nick%");
        assert count == 99;

        PageData<StudentSchoolJoinVO> page2 = dbHelper.getPage(StudentSchoolJoinVO.class, 1, 10);
        assert page2.getData().size() == 10;
        assert page2.getTotal() == 99;

        page2 = dbHelper.getPage(StudentSchoolJoinVO.class, 1, 100);
        assert page2.getData().size() == 99;
        assert page2.getTotal() == 99;

    }

    /**测试软删除DO查询条件中涉及到OR条件的情况*/
    @Test 
    public void testQueryWithDeletedAndOr() {
        // 先清表
        dbHelper.delete(StudentDO.class, "where 1=1");

        CommonOps.insertBatch(dbHelper, 10);
        dbHelper.delete(StudentDO.class, "where 1=1"); // 确保至少有10条删除记录

        CommonOps.insertBatch(dbHelper, 10);
        List<StudentDO> all = dbHelper.getAll(StudentDO.class, "where 1=1 or 1=1"); // 重点
        assert all.size() == 10; // 只应该查出10条记录，而不是20条以上的记录
        for(StudentDO studentDO : all) {
            assert !studentDO.getDeleted();
        }

        all = dbHelper.getAll(StudentDO.class, "where 1=1 and 1=1 or 1=1 or 1=1");
        assert all.size() == 10;
    }

    /**测试join真删除的类*/
    @Test 
    public void testJoinTrueDelete() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        StudentSelfTrueDeleteJoinVO joinVO = dbHelper.getOne(StudentSelfTrueDeleteJoinVO.class, "where t1.id=?", studentDO.getId());
        assert joinVO.getStudent1().getId().equals(studentDO.getId());
        assert joinVO.getStudent2().getId().equals(studentDO.getId());
    }

    /**测试慢速记录*/
    @Test 
    public void testSlowLog() {
        final StringBuilder sb = new StringBuilder();

        dbHelper.setTimeoutWarningValve(1);
        dbHelper.setTimeoutWarningCallback(new IDBHelperSlowSqlCallback() {
            @Override
            public void callback(long executeMsTime, String sql, List<Object> args) {
                System.out.println("==in slow callback== execMs:" + executeMsTime + "ms,"
                    + "sql:" + sql + "args:" + NimbleOrmJSON.toJson(args));
                sb.append(sql);
            }
        });

        CommonOps.insertOne(dbHelper);
        assert !sb.toString().isEmpty();

        dbHelper.setTimeoutWarningValve(1000);
    }

    /**测试分页最大数限制*/
    @Test 
    public void testMaxPageSize() {
        dbHelper.setMaxPageSize(5);

        CommonOps.insertBatch(dbHelper, 10);
        PageData<StudentDO> pageData = dbHelper.getPage(StudentDO.class, 1, 10);
        assert pageData.getData().size() == 5; // 受限制于maxPageSize

        pageData = dbHelper.getPageWithoutCount(StudentDO.class, 1, 10);
        assert pageData.getData().size() == 5; // 受限制于maxPageSize

        pageData = dbHelper.getPageWithoutCount(StudentDO.class, 1, 10, "where 1=1");
        assert pageData.getData().size() == 5; // 受限制于maxPageSize

        dbHelper.setMaxPageSize(1000000);
    }

    @Test
    public void testGetRawIfColumnNotExist() {
        final StudentDO studentDO1 = CommonOps.insertOne(dbHelper);

        // 这里故意只查回id，而DO类是要收id和name的，默认情况下不会报错
        List<StudentForRawDO> list = dbHelper.getRaw(StudentForRawDO.class,
                "select id from t_student where name=?", studentDO1.getName());

        assert list.get(0).getId().equals(studentDO1.getId());
        assert list.get(0).getName() == null;

        dbHelper.turnOnFeature(FeatureEnum.THROW_EXCEPTION_IF_COLUMN_NOT_EXIST);
        boolean isThrowEx = false;
        try {
            list = dbHelper.getRaw(StudentForRawDO.class,
                    "select id from t_student where name=?", studentDO1.getName());
        } catch (Exception e) {
            isThrowEx = true;
        }
        assert isThrowEx;

        dbHelper.turnOffFeature(FeatureEnum.THROW_EXCEPTION_IF_COLUMN_NOT_EXIST);
    }

    @Test 
    public void testGetRaw() {
        final StudentDO studentDO1 = CommonOps.insertOne(dbHelper);
        final StudentDO studentDO2 = CommonOps.insertOne(dbHelper);

        List<StudentForRawDO> list = dbHelper.getRaw(StudentForRawDO.class,
                "select id,name from t_student where name=?", studentDO1.getName());

        assert list.size() == 1;
        assert list.get(0).getName().equals(studentDO1.getName());

        List<Map> list2 = dbHelper.getRaw(Map.class,
                "select id,name from t_student where name=?", studentDO1.getName());
        assert list2.size() == 1;
        assert list2.get(0).get("name").equals(studentDO1.getName());


        Map<String, Object> params = new HashMap<>();
        params.put("name", studentDO1.getName());
        list = dbHelper.getRaw(StudentForRawDO.class,
                "select id,name from t_student where name=:name",
                params);

        assert list.size() == 1;
        assert list.get(0).getName().equals(studentDO1.getName());

        long count = dbHelper.getRawOne(Long.class, "select count(*) from t_student where name=:name",
                params);
        assert count == 1;

        List<String> names = new ArrayList<String>();
        names.add(studentDO1.getName());
        names.add(studentDO2.getName());
        count = dbHelper.getRawOne(Long.class, "select count(*) from t_student where name in (?)",
                names);
        assert count == 2;

        List<Long> sum = dbHelper.getRaw(Long.class, "select sum(age) from t_student where name=?",
                UUID.randomUUID().toString());
        assert sum.get(0) == 0;

        // test get rawOne
        Long sum2 = dbHelper.getRawOne(Long.class, "select sum(age) from t_student where name=?",
                UUID.randomUUID().toString());
        assert sum2 == 0;

        sum2 = dbHelper.getRawOne(Long.class, "select sum(age) from t_student where name=:name",
                MapUtils.of("name", UUID.randomUUID().toString()));
        assert sum2 == 0;
    }

    @Test 
    public void testGetRawWithBasicType() {

        StudentDO studentDO1 = CommonOps.insertOne(dbHelper);
        StudentDO studentDO2 = CommonOps.insertOne(dbHelper);
        StudentDO studentDO3 = CommonOps.insertOne(dbHelper);

        List<String> studentNames = dbHelper.getRaw(String.class, "select name from t_student where deleted=0");

        assert studentNames.contains(studentDO1.getName());
        assert studentNames.contains(studentDO2.getName());
        assert studentNames.contains(studentDO3.getName());

        List<Integer> count = dbHelper.getRaw(Integer.class, "select count(*) from t_student where deleted=0");
        assert count.get(0) >= 3;

        List<Boolean> bools = dbHelper.getRaw(Boolean.class, "select 1");
        assert bools.get(0);
        bools = dbHelper.getRaw(Boolean.class, "select 0");
        assert !bools.get(0);

        List<Byte> bytes = dbHelper.getRaw(Byte.class, "select 'a'");
        assert bytes.get(0) == 97;

        List<byte[]> bytes2 = dbHelper.getRaw(byte[].class, "select 'a'");
        assert bytes2.get(0)[0] == 97;

        List<Short> count2 = dbHelper.getRaw(Short.class, "select count(*) from t_student where deleted=0");
        assert count2.get(0) >= 3;

        List<Float> count3 = dbHelper.getRaw(Float.class, "select count(*) from t_student where deleted=0");
        assert count3.get(0) >= 3;

        List<Double> count4 = dbHelper.getRaw(Double.class, "select count(*) from t_student where deleted=0");
        assert count4.get(0) >= 3;

        List<BigDecimal> count5 = dbHelper.getRaw(BigDecimal.class, "select count(*) from t_student where deleted=0");
        assert count5.get(0).compareTo(BigDecimal.valueOf(3)) >= 0;

        List<Date> dates = dbHelper.getRaw(Date.class, "select now()");
        assert dates.get(0) != null;

        List<LocalDateTime> dates2 = dbHelper.getRaw(LocalDateTime.class, "select now()");
        assert dates2.get(0) != null;

        List<LocalDate> dates3 = dbHelper.getRaw(LocalDate.class, "select now()");
        assert dates3.get(0) != null;

        List<LocalTime> dates4 = dbHelper.getRaw(LocalTime.class, "select now()");
        assert dates4.get(0) != null;

        List<java.sql.Date> dates5 = dbHelper.getRaw(java.sql.Date.class, "select now()");
        assert dates5.get(0) != null;
        List<java.sql.Time> dates6 = dbHelper.getRaw(java.sql.Time.class, "select now()");
        assert dates6.get(0) != null;
        List<java.sql.Timestamp> dates7 = dbHelper.getRaw(java.sql.Timestamp.class, "select now()");
        assert dates7.get(0) != null;

    }
    
    @Test
    public void testSum() {
        // 故意让sum的记录不存在
        StudentSumVO one = dbHelper.getOne(StudentSumVO.class, "where id = -1");
        assert one.getAgeSum() == 0;

        dbHelper.delete(StudentDO.class, "where 1=1");
        CommonOps.insertBatch(dbHelper, 30);

        PageData<StudentSumVO> pageData = dbHelper.getPage(StudentSumVO.class,
                1, 10, "group by name order by ageSum");

        assert pageData.getTotal() == 30;
        assert pageData.getData().size() == 10;

    }

    /**
     * 测试相同列字段名称的情况
     */
    
    @Test
    public void testSameColumn() {
        StudentDO studentDO = CommonOps.insertOne(dbHelper);

        StudentSameColumnNameVO one = dbHelper.getOne(
                StudentSameColumnNameVO.class, "where name=?", studentDO.getName());

        assert one.getName().endsWith("FFFFFFFF");
        assert one.getName2() == null;
    }


    @Test
    public void testSubQuery() {
        StudentDO stu1 = CommonOps.insertOne(dbHelper);
        StudentDO stu2 = CommonOps.insertOne(dbHelper);
        StudentDO stu3 = CommonOps.insertOne(dbHelper);

        List<Long> ids = new ArrayList<Long>();
        ids.add(stu1.getId());
        ids.add(stu2.getId());
        ids.add(stu3.getId());

        SubQuery subQuery = new SubQuery("id", StudentDO.class, "where id in (?)", ids);

        List<StudentDO> all = dbHelper.getAll(StudentDO.class, "where id in (?)", subQuery);
        assert all.size() == 3;
        for(StudentDO stu : all) {
            assert ids.contains(stu.getId());
        }

        all = dbHelper.getAll(StudentDO.class, "where id in (?) and id > ?" +
                " and name != '\\''", subQuery, 0); // 测试subQuery和参数混合
        assert all.size() == 3;
        for(StudentDO stu : all) {
            assert ids.contains(stu.getId());
        }

        // 测试3层subQuery
        SubQuery subQuery1 = new SubQuery("id", StudentDO.class, "where id in (?)", ids);
        SubQuery subQuery2 = new SubQuery("id", StudentDO.class, "where id in (?)", subQuery1);
        SubQuery subQuery3 = new SubQuery("id", StudentDO.class, "where id in (?)", subQuery2);

        all = dbHelper.getAll(StudentDO.class, "where id in (?)", subQuery3);
        assert all.size() == 3;
        for(StudentDO stu : all) {
            assert ids.contains(stu.getId());
        }
    }

    @Test
    public void testReadIfNull() {
        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName(null);
        dbHelper.insert(schoolDO);

        SchoolForReadNullDO one = dbHelper.getOne(SchoolForReadNullDO.class, "where id=?", schoolDO.getId());
        assert one.getId().equals(schoolDO.getId());
        assert schoolDO.getName() == null;
        assert one.getName().equals("myname");
    }

    @Test
    public void testVirtualTable() {
        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName("collageA");
        dbHelper.insert(schoolDO);

        StudentDO studentDO = CommonOps.insertOne(dbHelper);
        studentDO.setSchoolId(schoolDO.getId());
        dbHelper.update(studentDO);

        StudentDO studentDO2 = CommonOps.insertOne(dbHelper);
        studentDO2.setSchoolId(schoolDO.getId());
        dbHelper.update(studentDO2);

        // virtual table SQL
        {
            // get all
            List<StudentVirtualTableVO> all = dbHelper.getAll(StudentVirtualTableVO.class,
                    "and t1.id in (?) order by t1.id",
                    ListUtils.newList(studentDO.getId(), studentDO2.getId()));
            assert all.size() == 2;
            assert all.get(0).getId().equals(studentDO.getId());
            assert all.get(1).getId().equals(studentDO2.getId());
            assert all.get(0).getName().equals(studentDO.getName());
            assert all.get(1).getName().equals(studentDO2.getName());
            assert all.get(0).getSchoolName().equals(schoolDO.getName());
            assert all.get(1).getSchoolName().equals(schoolDO.getName());

            // get page
            PageData<StudentVirtualTableVO> page = dbHelper.getPage(StudentVirtualTableVO.class, 1, 10,
                    "and t1.id in (?) order by t1.id",
                    ListUtils.newList(studentDO.getId(), studentDO2.getId()));
            assert page.getTotal() == 2;
            assert page.getData().size() == 2;
            assert page.getData().get(0).getId().equals(studentDO.getId());
            assert page.getData().get(1).getId().equals(studentDO2.getId());
            assert page.getData().get(0).getName().equals(studentDO.getName());
            assert page.getData().get(1).getName().equals(studentDO2.getName());
            assert page.getData().get(0).getSchoolName().equals(schoolDO.getName());
            assert page.getData().get(1).getSchoolName().equals(schoolDO.getName());
        }

        // virtual table path
        {
            // get all
            List<StudentVirtualTableVO2> all = dbHelper.getAll(StudentVirtualTableVO2.class,
                    "and t1.id in (?) order by t1.id",
                    ListUtils.newList(studentDO.getId(), studentDO2.getId()));
            assert all.size() == 2;
            assert all.get(0).getId().equals(studentDO.getId());
            assert all.get(1).getId().equals(studentDO2.getId());
            assert all.get(0).getName().equals(studentDO.getName());
            assert all.get(1).getName().equals(studentDO2.getName());
            assert all.get(0).getSchoolName().equals(schoolDO.getName());
            assert all.get(1).getSchoolName().equals(schoolDO.getName());

            // get page
            PageData<StudentVirtualTableVO2> page = dbHelper.getPage(StudentVirtualTableVO2.class, 1, 10,
                    "and t1.id in (?) order by t1.id",
                    ListUtils.newList(studentDO.getId(), studentDO2.getId()));
            assert page.getTotal() == 2;
            assert page.getData().size() == 2;
            assert page.getData().get(0).getId().equals(studentDO.getId());
            assert page.getData().get(1).getId().equals(studentDO2.getId());
            assert page.getData().get(0).getName().equals(studentDO.getName());
            assert page.getData().get(1).getName().equals(studentDO2.getName());
            assert page.getData().get(0).getSchoolName().equals(schoolDO.getName());
            assert page.getData().get(1).getSchoolName().equals(schoolDO.getName());
        }

    }

}
