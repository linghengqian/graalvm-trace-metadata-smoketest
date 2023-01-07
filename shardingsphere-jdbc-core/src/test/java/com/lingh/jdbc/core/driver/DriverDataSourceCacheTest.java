

package com.lingh.jdbc.core.driver;

import org.apache.shardingsphere.driver.jdbc.core.driver.DriverDataSourceCache;
import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class DriverDataSourceCacheTest {
    
    @Test
    public void assertGetNewDataSource() {
        DriverDataSourceCache dataSourceCache = new DriverDataSourceCache();
        DataSource fooDataSource = dataSourceCache.get("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml");
        DataSource barDataSource = dataSourceCache.get("jdbc:shardingsphere:classpath:config/driver/bar-driver-fixture.yaml");
        assertThat(fooDataSource, not(barDataSource));
    }
    
    @Test
    public void assertGetExistedDataSource() {
        DriverDataSourceCache dataSourceCache = new DriverDataSourceCache();
        DataSource dataSource1 = dataSourceCache.get("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml");
        DataSource dataSource2 = dataSourceCache.get("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml");
        assertThat(dataSource1, is(dataSource2));
    }
}
