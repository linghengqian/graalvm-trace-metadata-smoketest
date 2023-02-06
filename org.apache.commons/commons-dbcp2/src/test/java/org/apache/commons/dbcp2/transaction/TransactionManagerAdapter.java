

package org.apache.commons.dbcp2.transaction;

import javax.transaction.*;

/**
 * A TransactionManager adapter.
 */
public class TransactionManagerAdapter implements TransactionManager {

    @Override
    public void begin() throws NotSupportedException, SystemException {
        // Noop
    }

    @Override
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException,
        RollbackException, SecurityException, SystemException {
        // Noop
    }

    @Override
    public int getStatus() throws SystemException {
        return 0;
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return null;
    }

    @Override
    public void resume(final Transaction arg0) throws IllegalStateException, InvalidTransactionException, SystemException {
        // Noop
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        // Noop
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        // Noop
    }

    @Override
    public void setTransactionTimeout(final int arg0) throws SystemException {
        // Noop
    }

    @Override
    public Transaction suspend() throws SystemException {
        return null;
    }

}
