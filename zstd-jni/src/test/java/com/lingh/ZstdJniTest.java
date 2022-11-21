package com.lingh;

import com.github.luben.zstd.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class ZstdJniTest {
    @Test
    void testCompressAndDecompress() throws IOException {
        Path path = Paths.get("src/test/resources/originTest.txt");
        byte[] bytes = new String(Files.readAllBytes(path)).getBytes(StandardCharsets.UTF_8);
        assertThat(bytes.length).isEqualTo(26);
        byte[] dictBytes = new String(Files.readAllBytes(path)).getBytes(StandardCharsets.UTF_8);
        ZstdDictCompress compressDict = new ZstdDictCompress(dictBytes, 10);
        ZstdDictDecompress decompressDict = new ZstdDictDecompress(dictBytes);
        byte[] compressed = Zstd.compress(bytes, compressDict);
        assertThat(compressed.length).isEqualTo(15);
        byte[] decompressed = Zstd.decompress(compressed, decompressDict, 1000000);
        assertThat(decompressed.length).isEqualTo(26);
    }

    @Test
    void testCompress() throws IOException {
        File file = new File("src/test/resources/originTest.txt");
        File outFile = new File("src/test/resources/originTest.txt.zs");
        long numBytes = 0L;
        ByteBuffer inBuffer = ByteBuffer.allocateDirect(8 * 1024 * 1024);
        ByteBuffer compressedBuffer = ByteBuffer.allocateDirect(8 * 1024 * 1024);
        try (RandomAccessFile inRaFile = new RandomAccessFile(file, "r");//读取文件
             RandomAccessFile outRaFile = new RandomAccessFile(outFile, "rw");
             FileChannel inChannel = inRaFile.getChannel();
             FileChannel outChannel = outRaFile.getChannel()) {
            inBuffer.clear();
            while (inChannel.read(inBuffer) > 0) {
                inBuffer.flip();
                compressedBuffer.clear();
                long compressedSize = Zstd.compressDirectByteBuffer(compressedBuffer, 0, compressedBuffer.capacity(), inBuffer, 0, inBuffer.limit(), 10);
                numBytes = numBytes + compressedSize;
                compressedBuffer.position((int) compressedSize);
                compressedBuffer.flip();
                outChannel.write(compressedBuffer);
                inBuffer.clear();
            }
        }
        assertThat(numBytes).isEqualTo(35);
        assertDoesNotThrow(outFile::delete);
    }

    @Test
    void testDecompress() {
        File file = new File("src/test/resources/compressTest.zs");
        File out = new File("src/test/resources/DecompressTest.txt");
        byte[] buffer = new byte[1024 * 1024 * 8];
        try (FileOutputStream fo = new FileOutputStream(out);
             ZstdOutputStream zos = new ZstdOutputStream(fo);
             FileInputStream fi = new FileInputStream(file.getPath());
             ZstdInputStream zis = new ZstdInputStream(fi)) {
            while (true) {
                int count = zis.read(buffer, 0, buffer.length);
                if (count == -1) {
                    break;
                }
                zos.write(buffer, 0, count);
            }
            zos.flush();
        } catch (IOException ignore) {
        }
        assertDoesNotThrow(out::delete);
    }
}
