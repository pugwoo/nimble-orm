create database nimbleorm;

CREATE TABLE nimbleorm.t_student (
     id Int64,
     deleted Nullable(Int64),
     create_time Nullable(DateTime),
     update_time Nullable(DateTime),
     delete_time Nullable(DateTime),
     name Nullable(String),
     age Nullable(UInt8),
     intro Nullable(String),
     school_id Nullable(Int64),
     school_snapshot Nullable(String),
     course_snapshot Nullable(String)
) ENGINE=MergeTree ORDER BY(id);

CREATE TABLE nimbleorm.t_student_del (
     id Int64,
     deleted Nullable(Int64),
     create_time Nullable(DateTime),
     update_time Nullable(DateTime),
     delete_time Nullable(DateTime),
     name Nullable(String),
     age Nullable(UInt8),
     intro Nullable(String),
     school_id Nullable(Int64),
     school_snapshot Nullable(String),
     course_snapshot Nullable(String)
) ENGINE=MergeTree ORDER BY(id);

create table nimbleorm.t_school (
    id Int64,
    deleted Nullable(UInt8),
    create_time Nullable(DateTime),
    update_time Nullable(DateTime),
    delete_time Nullable(DateTime),
    name Nullable(String)
) ENGINE=MergeTree ORDER BY(id);

CREATE TABLE `t_uuid` (
    `uuid` String,
    `name` Nullable(String)
) ENGINE=MergeTree ORDER BY(uuid);

CREATE TABLE `t_course` (
     id Int64,
     deleted Nullable(UInt8),
     create_time Nullable(DateTime),
     update_time Nullable(DateTime),
     delete_time Nullable(DateTime),
     student_id Nullable(Int64),
     name Nullable(String),
     is_main Nullable(UInt8)
) ENGINE=MergeTree ORDER BY(id);

CREATE TABLE `t_json` (
      id Int64,
      json Nullable(String),
      json2 Nullable(String)
) ENGINE=MergeTree ORDER BY(id);

CREATE TABLE `t_cas_version` (
     `id` Int64,
     `name` Nullable(String),
     `version` Nullable(Int32)
) ENGINE=MergeTree ORDER BY(id);

CREATE TABLE `t_types` (
       `id1` Int64,
       `id2` Int64,
       `my_byte` Nullable(Byte),
       `my_short` Nullable(Int16),
       `my_float` Nullable(Float32),
       `my_double` Nullable(Float64),
       `my_decimal` Nullable(DECIMAL(10, 2)),
       `my_date` Nullable(date),
       `my_datetime` Nullable(datetime32),
       `my_timestamp` Nullable(datetime64),
       `my_mediumint` Nullable(Int32)
) ENGINE=MergeTree ORDER BY(id1, id2);