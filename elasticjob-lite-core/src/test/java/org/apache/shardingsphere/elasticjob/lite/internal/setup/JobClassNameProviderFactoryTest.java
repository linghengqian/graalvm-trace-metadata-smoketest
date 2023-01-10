package org.apache.shardingsphere.elasticjob.lite.internal.setup;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class JobClassNameProviderFactoryTest {
    
    @Test
    public void assertGetDefaultStrategy() {
        assertThat(JobClassNameProviderFactory.getProvider(), instanceOf(DefaultJobClassNameProvider.class));
    }
}
