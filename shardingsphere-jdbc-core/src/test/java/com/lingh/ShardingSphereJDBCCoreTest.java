package com.lingh;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class ShardingSphereJDBCCoreTest {

    @Test   // TODO
    void test1() throws SQLException, IOException {
        System.out.println("hello,world");
//        File yamlFile = new File("src/test/resources");
//        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
    }
}
