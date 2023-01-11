package com.lingh.api.listener.fixture;

import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;

public final class TestElasticJobListener implements ElasticJobListener {
    private final ElasticJobListenerCaller caller;
    private final String name;
    private final int order;
    private final StringBuilder orderResult;

    public TestElasticJobListener() {
        this(null, null, 0, new StringBuilder());
    }

    public TestElasticJobListener(ElasticJobListenerCaller caller, String name, int order, StringBuilder orderResult) {
        this.caller = caller;
        this.name = name;
        this.order = order;
        this.orderResult = orderResult;
    }

    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
        caller.before();
        orderResult.append(name);
    }
    
    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
        caller.after();
        orderResult.append(name);
    }
    
    @Override
    public String getType() {
        return "TEST";
    }

    @Override
    public int order() {
        return order;
    }
}
