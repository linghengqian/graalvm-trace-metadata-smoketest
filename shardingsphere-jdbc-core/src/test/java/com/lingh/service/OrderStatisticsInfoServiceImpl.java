

package com.lingh.service;

import com.lingh.entity.OrderStatisticsInfo;
import com.lingh.repository.OrderStatisticsInfoRepository;
import com.lingh.repository.OrderStatisticsInfoRepositoryImpl;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

public final class OrderStatisticsInfoServiceImpl implements ExampleService {

    private final OrderStatisticsInfoRepository orderStatisticsInfoRepository;

    public OrderStatisticsInfoServiceImpl(final DataSource dataSource) {
        orderStatisticsInfoRepository = new OrderStatisticsInfoRepositoryImpl(dataSource);
    }

    @Override
    public void initEnvironment() throws SQLException {
        orderStatisticsInfoRepository.createTableIfNotExists();
        orderStatisticsInfoRepository.truncateTable();
    }

    @Override
    public void cleanEnvironment() throws SQLException {
        orderStatisticsInfoRepository.dropTable();
    }

    @Override
    public void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        Collection<Long> ids = insertData();
        printData();
        deleteData(ids);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }

    private Collection<Long> insertData() throws SQLException {
        System.out.println("------------------- Insert Data --------------------");
        Collection<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            OrderStatisticsInfo orderStatisticsInfo = insertOrderStatisticsInfo(i);
            result.add(orderStatisticsInfo.getId());
        }
        return result;
    }

    private OrderStatisticsInfo insertOrderStatisticsInfo(final int i) throws SQLException {
        OrderStatisticsInfo result = new OrderStatisticsInfo();
        result.setUserId((long) i);
        if (0 == i % 2) {
            result.setOrderDate(LocalDate.now().plusYears(-1));
        } else {
            result.setOrderDate(LocalDate.now());
        }
        result.setOrderNum(i * 10);
        orderStatisticsInfoRepository.insert(result);
        return result;
    }

    private void deleteData(final Collection<Long> ids) throws SQLException {
        System.out.println("-------------------- Delete Data -------------------");
        for (Long each : ids) {
            orderStatisticsInfoRepository.delete(each);
        }
    }

    @Override
    public void printData() throws SQLException {
        System.out.println("---------------- Print Order Data ------------------");
        for (Object each : orderStatisticsInfoRepository.selectAll()) {
            System.out.println(each);
        }
    }
}
