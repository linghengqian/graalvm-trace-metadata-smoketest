package com.lingh.jdbc.unsupported;

import org.apache.shardingsphere.driver.jdbc.core.statement.metadata.ShardingSphereParameterMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.mockito.Mockito.mock;

public final class UnsupportedOperationParameterMetaTest {
    
    private final ShardingSphereParameterMetaData shardingSphereParameterMetaData = new ShardingSphereParameterMetaData(mock(SQLStatement.class));
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsNullable() throws SQLException {
        shardingSphereParameterMetaData.getParameterClassName(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsSigned() throws SQLException {
        shardingSphereParameterMetaData.isSigned(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetPrecision() throws SQLException {
        shardingSphereParameterMetaData.getPrecision(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetScale() throws SQLException {
        shardingSphereParameterMetaData.getScale(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterType() throws SQLException {
        shardingSphereParameterMetaData.getParameterType(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterTypeName() throws SQLException {
        shardingSphereParameterMetaData.getParameterTypeName(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterClassName() throws SQLException {
        shardingSphereParameterMetaData.getParameterClassName(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterMode() throws SQLException {
        shardingSphereParameterMetaData.getParameterMode(1);
    }
}
