

package com.lingh.repository;

import java.sql.SQLException;
import java.util.List;

public interface CommonRepository<T, P> {

    /**
     * Delete data.
     * 
     * @param primaryKey primaryKey
     * @throws SQLException SQL exception
     */
    void delete(P primaryKey) throws SQLException;
    
    /**
     * Select all data.
     * 
     * @return all data
     * @throws SQLException SQL exception
     */
    List<T> selectAll() throws SQLException;
}
