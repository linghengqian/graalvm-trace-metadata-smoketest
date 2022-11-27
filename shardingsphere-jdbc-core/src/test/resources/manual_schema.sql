DROP DATABASE IF EXISTS demo_ds;
DROP DATABASE IF EXISTS demo_ds_0;
DROP DATABASE IF EXISTS demo_ds_1;

DROP DATABASE IF EXISTS demo_write_ds;
DROP DATABASE IF EXISTS demo_read_ds_0;
DROP DATABASE IF EXISTS demo_read_ds_1;

DROP DATABASE IF EXISTS demo_write_ds_0;
DROP DATABASE IF EXISTS demo_write_ds_0_read_0;
DROP DATABASE IF EXISTS demo_write_ds_0_read_1;
DROP DATABASE IF EXISTS demo_write_ds_1;
DROP DATABASE IF EXISTS demo_write_ds_1_read_0;
DROP DATABASE IF EXISTS demo_write_ds_1_read_1;

DROP DATABASE IF EXISTS shadow_demo_ds;
DROP DATABASE IF EXISTS shadow_demo_ds_0;
DROP DATABASE IF EXISTS shadow_demo_ds_1;

DROP DATABASE IF EXISTS demo_shadow_write_ds;
DROP DATABASE IF EXISTS demo_shadow_read_ds;
DROP DATABASE IF EXISTS demo_read_ds;

CREATE DATABASE demo_ds;
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;

CREATE DATABASE demo_write_ds;
CREATE DATABASE demo_read_ds_0;
CREATE DATABASE demo_read_ds_1;

CREATE DATABASE demo_write_ds_0;
CREATE DATABASE demo_write_ds_0_read_0;
CREATE DATABASE demo_write_ds_0_read_1;
CREATE DATABASE demo_write_ds_1;
CREATE DATABASE demo_write_ds_1_read_0;
CREATE DATABASE demo_write_ds_1_read_1;

CREATE DATABASE shadow_demo_ds;
CREATE DATABASE shadow_demo_ds_0;
CREATE DATABASE shadow_demo_ds_1;

CREATE DATABASE demo_shadow_write_ds;
CREATE DATABASE demo_shadow_read_ds;
CREATE DATABASE demo_read_ds;


-- Execute SQL Script through logical-database connection , eg: sharding_db. Docker samples configuration in the 'conf/config-sharding.yaml' file
-- CREATE TABLE t_order ("order_id" serial4, "user_id" int4 NOT NULL, PRIMARY KEY ("order_id"));
-- CREATE TABLE t_order_item ("order_item_id" serial4, "order_id" int4 NOT NULL, "user_id" int4 NOT NULL, "status" varchar(50), PRIMARY KEY ("order_item_id"));
