package com.lingh.internal.storage;

import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class JobNodePathTest {
    
    private final JobNodePath jobNodePath = new JobNodePath("test_job");
    
    @Test
    public void assertGetFullPath() {
        assertThat(jobNodePath.getFullPath("node"), is("/test_job/node"));
    }
    
    @Test
    public void assertGetServerNodePath() {
        assertThat(jobNodePath.getServerNodePath(), is("/test_job/servers"));
    }
    
    @Test
    public void assertGetServerNodePathForServerIp() {
        assertThat(jobNodePath.getServerNodePath("ip0"), is("/test_job/servers/ip0"));
    }
    
    @Test
    public void assertGetShardingNodePath() {
        assertThat(jobNodePath.getShardingNodePath(), is("/test_job/sharding"));
    }
    
    @Test
    public void assertGetShardingNodePathWihItemAndNode() {
        assertThat(jobNodePath.getShardingNodePath("0", "running"), is("/test_job/sharding/0/running"));
    }
    
    @Test
    public void assertGetLeaderIpNodePath() {
        assertThat(jobNodePath.getLeaderHostNodePath(), is("/test_job/leader/election/instance"));
    }
}
