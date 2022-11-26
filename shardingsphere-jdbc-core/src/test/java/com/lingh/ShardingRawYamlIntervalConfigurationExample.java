

package com.lingh;

import com.lingh.factory.YamlRangeDataSourceFactory;
import com.lingh.service.OrderStatisticsInfoServiceImpl;
import com.lingh.type.ShardingType;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public final class ShardingRawYamlIntervalConfigurationExample {

    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_INTERVAL;

    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = YamlRangeDataSourceFactory.newInstance(shardingType);
        ExampleExecuteTemplate.run(new OrderStatisticsInfoServiceImpl(dataSource));
    }
}
