package com.lingh.jdbc.context;

import org.apache.shardingsphere.driver.jdbc.context.CachedDatabaseMetaData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.DatabaseMetaData;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CachedDatabaseMetaDataTest {
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Test
    public void assertGetRowIdLifetimeFromOriginMetaData() throws SQLException {
        RowIdLifetime rowIdLifetime = mock(RowIdLifetime.class);
        when(databaseMetaData.getRowIdLifetime()).thenReturn(rowIdLifetime);
        assertThat(new CachedDatabaseMetaData(databaseMetaData).getRowIdLifetime(), is(rowIdLifetime));
    }
    
    @Test
    public void assertGetRowIdLifetimeFromOriginMetaDataWhenNotSupported() throws SQLException {
        when(databaseMetaData.getRowIdLifetime()).thenThrow(SQLFeatureNotSupportedException.class);
        assertThat(new CachedDatabaseMetaData(databaseMetaData).getRowIdLifetime(), is(RowIdLifetime.ROWID_UNSUPPORTED));
    }
    
    @Test
    public void assertIsGeneratedKeyAlwaysReturned() throws SQLException {
        when(databaseMetaData.generatedKeyAlwaysReturned()).thenReturn(true);
        assertTrue(new CachedDatabaseMetaData(databaseMetaData).isGeneratedKeyAlwaysReturned());
    }
    
    @Test
    public void assertIsGeneratedKeyAlwaysReturnedWhenNotSupported() throws SQLException {
        when(databaseMetaData.generatedKeyAlwaysReturned()).thenThrow(AbstractMethodError.class);
        assertFalse(new CachedDatabaseMetaData(databaseMetaData).isGeneratedKeyAlwaysReturned());
    }
}
