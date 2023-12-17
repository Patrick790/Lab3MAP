package com.example.lab7v3.observer;

import com.example.lab7v3.domain.User;

public class UserChangeEvent implements Event{

    private UserChangeEventType type;

    private User oldUser;

    private User newUser;

    public UserChangeEvent(UserChangeEventType type, User oldUser, User newUser){
        this.type = type;
        this.oldUser = oldUser;
        this.newUser = newUser;
    }

    public UserChangeEventType getType() {
        return type;
    }

    public void setType(UserChangeEventType type){
        this.type = type;
    }

    public User getOldUser(){
        return oldUser;
    }

    public void setOldUser(User oldUser) {
        this.oldUser = oldUser;
    }

    public User getNewUser() {
        return newUser;
    }

    public void setNewUser(User newUser) {
        this.newUser = newUser;
    }
}
