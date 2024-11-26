package com.civka.monopoly.api.payload;

import com.civka.monopoly.api.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class PlayerMessage {

    private MessageType type;
    private String content;
    private Member member;

    public enum MessageType {
        CHANGE_COLOR,
        CHANGE_CIVILIZATION,
        ROLL_DICE,
        BYPASS_START,
        TOURIST,
        BERMUDA
    }
}
