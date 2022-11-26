

package com.lingh;

import com.lingh.service.ExampleService;
import com.lingh.service.OrderServiceImpl;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class ShardingSQLCommentHintRawExample {

    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(getFile());
        ExampleService exampleService = getExampleService(dataSource);
        exampleService.initEnvironment();
        processWithHintValue(dataSource);
        exampleService.cleanEnvironment();
    }

    private static File getFile() {
        return new File(ShardingSQLCommentHintRawExample.class.getResource("/META-INF/sharding-sql-comment-hint.yaml").getFile());
    }

    private static ExampleService getExampleService(final DataSource dataSource) {
        return new OrderServiceImpl(dataSource);
    }

    private static void processWithHintValue(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */select * from t_order");
            statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id");
            statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */select * from t_order_item");
            statement.execute("/* ShardingSphere hint: dataSourceName=ds_1 */INSERT INTO t_order (user_id, address_id, status) VALUES (1, 1, 'init')");
        }
    }
}

