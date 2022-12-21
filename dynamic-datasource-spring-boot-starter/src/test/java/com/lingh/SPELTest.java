package com.lingh;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = SPELApplication.class, webEnvironment = RANDOM_PORT)
public class SPELTest {
    @Autowired
    DataSource dataSource;
    @Autowired
    DefaultDataSourceCreator dataSourceCreator;

    @Test
    void testSPEL() {
        DataSourceProperty masterDataSourceProperty = new DataSourceProperty()
                .setPoolName("master").setDriverClassName("org.h2.Driver")
                .setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE;INIT=RUNSCRIPT FROM 'classpath:db/spring-expression-language.sql'")
                .setUsername("sa").setPassword("");
        DataSourceProperty tenant1_1DataSourceProperty = new DataSourceProperty()
                .setPoolName("tenant1_1").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DataSourceProperty tenant1_2DataSourceProperty = new DataSourceProperty()
                .setPoolName("tenant1_2").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DataSourceProperty tenant2_1DataSourceProperty = new DataSourceProperty()
                .setPoolName("tenant2_1").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DataSourceProperty tenant2_2DataSourceProperty = new DataSourceProperty()
                .setPoolName("tenant2_2").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        ds.addDataSource("master", dataSourceCreator.createDataSource(masterDataSourceProperty));
        ds.addDataSource("tenant1_1", dataSourceCreator.createDataSource(tenant1_1DataSourceProperty));
        ds.addDataSource("tenant1_2", dataSourceCreator.createDataSource(tenant1_2DataSourceProperty));
        ds.addDataSource("tenant2_1", dataSourceCreator.createDataSource(tenant2_1DataSourceProperty));
        ds.addDataSource("tenant2_2", dataSourceCreator.createDataSource(tenant2_2DataSourceProperty));
        assertThat(ds.getDataSources().keySet()).contains("master", "teacher", "student");
    }
}

@SpringBootApplication
class SPELApplication {
    public static void main(String[] args) {
        SpringApplication.run(SPELApplication.class, args);
    }
}