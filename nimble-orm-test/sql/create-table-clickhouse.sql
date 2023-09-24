create database nimbleorm;

CREATE TABLE nimbleorm.t_student (
                           id Int64,
                           deleted Nullable(UInt8),
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
                                         deleted Nullable(UInt8),
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
