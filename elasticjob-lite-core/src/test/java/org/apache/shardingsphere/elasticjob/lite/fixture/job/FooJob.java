package org.apache.shardingsphere.elasticjob.lite.fixture.job;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

public interface FooJob extends ElasticJob {
    void foo(ShardingContext shardingContext);
}
