package com.lingh.jdbc.adapter.invocation;

import org.apache.shardingsphere.driver.jdbc.adapter.invocation.MethodInvocationRecorder;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class MethodInvocationRecorderTest {
    
    @Test
    public void assertRecordMethodInvocationSuccess() throws SQLException {
        MethodInvocationRecorder<List<?>> methodInvocationRecorder = new MethodInvocationRecorder<>();
        methodInvocationRecorder.record("isEmpty", List::isEmpty);
        methodInvocationRecorder.replay(Collections.emptyList());
    }
    
    @Test
    public void assertRecordSameMethodTwice() throws SQLException {
        MethodInvocationRecorder<List<Integer>> methodInvocationRecorder = new MethodInvocationRecorder<>();
        methodInvocationRecorder.record("add", target -> target.add(1));
        methodInvocationRecorder.record("add", target -> target.add(2));
        List<Integer> actual = new ArrayList<>();
        methodInvocationRecorder.replay(actual);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(2));
    }
}
