

package com.lingh.jdbc.base;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractShardingSphereDataSourceForEncryptTest extends AbstractSQLTest {
    
    private static ShardingSphereDataSource queryWithPlainDataSource;
    
    private static ShardingSphereDataSource queryWithCipherDataSource;
    
    private static final List<String> ACTUAL_DATA_SOURCE_NAMES = Collections.singletonList("encrypt");
    
    private static final String CONFIG_FILE_WITH_QUERY_WITH_PLAIN = "config/config-encrypt-query-with-plain.yaml";
    
    private static final String CONFIG_FILE_WITH_QUERY_WITH_CIPHER = "config/config-encrypt-query-with-cipher.yaml";
    
    @BeforeClass
    public static void initEncryptDataSource() throws SQLException, IOException {
        if (null != queryWithPlainDataSource && null != queryWithCipherDataSource) {
            return;
        }
        DataSource dataSource = getDataSourceMap().values().iterator().next();
        queryWithPlainDataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(dataSource, getFile(CONFIG_FILE_WITH_QUERY_WITH_CIPHER));
        queryWithCipherDataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(dataSource, getFile(CONFIG_FILE_WITH_QUERY_WITH_PLAIN));
    }
    
    private static File getFile(final String fileName) {
        return new File(Objects.requireNonNull(
                AbstractShardingSphereDataSourceForEncryptTest.class.getClassLoader().getResource(fileName), String.format("File `%s` is not existed.", fileName)).getFile());
    }
    
    private static Map<String, DataSource> getDataSourceMap() {
        return getActualDataSources().entrySet().stream().filter(entry -> ACTUAL_DATA_SOURCE_NAMES.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    @Before
    public void initTable() {
        try (Connection connection = queryWithPlainDataSource.getConnection()) {
            RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("sql/encrypt_data.sql"))));
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected final Connection getEncryptConnection() {
        return queryWithPlainDataSource.getConnection();
    }
    
    protected final ShardingSphereConnection getEncryptConnectionWithProps() {
        return (ShardingSphereConnection) queryWithCipherDataSource.getConnection();
    }
    
    @AfterClass
    public static void close() throws Exception {
        if (null == queryWithPlainDataSource) {
            return;
        }
        queryWithPlainDataSource.close();
        queryWithPlainDataSource = null;
        if (null == queryWithCipherDataSource) {
            return;
        }
        queryWithCipherDataSource.close();
        queryWithCipherDataSource = null;
    }
}
