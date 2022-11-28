-- Execute SQL Script through logical-database connection , eg: sharding_db. Docker samples configuration in the 'conf/config-sharding.yaml' file
CREATE TABLE t_order
(
    "order_id" serial4,
    "user_id"  int4 NOT NULL,
    PRIMARY KEY ("order_id")
);
CREATE TABLE t_order_item
(
    "order_item_id" serial4,
    "order_id"      int4 NOT NULL,
    "user_id"       int4 NOT NULL,
    "status"        varchar(50),
    PRIMARY KEY ("order_item_id")
);