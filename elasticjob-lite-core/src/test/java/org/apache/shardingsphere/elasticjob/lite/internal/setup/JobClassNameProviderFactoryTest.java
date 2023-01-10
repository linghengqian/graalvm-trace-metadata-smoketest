package org.apache.shardingsphere.elasticjob.lite.internal.setup;

import org.apache.shardingsphere.elasticjob.lite.internal.setup.DefaultJobClassNameProvider;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.JobClassNameProviderFactory;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class JobClassNameProviderFactoryTest {
    
    @Test
    public void assertGetDefaultStrategy() {
        MatcherAssert.assertThat(JobClassNameProviderFactory.getProvider(), instanceOf(DefaultJobClassNameProvider.class));
    }
}
