package com.lingh.service.spel;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
@Service
@DS("slave")
public class UserService {

    private final DataSource dataSource;

    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @DS("#session.tenantName")
    public List<User> selectSpelBySession() {
        List<User> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from t_user");
            while (resultSet.next()) {
                result.add(new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getString(4)));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @DS("#header.tenantName")
    public List<User> selectSpelByHeader() {
        List<User> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from t_user");
            while (resultSet.next()) {
                result.add(new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getString(4)));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @DS("#tenantName")
    public List<User> selectSpelByKey(String tenantName) {
        List<User> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from t_user");
            while (resultSet.next()) {
                result.add(new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getString(4)));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @DS("#user.tenantName")
    public List<User> selecSpelByTenant(User user) {
        List<User> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from t_user");
            while (resultSet.next()) {
                result.add(new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getString(4)));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}