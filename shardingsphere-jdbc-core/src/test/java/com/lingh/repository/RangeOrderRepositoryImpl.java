

package com.lingh.repository;

import com.lingh.entity.Order;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public final class RangeOrderRepositoryImpl extends OrderRepositoryImpl {

    public RangeOrderRepositoryImpl(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Order> selectAll() throws SQLException {
        String sql = "SELECT * FROM t_order WHERE order_id BETWEEN 200000000000000000 AND 400000000000000000";
        return getOrders(sql);
    }
}
