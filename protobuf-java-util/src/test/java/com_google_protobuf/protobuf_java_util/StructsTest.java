package com_google_protobuf.protobuf_java_util;

import com.google.common.truth.Truth;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.Structs;
import com.google.protobuf.util.Values;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public final class StructsTest {
    @Test
    public void test1pair_constructsObject() throws Exception {
        Struct.Builder expected = Struct.newBuilder();
        JsonFormat.parser().merge("{\"k1\": 1}", expected);
        Truth.assertThat(Structs.of("k1", Values.of(1))).isEqualTo(expected.build());
    }

    @Test
    public void test2pair_constructsObject() throws Exception {
        Struct.Builder expected = Struct.newBuilder();
        JsonFormat.parser().merge("{\"k1\": 1, \"k2\": 2}", expected);
        assertThat(Structs.of("k1", Values.of(1), "k2", Values.of(2))).isEqualTo(expected.build());
    }

    @Test
    public void test3pair_constructsObject() throws Exception {
        Struct.Builder expected = Struct.newBuilder();
        JsonFormat.parser().merge("{\"k1\": 1, \"k2\": 2, \"k3\": 3}", expected);
        assertThat(Structs.of("k1", Values.of(1), "k2", Values.of(2), "k3", Values.of(3)))
                .isEqualTo(expected.build());
    }
}
