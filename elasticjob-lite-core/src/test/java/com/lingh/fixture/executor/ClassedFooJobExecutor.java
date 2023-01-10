package com.lingh.fixture.executor;

import com.lingh.fixture.job.FooJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.executor.item.impl.ClassedJobItemExecutor;

public final class ClassedFooJobExecutor implements ClassedJobItemExecutor<FooJob> {
    
    @Override
    public void process(final FooJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        elasticJob.foo(shardingContext);
    }
    
    @Override
    public Class<FooJob> getElasticJobClass() {
        return FooJob.class;
    }
}
