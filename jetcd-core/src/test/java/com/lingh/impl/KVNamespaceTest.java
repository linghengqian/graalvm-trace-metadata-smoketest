package com.lingh.impl;

import com.google.protobuf.ByteString;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Txn;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.support.Util;
import io.etcd.jetcd.test.EtcdClusterExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.lingh.impl.TestUtil.byteStringOf;
import static com.lingh.impl.TestUtil.bytesOf;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("resource")
@Timeout(value = 30)
public class KVNamespaceTest {
    private static final ByteSequence END_KEY = ByteSequence.from(new byte[]{0});

    @RegisterExtension
    public static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
            .withNodes(3)
            .build();

    private KV kvClient;
    private KV kvClientWithNamespace;
    private KV kvClientWithNamespace2;

    private static final AtomicInteger keyIndex = new AtomicInteger(-1);

    @AfterEach
    public void cleanUpCase() {
        if (kvClient != null) {
            kvClient.close();
            kvClient = null;
        }
        if (kvClientWithNamespace != null) {
            kvClientWithNamespace.close();
            kvClientWithNamespace = null;
        }
        if (kvClientWithNamespace2 != null) {
            kvClientWithNamespace2.close();
            kvClientWithNamespace2 = null;
        }
    }

    static Stream<Arguments> namespaceProvider() {
        return Stream.of(
                Arguments.of(bytesOf("pfx/"), byteStringOf("a"), null, byteStringOf("pfx/a"), null),
                Arguments.of(bytesOf("pfx/"), byteStringOf("abc"), byteStringOf("def"), byteStringOf("pfx/abc"),
                        byteStringOf("pfx/def")),
                Arguments.of(bytesOf("pfx/"), byteStringOf("abc"), ByteString.copyFrom(new byte[]{0}),
                        byteStringOf("pfx/abc"), byteStringOf("pfx0")),
                Arguments.of(ByteSequence.from(new byte[]{(byte) 0xff, (byte) 0xff}), byteStringOf("abc"),
                        ByteString.copyFrom(new byte[]{0}),
                        ByteString.copyFrom(new byte[]{(byte) 0xff, (byte) 0xff, 'a', 'b', 'c'}),
                        ByteString.copyFrom(new byte[]{0})));
    }

    @ParameterizedTest
    @MethodSource("namespaceProvider")
    public void testPrefixNamespace(ByteSequence namespace, ByteString key, ByteString end, ByteString expectedNsKey,
                                    ByteString expectedNsEnd) {
        ByteString nsKey = Util.prefixNamespace(key, namespace);
        assertThat(nsKey).isEqualTo(expectedNsKey);
        if (end != null) {
            ByteString nsEnd = Util.prefixNamespaceToRangeEnd(end, namespace);
            assertThat(nsEnd).isEqualTo(expectedNsEnd);
        }
    }

