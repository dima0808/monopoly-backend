package com.civka.monopoly.api.service;

import org.springframework.stereotype.Component;

@Component
public class InvalidCommandException extends RuntimeException {

    public InvalidCommandException() {
        super("Invalid command syntax");
    }
}