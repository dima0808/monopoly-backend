package com.civka.monopoly.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class StatsDto {

    private Integer gold;
    private Integer goldPerTurn;
    private Integer strength;
    private Integer tourism;

    private Integer plus;
    private Integer minus;

    private Integer discount;
}
