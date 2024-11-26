package com.civka.monopoly.api.controller;

import com.civka.monopoly.api.payload.ChatMessageResponse;
import com.civka.monopoly.api.service.ChatService;
import com.civka.monopoly.api.service.InvalidCommandException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CommandController {

    private final ChatService chatService;

    @MessageMapping("/chat/clear/{chatName}")
    @SendTo("/topic/chat/{chatName}")
    public ChatMessageResponse clearMessages(@Header("username") String admin,
                                             @Header("clearCount") String clearCountStr,
                                             @DestinationVariable String chatName) {
        if (clearCountStr.equals("All")) {
            chatService.clearMessages(chatName, admin);
        } else {
            try {
                Integer clearCount = Integer.parseInt(clearCountStr);
                chatService.clearMessages(clearCount, chatName, admin);
            } catch (NumberFormatException e) {
                throw new InvalidCommandException();
            }
        }
        return ChatMessageResponse.builder()
                .type(ChatMessageResponse.MessageType.CLEAR)
                .content(clearCountStr + " messages cleared by " + admin)
                .build();
    }
}
