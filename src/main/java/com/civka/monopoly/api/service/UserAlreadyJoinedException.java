package com.civka.monopoly.api.service;

public class UserAlreadyJoinedException extends RuntimeException {

    public UserAlreadyJoinedException(String username) {
        super(String.format("User %s already joined to the room", username));
    }
}
