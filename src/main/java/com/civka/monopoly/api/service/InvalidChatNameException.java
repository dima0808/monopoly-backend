package com.civka.monopoly.api.service;

public class InvalidChatNameException extends RuntimeException {

    public InvalidChatNameException() {
        super("Private chat name must contain exactly two usernames");
    }
}