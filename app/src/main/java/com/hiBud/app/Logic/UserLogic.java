package com.hiBud.app.Logic;

import com.hiBud.app.Firebase.User;

public class UserLogic {

    private String key;
    private User user;

    public UserLogic(String key, User user) {
        this.key = key;
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
