

DELETE FROM t_order;
DELETE FROM t_order_item;
DELETE FROM t_order_auto;
DELETE FROM t_order_item_auto;
DELETE FROM t_config;

INSERT INTO t_order VALUES(1000, 10, 'init');
INSERT INTO t_order VALUES(1001, 10, 'init');
INSERT INTO t_order VALUES(1100, 11, 'init');
INSERT INTO t_order VALUES(1101, 11, 'init');
INSERT INTO t_order_item VALUES(100000, 1000, 10, 'init');
INSERT INTO t_order_item VALUES(100001, 1000, 10, 'init');
INSERT INTO t_order_item VALUES(100100, 1001, 10, 'init');
INSERT INTO t_order_item VALUES(100101, 1001, 10, 'init');
INSERT INTO t_order_item VALUES(110000, 1100, 11, 'init');
INSERT INTO t_order_item VALUES(110001, 1100, 11, 'init');
INSERT INTO t_order_item VALUES(110100, 1101, 11, 'init');
INSERT INTO t_order_item VALUES(110101, 1101, 11, 'init');

INSERT INTO t_order_auto VALUES(1000, 10, 'init');
INSERT INTO t_order_auto VALUES(1100, 11, 'init');
INSERT INTO t_order_item_auto VALUES(100000, 1000, 10, 'init');
INSERT INTO t_order_item_auto VALUES(100100, 1001, 10, 'init');
INSERT INTO t_order_item_auto VALUES(110000, 1100, 11, 'init');
INSERT INTO t_order_item_auto VALUES(110100, 1101, 11, 'init');

INSERT INTO t_config VALUES(1, 'init');
