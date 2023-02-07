
package org.apache.commons.dbcp2.managed;

import org.apache.geronimo.transaction.manager.TransactionImpl;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.junit.jupiter.api.Test;

import javax.transaction.xa.XAResource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TestSuite for TransactionContext
 */
public class TestTransactionContext {

    /**
     * Transaction that always fails enlistResource.
     */
    private static class UncooperativeTransaction extends TransactionImpl {
        public UncooperativeTransaction() {
            super(null, null);
        }
        @Override
        public synchronized boolean enlistResource(final XAResource xaRes) {
            return false;
        }
    }

    /**
     * JIRA: DBCP-428
     */
    @Test
    public void testSetSharedConnectionEnlistFailure() throws Exception {
        try (final BasicManagedDataSource basicManagedDataSource = new BasicManagedDataSource()) {
            basicManagedDataSource.setTransactionManager(new TransactionManagerImpl());
            basicManagedDataSource.setDriverClassName("org.apache.commons.dbcp2.TesterDriver");
            basicManagedDataSource.setUrl("jdbc:apache:commons:testdriver");
            basicManagedDataSource.setUsername("userName");
            basicManagedDataSource.setPassword("password");
            basicManagedDataSource.setMaxIdle(1);
            try (final ManagedConnection<?> conn = (ManagedConnection<?>) basicManagedDataSource.getConnection()) {
                final UncooperativeTransaction transaction = new UncooperativeTransaction();
                final TransactionContext transactionContext = new TransactionContext(
                        basicManagedDataSource.getTransactionRegistry(), transaction);
                assertThrows(SQLException.class, () -> transactionContext.setSharedConnection(conn));
            }
        }
    }

}

