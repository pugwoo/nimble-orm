package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentTrueDeleteDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.StudentVO;
import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.collect.MapUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 说明：这里只测试了stream的逻辑正确性，stream的效果暂未体现（需要几十万条记录才能体现出来，故不再常规测试中，可以手工验证一下）
 */
@SpringBootTest
public class TestStream {

    @Autowired
    private DBHelper dbHelper;

    @Test
    public void testGetAllStream() {
        dbHelper.delete(StudentTrueDeleteDO.class, "where 1=1");

        dbHelper.setFetchSize(5);

        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName(UUID.randomUUID().toString().replace("-", ""));
        dbHelper.insert(schoolDO);

        int size = 9;
        String prefix = UUID.randomUUID().toString().replace("-", "");
        List<StudentDO> students = CommonOps.insertBatch(dbHelper, size, prefix);
        ListUtils.forEach(students, o -> o.setSchoolId(schoolDO.getId()));
        dbHelper.update(students);

        long start = System.currentTimeMillis();

        Stream<StudentDO> stream = dbHelper.getAllForStream(StudentDO.class, "where name like ?", prefix + "%");

        long end = System.currentTimeMillis();
        System.out.println("cost:" + (end - start) + "ms");

        List<StudentDO> list2 = stream.collect(Collectors.toList());

        assert list2.size() == size;
        for (StudentDO s : list2) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
        }

        // 测试一下relatedcolumn是否正确
        Stream<StudentVO> stream2 = dbHelper.getAllForStream(StudentVO.class,
                "where name like ?", prefix + "%");
        List<StudentVO> list3 = stream2.collect(Collectors.toList());

        assert list3.size() == size;
        for (StudentVO s : list3) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
            assert s.getSchoolDO().getName().equals(schoolDO.getName());
        }

        // 测试没有参数的
        CommonOps.insertOne(dbHelper);
        Stream<StudentVO> stream3 = dbHelper.getAllForStream(StudentVO.class);
        List<StudentVO> list4 = stream3.collect(Collectors.toList());
        assert list4.size() == dbHelper.getCount(StudentVO.class);
    }

    @Test
    public void testGetRawStream() {
        dbHelper.delete(StudentTrueDeleteDO.class, "where 1=1");

        dbHelper.setFetchSize(5);

        SchoolDO schoolDO = new SchoolDO();
        schoolDO.setName(UUID.randomUUID().toString().replace("-", ""));
        dbHelper.insert(schoolDO);

        int size = 9;
        String prefix = UUID.randomUUID().toString().replace("-", "");
        List<StudentDO> students = CommonOps.insertBatch(dbHelper, size, prefix);
        ListUtils.forEach(students, o -> o.setSchoolId(schoolDO.getId()));
        dbHelper.update(students);

        long start = System.currentTimeMillis();

        Stream<StudentDO> stream = dbHelper.getRawForStream(StudentDO.class,
                "select * from t_student where name like ?", prefix + "%");

        long end = System.currentTimeMillis();
        System.out.println("cost:" + (end - start) + "ms");

        List<StudentDO> list2 = stream.collect(Collectors.toList());

        assert list2.size() == size;
        for (StudentDO s : list2) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
        }

        // 测试一下relatedcolumn是否正确
        Stream<StudentVO> stream2 = dbHelper.getRawForStream(StudentVO.class,
                "select * from t_student where name like ?", prefix + "%");
        List<StudentVO> list3 = stream2.collect(Collectors.toList());

        assert list3.size() == size;
        for (StudentVO s : list3) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
            assert s.getSchoolDO().getName().equals(schoolDO.getName());
        }

        // 再测一下namedParam
        stream = dbHelper.getRawForStream(StudentDO.class, "select * from t_student where name like :name",
                MapUtils.of("name", prefix + "%"));
        list2 = stream.collect(Collectors.toList());
        assert list2.size() == size;
        for (StudentDO s : list2) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
        }

        stream2 = dbHelper.getRawForStream(StudentVO.class,
                "select * from t_student where name like :name", MapUtils.of("name", prefix + "%"));
        list3 = stream2.collect(Collectors.toList());
        assert list3.size() == size;
        for (StudentVO s : list3) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
            assert s.getSchoolDO().getName().equals(schoolDO.getName());
        }

        // 测试没有参数的
        List<StudentVO> list4 = dbHelper.getRawForStream(StudentVO.class, "select * from t_student")
                .collect(Collectors.toList());
        assert list4.size() == dbHelper.getRawOne(Integer.class, "select count(*) from t_student");

        // 测试空map参数的
        list4 = dbHelper.getRawForStream(StudentVO.class, "select * from t_student", new HashMap<>())
                .collect(Collectors.toList());
        assert list4.size() == dbHelper.getRawOne(Integer.class, "select count(*) from t_student");

    }

}
