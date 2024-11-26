package com.civka.monopoly.api.dto;

import com.civka.monopoly.api.entity.ArmySpending;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ArmySpendingDto {

    private ArmySpending armySpending;
    private Integer gold;
    private Integer strength;
}
