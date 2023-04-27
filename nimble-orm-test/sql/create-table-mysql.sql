DROP TABLE IF EXISTS `t_student`;
CREATE TABLE `t_student` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `delete_time` datetime DEFAULT NULL,
  `name` varchar(128) DEFAULT 'DEFAULT',
  `age` tinyint(1) DEFAULT NULL,
  `intro` blob,
  `school_id` int(11) DEFAULT NULL,
  `school_snapshot` varchar(1024) DEFAULT NULL,
  `course_snapshot` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_student_del`;
CREATE TABLE `t_student_del` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `deleted` int(11) DEFAULT NULL,
   `create_time` datetime DEFAULT NULL,
   `update_time` datetime DEFAULT NULL,
   `delete_time` datetime DEFAULT NULL,
   `name` varchar(128) DEFAULT 'DEFAULT',
   `age` tinyint(1) DEFAULT NULL,
   `intro` blob,
   `school_id` int(11) DEFAULT NULL,
   `school_snapshot` varchar(1024) DEFAULT NULL,
   `course_snapshot` varchar(1024) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_school`;
CREATE TABLE `t_school` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `delete_time` datetime DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_course`;
CREATE TABLE `t_course` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `delete_time` datetime DEFAULT NULL,
  `student_id` int(11) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL COMMENT '课程名称',
  `is_main` tinyint(4) DEFAULT '0' COMMENT '是否主课程',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_json`;
CREATE TABLE `t_json` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `json` json DEFAULT NULL,
  `json2` json default null,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_json_raw`;
CREATE TABLE `t_json_raw` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `json` varchar(4096) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_cas_version`;
CREATE TABLE `t_cas_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_types`;
CREATE TABLE `t_types` (
  `id1` bigint(20) NOT NULL,
  `id2` bigint(20) NOT NULL,
  `my_byte` tinyint(4) DEFAULT NULL,
  `my_short` int(11) DEFAULT NULL,
  `my_float` float DEFAULT NULL,
  `my_double` double DEFAULT NULL,
  `my_decimal` decimal(10,2) DEFAULT NULL,
  `my_date` date DEFAULT NULL,
  `my_datetime` time DEFAULT NULL,
  `my_timestamp` timestamp NULL DEFAULT NULL,
  `my_mediumint` MEDIUMINT DEFAULT NULL,
  PRIMARY KEY (`id1`,`id2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_area`;
CREATE TABLE `t_area` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `layer_code` varchar(20) DEFAULT NULL,
  `area_code` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_area_location`;
CREATE TABLE `t_area_location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `layer_code` varchar(20) DEFAULT NULL,
  `area_code` varchar(20) DEFAULT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_uuid`;
CREATE TABLE `t_uuid` (
  `uuid` VARCHAR (64) NOT NULL,
  `name` VARCHAR (64),
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;