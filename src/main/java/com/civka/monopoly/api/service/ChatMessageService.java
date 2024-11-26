package com.civka.monopoly.api.service;

import com.civka.monopoly.api.dto.ChatMessageDto;
import com.civka.monopoly.api.entity.Chat;
import com.civka.monopoly.api.entity.ChatMessage;

public interface ChatMessageService {

    ChatMessage save(Chat chat, ChatMessageDto chatMessageDto);
}
