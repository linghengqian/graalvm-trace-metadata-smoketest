

package org.apache.commons.dbcp2;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.datasources.PerUserPoolDataSource;
import org.apache.commons.dbcp2.datasources.SharedPoolDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests JNID bind and lookup for DataSource implementations.
 * Demonstrates problem indicated in BZ #38073.
 */
public class TestJndi {
    /**
     * The subcontext where the data source is bound.
     */
    protected static final String JNDI_SUBCONTEXT = "jdbc";

    /**
     * the full JNDI path to the data source.
     */
    protected static final String JNDI_PATH = JNDI_SUBCONTEXT + "/"
            + "jndiTestDataSource";

    /** JNDI context to use in tests **/
    protected Context context;

    /**
     * Binds a DataSource into JNDI.
     *
     * @throws Exception if creation or binding fails.
     */
    protected void bindDataSource(final DataSource dataSource) throws Exception {
        context.bind(JNDI_PATH, dataSource);
    }

    /**
     * Binds a DataSource to the JNDI and checks that we have successfully
     * bound it by looking it up again.
     *
     * @throws Exception if the bind, lookup or connect fails
     */
    protected void checkBind(final DataSource dataSource) throws Exception {
        bindDataSource(dataSource);
        retrieveDataSource();
    }

    /**
     * Retrieves (or creates if it does not exist) an InitialContext.
     *
     * @return the InitialContext.
     * @throws NamingException if the InitialContext cannot be retrieved
     *         or created.
     */
    protected InitialContext getInitialContext() throws NamingException {
        final Hashtable<String, String> environment = new Hashtable<>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY,
                org.apache.naming.java.javaURLContextFactory.class.getName());
        return new InitialContext(environment);
    }

    /**
     * Retrieves a DataSource from JNDI.
     *
     * @throws Exception if the JNDI lookup fails or no DataSource is bound.
     */
    protected DataSource retrieveDataSource() throws Exception {
        final Context ctx = getInitialContext();
        final DataSource dataSource = (DataSource) ctx.lookup(JNDI_PATH);

        if (dataSource == null) {
            fail("DataSource should not be null");
        }
        return dataSource;
    }

    @BeforeEach
    public void setUp() throws Exception {
        context = getInitialContext();
        context.createSubcontext(JNDI_SUBCONTEXT);
    }

    @AfterEach
    public void tearDown() throws Exception {
        context.unbind(JNDI_PATH);
        context.destroySubcontext(JNDI_SUBCONTEXT);
    }

    /**
     * Test BasicDatasource bind and lookup
     *
     * @throws Exception
     */
    @Test
    public void testBasicDataSourceBind() throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        checkBind(dataSource);
    }

    /**
     * Test PerUserPoolDataSource bind and lookup
     *
     * @throws Exception
     */
    @Test
    public void testPerUserPoolDataSourceBind() throws Exception {
        final PerUserPoolDataSource dataSource = new PerUserPoolDataSource();
        checkBind(dataSource);
    }

    /**
     * Test SharedPoolDataSource bind and lookup
     *
     * @throws Exception
     */
    @Test
    public void testSharedPoolDataSourceBind() throws Exception {
        final SharedPoolDataSource dataSource = new SharedPoolDataSource();
        checkBind(dataSource);
    }
}
