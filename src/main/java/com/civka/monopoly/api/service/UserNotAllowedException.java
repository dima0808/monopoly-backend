package com.civka.monopoly.api.service;

public class UserNotAllowedException extends RuntimeException {

    public UserNotAllowedException() {
        super("User is not allowed to perform this action");
    }
}

