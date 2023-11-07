package org.example.domain;

import java.time.LocalDateTime;

public class Message {
    private User from;
    private User to;
    private String message;
    private LocalDateTime datetime;

    public Message(User from, User to, String message){
        this.from = from;
        this.to = to;
        this.message = message;
        datetime = LocalDateTime.now();
    }

    public void setMessage(String message){
        this.message = message;
    }

    public User getFrom() {
        return from;
    }

    public User getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }
}