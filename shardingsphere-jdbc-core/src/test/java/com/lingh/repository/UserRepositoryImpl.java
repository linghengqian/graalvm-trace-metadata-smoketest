
package com.lingh.repository;

import com.lingh.entity.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public final class UserRepositoryImpl implements UserRepository {

    private final DataSource dataSource;

    public UserRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_user "
                     + "(user_id INT NOT NULL AUTO_INCREMENT, username VARCHAR(200), pwd VARCHAR(200), PRIMARY KEY (user_id))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    @Override
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE t_user";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    @Override
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_user";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    @Override
    public Long insert(final User entity) throws SQLException {
        String sql = "INSERT INTO t_user (user_id, username, pwd) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, entity.getUserId());
            preparedStatement.setString(2, entity.getUsername());
            preparedStatement.setString(3, entity.getPwd());
            preparedStatement.executeUpdate();
        }
        return (long) entity.getUserId();
    }

    @Override
    public void delete(final Long id) throws SQLException {
        String sql = "DELETE FROM t_user WHERE user_id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<User> selectAll() throws SQLException {
        String sql = "SELECT * FROM t_user";
        return getUsers(sql);
    }

    private List<User> getUsers(final String sql) throws SQLException {
        List<User> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUsername(resultSet.getString("username"));
                user.setPwd(resultSet.getString("pwd"));
                result.add(user);
            }
        }
        return result;
    }
}
