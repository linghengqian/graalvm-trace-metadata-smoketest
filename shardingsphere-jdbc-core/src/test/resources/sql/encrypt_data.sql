DELETE FROM t_encrypt;
DELETE FROM t_query_encrypt;
DELETE FROM t_encrypt_contains_column;

INSERT INTO t_encrypt VALUES(1, 'plainValue');
INSERT INTO t_encrypt VALUES(5, 'plainValue');
INSERT INTO t_query_encrypt VALUES(1, 'plainValue');
INSERT INTO t_query_encrypt VALUES(5, 'plainValue');
INSERT INTO t_encrypt_contains_column VALUES(1, 'plainValue', 'plainValue');
