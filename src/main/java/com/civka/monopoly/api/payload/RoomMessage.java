package com.civka.monopoly.api.payload;

import com.civka.monopoly.api.dto.PropertyDto;
import com.civka.monopoly.api.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class RoomMessage {

    private MessageType type;
    private String content;
    private Room room;
    private PropertyDto property;

    public enum MessageType {
        CREATE,
        JOIN,
        LEAVE,
        KICK,
        DELETE,
        START,
        END_TURN, FORCE_END_TURN,
        BUY_PROPERTY,
        UPGRADE_PROPERTY,
        PAY_RENT,
        CHEAT_ADD_GOLD,
        CHEAT_ADD_STRENGTH,
        CHEAT_ADD_EVENT,
        DOWNGRADE_PROPERTY,
        PROJECTS,
        GREAT_LIBRARY_PAYMENT,
        BIG_BEN_PAYMENT,
    }
}
