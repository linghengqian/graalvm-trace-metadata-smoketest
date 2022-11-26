

package com.lingh.entity;

import java.io.Serializable;

public class Account implements Serializable {

    private static final long serialVersionUID = -5889545274302226912L;

    private long accountId;

    private int userId;

    private String status;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(final int userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("account_id: %s, user_id: %s, status: %s", accountId, userId, status);
    }
}
