

package org.apache.shardingsphere.driver.jdbc.base;

import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public abstract class AbstractSQLTest {
    
    private static final List<String> ACTUAL_DATA_SOURCE_NAMES = Arrays.asList("jdbc_0", "jdbc_1", "single_jdbc", "shadow_jdbc_0", "shadow_jdbc_1", "encrypt", "test_primary_ds", "test_replica_ds");
    
    private static final Map<String, DataSource> ACTUAL_DATA_SOURCES = new HashMap<>();
    
    @BeforeClass
    public static synchronized void initializeDataSource() throws SQLException {
        for (String each : ACTUAL_DATA_SOURCE_NAMES) {
            createDataSources(each);
        }
    }
    
    private static void createDataSources(final String dataSourceName) throws SQLException {
        ACTUAL_DATA_SOURCES.put(dataSourceName, buildDataSource(dataSourceName));
        initializeSchema(dataSourceName);
    }
    
    private static void initializeSchema(final String dataSourceName) throws SQLException {
        try (Connection connection = ACTUAL_DATA_SOURCES.get(dataSourceName).getConnection()) {
            if ("encrypt".equals(dataSourceName)) {
                RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("sql/jdbc_encrypt_init.sql"))));
            } else if ("shadow_jdbc_0".equals(dataSourceName) || "shadow_jdbc_1".equals(dataSourceName)) {
                RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("sql/jdbc_shadow_init.sql"))));
            } else if ("single_jdbc".equals(dataSourceName)) {
                RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("sql/single_jdbc_init.sql"))));
            } else {
                RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("sql/jdbc_init.sql"))));
            }
        }
    }
    
    private static DataSource buildDataSource(final String dataSourceName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setJdbcUrl(String.format("jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL", dataSourceName));
        result.setUsername("sa");
        result.setPassword("");
        result.setMinimumIdle(0);
        result.setMaximumPoolSize(50);
        return result;
    }
    
    protected static Map<String, DataSource> getActualDataSources() {
        return ACTUAL_DATA_SOURCES;
    }
}
