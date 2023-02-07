

package com.lingh;

import org.apache.commons.dbcp2.TesterResultSet;
import org.apache.commons.dbcp2.TesterStatement;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.util.Calendar;

@SuppressWarnings("ConstantValue")
public class TesterPreparedStatement extends TesterStatement implements PreparedStatement {
    private final ResultSetMetaData _resultSetMetaData = null;
    private String _sql;
    private String _catalog;
    private int _autoGeneratedKeys = 1;
    private int[] _columnIndexes;
    private String[] _columnNames;

    public TesterPreparedStatement(final Connection conn) {
        super(conn);
        try {
            _catalog = conn.getCatalog();
        } catch (final SQLException e) {
        }
    }

    public TesterPreparedStatement(final Connection conn, final String sql) {
        super(conn);
        _sql = sql;
        try {
            _catalog = conn.getCatalog();
        } catch (final SQLException e) {
        }
    }

    public TesterPreparedStatement(final Connection conn, final String sql, final int autoGeneratedKeys) {
        super(conn);
        _sql = sql;
        _autoGeneratedKeys = autoGeneratedKeys;
        try {
            _catalog = conn.getCatalog();
        } catch (final SQLException e) {
        }
    }

    public TesterPreparedStatement(final Connection conn, final String sql, final int resultSetType, final int resultSetConcurrency) {
        super(conn, resultSetType, resultSetConcurrency);
        _sql = sql;
        try {
            _catalog = conn.getCatalog();
        } catch (final SQLException ignored) {
        }
    }

    public TesterPreparedStatement(final Connection conn, final String sql, final int resultSetType, final int resultSetConcurrency,
            final int resultSetHoldability) {
        super(conn, resultSetType, resultSetConcurrency, resultSetHoldability);
        _sql = sql;
        try {
            _catalog = conn.getCatalog();
        } catch (final SQLException e) {
        }
    }

    public TesterPreparedStatement(final Connection conn, final String sql, final int[] columnIndexes) {
        super(conn);
        _sql = sql;
        _columnIndexes = columnIndexes;
        try {
            _catalog = conn.getCatalog();
        } catch (final SQLException e) {
        }
    }

    public TesterPreparedStatement(final Connection conn, final String sql, final String[] columnNames) {
        super(conn);
        _sql = sql;
        _columnNames = columnNames;
        try {
            _catalog = conn.getCatalog();
        } catch (final SQLException e) {
        }
    }

    @Override
    public void addBatch() throws SQLException {
        checkOpen();
    }

    @Override
    public void clearParameters() throws SQLException {
        checkOpen();
    }

    @Override
    public boolean execute() throws SQLException {
        checkOpen(); return true;
    }

    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        checkOpen();
        return true;
    }

    @Override
    public boolean execute(final String sl, final int[] columnIndexes) throws SQLException {
        checkOpen();
        return true;
    }

    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        checkOpen();
        return true;
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        checkOpen();
        return _rowsUpdated;
    }

    @Override
    public long executeLargeUpdate(final String sql) throws SQLException {
        checkOpen();
        return _rowsUpdated;
    }

    @Override
    public long executeLargeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        checkOpen();
        return 0;
    }

    @Override
    public long executeLargeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        checkOpen();
        return 0;
    }

    @Override
    public long executeLargeUpdate(final String sql, final String[] columnNames) throws SQLException {
        checkOpen();
        return 0;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        checkOpen();
        if("null".equals(_sql)) {
            return null;
        }
        if (_queryTimeout > 0 && _queryTimeout < 5) {
            throw new SQLException("query timeout");
        }
        return new TesterResultSet(this, _resultSetType, _resultSetConcurrency);
    }

    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        checkOpen();
        if("null".equals(sql)) {
            return null;
        }
        return new TesterResultSet(this, _resultSetType, _resultSetConcurrency);
    }

    @Override
    public int executeUpdate() throws SQLException {
        checkOpen();
        return (int) _rowsUpdated;
    }

    @Override
    public int executeUpdate(final String sql) throws SQLException {
        checkOpen();
        return (int) _rowsUpdated;
    }

    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        checkOpen();
        return 0;
    }

    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        checkOpen();
        return 0;
    }

    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        checkOpen();
        return 0;
    }

    public int getAutoGeneratedKeys() {
        return _autoGeneratedKeys;
    }

    public String getCatalog() {
        return _catalog;
    }

    public int[] getColumnIndexes() {
        return _columnIndexes;
    }

    public String[] getColumnNames() {
        return _columnNames;
    }

    @Override
    public ResultSet getGeneratedKeys() {
        return new TesterResultSet(this, _resultSetType, _resultSetConcurrency);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        checkOpen();
        return _resultSetMetaData;
    }

    @Override
    public boolean getMoreResults(final int current) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public java.sql.ParameterMetaData getParameterMetaData() throws SQLException {
        throw new SQLException("Not implemented.");
    }

    public String getSql() {
        return _sql;
    }

    @Override
    public void setArray (final int i, final Array x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream inputStream) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        checkOpen();
    }

    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream inputStream) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        checkOpen();
    }


    @Override
    public void setBlob (final int i, final Blob x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        checkOpen();
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setClob (final int i, final Clob x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setClob(final int parameterIndex, final Reader reader) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setDate(final int parameterIndex, final java.sql.Date x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setDate(final int parameterIndex, final java.sql.Date x, final Calendar cal) throws SQLException {
        checkOpen();
    }

    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader reader) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value, final long length) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setNClob(final int parameterIndex, final Reader reader) throws SQLException {
        throw new SQLException("Not implemented.");
    }


    @Override
    public void setNClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setNString(final int parameterIndex, final String value) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        checkOpen();
    }

    @Override
    public void setNull (final int paramIndex, final int sqlType, final String typeName) throws SQLException {
        checkOpen();
    }

    @Override
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        checkOpen();
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scale) throws SQLException {
        checkOpen();
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final SQLType targetSqlType) throws SQLException {
        checkOpen();
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final SQLType targetSqlType, final int scaleOrLength) throws SQLException {
        checkOpen();
    }

    @Override
    public void setRef (final int i, final Ref x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setRowId(final int parameterIndex, final RowId value) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML value) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setTime(final int parameterIndex, final java.sql.Time x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setTime(final int parameterIndex, final java.sql.Time x, final Calendar cal) throws SQLException {
        checkOpen();
    }

    @Override
    public void setTimestamp(final int parameterIndex, final java.sql.Timestamp x) throws SQLException {
        checkOpen();
    }

    @Override
    public void setTimestamp(final int parameterIndex, final java.sql.Timestamp x, final Calendar cal) throws SQLException {
        checkOpen();
    }

    /** @deprecated */
    @Deprecated
    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        checkOpen();
    }

    @Override
    public void setURL(final int parameterIndex, final java.net.URL x) throws SQLException {
        throw new SQLException("Not implemented.");
    }

    @Override
    public String toString() {
        return _sql;
    }
}
