package com.civka.monopoly.api.dto;

import com.civka.monopoly.api.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UpgradeDto {

    private Property.Upgrade level;

    private Boolean isOwned;

    private Integer goldOnStep;
    private Integer tourismOnStep;
    private Integer goldPerTurn;
    private Integer price;
}
