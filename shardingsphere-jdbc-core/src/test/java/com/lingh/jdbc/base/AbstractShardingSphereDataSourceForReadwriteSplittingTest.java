

package com.lingh.jdbc.base;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractShardingSphereDataSourceForReadwriteSplittingTest extends AbstractSQLTest {
    
    private static ShardingSphereDataSource dataSource;
    
    private static final String CONFIG_FILE = "config/config-readwrite-splitting.yaml";
    
    private static final List<String> ACTUAL_DATA_SOURCE_NAMES = Arrays.asList("test_primary_ds", "test_replica_ds");
    
    @BeforeClass
    public static void initReadwriteSplittingDataSources() throws SQLException, IOException {
        if (null == dataSource) {
            dataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(getDataSourceMap(), getFile());
        }
    }
    
    private static Map<String, DataSource> getDataSourceMap() {
        return getActualDataSources().entrySet().stream().filter(entry -> ACTUAL_DATA_SOURCE_NAMES.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private static File getFile() {
        return new File(Objects.requireNonNull(
                AbstractShardingSphereDataSourceForReadwriteSplittingTest.class.getClassLoader().getResource(CONFIG_FILE), String.format("File `%s` is not existed.", CONFIG_FILE)).getFile());
    }
    
    protected final ShardingSphereDataSource getReadwriteSplittingDataSource() {
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
