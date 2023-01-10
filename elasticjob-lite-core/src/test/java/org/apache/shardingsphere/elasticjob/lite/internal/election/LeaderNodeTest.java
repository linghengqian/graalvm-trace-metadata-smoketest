package org.apache.shardingsphere.elasticjob.lite.internal.election;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class LeaderNodeTest {
    private final LeaderNode leaderNode = new LeaderNode("test_job");

    @Test
    public void assertIsLeaderInstancePath() {
        assertTrue(leaderNode.isLeaderInstancePath("/test_job/leader/election/instance"));
    }

    @Test
    public void assertIsNotLeaderInstancePath() {
        assertFalse(leaderNode.isLeaderInstancePath("/test_job/leader/election/instance1"));
    }
}
