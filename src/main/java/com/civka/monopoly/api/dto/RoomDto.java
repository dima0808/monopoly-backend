package com.civka.monopoly.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomDto {

    private String name;
    private Integer size;
    private String password;
}
