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

CREATE TABLE t_uuid (
    uuid VARCHAR(64) PRIMARY KEY NOT NULL,
    name VARCHAR(64)
);

CREATE TABLE t_json (
    id SERIAL PRIMARY KEY,
    json JSON,
    json2 JSON
);

CREATE TABLE t_cas_version (
   id SERIAL PRIMARY KEY,
   name VARCHAR(32),
   version INT
);

CREATE TABLE t_types (
     id1 bigint NOT NULL,
     id2 bigint NOT NULL,
     my_byte smallint DEFAULT NULL,
     my_short integer DEFAULT NULL,
     my_float real DEFAULT NULL,
     my_double double precision DEFAULT NULL,
     my_decimal numeric(10,2) DEFAULT NULL,
     my_date date DEFAULT NULL,
     my_datetime time DEFAULT NULL,
     my_timestamp timestamp DEFAULT NULL,
     my_mediumint integer DEFAULT NULL,
     PRIMARY KEY (id1, id2)
);

CREATE TABLE "t_json_raw" (
      "id" SERIAL PRIMARY KEY,
      "json" varchar(4096)
);

CREATE TABLE t_area (
    id bigserial PRIMARY KEY,
    layer_code varchar(20),
    area_code varchar(20)
);

CREATE TABLE t_area_location (
     id bigserial PRIMARY KEY,
     layer_code varchar(20),
     area_code varchar(20),
     longitude decimal(10,6),
     latitude decimal(10,6)
);
