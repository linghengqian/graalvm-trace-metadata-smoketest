package com.lingh.internal.snapshot;

import com.lingh.fixture.job.DetailedFooJob;
import org.apache.shardingsphere.elasticjob.lite.internal.snapshot.SnapshotService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SnapshotServiceDisableTest extends BaseSnapshotServiceTest {

    public SnapshotServiceDisableTest() {
        super(new DetailedFooJob());
    }

    @Test
    public void assertMonitorWithDumpCommand() {
        assertThrows(IOException.class, () -> SocketUtils.sendCommand(SnapshotService.DUMP_COMMAND, DUMP_PORT - 1));
    }

    @Test
    public void assertPortInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            SnapshotService snapshotService = new SnapshotService(getREG_CENTER(), -1);
            snapshotService.listen();
        });
    }

    @Test
    public void assertListenException() {
        try {
            ServerSocket serverSocket = new ServerSocket(9898);
            SnapshotService snapshotService = new SnapshotService(getREG_CENTER(), 9898);
            snapshotService.listen();
            serverSocket.close();
            Field field = snapshotService.getClass().getDeclaredField("serverSocket");
            field.setAccessible(true);
            assertNull(field.get(snapshotService));
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
