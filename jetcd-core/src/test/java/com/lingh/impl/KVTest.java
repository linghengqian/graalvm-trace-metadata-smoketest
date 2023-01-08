package com.lingh.impl;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
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
import io.etcd.jetcd.options.GetOption.SortOrder;
import io.etcd.jetcd.options.GetOption.SortTarget;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.test.EtcdClusterExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Charsets.UTF_8;
import static com.lingh.impl.TestUtil.bytesOf;
import static com.lingh.impl.TestUtil.randomString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@SuppressWarnings("resource")
@Timeout(value = 2, unit = TimeUnit.MINUTES)
public class KVTest {
    @RegisterExtension
    public static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
            .withNodes(1)
            .build();
    private static KV kvClient;
    private static final ByteSequence SAMPLE_KEY = bytesOf("sample_key");
    private static final ByteSequence SAMPLE_VALUE = bytesOf("sample_value");
    private static final ByteSequence SAMPLE_KEY_2 = bytesOf("sample_key2");
    private static final ByteSequence SAMPLE_VALUE_2 = bytesOf("sample_value2");
    private static final ByteSequence SAMPLE_KEY_3 = bytesOf("sample_key3");

    @BeforeAll
    public static void setUp() {
        kvClient = TestUtil.client(cluster).build().getKVClient();
    }

    @Test
    public void testByteSequence() {
        ByteSequence prefix = bytesOf("/test-service/");
        ByteSequence subPrefix = bytesOf("uuids/");
        String keyString = randomString();
        ByteSequence key = bytesOf(keyString);
        ByteSequence prefixedKey = prefix.concat(subPrefix).concat(key);
        assertThat(prefixedKey.startsWith(prefix)).isTrue();
        assertThat(prefixedKey.substring(prefix.size() + subPrefix.size()).toString(UTF_8)).isEqualTo(keyString);
        assertThat(prefixedKey.substring(prefix.size(), prefix.size() + subPrefix.size())).isEqualTo(subPrefix);
    }

    @Test
    public void testPut() throws Exception {
        CompletableFuture<PutResponse> feature = kvClient.put(SAMPLE_KEY, SAMPLE_VALUE);
        PutResponse response = feature.get();
        assertThat(response.getHeader()).isNotNull();
        assertThat(!response.hasPrevKv()).isTrue();
    }

    @Test
    public void testPutWithNotExistLease() {
        PutOption option = PutOption.newBuilder().withLeaseId(99999).build();
        CompletableFuture<PutResponse> future = kvClient.put(SAMPLE_KEY, SAMPLE_VALUE, option);
        assertThatExceptionOfType(ExecutionException.class)
                .isThrownBy(future::get).withMessageEndingWith("etcdserver: requested lease not found");
    }

    @Test
    public void testGet() throws Exception {
        CompletableFuture<PutResponse> feature = kvClient.put(SAMPLE_KEY_2, SAMPLE_VALUE_2);
        feature.get();
        CompletableFuture<GetResponse> getFeature = kvClient.get(SAMPLE_KEY_2);
        GetResponse response = getFeature.get();
        assertThat(response.getKvs()).hasSize(1);
        assertThat(response.getKvs().get(0).getValue().toString(UTF_8)).isEqualTo(SAMPLE_VALUE_2.toString(UTF_8));
        assertThat(!response.isMore()).isTrue();
    }

    @Test
    public void testGetWithRev() throws Exception {
        CompletableFuture<PutResponse> feature = kvClient.put(SAMPLE_KEY_3, SAMPLE_VALUE);
        PutResponse putResp = feature.get();
        kvClient.put(SAMPLE_KEY_3, SAMPLE_VALUE_2).get();
        GetOption option = GetOption.newBuilder().withRevision(putResp.getHeader().getRevision()).build();
        CompletableFuture<GetResponse> getFeature = kvClient.get(SAMPLE_KEY_3, option);
        GetResponse response = getFeature.get();
        assertThat(response.getKvs()).hasSize(1);
        assertThat(response.getKvs().get(0).getValue().toString(UTF_8)).isEqualTo(SAMPLE_VALUE.toString(UTF_8));
    }

