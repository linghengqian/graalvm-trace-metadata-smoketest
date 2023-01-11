package com.lingh.internal.sharding;

import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingNode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class ShardingNodeTest {

    private final ShardingNode shardingNode = new ShardingNode("test_job");

    @Test
    public void assertGetRunningNode() {
        assertThat(ShardingNode.getRunningNode(0), is("sharding/0/running"));
    }

    @Test
    public void assertGetItemWhenNotRunningItemPath() {
        assertNull(shardingNode.getItemByRunningItemPath("/test_job/sharding/0/completed"));
    }

    @Test
    public void assertGetItemByRunningItemPath() {
        assertThat(shardingNode.getItemByRunningItemPath("/test_job/sharding/0/running"), is(0));
    }
}
