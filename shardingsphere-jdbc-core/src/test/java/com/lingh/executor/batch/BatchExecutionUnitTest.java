package com.lingh.executor.batch;

import org.apache.shardingsphere.driver.executor.batch.BatchExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class BatchExecutionUnitTest {
    
    private static final String DATA_SOURCE_NAME = "ds";
    
    private static final String SQL = "SELECT * FROM table WHERE id = ?";
    
    @Test
    public void assertGetParameterSets() {
        BatchExecutionUnit batchExecutionUnit = new BatchExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit(SQL, Collections.singletonList(1))));
        List<List<Object>> actual = batchExecutionUnit.getParameterSets();
        assertThat(actual.size(), is(1));
        assertTrue(actual.get(0).isEmpty());
        batchExecutionUnit.mapAddBatchCount(0);
        actual = batchExecutionUnit.getParameterSets();
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).size(), is(1));
        assertThat(actual.get(0).get(0), is(1));
    }
    
    @Test
    public void assertEquals() {
        BatchExecutionUnit actual = new BatchExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit(SQL, Collections.singletonList(1))));
        BatchExecutionUnit expected = new BatchExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit(SQL, Collections.singletonList(2))));
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertToString() {
        ExecutionUnit executionUnit = new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit(SQL, Collections.singletonList(1)));
        BatchExecutionUnit actual = new BatchExecutionUnit(executionUnit);
        assertThat(actual.toString(), is(String.format("BatchExecutionUnit(executionUnit=ExecutionUnit"
                + "(dataSourceName=%s, sqlUnit=SQLUnit(sql=%s, parameters=[%d], tableRouteMappers=[])), "
                + "jdbcAndActualAddBatchCallTimesMap={}, actualCallAddBatchTimes=0)", DATA_SOURCE_NAME, SQL, 1, "null")));
    }
}
