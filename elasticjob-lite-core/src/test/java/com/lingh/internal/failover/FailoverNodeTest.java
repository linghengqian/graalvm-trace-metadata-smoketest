package com.lingh.internal.failover;

import org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverNode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class FailoverNodeTest {
    private final FailoverNode failoverNode = new FailoverNode("test_job");

    @Test
    public void assertGetItemWhenNotExecutionFailoverPath() {
        assertNull(failoverNode.getItemByExecutionFailoverPath("/test_job/sharding/0/completed"));
    }

    @Test
    public void assertGetItemByExecutionFailoverPath() {
        assertThat(failoverNode.getItemByExecutionFailoverPath("/test_job/sharding/0/failover"), is(0));
    }
}
