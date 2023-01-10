package org.apache.shardingsphere.elasticjob.lite.internal.setup;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;

public final class JobClassNameProviderFactoryTest {
    
    @Test
    public void assertGetDefaultStrategy() {
        MatcherAssert.assertThat(JobClassNameProviderFactory.getProvider(), instanceOf(DefaultJobClassNameProvider.class));
    }
}
