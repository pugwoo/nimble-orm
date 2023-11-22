create schema nimbleorm;

CREATE TABLE t_student (
       id SERIAL PRIMARY KEY,
       deleted boolean,
       create_time timestamp,
       update_time timestamp,
       delete_time timestamp,
       name varchar(128) DEFAULT 'DEFAULT',
       age smallint,
       intro bytea,
       school_id integer,
       school_snapshot varchar(1024),
       course_snapshot varchar(1024)
);
