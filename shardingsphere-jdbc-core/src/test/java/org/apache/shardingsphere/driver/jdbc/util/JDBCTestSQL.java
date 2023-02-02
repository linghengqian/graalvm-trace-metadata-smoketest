

package org.apache.shardingsphere.driver.jdbc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCTestSQL {
    
    public static final String SELECT_GROUP_BY_USER_ID_SQL = "SELECT user_id FROM t_order GROUP BY user_id";
    
    public static final String SELECT_ORDER_BY_USER_ID_SQL = "SELECT user_id FROM t_order WHERE status = 'init' ORDER BY user_id";
}
