

package org.apache.shardingsphere.driver.jdbc.unsupported;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Properties;

import static org.mockito.Mockito.*;

public final class UnsupportedOperationStatementTest {
    
    private ShardingSphereStatement shardingSphereStatement;
    
    @Before
    public void setUp() {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getDatabaseName()).thenReturn("db");
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(
                Arrays.asList(new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build()),
                        new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build()),
                        new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()))));
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        shardingSphereStatement = new ShardingSphereStatement(connection);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAddBatch() throws SQLException {
        shardingSphereStatement.addBatch("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertClearBatch() throws SQLException {
        shardingSphereStatement.clearBatch();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertExecuteBatch() throws SQLException {
        shardingSphereStatement.executeBatch();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCloseOnCompletion() throws SQLException {
        shardingSphereStatement.closeOnCompletion();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsCloseOnCompletion() throws SQLException {
        shardingSphereStatement.isCloseOnCompletion();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetCursorName() throws SQLException {
        shardingSphereStatement.setCursorName("cursorName");
    }
}
