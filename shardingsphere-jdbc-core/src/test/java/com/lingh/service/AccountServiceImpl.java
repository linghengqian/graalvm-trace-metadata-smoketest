

package com.lingh.service;

import com.lingh.entity.Account;
import com.lingh.repository.AccountRepository;
import com.lingh.repository.AccountRepositoryImpl;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class AccountServiceImpl implements ExampleService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(final DataSource dataSource) {
        accountRepository = new AccountRepositoryImpl(dataSource);
    }

    public AccountServiceImpl(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void initEnvironment() throws SQLException {
        accountRepository.createTableIfNotExists();
        accountRepository.truncateTable();
    }

    @Override
    public void cleanEnvironment() throws SQLException {
        accountRepository.dropTable();
    }

    @Override
    public void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> accountIds = insertData();
        printData();
        deleteData(accountIds);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }

    private List<Long> insertData() throws SQLException {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Account account = insertAccounts(i);
            result.add(account.getAccountId());
        }
        return result;
    }

    private Account insertAccounts(final int i) throws SQLException {
        Account account = new Account();
        account.setUserId(i);
        account.setStatus("INSERT_TEST");
        accountRepository.insert(account);
        return account;
    }

    private void deleteData(final List<Long> accountIds) throws SQLException {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : accountIds) {
            accountRepository.delete(each);
        }
    }

    @Override
    public void printData() throws SQLException {
        System.out.println("---------------------------- Print Account Data -----------------------");
        for (Object each : accountRepository.selectAll()) {
            System.out.println(each);
        }
    }
}
