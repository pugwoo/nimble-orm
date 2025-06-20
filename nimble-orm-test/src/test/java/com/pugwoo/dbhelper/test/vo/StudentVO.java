package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.FillColumn;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.CourseDO;
import com.pugwoo.dbhelper.test.entity.SchoolDO;
import com.pugwoo.dbhelper.test.entity.StudentDO;
import lombok.Data;

import java.util.List;

/**
 * 关联上schoolDO
 * @author NICK
 */
@Data
public class StudentVO extends StudentDO {

	/**特别说明：dbHelperBean可以不用指定，这里只是测试指定DBHelper；这里remoteColumn故意用大写，也是可以自动匹配上的*/
	@RelatedColumn(localColumn = "school_id", remoteColumn = "ID")
	private SchoolDO schoolDO;
	
	@RelatedColumn(localColumn = "id", remoteColumn = "student_id")
	private List<CourseDO> courses;

	/**localColumn/remoteColumn故意用大写*/
	@RelatedColumn(localColumn = "ID", remoteColumn = "Student_Id", extraWhere = "where is_main=true")
	private List<CourseDO> mainCourses;
	
	// 计算列示例，生成的select字段为：CONCAT(name,'hi') AS nameWithHi
	@Column(value = "nameWithHi", computed = "CONCAT(name,'hi')")
	private String nameWithHi;
	
	@FillColumn(refField = "school_id" /*或schoolId都可以*/, fillScript = "com.pugwoo.dbhelper.test.utils.FillColumnTestUtils.getSchoolNameById(refField)" /*这些refField写成refField1也可以*/)
	private String schoolName;

	// 这里演示依赖于另外一个@FillColmn的情况
	@FillColumn(refField = "schoolName", fillScript = "com.pugwoo.dbhelper.test.utils.FillColumnTestUtils.getSchoolGroupNameBySchoolName(refField1)" /*这些refField写成refField1也可以*/)
	private String schoolGroupName;

	// 这个是个示例，演示当多个列决定一个列的场景
	@FillColumn(refField = "class_id1,class_id2", fillScript = "com.pugwoo.dbhelper.test.utils.FillColumnTestUtils.getClassNameByIds(refField1, refField2)")
	private String className;
	
	/*第一个参数固定叫 refField 或 refField1，剩下的按顺序叫  refField2,  refField3, refField4 ..*/
}
