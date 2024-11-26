package com.civka.monopoly.api.service;

public class RoomFullException extends RuntimeException {

    public RoomFullException(Long roomId, Integer size) {
        super(String.format("Room with id %d is full (%d/%d)", roomId, size, size));
    }
}
