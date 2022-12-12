create database nimbleorm;

CREATE TABLE nimbleorm.t_student (
                           id Int64,
                           deleted UInt8,
                           create_time DateTime,
                           update_time DateTime,
                           delete_time DateTime,
                           name String,
                           age UInt8,
                           intro String,
                           school_id Int64,
                           school_snapshot String,
                           course_snapshot String
) ENGINE=MergeTree ORDER BY(id);

