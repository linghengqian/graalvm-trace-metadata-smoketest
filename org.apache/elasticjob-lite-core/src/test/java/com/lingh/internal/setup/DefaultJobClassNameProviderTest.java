package com.lingh.internal.setup;

import com.lingh.fixture.job.DetailedFooJob;
import com.lingh.fixture.job.FooJob;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.DefaultJobClassNameProvider;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.JobClassNameProvider;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DefaultJobClassNameProviderTest {
    @Test
    public void assertGetOrdinaryClassJobName() {
        JobClassNameProvider jobClassNameProvider = new DefaultJobClassNameProvider();
        String result = jobClassNameProvider.getJobClassName(new DetailedFooJob());
        assertThat(result, is(DetailedFooJob.class.getName()));
    }

    @Test
    public void assertGetLambdaJobName() {
        JobClassNameProvider jobClassNameProvider = new DefaultJobClassNameProvider();
        FooJob lambdaFooJob = shardingContext -> {
        };
        String result = jobClassNameProvider.getJobClassName(lambdaFooJob);
        assertThat(result, is("com.lingh.internal.setup.DefaultJobClassNameProviderTest$$Lambda$"));
    }
}
