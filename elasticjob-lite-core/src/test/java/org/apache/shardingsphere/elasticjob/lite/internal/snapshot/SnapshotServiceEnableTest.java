package org.apache.shardingsphere.elasticjob.lite.internal.snapshot;

import com.lingh.fixture.job.DetailedFooJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public final class SnapshotServiceEnableTest extends BaseSnapshotServiceTest {
    
    public SnapshotServiceEnableTest() {
        super(new DetailedFooJob());
    }
    
    @Before
    public void listenMonitor() {
        getSnapshotService().listen();
    }
    
    @After
    public void closeMonitor() {
        getSnapshotService().close();
    }
    
    @Test
    public void assertMonitorWithCommand() throws IOException {
        assertNotNull(SocketUtils.sendCommand(SnapshotService.DUMP_COMMAND + getJobName(), DUMP_PORT));
        assertNull(SocketUtils.sendCommand("unknown_command", DUMP_PORT));
    }
}
