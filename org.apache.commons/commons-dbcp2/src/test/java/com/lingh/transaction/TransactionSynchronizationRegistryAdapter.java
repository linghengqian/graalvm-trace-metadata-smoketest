

package com.lingh.transaction;

import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * A TransactionSynchronizationRegistry adapter.
 */
public class TransactionSynchronizationRegistryAdapter implements TransactionSynchronizationRegistry {

    @Override
    public Object getResource(final Object arg0) {
        return null;
    }

    @Override
    public boolean getRollbackOnly() {
        return false;
    }

    @Override
    public Object getTransactionKey() {
        return null;
    }

    @Override
    public int getTransactionStatus() {
        return 0;
    }

    @Override
    public void putResource(final Object arg0, final Object arg1) {
        // Noop
    }

    @Override
    public void registerInterposedSynchronization(final Synchronization arg0) {
        // Noop
    }

    @Override
    public void setRollbackOnly() {
        // Noop
    }

}
