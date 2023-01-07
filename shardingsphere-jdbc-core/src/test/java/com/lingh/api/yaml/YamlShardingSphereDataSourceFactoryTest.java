package com.lingh.api.yaml;

import com.lingh.test.fixture.jdbc.MockedDataSource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class YamlShardingSphereDataSourceFactoryTest {
    
    @Test
    public void assertCreateDataSourceWithFile() throws Exception {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new File(getYamlFileUrl().toURI())));
    }
    
    @Test
    public void assertCreateDataSourceWithBytes() throws SQLException, IOException {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(readFile(getYamlFileUrl()).getBytes()));
    }
    
    @Test
    public void assertCreateDataSourceWithFileForExternalDataSources() throws Exception {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, new File(getYamlFileUrl().toURI())));
    }
    
    @Test
    public void assertCreateDataSourceWithFileForExternalSingleDataSource() throws Exception {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), new File(getYamlFileUrl().toURI())));
    }
    
    @Test
    public void assertCreateDataSourceWithBytesForExternalDataSources() throws Exception {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, readFile(getYamlFileUrl()).getBytes()));
    }
    
    @Test
    public void assertCreateDataSourceWithBytesForExternalSingleDataSource() throws Exception {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), readFile(getYamlFileUrl()).getBytes()));
    }
    
    private URL getYamlFileUrl() {
        return Objects.requireNonNull(YamlShardingSphereDataSourceFactoryTest.class.getResource("/config/factory/config-for-factory-test.yaml"));
    }
    
    private String readFile(final URL url) throws IOException {
        StringBuilder result = new StringBuilder();
        try (
                FileReader fileReader = new FileReader(url.getFile());
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                result.append(line).append(System.lineSeparator());
            }
        }
        return result.toString();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void assertDataSource(final DataSource dataSource) {
        assertThat(Plugins.getMemberAccessor().get(ShardingSphereDataSource.class.getDeclaredField("databaseName"), dataSource), is("logic_db"));
    }
}
