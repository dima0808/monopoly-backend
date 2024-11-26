package com.civka.monopoly.api.payload;

import com.civka.monopoly.api.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatMessageResponse {

    private MessageType type;
    private String content;
    private ChatMessage chatMessage;

    public enum MessageType {
        CLEAR,
        DELETE
    }
}