    @Test
    public void testKV() throws Exception {
        kvClient = TestUtil.client(cluster).build().getKVClient();
        ByteSequence namespace = ByteSequence
                .from(TestUtil.randomByteSequence().concat(ByteSequence.NAMESPACE_DELIMITER).getBytes());
        kvClientWithNamespace = TestUtil.client(cluster).namespace(namespace).build().getKVClient();
        ByteSequence namespace2 = ByteSequence.from(TestUtil.randomByteSequence().concat(ByteSequence.NAMESPACE_DELIMITER).getBytes());
        kvClientWithNamespace2 = TestUtil.client(cluster).namespace(namespace2).build().getKVClient();
        {
            ByteSequence key = getNonexistentKey();
            ByteSequence nsKey = ByteSequence.from(namespace.concat(key).getBytes());
            ByteSequence value;
            assertNonexistentKey(kvClient, nsKey);
            assertNonexistentKey(kvClientWithNamespace, key);
            assertNonexistentKey(kvClientWithNamespace2, key);
            value = TestUtil.randomByteSequence();
            assertThat(putKVWithAssertion(kvClient, key, value, null)).isFalse();
            assertExistentKey(kvClient, key, value);
            assertNonexistentKey(kvClientWithNamespace, key);
            assertNonexistentKey(kvClientWithNamespace2, key);
            deleteKVWithAssertion(kvClient, key, value);
            value = TestUtil.randomByteSequence();
            assertThat(putKVWithAssertion(kvClient, nsKey, value, null)).isFalse();
            assertExistentKey(kvClient, nsKey, value);
            assertExistentKey(kvClientWithNamespace, key, value);
            assertNonexistentKey(kvClientWithNamespace2, key);
            ByteSequence prevValue = value;
            value = TestUtil.randomByteSequence();
            assertThat(putKVWithAssertion(kvClientWithNamespace, key, value, prevValue)).isTrue();
            assertExistentKey(kvClient, nsKey, value);
            assertExistentKey(kvClientWithNamespace, key, value);
            assertNonexistentKey(kvClientWithNamespace2, key);
            deleteKVWithAssertion(kvClientWithNamespace, key, value);
        }
        {
            List<TestKeyValue> kvsOfNoNamespace = Arrays.asList(
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()),
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()),
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()));
            putKVsWithAssertion(kvClient, kvsOfNoNamespace);
            for (TestKeyValue keyValue : kvsOfNoNamespace) {
                assertExistentKey(kvClient, keyValue.key, keyValue.value);
            }
            List<TestKeyValue> kvsOfNamespace = Arrays.asList(
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()),
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()),
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()),
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()));
            putKVsWithAssertion(kvClientWithNamespace, kvsOfNamespace);
            for (TestKeyValue keyValue : kvsOfNamespace) {
                assertExistentKey(kvClientWithNamespace, keyValue.key, keyValue.value);
            }
            List<TestKeyValue> kvsOfNamespace2 = Arrays.asList(
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()),
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()),
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()),
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()),
                    new TestKeyValue(getNonexistentKey(), TestUtil.randomByteSequence()));
            putKVsWithAssertion(kvClientWithNamespace2, kvsOfNamespace2);
            for (TestKeyValue keyValue : kvsOfNamespace2) {
                assertExistentKey(kvClientWithNamespace2, keyValue.key, keyValue.value);
            }
            assertExistentKVs(kvClient, kvsOfNoNamespace.get(0).key, END_KEY, kvsOfNoNamespace);
            assertExistentKVs(kvClientWithNamespace, kvsOfNamespace.get(0).key, END_KEY, kvsOfNamespace);
            assertExistentKVs(kvClientWithNamespace2, kvsOfNamespace2.get(0).key, END_KEY, kvsOfNamespace2);

            assertExistentKVs(kvClient, kvsOfNoNamespace.get(0).key, kvsOfNoNamespace.get(2).key,
                    kvsOfNoNamespace.subList(0, 2));
            assertExistentKVs(kvClientWithNamespace, kvsOfNamespace.get(1).key, kvsOfNamespace.get(3).key,
                    kvsOfNamespace.subList(1, 3));
            assertExistentKVs(kvClientWithNamespace2, kvsOfNamespace2.get(1).key, kvsOfNamespace2.get(3).key,
                    kvsOfNamespace2.subList(1, 3));
            deleteKVsWithAssertion(kvClient, kvsOfNoNamespace.get(0).key, kvsOfNoNamespace.get(2).key,
                    kvsOfNoNamespace.subList(0, 2));
            deleteKVsWithAssertion(kvClientWithNamespace, kvsOfNamespace.get(1).key, kvsOfNamespace.get(3).key,
                    kvsOfNamespace.subList(1, 3));
            deleteKVsWithAssertion(kvClientWithNamespace2, kvsOfNamespace2.get(1).key, kvsOfNamespace2.get(3).key,
                    kvsOfNamespace2.subList(1, 3));
            deleteKVsWithAssertion(kvClient, kvsOfNoNamespace.get(2).key, END_KEY, kvsOfNoNamespace.subList(2, 3));
            deleteKVsWithAssertion(kvClientWithNamespace, kvsOfNamespace.get(3).key, END_KEY, kvsOfNamespace.subList(3, 4));
            deleteKVsWithAssertion(kvClientWithNamespace2, kvsOfNamespace2.get(3).key, END_KEY, kvsOfNamespace2.subList(3, 5));
        }
    }

    @Test
    public void testTxn() throws Exception {
        kvClient = TestUtil.client(cluster).build().getKVClient();
        ByteSequence namespace = ByteSequence.from(TestUtil.randomByteSequence().concat(ByteSequence.NAMESPACE_DELIMITER).getBytes());
        kvClientWithNamespace = TestUtil.client(cluster).namespace(namespace).build().getKVClient();
        ByteSequence cmpKey = getNonexistentKey();
        putKVWithAssertion(kvClient, cmpKey, TestUtil.randomByteSequence(), null);
        ByteSequence key1 = getNonexistentKey();
        ByteSequence value1 = TestUtil.randomByteSequence();
        putKVWithAssertion(kvClientWithNamespace, key1, value1, null);
        ByteSequence key2 = getNonexistentKey();
        ByteSequence value2 = TestUtil.randomByteSequence();
        putKVWithAssertion(kvClientWithNamespace, key2, value2, null);
        {
            Txn txn = kvClientWithNamespace.txn();
            CompletableFuture<TxnResponse> txnFuture = txn
                    .If(new Cmp(cmpKey, Cmp.Op.EQUAL, CmpTarget.version(0)))
                    .Then(Op.put(key1, TestUtil.randomByteSequence(), PutOption.newBuilder().withPrevKV().build()))
                    .Else(Op.put(key2, TestUtil.randomByteSequence(), PutOption.newBuilder().withPrevKV().build())).commit();
            TxnResponse txnResponse = txnFuture.get();
            assertThat(txnResponse.getPutResponses().size()).isEqualTo(1);
            assertThat(txnResponse.getPutResponses().get(0).hasPrevKv()).isTrue();
            assertThat(txnResponse.getPutResponses().get(0).getPrevKv().getKey()).isEqualTo(key1);
            assertThat(txnResponse.getPutResponses().get(0).getPrevKv().getValue()).isEqualTo(value1);
        }

        {
            Txn txn = kvClientWithNamespace.txn();
            CompletableFuture<TxnResponse> txnFuture = txn
                    .If(new Cmp(key1, Cmp.Op.EQUAL, CmpTarget.version(0))).Then(Op.get(key1, GetOption.newBuilder().build()))
                    .Else(Op.get(key2, GetOption.newBuilder().build())).commit();
            TxnResponse txnResponse = txnFuture.get();
            assertThat(txnResponse.getGetResponses().size()).isEqualTo(1);
            assertThat(txnResponse.getGetResponses().get(0).getKvs().size()).isEqualTo(1);
            assertThat(txnResponse.getGetResponses().get(0).getKvs().get(0).getKey()).isEqualTo(key2);
            assertThat(txnResponse.getGetResponses().get(0).getKvs().get(0).getValue()).isEqualTo(value2);
        }
        {
            Txn txn = kvClientWithNamespace.txn();
            CompletableFuture<TxnResponse> txnFuture = txn
                    .If(new Cmp(key1, Cmp.Op.GREATER, CmpTarget.version(0)))
                    .Then(Op.delete(key2, DeleteOption.newBuilder().withPrevKV(true).build()))
                    .Else(Op.delete(key1, DeleteOption.newBuilder().withPrevKV(true).build())).commit();
            TxnResponse txnResponse = txnFuture.get();
            assertThat(txnResponse.getDeleteResponses().size()).isEqualTo(1);
            assertThat(txnResponse.getDeleteResponses().get(0).getPrevKvs().size()).isEqualTo(1);
            assertThat(txnResponse.getDeleteResponses().get(0).getPrevKvs().get(0).getKey()).isEqualTo(key2);
            assertThat(txnResponse.getDeleteResponses().get(0).getPrevKvs().get(0).getValue()).isEqualTo(value2);
        }
    }

    @Test
    public void testNestedTxn() throws Exception {
        kvClient = TestUtil.client(cluster).build().getKVClient();
        ByteSequence namespace = ByteSequence
                .from(TestUtil.randomByteSequence().concat(ByteSequence.NAMESPACE_DELIMITER).getBytes());
        kvClientWithNamespace = TestUtil.client(cluster).namespace(namespace).build()
                .getKVClient();
        ByteSequence cmpKey1 = getNonexistentKey();
        putKVWithAssertion(kvClient, cmpKey1, TestUtil.randomByteSequence(), null);
        ByteSequence cmpKey2 = getNonexistentKey();
        putKVWithAssertion(kvClientWithNamespace, cmpKey2, TestUtil.randomByteSequence(), null);
        ByteSequence key1 = getNonexistentKey();
        ByteSequence value1 = TestUtil.randomByteSequence();
        putKVWithAssertion(kvClientWithNamespace, key1, value1, null);
        ByteSequence key2 = getNonexistentKey();
        ByteSequence value2 = TestUtil.randomByteSequence();
        putKVWithAssertion(kvClientWithNamespace, key2, value2, null);
        {
            Txn txn = kvClientWithNamespace.txn();
            ByteSequence nextValue1 = TestUtil.randomByteSequence();
            CompletableFuture<TxnResponse> txnFuture = txn.If(new Cmp(cmpKey1, Cmp.Op.EQUAL, CmpTarget.version(0)))
                    .Then(Op.txn(new Cmp[]{new Cmp(cmpKey2, Cmp.Op.GREATER, CmpTarget.version(0))},
                            new Op[]{Op.put(key1, nextValue1, PutOption.newBuilder().withPrevKV().build())},
                            new Op[]{
                                    Op.put(key2, TestUtil.randomByteSequence(), PutOption.newBuilder().withPrevKV().build())}))
                    .Else(Op.txn(new Cmp[]{new Cmp(cmpKey2, Cmp.Op.GREATER, CmpTarget.version(0))},
                            new Op[]{
                                    Op.put(key2, TestUtil.randomByteSequence(), PutOption.newBuilder().withPrevKV().build())},
                            new Op[]{
                                    Op.put(key1, TestUtil.randomByteSequence(), PutOption.newBuilder().withPrevKV().build())}))
                    .commit();
            TxnResponse response = txnFuture.get();
            assertThat(response.getTxnResponses().size()).isEqualTo(1);
            assertThat(response.getTxnResponses().get(0).getPutResponses().size()).isEqualTo(1);
            assertThat(response.getTxnResponses().get(0).getPutResponses().get(0).hasPrevKv()).isTrue();
            assertThat(response.getTxnResponses().get(0).getPutResponses().get(0).getPrevKv().getKey()).isEqualTo(key1);
            assertThat(response.getTxnResponses().get(0).getPutResponses().get(0).getPrevKv().getValue()).isEqualTo(value1);
            value1 = nextValue1;
            assertExistentKey(kvClient, ByteSequence.from(namespace.concat(key1).getBytes()), value1);
        }
    }

    static class TestKeyValue {
        ByteSequence key;
        ByteSequence value;

        TestKeyValue(ByteSequence key, ByteSequence value) {
            this.key = key;
            this.value = value;
        }
    }

    private static ByteSequence getNonexistentKey() {
        return bytesOf("sample_key_" + String.format("%05d", keyIndex.incrementAndGet()));
    }

    private static void assertNonexistentKey(KV kvClient, ByteSequence key) throws Exception {
        CompletableFuture<GetResponse> getFeature = kvClient.get(key);
        GetResponse response = getFeature.get();
        assertThat(response.getKvs().size()).isEqualTo(0);
    }

    private static void assertExistentKey(KV kvClient, ByteSequence key, ByteSequence value) throws Exception {
        CompletableFuture<GetResponse> getFeature = kvClient.get(key);
        GetResponse response = getFeature.get();
        assertThat(response.getKvs().size()).isEqualTo(1);
        assertThat(response.getKvs().get(0).getKey()).isEqualTo(key);
        assertThat(response.getKvs().get(0).getValue()).isEqualTo(value);
    }

    private static boolean putKVWithAssertion(KV kvClient, ByteSequence key, ByteSequence value, ByteSequence prevValue)
            throws Exception {
        CompletableFuture<PutResponse> feature = kvClient.put(key, value, PutOption.newBuilder().withPrevKV().build());
        PutResponse response = feature.get();
        if (prevValue != null) {
            assertThat(response.hasPrevKv()).isTrue();
            assertThat(response.getPrevKv().getKey()).isEqualTo(key);
            assertThat(response.getPrevKv().getValue()).isEqualTo(prevValue);
        }
        return response.hasPrevKv();
    }

    private static void deleteKVWithAssertion(KV kvClient, ByteSequence key, ByteSequence prevValue) throws Exception {
        CompletableFuture<DeleteResponse> deleteFuture = kvClient.delete(key,
                DeleteOption.newBuilder().withPrevKV(true).build());
        DeleteResponse deleteResponse = deleteFuture.get();
        assertThat(deleteResponse.getDeleted()).isEqualTo(1);
        assertThat(deleteResponse.getPrevKvs().size()).isEqualTo(1);
        assertThat(deleteResponse.getPrevKvs().get(0).getKey()).isEqualTo(key);
        assertThat(deleteResponse.getPrevKvs().get(0).getValue()).isEqualTo(prevValue);
        assertNonexistentKey(kvClient, key);
    }

    private static void putKVsWithAssertion(KV kvClient, List<TestKeyValue> keyValues) throws Exception {
        for (TestKeyValue keyValue : keyValues) {
            putKVWithAssertion(kvClient, keyValue.key, keyValue.value, null);
        }
    }

    private static void assertExistentKVs(KV kvClient, ByteSequence key, ByteSequence end, List<TestKeyValue> expectedKVs)
            throws Exception {
        CompletableFuture<GetResponse> getFuture = kvClient.get(key, GetOption.newBuilder().withRange(end).build());
        GetResponse getResponse = getFuture.get();
        assertThat(getResponse.getKvs().size()).isEqualTo(expectedKVs.size());
        for (KeyValue keyValue : getResponse.getKvs()) {
            boolean exist = false;
            for (TestKeyValue expectedKV : expectedKVs) {
                if (expectedKV.key.equals(keyValue.getKey())) {
                    exist = true;
                    assertThat(keyValue.getValue()).isEqualTo(expectedKV.value);
                    break;
                }
            }
            assertThat(exist).isTrue();
        }
    }

    private static void deleteKVsWithAssertion(KV kvClient, ByteSequence key, ByteSequence end, List<TestKeyValue> previousKVs)
            throws Exception {
        CompletableFuture<DeleteResponse> deleteFuture = kvClient.delete(key,
                DeleteOption.newBuilder().withRange(end).withPrevKV(true).build());
        DeleteResponse deleteResponse = deleteFuture.get();
        assertThat(deleteResponse.getDeleted()).isEqualTo(previousKVs.size());
        assertThat(deleteResponse.getPrevKvs().size()).isEqualTo(previousKVs.size());
        for (KeyValue keyValue : deleteResponse.getPrevKvs()) {
            boolean exist = false;
            for (TestKeyValue previousKV : previousKVs) {
                if (previousKV.key.equals(keyValue.getKey())) {
                    exist = true;
                    assertThat(keyValue.getValue()).isEqualTo(previousKV.value);
                    break;
                }
            }
            assertThat(exist).isTrue();
        }
    }
}
