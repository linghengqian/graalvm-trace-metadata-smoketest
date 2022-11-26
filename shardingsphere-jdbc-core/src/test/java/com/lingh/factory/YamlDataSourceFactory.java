

package com.lingh.factory;

import com.lingh.type.ShardingType;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public final class YamlDataSourceFactory {

    public static DataSource newInstance(final ShardingType shardingType) throws SQLException, IOException {
        switch (shardingType) {
            case SHARDING_DATABASES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-databases.yaml"));
            case SHARDING_TABLES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-tables.yaml"));
            case SHARDING_DATABASES_AND_TABLES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-databases-tables.yaml"));
            case SHARDING_AUTO_TABLES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-auto-tables.yaml"));
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }

    private static File getFile(final String fileName) {
        return new File(YamlDataSourceFactory.class.getResource(fileName).getFile());
    }
}
