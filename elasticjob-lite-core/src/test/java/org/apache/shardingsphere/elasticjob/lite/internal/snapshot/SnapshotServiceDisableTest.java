package org.apache.shardingsphere.elasticjob.lite.internal.snapshot;

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.DetailedFooJob;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;

import static org.junit.Assert.assertNull;

public final class SnapshotServiceDisableTest extends BaseSnapshotServiceTest {
    
    public SnapshotServiceDisableTest() {
        super(new DetailedFooJob());
    }
    
    @Test(expected = IOException.class)
    public void assertMonitorWithDumpCommand() throws IOException {
        SocketUtils.sendCommand(SnapshotService.DUMP_COMMAND, DUMP_PORT - 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertPortInvalid() {
        SnapshotService snapshotService = new SnapshotService(getREG_CENTER(), -1);
        snapshotService.listen();
    }
    
    @Test
    @SneakyThrows
    public void assertListenException() {
        ServerSocket serverSocket = new ServerSocket(9898);
        SnapshotService snapshotService = new SnapshotService(getREG_CENTER(), 9898);
        snapshotService.listen();
        serverSocket.close();
        Field field = snapshotService.getClass().getDeclaredField("serverSocket");
        field.setAccessible(true);
        assertNull(field.get(snapshotService));
    }
}
