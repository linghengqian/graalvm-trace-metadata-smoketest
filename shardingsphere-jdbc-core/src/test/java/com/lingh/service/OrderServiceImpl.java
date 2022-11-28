
package com.lingh.service;

import com.lingh.entity.Address;
import com.lingh.entity.Order;
import com.lingh.entity.OrderItem;
import com.lingh.repository.AddressRepository;
import com.lingh.repository.AddressRepositoryImpl;
import com.lingh.repository.OrderItemRepository;
import com.lingh.repository.OrderItemRepositoryImpl;
import com.lingh.repository.OrderRepository;
import com.lingh.repository.OrderRepositoryImpl;

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
        addressRepository.createTableIfNotExists();
        addressRepository.truncateTable();
        for (int i = 0; i < 10; i++) {
            Address address = new Address();
            address.setAddressId((long) i);
            address.setAddressName("address_" + i);
            addressRepository.insert(address);
        }
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
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> orderIds = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            OrderItem item = new OrderItem();
            item.setOrderId(order.getOrderId());
            item.setUserId(i);
            item.setStatus("INSERT_TEST");
            orderItemRepository.insert(item);
            orderIds.add(order.getOrderId());
        }
        printData();
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
        }
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Override
    public void processFailure() throws SQLException {
        System.out.println("-------------- Process Failure Begin ---------------");
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            OrderItem item = new OrderItem();
            item.setOrderId(order.getOrderId());
            item.setUserId(i);
            item.setStatus("INSERT_TEST");
            orderItemRepository.insert(item);
            result.add(order.getOrderId());
        }
        System.out.println("-------------- Process Failure Finish --------------");
        throw new RuntimeException("Exception occur for transaction test.");
    }

    @Override
    public void printData() throws SQLException {
        System.out.println("---------------------------- Print Order Data -----------------------");
        orderRepository.selectAll().forEach(System.out::println);
        System.out.println("---------------------------- Print OrderItem Data -------------------");
        orderItemRepository.selectAll().forEach(System.out::println);
    }
}
