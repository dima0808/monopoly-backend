package com.civka.monopoly.api.service;

public class RoomAlreadyExistException extends RuntimeException {

    public RoomAlreadyExistException(String name) {
        super("Room with name " + name + " already exists");
    }
}
