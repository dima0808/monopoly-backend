package com.civka.monopoly.api.service;

import com.civka.monopoly.api.dto.ChatMessageDto;
import com.civka.monopoly.api.dto.ContactDto;
import com.civka.monopoly.api.entity.Chat;
import com.civka.monopoly.api.entity.ChatMessage;

import java.util.List;

public interface ChatService {

    Chat create(String chatName, Boolean isLobbyChat);

    Chat findByName(String chatName);

    Chat findPrivateChatByName(String chatName);

    List<Chat> findAllByUsername(String username);

    List<ContactDto> getUserContacts(String username);

    List<ContactDto> getUserSuggestedContacts(String username, String suggestedNickname);

    Chat save(Chat chat);

    ChatMessage sendPublicMessage(String chatName, ChatMessageDto chatMessageDto);

    ChatMessage sendPrivateMessage(String chatName, ChatMessageDto chatMessageDto);

    void readMessages(String chatName, String reader);

    void clearMessages(String chatName, String admin);

    void clearMessages(Integer clearCount, String chatName, String admin);

    void deleteByName(String chatName);
}
