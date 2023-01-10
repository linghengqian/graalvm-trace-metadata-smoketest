package org.apache.shardingsphere.elasticjob.lite.internal.setup;

import org.apache.shardingsphere.elasticjob.lite.fixture.job.DetailedFooJob;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.FooJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DefaultJobClassNameProviderTest {
    
    @Test
    public void assertGetOrdinaryClassJobName() {
        JobClassNameProvider jobClassNameProvider = new DefaultJobClassNameProvider();
        String result = jobClassNameProvider.getJobClassName(new DetailedFooJob());
        assertThat(result, is("org.apache.shardingsphere.elasticjob.lite.fixture.job.DetailedFooJob"));
    }
    
    @Test
    public void assertGetLambdaJobName() {
        JobClassNameProvider jobClassNameProvider = new DefaultJobClassNameProvider();
        FooJob lambdaFooJob = shardingContext -> { };
        String result = jobClassNameProvider.getJobClassName(lambdaFooJob);
        assertThat(result, is("org.apache.shardingsphere.elasticjob.lite.internal.setup.DefaultJobClassNameProviderTest$$Lambda$"));
    }
}
