package com.lingh.impl;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.test.EtcdClusterExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

import static com.lingh.impl.TestUtil.bytesOf;
import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 30)
public class SslTest {
    @RegisterExtension
    public static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
            .withNodes(1)
            .withSsl(true)
            .build();
    private static final String DEFAULT_SSL_AUTHORITY = "etcd0";
    private static final String DEFAULT_SSL_CA_PATH = "/ssl/cert/ca.pem";

    @Test
    public void testSimpleSllSetup() throws Exception {
        final ByteSequence key = bytesOf(TestUtil.randomString());
        final ByteSequence val = bytesOf(TestUtil.randomString());
        final String capath = System.getProperty("ssl.cert.capath");
        final String authority = System.getProperty("ssl.cert.authority", DEFAULT_SSL_AUTHORITY);
        final URI endpoint = new URI(System.getProperty("ssl.cert.endpoints", cluster.clientEndpoints().get(0).toString()));
        try (InputStream is = Objects.nonNull(capath)
                ? new FileInputStream(capath)
                : getClass().getResourceAsStream(DEFAULT_SSL_CA_PATH)) {
            Client client = Client.builder().endpoints(endpoint).authority(authority).sslContext(b -> b.trustManager(is)).build();
            KV kv = client.getKVClient();
            kv.put(key, val).join();
            assertThat(kv.get(key).join().getCount()).isEqualTo(1);
            assertThat(kv.get(key).join().getKvs().get(0).getValue()).isEqualTo(val);
            kv.close();
            client.close();
        }
    }
}
