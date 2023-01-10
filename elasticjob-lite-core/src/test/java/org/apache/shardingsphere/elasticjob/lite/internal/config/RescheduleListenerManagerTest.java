package org.apache.shardingsphere.elasticjob.lite.internal.config;

import com.lingh.fixture.LiteYamlConstants;
import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class RescheduleListenerManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private final RescheduleListenerManager rescheduleListenerManager = new RescheduleListenerManager(null, "test_job");
    
    @Before
    public void setUp() {
        ReflectionUtils.setSuperclassFieldValue(rescheduleListenerManager, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        rescheduleListenerManager.start();
        verify(jobNodeStorage).addDataListener(ArgumentMatchers.<RescheduleListenerManager.CronSettingAndJobEventChangedJobListener>any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsNotCronPath() {
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.ADDED, "/test_job/config/other", LiteYamlConstants.getJobYaml()));
        verify(jobScheduleController, times(0)).rescheduleJob(any(), any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathButNotUpdate() {
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.ADDED, "/test_job/config", LiteYamlConstants.getJobYaml()));
        verify(jobScheduleController, times(0)).rescheduleJob(any(), any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathAndUpdateButCannotFindJob() {
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.UPDATED, "/test_job/config", LiteYamlConstants.getJobYaml()));
        verify(jobScheduleController, times(0)).rescheduleJob(any(), any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathAndUpdateAndFindJob() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.UPDATED, "/test_job/config", LiteYamlConstants.getJobYaml()));
        verify(jobScheduleController).rescheduleJob("0/1 * * * * ?", null);
        JobRegistry.getInstance().shutdown("test_job");
    }
}
