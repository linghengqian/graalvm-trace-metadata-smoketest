

package com.lingh.service;

import com.lingh.entity.User;
import com.lingh.repository.UserRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class UserServiceImpl implements ExampleService {

    private final UserRepository userRepository;

    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void initEnvironment() throws SQLException {
        userRepository.createTableIfNotExists();
        userRepository.truncateTable();
    }

    @Override
    public void cleanEnvironment() throws SQLException {
        userRepository.dropTable();
    }

    @Override
    public void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> userIds = insertData();
        printData();
        deleteData(userIds);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }

    @Override
    public void processFailure() throws SQLException {
        System.out.println("-------------- Process Failure Begin ---------------");
        insertData();
        System.out.println("-------------- Process Failure Finish --------------");
        throw new RuntimeException("Exception occur for transaction test.");
    }

    private List<Long> insertData() throws SQLException {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            User user = new User();
            user.setUserId(i);
            user.setUsername("test_" + i);
            user.setPwd("pwd" + i);
            userRepository.insert(user);
            result.add((long) user.getUserId());
        }
        return result;
    }

    private void deleteData(final List<Long> userIds) throws SQLException {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : userIds) {
            userRepository.delete(each);
        }
    }

    @Override
    public void printData() throws SQLException {
        System.out.println("---------------------------- Print User Data -----------------------");
        for (Object each : userRepository.selectAll()) {
            System.out.println(each);
        }
    }
}
