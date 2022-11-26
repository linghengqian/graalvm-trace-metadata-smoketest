
package com.lingh.factory;

import com.lingh.type.ShardingType;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public final class YamlRangeDataSourceFactory {

    public static DataSource newInstance(final ShardingType shardingType) throws SQLException, IOException {
        switch (shardingType) {
            case SHARDING_DATABASES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-databases-range.yaml"));
            case SHARDING_TABLES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-tables-range.yaml"));
            case SHARDING_DATABASES_AND_TABLES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-databases-tables-range.yaml"));
            case SHARDING_DATABASES_INTERVAL:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-databases-interval.yaml"));
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }

    private static File getFile(final String fileName) {
        return new File(YamlRangeDataSourceFactory.class.getResource(fileName).getFile());
    }
}
