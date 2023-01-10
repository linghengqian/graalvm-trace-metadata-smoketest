package com.lingh.api.listener.fixture;

import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;

public final class TestDistributeOnceElasticJobListener extends AbstractDistributeOnceElasticJobListener {
    private final ElasticJobListenerCaller caller;
    
    public TestDistributeOnceElasticJobListener() {
        this(null);
    }
    
    public TestDistributeOnceElasticJobListener(final ElasticJobListenerCaller caller) {
        super(1L, 1L);
        this.caller = caller;
    }
    
    @Override
    public void doBeforeJobExecutedAtLastStarted(final ShardingContexts shardingContexts) {
        caller.before();
    }
    
    @Override
    public void doAfterJobExecutedAtLastCompleted(final ShardingContexts shardingContexts) {
        caller.after();
    }
    
    @Override
    public String getType() {
        return "DISTRIBUTE";
    }
}
