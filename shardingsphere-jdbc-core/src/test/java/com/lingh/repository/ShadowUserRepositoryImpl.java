

package com.lingh.repository;

import com.lingh.entity.ShadowUser;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public final class ShadowUserRepositoryImpl implements ShadowUserRepository {

    private static final String SQL_NOTE = "/*shadow:true,foo:bar*/";

    private final DataSource dataSource;

    public ShadowUserRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_user (user_id INT NOT NULL AUTO_INCREMENT, user_type INT(11), username VARCHAR(200), pwd VARCHAR(200), PRIMARY KEY (user_id))";
        createTableIfNotExistsShadow(sql);
        createTableIfNotExistsNative(sql);
    }

    private void createTableIfNotExistsNative(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void createTableIfNotExistsShadow(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql + SQL_NOTE);
        }
    }

    @Override
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE IF EXISTS t_user;";
        dropTableShadow(sql);
        dropTableNative(sql);
    }

    private void dropTableNative(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void dropTableShadow(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql + SQL_NOTE);
        }
    }

    @Override
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_user";
        truncateTableShadow(sql);
        truncateTableNative(sql);
    }

    private void truncateTableNative(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void truncateTableShadow(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql + SQL_NOTE);
        }
    }

    @Override
    public Long insert(final ShadowUser entity) throws SQLException {
        String sql = "INSERT INTO t_user (user_id, user_type, username, pwd) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, entity.getUserId());
            preparedStatement.setInt(2, entity.getUserType());
            preparedStatement.setString(3, entity.getUsername());
            preparedStatement.setString(4, entity.getPwd());
            preparedStatement.executeUpdate();
        }
        return (long) entity.getUserId();
    }

    @Override
    public void delete(final Long id) throws SQLException {
        String sql = "DELETE FROM t_user WHERE user_id = ? AND user_type = ?";
        deleteUser(sql, id, (int) (id % 2));
        deleteUser(sql, id, (int) (id % 2));
    }

    private void deleteUser(final String sql, final Long id, final int userType) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setInt(2, userType);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<ShadowUser> selectAll() throws SQLException {
        String sql = "SELECT * FROM t_user where user_type = ?";
        List<ShadowUser> users = getUsers(sql, 1);
        users.addAll(getUsers(sql, 0));
        return users;
    }

    private List<ShadowUser> getUsers(final String sql, final int userType) throws SQLException {
        List<ShadowUser> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userType);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ShadowUser user = new ShadowUser();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUserType(resultSet.getInt("user_type"));
                user.setUsername(resultSet.getString("username"));
                user.setPwd(resultSet.getString("pwd"));
                result.add(user);
            }
        }
        return result;
    }
}
