package org.apache.shardingsphere.elasticjob.lite.internal.guarantee;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class GuaranteeNodeTest {
    private final GuaranteeNode guaranteeNode = new GuaranteeNode("test_job");
    
    @Test
    public void assertGetStartedNode() {
        assertThat(GuaranteeNode.getStartedNode(1), is("guarantee/started/1"));
    }
    
    @Test
    public void assertGetCompletedNode() {
        assertThat(GuaranteeNode.getCompletedNode(1), is("guarantee/completed/1"));
    }
    
    @Test
    public void assertIsStartedRootNode() {
        assertTrue(guaranteeNode.isStartedRootNode("/test_job/guarantee/started"));
    }
    
    @Test
    public void assertIsNotStartedRootNode() {
        assertFalse(guaranteeNode.isStartedRootNode("/otherJob/guarantee/started"));
    }
    
    @Test
    public void assertIsCompletedRootNode() {
        assertTrue(guaranteeNode.isCompletedRootNode("/test_job/guarantee/completed"));
    }
    
    @Test
    public void assertIsNotCompletedRootNode() {
        assertFalse(guaranteeNode.isCompletedRootNode("/otherJob/guarantee/completed"));
    }
}
