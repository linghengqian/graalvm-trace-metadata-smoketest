package com.lingh.internal.storage;

import com.lingh.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.ListenerNotifierManager;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.transaction.TransactionOperation;
import org.apache.shardingsphere.elasticjob.reg.exception.RegException;
import org.apache.shardingsphere.elasticjob.reg.listener.ConnectionStateChangedEventListener;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public final class JobNodeStorageTest {
    @Mock
    private CoordinatorRegistryCenter regCenter;

    private JobNodeStorage jobNodeStorage;

    @BeforeEach
    public void setUp() {
        jobNodeStorage = new JobNodeStorage(regCenter, "test_job");
        ReflectionUtils.setFieldValue(jobNodeStorage, "regCenter", regCenter);
    }

    @Test
    public void assertIsJobNodeExisted() {
        when(regCenter.isExisted("/test_job/config")).thenReturn(true);
        Assertions.assertTrue(jobNodeStorage.isJobNodeExisted("config"));
        verify(regCenter).isExisted("/test_job/config");
    }

    @Test
    public void assertGetJobNodeData() {
        when(regCenter.get("/test_job/config/cron")).thenReturn("0/1 * * * * ?");
        assertThat(jobNodeStorage.getJobNodeData("config/cron"), is("0/1 * * * * ?"));
        verify(regCenter).get("/test_job/config/cron");
    }

    @Test
    public void assertGetJobNodeDataDirectly() {
        when(regCenter.getDirectly("/test_job/config/cron")).thenReturn("0/1 * * * * ?");
        assertThat(jobNodeStorage.getJobNodeDataDirectly("config/cron"), is("0/1 * * * * ?"));
        verify(regCenter).getDirectly("/test_job/config/cron");
    }

    @Test
    public void assertGetJobNodeChildrenKeys() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("host0", "host1"));
        assertThat(jobNodeStorage.getJobNodeChildrenKeys("servers"), is(Arrays.asList("host0", "host1")));
        verify(regCenter).getChildrenKeys("/test_job/servers");
    }

    @Test
    public void assertCreateJobNodeIfNeeded() {
        when(regCenter.isExisted("/test_job")).thenReturn(true);
        when(regCenter.isExisted("/test_job/config")).thenReturn(false);
        jobNodeStorage.createJobNodeIfNeeded("config");
        verify(regCenter).isExisted("/test_job");
        verify(regCenter).isExisted("/test_job/config");
        verify(regCenter).persist("/test_job/config", "");
    }

    @Test
    public void assertCreateJobNodeIfRootJobNodeIsNotExist() {
        when(regCenter.isExisted("/test_job")).thenReturn(false);
        jobNodeStorage.createJobNodeIfNeeded("config");
        verify(regCenter).isExisted("/test_job");
        verify(regCenter, times(0)).isExisted("/test_job/config");
        verify(regCenter, times(0)).persist("/test_job/config", "");
    }

    @Test
    public void assertCreateJobNodeIfNotNeeded() {
        when(regCenter.isExisted("/test_job")).thenReturn(true);
        when(regCenter.isExisted("/test_job/config")).thenReturn(true);
        jobNodeStorage.createJobNodeIfNeeded("config");
        verify(regCenter).isExisted("/test_job");
        verify(regCenter).isExisted("/test_job/config");
        verify(regCenter, times(0)).persist("/test_job/config", "");
    }

    @Test
    public void assertRemoveJobNodeIfNeeded() {
        when(regCenter.isExisted("/test_job/config")).thenReturn(true);
        jobNodeStorage.removeJobNodeIfExisted("config");
        verify(regCenter).isExisted("/test_job/config");
        verify(regCenter).remove("/test_job/config");
    }

    @Test
    public void assertRemoveJobNodeIfNotNeeded() {
        when(regCenter.isExisted("/test_job/config")).thenReturn(false);
        jobNodeStorage.removeJobNodeIfExisted("config");
        verify(regCenter).isExisted("/test_job/config");
        verify(regCenter, times(0)).remove("/test_job/config");
    }

    @Test
    public void assertFillJobNode() {
        jobNodeStorage.fillJobNode("config/cron", "0/1 * * * * ?");
        verify(regCenter).persist("/test_job/config/cron", "0/1 * * * * ?");
    }

    @Test
    public void assertFillEphemeralJobNode() {
        jobNodeStorage.fillEphemeralJobNode("config/cron", "0/1 * * * * ?");
        verify(regCenter).persistEphemeral("/test_job/config/cron", "0/1 * * * * ?");
    }

    @Test
    public void assertUpdateJobNode() {
        jobNodeStorage.updateJobNode("config/cron", "0/1 * * * * ?");
        verify(regCenter).update("/test_job/config/cron", "0/1 * * * * ?");
    }

    @Test
    public void assertReplaceJobNode() {
        jobNodeStorage.replaceJobNode("config/cron", "0/1 * * * * ?");
        verify(regCenter).persist("/test_job/config/cron", "0/1 * * * * ?");
    }

    @Test
    public void assertExecuteInTransactionSuccess() throws Exception {
        jobNodeStorage.executeInTransaction(Collections.singletonList(TransactionOperation.opAdd("/test_transaction", "")));
        verify(regCenter).executeInTransaction(any(List.class));
    }

    @Test
    public void assertExecuteInTransactionFailure() throws Exception {
        doThrow(RuntimeException.class).when(regCenter).executeInTransaction(any(List.class));
        assertThrows(RegException.class,
                () -> jobNodeStorage.executeInTransaction(Collections.singletonList(TransactionOperation.opAdd("/test_transaction", ""))));
    }

    @Test
    public void assertAddConnectionStateListener() {
        ConnectionStateChangedEventListener listener = mock(ConnectionStateChangedEventListener.class);
        jobNodeStorage.addConnectionStateListener(listener);
        verify(regCenter).addConnectionStateChangedEventListener(listener);
    }

    @Test
    public void assertAddDataListener() {
        DataChangedEventListener listener = mock(DataChangedEventListener.class);
        String jobName = "test_job";
        ListenerNotifierManager.getInstance().registerJobNotifyExecutor(jobName);
        Executor executor = ListenerNotifierManager.getInstance().getJobNotifyExecutor(jobName);
        jobNodeStorage.addDataListener(listener);
        verify(regCenter).watch("/test_job", listener, executor);
    }

    @Test
    public void assertGetRegistryCenterTime() {
        when(regCenter.getRegistryCenterTime("/test_job/systemTime/current")).thenReturn(0L);
        assertThat(jobNodeStorage.getRegistryCenterTime(), is(0L));
        verify(regCenter).getRegistryCenterTime("/test_job/systemTime/current");
    }
}