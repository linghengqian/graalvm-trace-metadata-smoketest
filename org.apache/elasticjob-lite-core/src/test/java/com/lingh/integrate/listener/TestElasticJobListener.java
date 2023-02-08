package com.lingh.integrate.listener;

import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;

public class TestElasticJobListener implements ElasticJobListener {
    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public String getType() {
        return "INTEGRATE-TEST";
    }
}
