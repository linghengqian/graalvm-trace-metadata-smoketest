package com.lingh.jdbc.unsupported;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UnsupportedOperationPreparedStatementTest {
    
    private ShardingSpherePreparedStatement shardingSpherePreparedStatement;
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    private final TrafficRule trafficRule = new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build());
    
    private final SQLFederationRule sqlFederationRule = new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build());
    
    @Before
    public void setUp() throws SQLException {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(globalRuleMetaData.getSingleRule(SQLParserRule.class)).thenReturn(sqlParserRule);
        when(globalRuleMetaData.getSingleRule(TrafficRule.class)).thenReturn(trafficRule);
        when(globalRuleMetaData.getSingleRule(SQLFederationRule.class)).thenReturn(sqlFederationRule);
        shardingSpherePreparedStatement = new ShardingSpherePreparedStatement(connection, "SELECT 1");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetMetaData() throws SQLException {
        shardingSpherePreparedStatement.getMetaData();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNString() throws SQLException {
        shardingSpherePreparedStatement.setNString(1, "");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClob() throws SQLException {
        shardingSpherePreparedStatement.setNClob(1, (NClob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClobForReader() throws SQLException {
        shardingSpherePreparedStatement.setNClob(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClobForReaderAndLength() throws SQLException {
        shardingSpherePreparedStatement.setNClob(1, new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNCharacterStream() throws SQLException {
        shardingSpherePreparedStatement.setNCharacterStream(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNCharacterStreamWithLength() throws SQLException {
        shardingSpherePreparedStatement.setNCharacterStream(1, new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetRowId() throws SQLException {
        shardingSpherePreparedStatement.setRowId(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetRef() throws SQLException {
        shardingSpherePreparedStatement.setRef(1, null);
    }
}
