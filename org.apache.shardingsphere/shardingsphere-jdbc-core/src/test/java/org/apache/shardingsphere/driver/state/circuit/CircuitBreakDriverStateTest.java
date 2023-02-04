

package org.apache.shardingsphere.driver.state.circuit;

import org.apache.shardingsphere.driver.jdbc.context.JDBCContext;
import org.apache.shardingsphere.driver.state.circuit.connection.CircuitBreakerConnection;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.Test;

import java.sql.Connection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class CircuitBreakDriverStateTest {
    
    @Test
    public void assertGetConnection() {
        Connection actual = new CircuitBreakDriverState().getConnection(DefaultDatabase.LOGIC_NAME, mock(ContextManager.class, RETURNS_DEEP_STUBS), mock(JDBCContext.class));
        assertThat(actual, instanceOf(CircuitBreakerConnection.class));
    }
}
