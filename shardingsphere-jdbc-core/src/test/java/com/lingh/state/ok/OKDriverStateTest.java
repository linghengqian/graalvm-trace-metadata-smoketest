package com.lingh.state.ok;

import org.apache.shardingsphere.driver.jdbc.context.JDBCContext;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.state.ok.OKDriverState;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.After;
import org.junit.Test;

import java.sql.Connection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OKDriverStateTest {
    
    @Test
    public void assertGetConnection() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(globalRuleMetaData.getSingleRule(TransactionRule.class)).thenReturn(mock(TransactionRule.class, RETURNS_DEEP_STUBS));
        when(globalRuleMetaData.getSingleRule(TrafficRule.class)).thenReturn(mock(TrafficRule.class));
        Connection actual = new OKDriverState().getConnection(DefaultDatabase.LOGIC_NAME, contextManager, mock(JDBCContext.class));
        assertThat(actual, instanceOf(ShardingSphereConnection.class));
    }
    
    @After
    public void tearDown() {
        TransactionTypeHolder.clear();
    }
}
