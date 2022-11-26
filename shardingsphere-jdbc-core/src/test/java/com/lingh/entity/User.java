

package com.lingh.entity;

import java.io.Serializable;

public class User implements Serializable {
    
    private static final long serialVersionUID = 263434701950670170L;
    
    private int userId;
    
    private String username;
    
    private String pwd;
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(final int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(final String username) {
        this.username = username;
    }
    
    public String getPwd() {
        return pwd;
    }
    
    public void setPwd(final String pwd) {
        this.pwd = pwd;
    }
    
    @Override
    public String toString() {
        return String.format("user_id: %d, username: %s, pwd: %s", userId, username, pwd);
    }
}
