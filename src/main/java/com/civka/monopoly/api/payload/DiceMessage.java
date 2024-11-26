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
public class DiceMessage {

    private PlayerMessage.MessageType type;
    private String content;
    private Member member;
    private Integer firstRoll;
    private Integer secondRoll;
}
