package com.lingh;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = AddRemoveApplication.class, webEnvironment = RANDOM_PORT)
public class AddRemoveDatasourceTest {
    @Autowired
    DataSource dataSource;
    @Autowired
    DefaultDataSourceCreator dataSourceCreator;

    @Test
    void testAddAndRemoveDataSource() {
        DruidConfig druidConfig = new DruidConfig();
        druidConfig.setValidationQuery("select 1");
        DataSourceProperty dataSourceProperty = new DataSourceProperty()
                .setPoolName("slave_1").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test1;MODE=MySQL")
                .setUsername("sa").setPassword("").setDruid(druidConfig);
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        ds.addDataSource(dataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(dataSourceProperty));
        assertThat(ds.getDataSources().keySet()).contains("slave_1");
        ds.removeDataSource("slave_1");
        assertThat(ds.getDataSources().keySet()).doesNotContain("slave_1");
    }
}

@SpringBootApplication
class AddRemoveApplication {
    public static void main(String[] args) {
        SpringApplication.run(AddRemoveApplication.class, args);
    }
}
