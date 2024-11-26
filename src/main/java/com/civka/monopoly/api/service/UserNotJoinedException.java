package com.civka.monopoly.api.service;

public class UserNotJoinedException extends RuntimeException {

    public UserNotJoinedException(String username) {
        super(String.format("User %s is not joined to the room", username));
    }
}

