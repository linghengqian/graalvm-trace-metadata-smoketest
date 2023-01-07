

package com.lingh.jdbc.core.resultset;

import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetUtil;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.junit.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSphereResultSetUtilTest {
    
    @Test
    public void assertCreateColumnLabelAndIndexMapWithSelectWithoutExpandProjections() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, Collections.emptyList()));
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("label");
        Map<String, Integer> actual = ShardingSphereResultSetUtil.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(Collections.singletonMap("label", 1)));
    }
    
    @Test
    public void assertCreateColumnLabelAndIndexMapWithSelectWithExpandProjections() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null));
        projections.add(new ColumnProjection(null, "col2", null));
        when(selectStatementContext.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, projections));
        Map<String, Integer> expected = new HashMap<>(2, 1);
        expected.put("col1", 1);
        expected.put("col2", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtil.createColumnLabelAndIndexMap(selectStatementContext, null);
        assertThat(actual, is(expected));
    }
}
