package com.lingh.fixture.job;

import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

public final class DetailedFooJob implements FooJob {
    
    private final Collection<Integer> completedJobItems = new CopyOnWriteArraySet<>();
    
    private volatile boolean completed;
    
    @Override
    public void foo(final ShardingContext shardingContext) {
        completedJobItems.add(shardingContext.getShardingItem());
        completed = completedJobItems.size() == shardingContext.getShardingTotalCount();
    }

    public boolean isCompleted() {
        return this.completed;
    }
}
