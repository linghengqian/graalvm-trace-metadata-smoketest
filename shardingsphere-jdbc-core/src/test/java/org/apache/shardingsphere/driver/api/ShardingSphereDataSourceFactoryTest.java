

package org.apache.shardingsphere.driver.api;

import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShardingSphereDataSourceFactoryTest {
    
    @Test
    public void assertCreateDataSourceWithModeConfiguration() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource(new ModeConfiguration("Standalone", null)), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    public void assertCreateDataSourceWithDatabaseNameAndModeConfiguration() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource("test_db", new ModeConfiguration("Standalone", null), new HashMap<>(), null, null), "test_db");
    }
    
    @Test
    public void assertCreateDataSourceWithAllParametersForMultipleDataSourcesWithDefaultDatabaseName() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource(
                new ModeConfiguration("Standalone", null), new HashMap<>(), new LinkedList<>(), new Properties()), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    public void assertCreateDataSourceWithAllParametersForMultipleDataSources() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource(
                "test_db", new ModeConfiguration("Standalone", null), new HashMap<>(), new LinkedList<>(), new Properties()), "test_db");
    }
    
    @Test
    public void assertCreateDataSourceWithAllParametersForSingleDataSourceWithDefaultDatabaseName() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource(
                new ModeConfiguration("Standalone", null), new MockedDataSource(), new LinkedList<>(), new Properties()), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    public void assertCreateDataSourceWithAllParametersForSingleDataSource() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource("test_db",
                new ModeConfiguration("Standalone", null), new MockedDataSource(), new LinkedList<>(), new Properties()), "test_db");
    }
    
    @Test
    public void assertCreateDataSourceWithDefaultModeConfigurationForMultipleDataSources() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource(null), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    public void assertCreateDataSourceWithDatabaseNameAndDefaultModeConfigurationForMultipleDataSources() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource("test_db", null), "test_db");
    }
    
    @Test
    public void assertCreateDataSourceWithDefaultModeConfigurationForSingleDataSource() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource((ModeConfiguration) null, new MockedDataSource(), new LinkedList<>(), new Properties()), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    public void assertCreateDataSourceWithDatabaseNameAndDefaultModeConfigurationForSingleDataSource() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource("test_db", null, new MockedDataSource(), new LinkedList<>(), new Properties()), "test_db");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void assertDataSource(final DataSource actualDataSource, final String expectedDataSourceName) {
        assertThat(Plugins.getMemberAccessor().get(ShardingSphereDataSource.class.getDeclaredField("databaseName"), actualDataSource), is(expectedDataSourceName));
    }
}
