

package com.lingh;

import com.lingh.factory.YamlRangeDataSourceFactory;
import com.lingh.service.AccountServiceImpl;
import com.lingh.service.OrderServiceImpl;
import com.lingh.type.ShardingType;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

/*
 * Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise this example will query empty data from replica.
 */
public final class ShardingRawYamlRangeConfigurationExample {

    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES;
//    private static ShardingType shardingType = ShardingType.SHARDING_TABLES;
//    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;

    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = YamlRangeDataSourceFactory.newInstance(shardingType);
        ExampleExecuteTemplate.run(new OrderServiceImpl(dataSource));
        ExampleExecuteTemplate.run(new AccountServiceImpl(dataSource));
    }
}
