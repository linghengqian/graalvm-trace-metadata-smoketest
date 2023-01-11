package com.lingh.internal.setup;

import org.apache.shardingsphere.elasticjob.lite.internal.setup.DefaultJobClassNameProvider;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.JobClassNameProviderFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;

public final class JobClassNameProviderFactoryTest {
    @Test
    public void assertGetDefaultStrategy() {
        MatcherAssert.assertThat(JobClassNameProviderFactory.getProvider(), instanceOf(DefaultJobClassNameProvider.class));
    }
}
