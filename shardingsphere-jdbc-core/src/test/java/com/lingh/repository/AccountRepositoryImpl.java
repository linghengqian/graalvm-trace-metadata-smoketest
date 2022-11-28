

package com.lingh.repository;

import com.lingh.entity.Account;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class AccountRepositoryImpl implements AccountRepository {

    private final DataSource dataSource;

    public AccountRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_account (account_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (account_id))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    @Override
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE t_account";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    @Override
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_account";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    @Override
    public Long insert(final Account account) throws SQLException {
        String sql = "INSERT INTO t_account (user_id, status) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, account.getUserId());
            preparedStatement.setString(2, account.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    account.setAccountId(resultSet.getLong(1));
                }
            }
        }
        return account.getAccountId();
    }

    @Override
    public void delete(final Long accountId) throws SQLException {
        String sql = "DELETE FROM t_account WHERE account_id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, accountId);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<Account> selectAll() throws SQLException {
        String sql = "SELECT * FROM t_account";
        return getAccounts(sql);
    }

    protected List<Account> getAccounts(final String sql) throws SQLException {
        List<Account> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Account account = new Account();
                account.setAccountId(resultSet.getLong(1));
                account.setUserId(resultSet.getInt(2));
                account.setStatus(resultSet.getString(3));
                result.add(account);
            }
        }
        return result;
    }
}
