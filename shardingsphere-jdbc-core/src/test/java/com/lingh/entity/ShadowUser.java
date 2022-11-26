

package com.lingh.entity;

import java.io.Serializable;

public class ShadowUser implements Serializable {
    
    private static final long serialVersionUID = -6711618386636677067L;
    
    private int userId;
    
    private int userType;
    
    private String username;
    
    private String usernamePlain;
    
    private String pwd;
    
    private String assistedQueryPwd;
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getUserType() {
        return userType;
    }
    
    public void setUserType(int userType) {
        this.userType = userType;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsernamePlain() {
        return usernamePlain;
    }
    
    public void setUsernamePlain(String usernamePlain) {
        this.usernamePlain = usernamePlain;
    }
    
    public String getPwd() {
        return pwd;
    }
    
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    
    public String getAssistedQueryPwd() {
        return assistedQueryPwd;
    }
    
    public void setAssistedQueryPwd(String assistedQueryPwd) {
        this.assistedQueryPwd = assistedQueryPwd;
    }
    
    @Override
    public String toString() {
        return String.format("user_id: %d, user_type: %d, username: %s, username_plain: %s, pwd: %s, assisted_query_pwd: %s", userId, userType, username, usernamePlain, pwd,
                assistedQueryPwd);
    }
}
