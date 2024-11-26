package com.civka.monopoly.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IllegalRoomSizeException extends RuntimeException {

    public IllegalRoomSizeException(Integer size, Integer maxSize) {
        super(String.format("Room with size %d cannot be created (2-%d allowed)", size,maxSize));
    }
}