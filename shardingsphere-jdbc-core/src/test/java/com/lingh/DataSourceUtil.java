package com.lingh;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;
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

    public static File getFile(final String fileName) {
        URL resource = DataSourceUtil.class.getResource(fileName);
        assertThat(resource).isNotNull();
        return new File(resource.getFile());
    }
}
