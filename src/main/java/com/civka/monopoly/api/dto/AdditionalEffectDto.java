package com.civka.monopoly.api.dto;

import com.civka.monopoly.api.entity.AdditionalEffect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdditionalEffectDto {

    private AdditionalEffect.AdditionalEffectType type;
    private Integer turnsLeft;
    private Integer goldPerTurn;
}
