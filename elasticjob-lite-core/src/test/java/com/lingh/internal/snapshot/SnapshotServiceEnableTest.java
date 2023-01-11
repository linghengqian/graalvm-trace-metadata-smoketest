package com.lingh.internal.snapshot;

import com.lingh.fixture.job.DetailedFooJob;
import org.apache.shardingsphere.elasticjob.lite.internal.snapshot.SnapshotService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class SnapshotServiceEnableTest extends BaseSnapshotServiceTest {
    public SnapshotServiceEnableTest() {
        super(new DetailedFooJob());
    }

    @BeforeEach
    public void listenMonitor() {
        getSnapshotService().listen();
    }

    @AfterEach
    public void closeMonitor() {
        getSnapshotService().close();
    }

    @Test
    public void assertMonitorWithCommand() throws IOException {
        assertNotNull(SocketUtils.sendCommand(SnapshotService.DUMP_COMMAND + getJobName(), DUMP_PORT));
        assertNull(SocketUtils.sendCommand("unknown_command", DUMP_PORT));
    }
}
