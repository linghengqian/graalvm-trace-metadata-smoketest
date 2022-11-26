

package com.lingh;

import com.lingh.factory.RangeDataSourceFactory;
import com.lingh.repository.AddressRepositoryImpl;
import com.lingh.repository.OrderItemRepositoryImpl;
import com.lingh.repository.RangeOrderRepositoryImpl;
import com.lingh.service.ExampleService;
import com.lingh.service.OrderServiceImpl;
import com.lingh.type.ShardingType;

import javax.sql.DataSource;
import java.sql.SQLException;

/*
 * Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise this example will query empty data from replica.
 */
public final class ShardingRawJavaRangeConfigurationExample {

    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES;
//    private static ShardingType shardingType = ShardingType.SHARDING_TABLES;
//    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;

    public static void main(final String[] args) throws SQLException {
        DataSource dataSource = RangeDataSourceFactory.newInstance(shardingType);
        ExampleExecuteTemplate.run(getExampleService(dataSource));
    }

    private static ExampleService getExampleService(final DataSource dataSource) {
        return new OrderServiceImpl(new RangeOrderRepositoryImpl(dataSource), new OrderItemRepositoryImpl(dataSource), new AddressRepositoryImpl(dataSource));
    }
}
