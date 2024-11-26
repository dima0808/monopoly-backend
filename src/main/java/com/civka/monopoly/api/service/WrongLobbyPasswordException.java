package com.civka.monopoly.api.service;

public class WrongLobbyPasswordException extends RuntimeException {

    public WrongLobbyPasswordException() {
        super("Wrong lobby password");
    }
}

