package com.lingh;

import com.lingh.service.ExampleService;
import com.lingh.service.OrderServiceImpl;
import com.lingh.type.ShardingType;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.hint.HintManager;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class ShardingHintRawExample {

    private static final ShardingType TYPE = ShardingType.SHARDING_HINT_DATABASES_ONLY;
//    private static final ShardingType TYPE = ShardingType.SHARDING_HINT_DATABASES_TABLES;

    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = getDataSource();
        ExampleService exampleService = getExampleService(dataSource);
        exampleService.initEnvironment();
        processWithHintValue(dataSource);
        exampleService.cleanEnvironment();
    }

    private static DataSource getDataSource() throws IOException, SQLException {
        switch (TYPE) {
            case SHARDING_HINT_DATABASES_ONLY:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-hint-databases-only.yaml"));
            case SHARDING_HINT_DATABASES_TABLES:
                return YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/sharding-hint-databases-tables.yaml"));
            default:
                throw new UnsupportedOperationException("unsupported type");
        }
    }

    private static File getFile(final String configFile) {
        return new File(ShardingHintRawExample.class.getResource(configFile).getFile());
    }

    private static ExampleService getExampleService(final DataSource dataSource) {
        return new OrderServiceImpl(dataSource);
    }

    private static void processWithHintValue(final DataSource dataSource) throws SQLException {
        try (HintManager hintManager = HintManager.getInstance();
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            setHintValue(hintManager);
            statement.execute("select * from t_order");
            statement.execute("SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
            statement.execute("select * from t_order_item");
            statement.execute("INSERT INTO t_order (user_id, address_id, status) VALUES (1, 1, 'init')");
        }
    }

    private static void setHintValue(final HintManager hintManager) {
        switch (TYPE) {
            case SHARDING_HINT_DATABASES_ONLY:
                hintManager.setDatabaseShardingValue(1L);
                return;
            case SHARDING_HINT_DATABASES_TABLES:
                hintManager.addDatabaseShardingValue("t_order", 2L);
                hintManager.addTableShardingValue("t_order", 1L);
                return;
            default:
                throw new UnsupportedOperationException("unsupported type");
        }
    }
}

