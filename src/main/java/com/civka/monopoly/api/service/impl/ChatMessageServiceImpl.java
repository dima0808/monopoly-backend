package com.civka.monopoly.api.service.impl;

import com.civka.monopoly.api.dto.ChatMessageDto;
import com.civka.monopoly.api.entity.Chat;
import com.civka.monopoly.api.entity.ChatMessage;
import com.civka.monopoly.api.repository.ChatRepository;
import com.civka.monopoly.api.service.ChatMessageService;
import com.civka.monopoly.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatRepository chatRepository;
    private final UserService userService;

    @Value("${monopoly.app.chat.max-size}")
    private Integer maxChatSize;

    @Value("${monopoly.app.room.chat.max-size}")
    private Integer maxLobbyChatSize;

    @Override
    public ChatMessage save(Chat chat, ChatMessageDto chatMessageDto) {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(chatMessageDto.getType())
                .sender(chatMessageDto.getSender() != null ?
                        userService.findByUsername(chatMessageDto.getSender()) : null)
                .content(chatMessageDto.getContent())
                .timestamp(LocalDateTime.now())
                .receiver(chatMessageDto.getReceiver() != null ?
                        userService.findByUsername(chatMessageDto.getReceiver()) : null)
                .chat(chat)
                .build();

        chat.getMessages().add(chatMessage);
        if (chat.getMessages().size() > (chat.getIsLobbyChat() ? maxLobbyChatSize : maxChatSize)) {
            chat.getMessages().remove(0);
        }

        chatRepository.save(chat);
        return chatMessage;
    }
}