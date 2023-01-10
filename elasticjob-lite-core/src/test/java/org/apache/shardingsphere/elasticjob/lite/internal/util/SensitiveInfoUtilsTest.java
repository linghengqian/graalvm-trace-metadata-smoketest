package org.apache.shardingsphere.elasticjob.lite.internal.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class SensitiveInfoUtilsTest {
    @Test
    public void assertFilterContentWithoutIp() {
        List<String> actual = Arrays.asList("/simpleElasticDemoJob/servers", "/simpleElasticDemoJob/leader");
        assertThat(SensitiveInfoUtils.filterSensitiveIps(actual), is(actual));
    }

    @Test
    public void assertFilterContentWithSensitiveIp() {
        List<String> actual = Arrays.asList("/simpleElasticDemoJob/servers/127.0.0.1", "/simpleElasticDemoJob/servers/192.168.0.1/hostName | 192.168.0.1",
                "/simpleElasticDemoJob/servers/192.168.0.11", "/simpleElasticDemoJob/servers/192.168.0.111");
        List<String> expected = Arrays.asList("/simpleElasticDemoJob/servers/ip1", "/simpleElasticDemoJob/servers/ip2/hostName | ip2",
                "/simpleElasticDemoJob/servers/ip3", "/simpleElasticDemoJob/servers/ip4");
        assertThat(SensitiveInfoUtils.filterSensitiveIps(actual), is(expected));
    }
}
