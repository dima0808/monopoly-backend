package com.civka.monopoly.api.service;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(Long memberId) {
        super(String.format("Member with id %d not found", memberId));
    }
}
