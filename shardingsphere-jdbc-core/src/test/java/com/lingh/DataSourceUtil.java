package com.lingh;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public final class DataSourceUtil {

    public static DataSource createDataSource(final String dataSourceName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setJdbcUrl("jdbc:h2:mem:" + dataSourceName);
        result.setUsername("root");
        result.setPassword("");
        return result;
    }

    @SuppressWarnings("resource")
    public static byte[] getFile(final String fileName) throws IOException {
        URL resource = DataSourceUtil.class.getResource(fileName);
        assertThat(resource).isNotNull();
        return resource.openStream().readAllBytes();
    }
}
