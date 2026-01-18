package com.pugwoo.dbhelper.test.test_common;

import com.pugwoo.dbhelper.DBHelper;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import com.pugwoo.dbhelper.test.entity.StudentHardDeleteDO;
import com.pugwoo.dbhelper.test.utils.CommonOps;
import com.pugwoo.dbhelper.test.vo.StudentVO;
import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.collect.MapUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 说明：这里只测试了stream的逻辑正确性，stream的效果暂未体现
 * （需要几十万条记录才能体现出来，故不再常规测试中，可以手工验证一下）
 */
public abstract class Test1Query_Stream {

    public abstract DBHelper getDBHelper();

    @Test
    public void testGetAllStream() {
        getDBHelper().delete(StudentHardDeleteDO.class, "where 1=1");

        getDBHelper().setFetchSize(5);

        SchoolDO schoolDO = CommonOps.insertOneSchoolDO(getDBHelper(), UUID.randomUUID().toString().replace("-", ""));

        int size = 9;
        String prefix = UUID.randomUUID().toString().replace("-", "");
        List<StudentDO> students = CommonOps.insertBatch(getDBHelper(), size, prefix);
        ListUtils.forEach(students, o -> o.setSchoolId(schoolDO.getId()));
        getDBHelper().update(students);

        long start = System.currentTimeMillis();

        Stream<StudentDO> stream = getDBHelper().getAllForStream(StudentDO.class, "where name like ?", prefix + "%");

        long end = System.currentTimeMillis();
        System.out.println("cost:" + (end - start) + "ms");

        List<StudentDO> list2 = stream.collect(Collectors.toList());

        assert list2.size() == size;
        for (StudentDO s : list2) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
        }
        stream.close();

        // 测试一下relatedcolumn是否正确
        Stream<StudentVO> stream2 = getDBHelper().getAllForStream(StudentVO.class,
                "where name like ?", prefix + "%");
        List<StudentVO> list3 = stream2.collect(Collectors.toList());

        assert list3.size() == size;
        for (StudentVO s : list3) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
            assert s.getSchoolDO().getName().equals(schoolDO.getName());
        }
        stream2.close();

        // 测试没有参数的
        CommonOps.insertOne(getDBHelper());
        Stream<StudentVO> stream3 = getDBHelper().getAllForStream(StudentVO.class);
        List<StudentVO> list4 = stream3.collect(Collectors.toList());
        assert list4.size() == getDBHelper().getCount(StudentVO.class);
        stream3.close();
    }

    @Test
    public void testGetRawStream() {
        getDBHelper().delete(StudentHardDeleteDO.class, "where 1=1");

        getDBHelper().setFetchSize(5);

        SchoolDO schoolDO = CommonOps.insertOneSchoolDO(getDBHelper(), UUID.randomUUID().toString().replace("-", ""));

        int size = 9;
        String prefix = UUID.randomUUID().toString().replace("-", "");
        List<StudentDO> students = CommonOps.insertBatch(getDBHelper(), size, prefix);
        ListUtils.forEach(students, o -> o.setSchoolId(schoolDO.getId()));
        getDBHelper().update(students);

        long start = System.currentTimeMillis();

        Stream<StudentDO> stream = getDBHelper().getRawForStream(StudentDO.class,
                "select * from t_student where name like ?", prefix + "%");

        long end = System.currentTimeMillis();
        System.out.println("cost:" + (end - start) + "ms");

        List<StudentDO> list2 = stream.collect(Collectors.toList());

        assert list2.size() == size;
        for (StudentDO s : list2) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
        }

        stream.close();

        // 测试一下relatedcolumn是否正确
        Stream<StudentVO> stream2 = getDBHelper().getRawForStream(StudentVO.class,
                "select * from t_student where name like ?", prefix + "%");
        List<StudentVO> list3 = stream2.collect(Collectors.toList());

        assert list3.size() == size;
        for (StudentVO s : list3) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
            assert s.getSchoolDO().getName().equals(schoolDO.getName());
        }
        stream2.close();

        // 再测一下namedParam
        stream = getDBHelper().getRawForStream(StudentDO.class, "select * from t_student where name like :name",
                MapUtils.of("name", prefix + "%"));
        list2 = stream.collect(Collectors.toList());
        assert list2.size() == size;
        for (StudentDO s : list2) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
        }
        stream.close();

        stream2 = getDBHelper().getRawForStream(StudentVO.class,
                "select * from t_student where name like :name", MapUtils.of("name", prefix + "%"));
        list3 = stream2.collect(Collectors.toList());
        assert list3.size() == size;
        for (StudentVO s : list3) {
            assert ListUtils.filter(students, o -> o.getId().equals(s.getId())).size() == 1;
            assert s.getSchoolDO().getName().equals(schoolDO.getName());
        }
        stream2.close();

        // 测试没有参数的
        Stream<StudentVO> stream3 = getDBHelper().getRawForStream(StudentVO.class, "select * from t_student");
        List<StudentVO> list4 = stream3.collect(Collectors.toList());
        assert list4.size() == getDBHelper().getRawOne(Integer.class, "select count(*) from t_student");
        stream3.close();

        // 测试空map参数的
        stream3 = getDBHelper().getRawForStream(StudentVO.class, "select * from t_student", new HashMap<>());
        list4 = stream3.collect(Collectors.toList());
        assert list4.size() == getDBHelper().getRawOne(Integer.class, "select count(*) from t_student");
        stream3.close();
    }

    @Test
    public void testGetRawStreamAsMap() {
        getDBHelper().delete(StudentHardDeleteDO.class, "where 1=1");

        getDBHelper().setFetchSize(5);

        int size = 9;
        String prefix = UUID.randomUUID().toString().replace("-", "");
        List<StudentDO> students = CommonOps.insertBatch(getDBHelper(), size, prefix);

        // 测试 getRawForStreamAsMap(String sql, Object... args)
        try (Stream<java.util.Map<String, Object>> stream = getDBHelper().getRawForStreamAsMap(
                "select * from t_student where name like ?", prefix + "%")) {
            List<java.util.Map<String, Object>> list = stream.collect(Collectors.toList());
            assert list.size() == size;
            for (java.util.Map<String, Object> map : list) {
                assert map.get("name") != null;
                String name = map.get("name").toString();
                assert name.startsWith(prefix);
            }
        }

        // 测试 getRawForStreamAsMap(String sql, Map<String, ?> args)
        try (Stream<java.util.Map<String, Object>> stream = getDBHelper().getRawForStreamAsMap(
                "select * from t_student where name like :name", MapUtils.of("name", prefix + "%"))) {
            List<java.util.Map<String, Object>> list = stream.collect(Collectors.toList());
            assert list.size() == size;
            for (java.util.Map<String, Object> map : list) {
                assert map.get("name") != null;
                String name = map.get("name").toString();
                assert name.startsWith(prefix);
            }
        }

        // 测试没有参数的
        try (Stream<java.util.Map<String, Object>> stream = getDBHelper().getRawForStreamAsMap(
                "select * from t_student")) {
            List<java.util.Map<String, Object>> list = stream.collect(Collectors.toList());
            assert list.size() == getDBHelper().getRawOne(Integer.class, "select count(*) from t_student");
        }

        // 测试空map参数的
        try (Stream<java.util.Map<String, Object>> stream = getDBHelper().getRawForStreamAsMap(
                "select * from t_student", new HashMap<>())) {
            List<java.util.Map<String, Object>> list = stream.collect(Collectors.toList());
            assert list.size() == getDBHelper().getRawOne(Integer.class, "select count(*) from t_student");
        }
    }

}
