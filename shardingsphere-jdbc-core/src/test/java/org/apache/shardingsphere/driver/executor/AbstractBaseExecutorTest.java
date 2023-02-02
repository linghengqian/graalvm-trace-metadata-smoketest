

package org.apache.shardingsphere.driver.executor;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.context.JDBCContext;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractBaseExecutorTest {
    
    private ExecutorEngine executorEngine;
    
    private ShardingSphereConnection connection;
    
    @Before
    public void setUp() throws SQLException {
        SQLExecutorExceptionHandler.setExceptionThrown(true);
        executorEngine = ExecutorEngine.createExecutorEngineWithCPU();
        TransactionTypeHolder.set(TransactionType.LOCAL);
        connection = new ShardingSphereConnection(DefaultDatabase.LOGIC_NAME, mockContextManager(), mock(JDBCContext.class));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getDataSourceMap(DefaultDatabase.LOGIC_NAME)).thenReturn(mockDataSourceMap());
        return result;
    }
    
    private Map<String, DataSource> mockDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        result.put("ds_0", dataSource);
        result.put("ds_1", dataSource);
        return result;
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(Arrays.asList(mockTransactionRule(), new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build())));
        when(result.getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME).getResourceMetaData().getStorageTypes())
                .thenReturn(Collections.singletonMap("ds_0", TypedSPILoader.getService(DatabaseType.class, "H2")));
        ShardingSphereRuleMetaData databaseRuleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(mockShardingRule()));
        when(result.getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME).getRuleMetaData()).thenReturn(databaseRuleMetaData);
        return result;
    }
    
    private TransactionRule mockTransactionRule() {
        TransactionRule result = mock(TransactionRule.class);
        when(result.getResource()).thenReturn(new ShardingSphereTransactionManagerEngine());
        return result;
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.isNeedAccumulate(any())).thenReturn(true);
        return result;
    }
    
    @After
    public void tearDown() {
        executorEngine.close();
        TransactionTypeHolder.clear();
    }
}
