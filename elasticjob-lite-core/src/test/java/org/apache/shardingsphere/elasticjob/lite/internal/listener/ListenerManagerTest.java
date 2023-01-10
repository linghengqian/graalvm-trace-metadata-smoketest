package org.apache.shardingsphere.elasticjob.lite.internal.listener;

import org.apache.shardingsphere.elasticjob.lite.internal.config.RescheduleListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.ShutdownListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.MonitorExecutionListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.internal.trigger.TriggerListenerManager;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ElectionListenerManager electionListenerManager;
    
    @Mock
    private ShardingListenerManager shardingListenerManager;
    
    @Mock
    private FailoverListenerManager failoverListenerManager;
    
    @Mock
    private MonitorExecutionListenerManager monitorExecutionListenerManager;
    
    @Mock
    private ShutdownListenerManager shutdownListenerManager;
    
    @Mock
    private TriggerListenerManager triggerListenerManager;
    
    @Mock
    private RescheduleListenerManager rescheduleListenerManager;
    
    @Mock
    private GuaranteeListenerManager guaranteeListenerManager;
    
    @Mock
    private RegistryCenterConnectionStateListener regCenterConnectionStateListener;
    
    private final ListenerManager listenerManager = new ListenerManager(null, "test_job", Collections.emptyList());
    
    @Before
    public void setUp() {
        ReflectionUtils.setFieldValue(listenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(listenerManager, "electionListenerManager", electionListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "shardingListenerManager", shardingListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "failoverListenerManager", failoverListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "monitorExecutionListenerManager", monitorExecutionListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "shutdownListenerManager", shutdownListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "triggerListenerManager", triggerListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "rescheduleListenerManager", rescheduleListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "guaranteeListenerManager", guaranteeListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "regCenterConnectionStateListener", regCenterConnectionStateListener);
    }
    
    @Test
    public void assertStartAllListeners() {
        listenerManager.startAllListeners();
        verify(electionListenerManager).start();
        verify(shardingListenerManager).start();
        verify(failoverListenerManager).start();
        verify(monitorExecutionListenerManager).start();
        verify(shutdownListenerManager).start();
        verify(rescheduleListenerManager).start();
        verify(guaranteeListenerManager).start();
        verify(jobNodeStorage).addConnectionStateListener(regCenterConnectionStateListener);
    }
}
