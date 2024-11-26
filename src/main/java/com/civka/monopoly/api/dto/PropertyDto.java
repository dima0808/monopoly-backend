package com.civka.monopoly.api.dto;

import com.civka.monopoly.api.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class PropertyDto {

    private Long id;

    private Member member;

    private Integer position;

    private List<UpgradeDto> upgrades;

    private Integer mortgage;

    private Integer goldOnStep;
    private Integer tourismOnStep;

    private Integer goldPerTurn;

    private List<RequirementDto> upgradeRequirements;
}