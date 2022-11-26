

package com.lingh.factory;

import com.lingh.config.ShardingDatabasesAndTablesConfigurationRange;
import com.lingh.config.ShardingDatabasesConfigurationRange;
import com.lingh.config.ShardingTablesConfigurationRange;
import com.lingh.type.ShardingType;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class RangeDataSourceFactory {

    public static DataSource newInstance(final ShardingType shardingType) throws SQLException {
        switch (shardingType) {
            case SHARDING_DATABASES:
                return new ShardingDatabasesConfigurationRange().getDataSource();
            case SHARDING_TABLES:
                return new ShardingTablesConfigurationRange().getDataSource();
            case SHARDING_DATABASES_AND_TABLES:
                return new ShardingDatabasesAndTablesConfigurationRange().getDataSource();
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }
}
