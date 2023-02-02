

package org.apache.shardingsphere.driver.jdbc.adapter;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public final class AbstractDataSourceAdapterTest {
    
    private ShardingSphereDataSource shardingSphereDataSource;
    
    @Before
    public void setUp() throws SQLException {
        shardingSphereDataSource = new ShardingSphereDataSource(DefaultDatabase.LOGIC_NAME, null, Collections.singletonMap("ds", new MockedDataSource()), getRuleConfigurations(), new Properties());
    }
    
    private Collection<RuleConfiguration> getRuleConfigurations() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("table", "ds" + "." + "table");
        shardingRuleConfig.setTables(Collections.singletonList(shardingTableRuleConfig));
        return Collections.singleton(shardingRuleConfig);
    }
    
    @Test
    public void assertSetLogWriter() {
        assertThat(shardingSphereDataSource.getLogWriter(), instanceOf(PrintWriter.class));
        shardingSphereDataSource.setLogWriter(null);
        assertNull(shardingSphereDataSource.getLogWriter());
    }
    
    @Test
    public void assertGetParentLogger() {
        assertThat(shardingSphereDataSource.getParentLogger().getName(), is(Logger.GLOBAL_LOGGER_NAME));
    }
}
