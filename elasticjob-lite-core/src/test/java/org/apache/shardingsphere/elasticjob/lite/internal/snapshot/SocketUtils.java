package org.apache.shardingsphere.elasticjob.lite.internal.snapshot;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SocketUtils {
    public static String sendCommand(final String command, final int dumpPort) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", dumpPort);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            writer.write(command);
            writer.newLine();
            writer.flush();
            return reader.readLine();
        }
    }
}