    @Test
    public void testGetSortedPrefix() throws Exception {
        String prefix = randomString();
        int numPrefix = 3;
        putKeysWithPrefix(prefix, numPrefix);
        GetOption option = GetOption.newBuilder().withSortField(SortTarget.KEY).withSortOrder(SortOrder.DESCEND)
                .isPrefix(true).build();
        CompletableFuture<GetResponse> getFeature = kvClient.get(bytesOf(prefix), option);
        GetResponse response = getFeature.get();
        assertThat(response.getKvs()).hasSize(numPrefix);
        for (int i = 0; i < numPrefix; i++) {
            assertThat(response.getKvs().get(i).getKey().toString(UTF_8)).isEqualTo(prefix + (numPrefix - i - 1));
            assertThat(response.getKvs().get(i).getValue().toString(UTF_8)).isEqualTo(String.valueOf(numPrefix - i - 1));
        }
    }

    @Test
    public void testDelete() throws Exception {
        testPut();
        ByteSequence keyToDelete = SAMPLE_KEY;
        CompletableFuture<GetResponse> getFeature = kvClient.get(keyToDelete);
        GetResponse resp = getFeature.get();
        CompletableFuture<DeleteResponse> deleteFuture = kvClient.delete(keyToDelete);
        DeleteResponse delResp = deleteFuture.get();
        assertThat(delResp.getDeleted()).isEqualTo(resp.getKvs().size());
    }

    @Test
    public void testGetAndDeleteWithPrefix() throws Exception {
        String prefix = randomString();
        ByteSequence key = bytesOf(prefix);
        int numPrefixes = 10;
        putKeysWithPrefix(prefix, numPrefixes);
        CompletableFuture<GetResponse> getFuture = kvClient.get(key, GetOption.newBuilder().isPrefix(true).build());
        GetResponse getResp = getFuture.get();
        assertThat(getResp.getCount()).isEqualTo(numPrefixes);
        DeleteOption deleteOpt = DeleteOption.newBuilder().isPrefix(true).build();
        CompletableFuture<DeleteResponse> delFuture = kvClient.delete(key, deleteOpt);
        DeleteResponse delResp = delFuture.get();
        assertThat(delResp.getDeleted()).isEqualTo(numPrefixes);
    }

    private static void putKeysWithPrefix(String prefix, int numPrefixes) throws ExecutionException, InterruptedException {
        for (int i = 0; i < numPrefixes; i++) {
            ByteSequence key = bytesOf(prefix + i);
            ByteSequence value = bytesOf("" + i);
            kvClient.put(key, value).get();
        }
    }

    @Test
    public void testTxn() throws Exception {
        ByteSequence sampleKey = bytesOf("txn_key");
        ByteSequence sampleValue = bytesOf("xyz");
        ByteSequence cmpValue = bytesOf("abc");
        ByteSequence putValue = bytesOf("XYZ");
        ByteSequence putValueNew = bytesOf("ABC");
        kvClient.put(sampleKey, sampleValue).get();
        Txn txn = kvClient.txn();
        Cmp cmp = new Cmp(sampleKey, Cmp.Op.GREATER, CmpTarget.value(cmpValue));
        CompletableFuture<TxnResponse> txnResp = txn.If(cmp)
                .Then(Op.put(sampleKey, putValue, PutOption.DEFAULT)).Else(Op.put(sampleKey, putValueNew, PutOption.DEFAULT))
                .commit();
        txnResp.get();
        GetResponse getResp = kvClient.get(sampleKey).get();
        assertThat(getResp.getKvs()).hasSize(1);
        assertThat(getResp.getKvs().get(0).getValue().toString(UTF_8)).isEqualTo(putValue.toString(UTF_8));
    }

    @Test
    void testTxnGetAndDeleteWithPrefix() throws ExecutionException, InterruptedException {
        String prefix = randomString();
        ByteSequence sampleKey = bytesOf(prefix);
        int numPrefixes = 10;
        putKeysWithPrefix(prefix, numPrefixes);
        Cmp cmp = new Cmp(sampleKey, Cmp.Op.EQUAL, CmpTarget.value(bytesOf("not_exists")));
        Op.PutOp putOp = Op.put(bytesOf("other_string"), bytesOf("other_value"), PutOption.DEFAULT);
        Op.GetOp getByPrefix = Op.get(sampleKey, GetOption.newBuilder().isPrefix(true).build());
        Op.DeleteOp delete = Op.delete(sampleKey, DeleteOption.newBuilder().isPrefix(true).withPrevKV(true).build());
        TxnResponse txnResponse = kvClient.txn().If(cmp)
                .Then(putOp).Else(getByPrefix, delete).commit().get();
        List<GetResponse> getResponse = txnResponse.getGetResponses();
        assertThat(getResponse).hasSize(1);
        assertThat(getResponse.get(0).getKvs()).hasSize(10);
        assertThat(getResponse.get(0).getKvs()).anyMatch(keyValue -> keyValue.getKey().startsWith(sampleKey));
        List<DeleteResponse> deleteResponses = txnResponse.getDeleteResponses();
        assertThat(deleteResponses).hasSize(1);
        assertThat(deleteResponses.get(0).getDeleted()).isEqualTo(10);
        assertThat(deleteResponses.get(0).getPrevKvs()).anyMatch(keyValue -> keyValue.getKey().startsWith(sampleKey));
        assertThat(txnResponse.getPutResponses()).isEmpty();
    }

