package com.civka.monopoly.api.service;

public class ChatNotFoundException extends RuntimeException {

    public ChatNotFoundException(String name) {
        super(String.format("Chat with name '%s' not found", name));
    }
}
