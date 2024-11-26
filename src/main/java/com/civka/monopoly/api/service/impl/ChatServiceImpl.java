package com.civka.monopoly.api.service.impl;

import com.civka.monopoly.api.dto.ChatMessageDto;
import com.civka.monopoly.api.dto.ContactDto;
import com.civka.monopoly.api.entity.Chat;
import com.civka.monopoly.api.entity.ChatMessage;
import com.civka.monopoly.api.entity.User;
import com.civka.monopoly.api.payload.NotificationResponse;
import com.civka.monopoly.api.repository.ChatRepository;
import com.civka.monopoly.api.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Chat create(String chatName, Boolean isLobbyChat) {
        Chat newChat = Chat.builder()
                .name(chatName)
                .isLobbyChat(isLobbyChat)
                .build();
        return chatRepository.save(newChat);
    }

    @Override
    public Chat findByName(String chatName) {
        return chatRepository.findByName(chatName)
                .orElseThrow(() -> new ChatNotFoundException(chatName));
    }

    @Override
    public Chat findPrivateChatByName(String chatName) {
        return chatRepository.findByName(chatName)
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .name(chatName)
                            .isLobbyChat(false)
                            .unreadMessages(0)
                            .build();
                    return chatRepository.save(newChat);
                });
    }

    @Override
    public List<Chat> findAllByUsername(String username) {
        return chatRepository.findAllByUsername(username);
    }

    @Override
    public List<ContactDto> getUserContacts(String username) {
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals(username)) {
            throw new UserNotAllowedException();
        }
        List<Chat> chats = findAllByUsername(username);
        List<ContactDto> contacts = new ArrayList<>();

        for (Chat chat : chats) {
            if (!chat.getMessages().isEmpty()) {
                ChatMessage lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
                User user = userService.findByUsername(chat.getName().replace(username, "").trim());
                contacts.add(ContactDto.builder()
                        .nickname(user.getNickname())
                        .lastMessage(lastMessage.toDto())
                        .unreadMessages(chat.getUnreadMessages())
                        .build());
            }
        }

        return contacts;
    }

    @Override
    public List<ContactDto> getUserSuggestedContacts(String username, String suggestedNickname) {
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals(username)) {
            throw new UserNotAllowedException();
        }
        List<User> suggestedUsers = userService.findUsersByNicknameContaining(suggestedNickname);
        List<ContactDto> suggestedContacts = new ArrayList<>();

        for (User suggestedUser : suggestedUsers) {
            List<String> usernames = Arrays.asList(username, suggestedUser.getUsername());
            Collections.sort(usernames);
            String chatName = String.join(" ", usernames);
            Optional<Chat> chatOptional = chatRepository.findByName(chatName);

            if (chatOptional.isPresent()) {
                Chat chat = chatOptional.get();
                if (!chat.getMessages().isEmpty()) {
                    ChatMessage lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
                    suggestedContacts.add(ContactDto.builder()
                            .nickname(suggestedUser.getNickname())
                            .lastMessage(lastMessage.toDto())
                            .unreadMessages(chat.getUnreadMessages())
                            .build());
                } else {
                    suggestedContacts.add(ContactDto.builder()
                            .nickname(suggestedUser.getNickname())
                            .build());
                }
            } else {
                suggestedContacts.add(ContactDto.builder()
                        .nickname(suggestedUser.getNickname())
                        .build());
            }
        }
        return suggestedContacts;
    }

    @Override
    public Chat save(Chat chat) {
        return chatRepository.save(chat);
    }

    @Override
    public ChatMessage sendPublicMessage(String chatName, ChatMessageDto chatMessageDto) {
        Chat chat = findByName(chatName);
        return chatMessageService.save(chat, chatMessageDto);
    }

    @Override
    public ChatMessage sendPrivateMessage(String chatName, ChatMessageDto chatMessageDto) {
        Chat chat = findPrivateChatByName(chatName);
        chat.setUnreadMessages(chat.getUnreadMessages() + 1);
        ChatMessage chatMessage = chatMessageService.save(chat, chatMessageDto);
        messagingTemplate.convertAndSendToUser(chatMessage.getReceiver().getUsername(), "/chat/private/" + chatMessage.getSender().getUsername(), chatMessage);
        messagingTemplate.convertAndSendToUser(chatMessage.getSender().getUsername(), "/chat/private/" + chatMessage.getReceiver().getUsername(), chatMessage);
        NotificationResponse notificationResponse = NotificationResponse.builder()
                .timestamp(LocalDateTime.now())
                .type(NotificationResponse.NotificationType.MESSAGE)
                .sender(chatMessage.getSender().getNickname())
                .message(chatMessage.getContent())
                .build();
        messagingTemplate.convertAndSendToUser(chatMessage.getReceiver().getUsername(), "/queue/notifications", notificationResponse);
        ContactDto contactDtoForReceiver = ContactDto.builder()
                .nickname(chatMessage.getSender().getNickname())
                .lastMessage(chatMessage.toDto())
                .unreadMessages(chat.getUnreadMessages())
                .build();
        ContactDto contactDtoForSender = ContactDto.builder()
                .nickname(chatMessage.getReceiver().getNickname())
                .lastMessage(chatMessage.toDto())
                .unreadMessages(chat.getUnreadMessages())
                .build();
        messagingTemplate.convertAndSendToUser(chatMessage.getReceiver().getUsername(), "/chat/contacts", contactDtoForReceiver);
        messagingTemplate.convertAndSendToUser(chatMessage.getSender().getUsername(), "/chat/contacts", contactDtoForSender);
        return chatMessage;
    }

    @Override
    public void readMessages(String chatName, String reader) {
        Chat chat = findByName(chatName);
        ChatMessage lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
        if (chat.getUnreadMessages() > 0 && lastMessage.getReceiver().getUsername().equals(reader)) {
            chat.setUnreadMessages(0);
            chatRepository.save(chat);
            String sender = chat.getName().replace(reader, "").trim();
            ContactDto contactDtoForReader = ContactDto.builder()
                    .nickname(userService.findByUsername(sender).getNickname())
                    .lastMessage(lastMessage.toDto())
                    .unreadMessages(0)
                    .build();
            ContactDto contactDtoForSender = ContactDto.builder()
                    .nickname(userService.findByUsername(reader).getNickname())
                    .lastMessage(lastMessage.toDto())
                    .unreadMessages(0)
                    .build();
            messagingTemplate.convertAndSendToUser(reader, "/chat/contacts", contactDtoForReader);
            messagingTemplate.convertAndSendToUser(sender, "/chat/contacts", contactDtoForSender);
        }
    }

    @Override
    public void clearMessages(String chatName, String admin) {
        User adminUser = userService.findByUsername(admin);
        if (adminUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            Chat chat = findByName(chatName);
            chat.getMessages().clear();
            chatRepository.save(chat);
        } else {
            throw new UserNotAllowedException();
        }
    }

    @Override
    public void clearMessages(Integer clearCount, String chatName, String admin) {
        if (clearCount < 1) {
            throw new InvalidCommandException();
        }
        User adminUser = userService.findByUsername(admin);
        if (adminUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            Chat chat = findByName(chatName);
            int size = chat.getMessages().size();
            for (int i = 0; i < clearCount && size > 0; i++) {
                chat.getMessages().remove(size - 1);
                size--;
            }
            chatRepository.save(chat);
        } else {
            throw new UserNotAllowedException();
        }
    }

    @Override
    public void deleteByName(String chatName) {
        chatRepository.deleteByName(chatName);
    }
}
