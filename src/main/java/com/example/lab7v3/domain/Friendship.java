package com.example.lab7v3.domain;

import java.time.LocalDateTime;

public class Friendship extends Entity<Tuple<Long, Long>> {
    LocalDateTime friendsFrom;
    public Friendship(){
        friendsFrom = LocalDateTime.now();
    }
    /**
     *
     * @return the date when the friendship was created
     */
    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    public void setFriendsFrom(LocalDateTime friendsFrom) {
        this.friendsFrom = friendsFrom;
    }
}


