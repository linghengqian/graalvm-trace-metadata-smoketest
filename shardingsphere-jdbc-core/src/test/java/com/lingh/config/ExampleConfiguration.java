
package com.lingh.config;

import javax.sql.DataSource;
import java.sql.SQLException;

public interface ExampleConfiguration {
    
    DataSource getDataSource() throws SQLException;
}
