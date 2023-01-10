package org.apache.shardingsphere.elasticjob.lite.integrate.listener;

import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;

public class TestDistributeOnceElasticJobListener extends AbstractDistributeOnceElasticJobListener {
    public TestDistributeOnceElasticJobListener() {
        super(100L, 100L);
    }
    
    @Override
    public void doBeforeJobExecutedAtLastStarted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public void doAfterJobExecutedAtLastCompleted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public String getType() {
        return "INTEGRATE-DISTRIBUTE";
    }
}
