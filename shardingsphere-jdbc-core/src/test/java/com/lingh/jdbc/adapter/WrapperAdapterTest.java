package com.lingh.jdbc.adapter;

import com.lingh.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class WrapperAdapterTest {
    
    private ShardingSphereDataSource wrapperAdapter;
    
    @Before
    public void setUp() throws SQLException {
        wrapperAdapter = new ShardingSphereDataSource(
                DefaultDatabase.LOGIC_NAME, null, Collections.singletonMap("ds", new MockedDataSource()), Collections.singletonList(mock(RuleConfiguration.class)), new Properties());
    }
    
    @Test
    public void assertUnwrapSuccess() throws SQLException {
        assertThat(wrapperAdapter.unwrap(Object.class), is(wrapperAdapter));
    }
    
    @Test(expected = SQLException.class)
    public void assertUnwrapFailure() throws SQLException {
        wrapperAdapter.unwrap(String.class);
    }
    
    @Test
    public void assertIsWrapperFor() {
        assertTrue(wrapperAdapter.isWrapperFor(Object.class));
    }
}
