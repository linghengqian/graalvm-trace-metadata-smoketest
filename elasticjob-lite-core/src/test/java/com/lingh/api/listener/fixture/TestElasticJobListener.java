package com.lingh.api.listener.fixture;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;

@RequiredArgsConstructor
public final class TestElasticJobListener implements ElasticJobListener {
    private final ElasticJobListenerCaller caller;
    private final String name;
    private final int order;
    private final StringBuilder orderResult;

    public TestElasticJobListener() {
        this(null, null, 0, new StringBuilder());
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
