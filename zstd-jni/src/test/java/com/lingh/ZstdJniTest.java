package com.lingh;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdDictCompress;
import com.github.luben.zstd.ZstdDictDecompress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;


public class ZstdJniTest {
    @Test
    @EnabledOnOs(OS.LINUX)
    void testCompressAndDecompress() throws IOException {
        Path path = Paths.get("src/test/resources/test.txt");
        byte[] bytes = new String(Files.readAllBytes(path)).getBytes(StandardCharsets.UTF_8);
        byte[] dictBytes = new String(Files.readAllBytes(path)).getBytes(StandardCharsets.UTF_8);
        ZstdDictCompress compressDict = new ZstdDictCompress(dictBytes, 10);
        ZstdDictDecompress decompressDict = new ZstdDictDecompress(dictBytes);
        assertThat(bytes.length).isEqualTo(26);
        long compressBeginTime = System.currentTimeMillis();
        byte[] compressed = Zstd.compress(bytes, compressDict);
        long compressEndTime = System.currentTimeMillis();
        assertThat(compressEndTime - compressBeginTime).isLessThan(100);
        assertThat(compressed.length).isEqualTo(15);
        long decompressBeginTime = System.currentTimeMillis();
        byte[] decompressed = Zstd.decompress(compressed, decompressDict, 1000000);
        long decompressEndTime = System.currentTimeMillis();
        assertThat(decompressEndTime - decompressBeginTime).isLessThan(100);
        assertThat(decompressed.length).isEqualTo(26);

    }
}
