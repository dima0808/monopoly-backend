package com.civka.monopoly.api.service;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String credentials) {
        super(String.format("User with credentials '%s' not found", credentials));
    }

    public UserNotFoundException(Long id) {
        super(String.format("User with id %s not found", id));
    }
}
