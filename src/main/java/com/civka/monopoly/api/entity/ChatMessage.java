package com.civka.monopoly.api.entity;

import com.civka.monopoly.api.dto.ChatMessageDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private User sender;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private MessageType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private Chat chat;

    @ManyToOne(fetch = FetchType.EAGER)
    private User receiver;

    public ChatMessageDto toDto() {
        return ChatMessageDto.builder()
                .sender(sender.getUsername())
                .content(content)
                .timestamp(timestamp)
                .receiver(receiver.getUsername())
                .build();
    }

    public enum MessageType {
        SYSTEM_ROLL_DICE,
        SYSTEM_BERMUDA,
        SYSTEM_PAY_RENT,
        SYSTEM_BUY_PROPERTY,
        SYSTEM_UPGRADE_PROPERTY,
        SYSTEM_BYPASS_START,
        SYSTEM_DOWNGRADE_PROPERTY,
        SYSTEM_REDEMPTION_PROPERTY,
        SYSTEM_MORTGAGE_PROPERTY, SYSTEM_WINNER,
        SYSTEM_BIG_BEN,
        SYSTEM_SCIENCE_PROJECT,
        SYSTEM_CONCERT,
    }
}
