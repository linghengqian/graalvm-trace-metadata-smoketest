
package com.lingh.service;

import com.lingh.entity.Address;
import com.lingh.entity.Order;
import com.lingh.entity.OrderItem;
import com.lingh.repository.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class OrderServiceImpl implements ExampleService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final AddressRepository addressRepository;

    public OrderServiceImpl(final DataSource dataSource) {
        orderRepository = new OrderRepositoryImpl(dataSource);
        orderItemRepository = new OrderItemRepositoryImpl(dataSource);
        addressRepository = new AddressRepositoryImpl(dataSource);
    }

    public OrderServiceImpl(final OrderRepository orderRepository, final OrderItemRepository orderItemRepository, final AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    public void initEnvironment() throws SQLException {
        orderRepository.createTableIfNotExists();
        orderItemRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
        initAddressTable();
    }

    private void initAddressTable() throws SQLException {
        addressRepository.createTableIfNotExists();
        addressRepository.truncateTable();
        initAddressData();
    }

    private void initAddressData() throws SQLException {
        for (int i = 0; i < 10; i++) {
            insertAddress(i);
        }
    }

    private void insertAddress(final int i) throws SQLException {
        Address address = new Address();
        address.setAddressId((long) i);
        address.setAddressName("address_" + i);
        addressRepository.insert(address);
    }

    @Override
    public void cleanEnvironment() throws SQLException {
        orderRepository.dropTable();
        orderItemRepository.dropTable();
        addressRepository.dropTable();
    }

    @Override
    public void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData();
        deleteData(orderIds);
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
            Order order = insertOrder(i);
            insertOrderItem(i, order);
            result.add(order.getOrderId());
        }
        return result;
    }

    private Order insertOrder(final int i) throws SQLException {
        Order order = new Order();
        order.setUserId(i);
        order.setAddressId(i);
        order.setStatus("INSERT_TEST");
        orderRepository.insert(order);
        return order;
    }

    private void insertOrderItem(final int i, final Order order) throws SQLException {
        OrderItem item = new OrderItem();
        item.setOrderId(order.getOrderId());
        item.setUserId(i);
        item.setStatus("INSERT_TEST");
        orderItemRepository.insert(item);
    }

    private void deleteData(final List<Long> orderIds) throws SQLException {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
        }
    }

    @Override
    public void printData() throws SQLException {
        System.out.println("---------------------------- Print Order Data -----------------------");
        for (Object each : orderRepository.selectAll()) {
            System.out.println(each);
        }
        System.out.println("---------------------------- Print OrderItem Data -------------------");
        for (Object each : orderItemRepository.selectAll()) {
            System.out.println(each);
        }
    }
}
