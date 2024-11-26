package com.civka.monopoly.api.payload;

import com.civka.monopoly.api.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class EventMessage {

    private MessageType type;
    private String content;
    private Event event;

    public enum MessageType {
        ADD_EVENT,
        DELETE_EVENT, DELETE_ALL_EVENTS,
    }
}
