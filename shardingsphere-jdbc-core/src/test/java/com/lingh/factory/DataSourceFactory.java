package com.lingh.factory;

import com.lingh.config.ShardingDatabasesAndTablesConfigurationPrecise;
import com.lingh.config.ShardingDatabasesConfigurationPrecise;
import com.lingh.config.ShardingTablesConfigurationPrecise;
import com.lingh.type.ShardingType;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class DataSourceFactory {

    public static DataSource newInstance(final ShardingType shardingType) throws SQLException {
        switch (shardingType) {
            case SHARDING_DATABASES:
                return new ShardingDatabasesConfigurationPrecise().getDataSource();
            case SHARDING_TABLES:
                return new ShardingTablesConfigurationPrecise().getDataSource();
            case SHARDING_DATABASES_AND_TABLES:
                return new ShardingDatabasesAndTablesConfigurationPrecise().getDataSource();
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }
}
