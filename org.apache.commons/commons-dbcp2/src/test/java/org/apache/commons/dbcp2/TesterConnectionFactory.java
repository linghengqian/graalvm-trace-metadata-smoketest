

package org.apache.commons.dbcp2;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Dummy {@link ConnectionFactory} for testing purpose.
 */
public class TesterConnectionFactory implements ConnectionFactory {

    private final String connectionString;
    private final Driver driver;
    private final Properties properties;

    /**
     * Constructs a connection factory for a given Driver.
     *
     * @param driver        The Driver.
     * @param connectString The connection string.
     * @param properties    The connection properties.
     */
    public TesterConnectionFactory(final Driver driver, final String connectString, final Properties properties) {
        this.driver = driver;
        this.connectionString = connectString;
        this.properties = properties;
    }

    @Override
    public Connection createConnection() throws SQLException {
        final Connection conn = driver.connect(connectionString, properties);
        doSomething(conn);
        return conn;
    }

    private void doSomething(final Connection conn) {
        // do something
    }

    /**
     * @return The connection String.
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * @return The Driver.
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * @return The Properties.
     */
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [" + driver + ";" + connectionString + ";" + properties + "]";
    }
}
