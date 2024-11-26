package com.civka.monopoly.api.service;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String role) {
        super(String.format("Role with name '%s' not found", role));
    }
}