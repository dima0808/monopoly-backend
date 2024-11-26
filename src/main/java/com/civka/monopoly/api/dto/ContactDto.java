package com.civka.monopoly.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ContactDto {

    private String nickname;
    private ChatMessageDto lastMessage;
    private Integer unreadMessages;
}