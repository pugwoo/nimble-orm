DROP TABLE IF EXISTS `t_student`;

CREATE TABLE `t_student` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `deleted` TINYINT(4) DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `delete_time` DATETIME DEFAULT NULL,
  `name` VARCHAR(128) DEFAULT 'DEFAULT',
  `age` TINYINT(1) DEFAULT NULL,
  `intro` BLOB,
  `school_id` INT(11) DEFAULT NULL,
  `school_snapshot` VARCHAR(1024) DEFAULT NULL,
  `course_snapshot` VARCHAR(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8

DROP TABLE IF EXISTS `t_school`;

CREATE TABLE `t_school` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `delete_time` DATETIME DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_course`;

CREATE TABLE `t_course` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `delete_time` DATETIME DEFAULT NULL,
  `student_id` int(11) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL COMMENT '课程名称',
  `is_main` tinyint(4) DEFAULT '0' COMMENT '是否主课程',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_json`;

CREATE TABLE `t_json` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `json` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;