

package org.apache.commons.dbcp2.transaction;

import javax.transaction.*;
import javax.transaction.xa.XAResource;

/**
 * A Transaction adapter.
 */
public class TransactionAdapter implements Transaction {

    @Override
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException,
        SecurityException, SystemException {
        // Noop
    }

    @Override
    public boolean delistResource(final XAResource arg0, final int arg1) throws IllegalStateException, SystemException {
        return false;
    }

    @Override
    public boolean enlistResource(final XAResource arg0) throws IllegalStateException, RollbackException, SystemException {
        return false;
    }

    @Override
    public int getStatus() throws SystemException {
        return 0;
    }

    @Override
    public void registerSynchronization(final Synchronization arg0)
        throws IllegalStateException, RollbackException, SystemException {
        // Noop
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
        // Noop
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        // Noop
    }

}
