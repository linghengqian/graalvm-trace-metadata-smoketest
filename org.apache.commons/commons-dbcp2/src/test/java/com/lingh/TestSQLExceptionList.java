

package com.lingh;

import org.apache.commons.dbcp2.SQLExceptionList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLTransientException;
import java.util.Collections;
import java.util.List;

public class TestSQLExceptionList {
    @Test
    public void testCause() {
        final SQLTransientException cause = new SQLTransientException();
        final List<SQLTransientException> list = Collections.singletonList(cause);
        final SQLExceptionList sqlExceptionList = new SQLExceptionList(list);
        Assertions.assertEquals(cause, sqlExceptionList.getCause());
        Assertions.assertEquals(list, sqlExceptionList.getCauseList());
        sqlExceptionList.printStackTrace();
    }

    @Test
    public void testNullCause() {
        final SQLExceptionList sqlExceptionList = new SQLExceptionList(null);
        Assertions.assertNull(sqlExceptionList.getCause());
        Assertions.assertNull(sqlExceptionList.getCauseList());
    }
}
