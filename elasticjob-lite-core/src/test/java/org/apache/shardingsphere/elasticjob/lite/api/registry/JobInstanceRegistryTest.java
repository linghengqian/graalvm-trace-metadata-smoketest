package org.apache.shardingsphere.elasticjob.lite.api.registry;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class JobInstanceRegistryTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Test
    public void assertListenWithoutConfigPath() {
        JobInstanceRegistry jobInstanceRegistry = new JobInstanceRegistry(regCenter, new JobInstance("id"));
        jobInstanceRegistry.new JobInstanceRegistryListener().onChange(new DataChangedEvent(Type.ADDED, "/jobName", ""));
        verify(regCenter, times(0)).get("/jobName");
    }
    
    @Test
    public void assertListenLabelNotMatch() {
        JobInstanceRegistry jobInstanceRegistry = new JobInstanceRegistry(regCenter, new JobInstance("id", "label1,label2"));
        String jobConfig = toYaml(JobConfiguration.newBuilder("jobName", 1).label("label").build());
        jobInstanceRegistry.new JobInstanceRegistryListener().onChange(new DataChangedEvent(Type.ADDED, "/jobName/config", jobConfig));
        verify(regCenter, times(0)).get("/jobName");
    }
    
    @Test(expected = RuntimeException.class)
    public void assertListenScheduleJob() {
        JobInstanceRegistry jobInstanceRegistry = new JobInstanceRegistry(regCenter, new JobInstance("id"));
        String jobConfig = toYaml(JobConfiguration.newBuilder("jobName", 1).cron("0/1 * * * * ?").label("label").build());
        jobInstanceRegistry.new JobInstanceRegistryListener().onChange(new DataChangedEvent(Type.ADDED, "/jobName/config", jobConfig));
    }
    
    @Test(expected = RuntimeException.class)
    public void assertListenOneOffJob() {
        JobInstanceRegistry jobInstanceRegistry = new JobInstanceRegistry(regCenter, new JobInstance("id", "label"));
        String jobConfig = toYaml(JobConfiguration.newBuilder("jobName", 1).label("label").build());
        jobInstanceRegistry.new JobInstanceRegistryListener().onChange(new DataChangedEvent(Type.ADDED, "/jobName/config", jobConfig));
    }
    
    private String toYaml(final JobConfiguration build) {
        return YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(build));
    }
}
