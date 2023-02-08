

package com.lingh.datasources;

import org.apache.commons.dbcp2.datasources.SharedPoolDataSource;
import org.apache.commons.dbcp2.datasources.SharedPoolDataSourceFactory;
import org.junit.jupiter.api.Test;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestFactory {
    @Test
    public void testJNDI2Pools() throws Exception {
        final Reference refObj = new Reference(SharedPoolDataSource.class.getName());
        refObj.add(new StringRefAddr("dataSourceName", "java:comp/env/jdbc/bookstoreCPDS"));
        final Context context = new InitialContext();
        final Hashtable<?, ?> env = new Hashtable<>();
        final ObjectFactory factory = new SharedPoolDataSourceFactory();
        final Name name = new CompositeName("myDB");
        final Object obj = factory.getObjectInstance(refObj, name, context, env);
        assertNotNull(obj);
        final Name name2 = new CompositeName("myDB2");
        final Object obj2 = factory.getObjectInstance(refObj, name2, context, env);
        assertNotNull(obj2);
    }
}