    @Test
    public void testTxnForCmpOpNotEqual() throws Exception {
        ByteSequence sampleKey = bytesOf("txn_key");
        ByteSequence sampleValue = bytesOf("xyz");
        ByteSequence cmpValue = bytesOf("abc");
        ByteSequence putValue = bytesOf("XYZ");
        ByteSequence putValueNew = bytesOf("ABC");
        kvClient.put(sampleKey, sampleValue).get();
        Txn txn = kvClient.txn();
        Cmp cmp = new Cmp(sampleKey, Cmp.Op.NOT_EQUAL, CmpTarget.value(cmpValue));
        CompletableFuture<TxnResponse> txnResp = txn.If(cmp)
                .Then(Op.put(sampleKey, putValue, PutOption.DEFAULT)).Else(Op.put(sampleKey, putValueNew, PutOption.DEFAULT))
                .commit();
        txnResp.get();
        GetResponse getResp = kvClient.get(sampleKey).get();
        assertThat(getResp.getKvs()).hasSize(1);
        assertThat(getResp.getKvs().get(0).getValue().toString(UTF_8)).isEqualTo(putValue.toString(UTF_8));
    }

    @Test
    public void testNestedTxn() throws Exception {
        ByteSequence foo = bytesOf("txn_foo");
        ByteSequence bar = bytesOf("txn_bar");
        ByteSequence barz = bytesOf("txn_barz");
        ByteSequence abc = bytesOf("txn_abc");
        ByteSequence oneTwoThree = bytesOf("txn_123");
        Txn txn = kvClient.txn();
        Cmp cmp = new Cmp(foo, Cmp.Op.EQUAL, CmpTarget.version(0));
        CompletableFuture<TxnResponse> txnResp = txn.If(cmp)
                .Then(Op.put(foo, bar, PutOption.DEFAULT),
                        Op.txn(null, new Op[]{Op.put(abc, oneTwoThree, PutOption.DEFAULT)}, null))
                .Else(Op.put(foo, barz, PutOption.DEFAULT)).commit();
        txnResp.get();
        GetResponse getResp = kvClient.get(foo).get();
        assertThat(getResp.getKvs()).hasSize(1);
        assertThat(getResp.getKvs().get(0).getValue().toString(UTF_8)).isEqualTo(bar.toString(UTF_8));
        GetResponse getResp2 = kvClient.get(abc).get();
        assertThat(getResp2.getKvs()).hasSize(1);
        assertThat(getResp2.getKvs().get(0).getValue().toString(UTF_8)).isEqualTo(oneTwoThree.toString(UTF_8));
    }

    @Test()
    public void waitForReadySemantics() throws ExecutionException, InterruptedException, TimeoutException {
        String nonExistingServer = "http://127.0.0.1:9999";
        try (Client customClient = Client.builder().endpoints(nonExistingServer)
                .waitForReady(false)
                .retryMaxDuration(Duration.ofSeconds(3))
                .retryDelay(1)
                .retryMaxDelay(2)
                .retryChronoUnit(ChronoUnit.SECONDS)
                .connectTimeout(Duration.ofSeconds(1))
                .build()) {
            KV kvClient = customClient.getKVClient();
            CompletableFuture<String> future = kvClient.get(ByteSequence.from("/x", StandardCharsets.UTF_8))
                    .thenApply(response -> "we got a response")
                    .exceptionally(throwable -> "completed exceptionally");
            assertThat(future.get(5, TimeUnit.SECONDS))
                    .isEqualTo("completed exceptionally");
        }
    }
}
