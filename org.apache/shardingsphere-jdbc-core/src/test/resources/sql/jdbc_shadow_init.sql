

CREATE TABLE IF NOT EXISTS t_order_0 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_1 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item_0 (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE TABLE IF NOT EXISTS t_order_item_1 (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));

CREATE TABLE IF NOT EXISTS t_order_auto_0 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_auto_1 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item_auto_0 (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));
CREATE TABLE IF NOT EXISTS t_order_item_auto_1 (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (item_id));

CREATE TABLE IF NOT EXISTS t_config (id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS t_user_0 (id INT NOT NULL, name VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS t_user_1 (id INT NOT NULL, name VARCHAR(45) NULL, PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS t_encrypt (id INT NOT NULL AUTO_INCREMENT, cipher_pwd VARCHAR(45) NULL, plain_pwd VARCHAR(45), PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS t_query_encrypt (id INT NOT NULL AUTO_INCREMENT, cipher_pwd VARCHAR(45) NULL, assist_pwd VARCHAR(45) NULL, PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS t_encrypt_contains_column (id INT NOT NULL AUTO_INCREMENT, cipher_pwd VARCHAR(45) NULL, plain_pwd VARCHAR(45), cipher_pwd2 VARCHAR(45) NULL, plain_pwd2 VARCHAR(45), PRIMARY KEY (id));
