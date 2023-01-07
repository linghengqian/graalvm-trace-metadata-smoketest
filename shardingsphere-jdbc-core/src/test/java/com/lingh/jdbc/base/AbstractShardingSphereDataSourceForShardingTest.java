package com.lingh.jdbc.base;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractShardingSphereDataSourceForShardingTest extends AbstractSQLTest {
    
    private static ShardingSphereDataSource dataSource;
    
    private static final List<String> ACTUAL_DATA_SOURCE_NAMES = Arrays.asList("jdbc_0", "jdbc_1", "single_jdbc");
    
    private static final String CONFIG_FILE = "config/config-sharding.yaml";
    
    @BeforeClass
    public static void initShardingSphereDataSource() throws SQLException, IOException {
        if (null == dataSource) {
            dataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(getDataSourceMap(), getFile());
        }
    }
    
    private static Map<String, DataSource> getDataSourceMap() {
        return getActualDataSources().entrySet().stream().filter(entry -> ACTUAL_DATA_SOURCE_NAMES.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private static File getFile() {
        return new File(Objects.requireNonNull(
                AbstractShardingSphereDataSourceForShardingTest.class.getClassLoader().getResource(CONFIG_FILE), String.format("File `%s` is not existed.", CONFIG_FILE)).getFile());
    }
    
    @Before
    public void initTable() {
        try {
            Connection connection = dataSource.getConnection();
            RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("sql/jdbc_data.sql"))));
            connection.close();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected final ShardingSphereDataSource getShardingSphereDataSource() {
        return dataSource;
    }
    
    @AfterClass
    public static void clear() throws Exception {
        if (null == dataSource) {
            return;
        }
        dataSource.close();
        dataSource = null;
    }
}
