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

CREATE TABLE t_student_del (
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

CREATE TABLE t_school (
      id SERIAL PRIMARY KEY,
      deleted boolean,
      create_time timestamp,
      update_time timestamp,
      delete_time timestamp,
      name varchar(32)
);

CREATE TABLE t_course (
      id SERIAL PRIMARY KEY,
      deleted boolean,
      create_time timestamp,
      update_time timestamp,
      delete_time timestamp,
      student_id integer,
      name varchar(32),
      is_main boolean DEFAULT false
);