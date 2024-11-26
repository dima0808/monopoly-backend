package com.civka.monopoly.api.dto;

import com.civka.monopoly.api.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class RequirementDto {

    private Property.Upgrade level;

    private Map<Requirement, Boolean> requirements;

    public enum Requirement {
        OWN_DEER_OR_FURS, OWN_CAMP,
        MAKE_ONE_ROUND, MAKE_TWO_ROUNDS, MAKE_THREE_ROUNDS, MAKE_FOUR_ROUNDS, MAKE_FIVE_ROUNDS,
        MAKE_ONE_TURN, MAKE_TWO_TURNS, MAKE_THREE_TURNS, MAKE_FOUR_TURNS, MAKE_FIVE_TURNS,
        HAVE_LOW_GOLD_PER_TURN, HAVE_MEDIUM_GOLD_PER_TURN, HAVE_HIGH_GOLD_PER_TURN,
        HAVE_LOW_GOLD_CAP, HAVE_MEDIUM_GOLD_CAP, HAVE_HIGH_GOLD_CAP,
        HAVE_LOW_TOURISM, HAVE_MEDIUM_TOURISM, HAVE_HIGH_TOURISM,
        HAVE_LOW_STRENGTH, HAVE_MEDIUM_STRENGTH, HAVE_HIGH_STRENGTH,
        ON_CLASSICAL_ERA, ON_MEDIEVAL_ERA, ON_RENAISSANCE_ERA, ON_INDUSTRIAL_ERA, ON_MODERN_ERA, ON_ATOMIC_ERA, ON_INFORMATION_ERA,
        HAVE_TWO_RESOURCES,
        HAVE_TWO_WONDERS,
        OWN_RESEARCH_GRANTS,
        WIDE_EMPIRE, SUPER_WIDE_EMPIRE,
        TALL_EMPIRE, SUPER_TALL_EMPIRE,
        OWN_ENCAMPMENT,
        OWN_CAMPUS, OWN_LIBRARY, OWN_UNIVERSITY,
        OWN_GOVERNMENT_PLAZA,
        NOT_OWN_GOVERNMENT_PLAZA,
        OWN_ENTERTAINMENT_COMPLEX, OWN_ARENA, OWN_STADIUM,
        OWN_HARBOR,
        OWN_INDUSTRIAL_ZONE, OWN_FACTORY,
        OWN_COMMERCIAL_HUB, OWN_STOCK_EXCHANGE,
        OWN_SPACEPORT_OR_LAB
    }
}
