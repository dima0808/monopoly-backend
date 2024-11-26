package com.civka.monopoly.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private String nickname;
    private String email;
    private String password;

    private Integer elo;
    private Integer matchesPlayed;
    private Integer matchesWon;
    private Float averagePlacement;
}
