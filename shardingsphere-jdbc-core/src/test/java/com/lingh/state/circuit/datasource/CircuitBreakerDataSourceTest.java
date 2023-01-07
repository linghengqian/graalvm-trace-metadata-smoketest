package com.lingh.state.circuit.datasource;

import org.apache.shardingsphere.driver.state.circuit.connection.CircuitBreakerConnection;
import org.apache.shardingsphere.driver.state.circuit.datasource.CircuitBreakerDataSource;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class CircuitBreakerDataSourceTest {
    private final CircuitBreakerDataSource dataSource = new CircuitBreakerDataSource();
    
    @Test
    public void assertClose() {
        dataSource.close();
    }
    
    @Test
    public void assertGetConnection() {
        assertThat(dataSource.getConnection(), instanceOf(CircuitBreakerConnection.class));
        assertThat(dataSource.getConnection("", ""), instanceOf(CircuitBreakerConnection.class));
    }
    
    @Test
    public void assertGetLoginTimeout() {
        assertThat(dataSource.getLoginTimeout(), is(0));
    }
    
    @Test
    public void assertSetLoginTimeout() {
        dataSource.setLoginTimeout(10);
        assertThat(dataSource.getLoginTimeout(), is(0));
    }
}
