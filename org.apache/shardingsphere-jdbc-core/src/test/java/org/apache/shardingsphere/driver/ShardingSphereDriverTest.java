

package org.apache.shardingsphere.driver;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.junit.Test;

import java.sql.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingSphereDriverTest {
    
    @Test(expected = SQLException.class)
    public void assertConnectWithInvalidURL() throws SQLException {
        DriverManager.getConnection("jdbc:invalid:xxx");
    }
    
    @Test
    public void assertDriverWorks() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml");
                Statement statement = connection.createStatement()) {
            assertThat(connection, instanceOf(ShardingSphereConnection.class));
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT)");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 101), (2, 102)");
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(1) FROM t_order")) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(2));
            }
        }
    }
}
