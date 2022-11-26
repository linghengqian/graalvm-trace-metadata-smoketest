

package com.lingh.repository;

import com.lingh.entity.OrderItem;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public final class RangeOrderItemRepositoryImpl extends OrderItemRepositoryImpl {

    public RangeOrderItemRepositoryImpl(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<OrderItem> selectAll() throws SQLException {
        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id AND o.user_id BETWEEN 1 AND 5";
        return getOrderItems(sql);
    }
}
