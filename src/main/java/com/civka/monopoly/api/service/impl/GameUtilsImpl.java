package com.civka.monopoly.api.service.impl;

import com.civka.monopoly.api.config.GameProperties;
import com.civka.monopoly.api.dto.*;
import com.civka.monopoly.api.entity.*;
import com.civka.monopoly.api.service.GameUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GameUtilsImpl implements GameUtils {

    private final GameProperties gameProperties;

    @Override
    public int calculateGoldOnStep(Property property) {
        int onStep = 0;
        if (property.getMortgage() != -1) {
            return 0;
        }
        for (Property.Upgrade upgrade : property.getUpgrades()) {
            Integer upgradeOnStep = gameProperties.getGoldOnStepByPositionAndLevel(property.getPosition(), upgrade);
            if (upgradeOnStep != null) {
                onStep += upgradeOnStep;
            }
        }
        return onStep;
    }

    @Override
    public int getGoldOnStepByLevel(Integer position, Property.Upgrade level) {
        return gameProperties.getGoldOnStepByPositionAndLevel(position, level);
    }

    @Override
    public int calculateTourismOnStep(Property property) {
        int onStep = 0;
        if (property.getMortgage() != -1) {
            return 0;
        }
        for (Property.Upgrade upgrade : property.getUpgrades()) {
            Integer upgradeOnStep = gameProperties.getTourismOnStepByPositionAndLevel(property.getPosition(), upgrade);
            if (upgradeOnStep != null) {
                onStep += upgradeOnStep;
            }
        }
        return onStep;
    }

    @Override
    public int getTourismOnStepByLevel(Integer position, Property.Upgrade level) {
        return gameProperties.getTourismOnStepByPositionAndLevel(position, level);
    }

    @Override
    public int calculateGoldPerTurn(Property property) {
        int perTurn = 0;
        if (property.getMortgage() != -1) {
            return 0;
        }
        for (Property.Upgrade upgrade : property.getUpgrades()) {
            Integer upgradePerTurn = gameProperties.getPerTurnByPositionAndLevel(property.getPosition(), upgrade);
            if (upgradePerTurn != null) {
                perTurn += upgradePerTurn;
            }
        }
        return perTurn;
    }

    @Override
    public int calculateGeneralGoldPerTurn(Member member) {
        return member.getProperties().stream()
                .map(this::calculateGoldPerTurn)
                .reduce(0, Integer::sum);
    }

    @Override
    public int getGoldPerTurnByLevel(Integer position, Property.Upgrade level) {
        return gameProperties.getPerTurnByPositionAndLevel(position, level);
    }

    @Override
    public int getPriceByPositionAndLevel(Integer position, Property.Upgrade level) {
        return gameProperties.getPriceByPositionAndLevel(position, level);
    }

    @Override
    public int getScoreByPositionAndLevel(Integer position, Property.Upgrade level) {
        return gameProperties.getScoreByPositionAndLevel(position, level);
    }

    @Override
    public int getStrengthFromArmySpending(ArmySpending armySpendingLevel) {
        return gameProperties.getStrengthFromArmySpending(armySpendingLevel);
    }

    @Override
    public int getGoldFromArmySpending(ArmySpending armySpendingLevel) {
        return gameProperties.getGoldFromArmySpending(armySpendingLevel);
    }

    @Override
    public double getDiscountByAdditionalEffect(AdditionalEffect.AdditionalEffectType type) {
        return gameProperties.getDiscountByAdditionalEffect(type);
    }

    @Override
    public Map<String, String> getUpgradeProperties() {
        return gameProperties.getUpgrade();
    }

    @Override
    public List<RequirementDto> getRequirements(Integer position, Member member) {
        List<RequirementDto> allRequirements = new ArrayList<>();
        for (Property.Upgrade upgrade : List.of(Property.Upgrade.LEVEL_1, Property.Upgrade.LEVEL_2,
                Property.Upgrade.LEVEL_3, Property.Upgrade.LEVEL_4, Property.Upgrade.LEVEL_4_1,
                Property.Upgrade.LEVEL_4_2, Property.Upgrade.LEVEL_4_3)) {
            String requirements = gameProperties.getRequirement().get(position + "." + upgrade);
            if (requirements != null) {
                Map<RequirementDto.Requirement, Boolean> requirementMap = new HashMap<>();
                for (String req : requirements.split(",")) {
                    RequirementDto.Requirement requirement = RequirementDto.Requirement.valueOf(req);
                    requirementMap.put(requirement, calculateRequirement(requirement, position, member));
                }
                allRequirements.add(RequirementDto.builder()
                        .level(upgrade)
                        .requirements(requirementMap)
                        .build());
            }
        }
        return allRequirements;
    }

    @Override
    public List<UpgradeDto> getUpgrades(Integer position, Property property) {
        List<UpgradeDto> upgrades = new ArrayList<>();
        for (String upgrade : gameProperties.getUpgrade().get(position.toString()).split(",")) {
            Property.Upgrade level = Property.Upgrade.valueOf(upgrade);
            UpgradeDto upgradeDto = UpgradeDto.builder()
                    .level(level)
                    .isOwned(property != null && property.getUpgrades().contains(level))
                    .goldOnStep(gameProperties.getGoldOnStepByPositionAndLevel(position, level))
                    .tourismOnStep(gameProperties.getTourismOnStepByPositionAndLevel(position, level))
                    .goldPerTurn(gameProperties.getPerTurnByPositionAndLevel(position, level))
                    .price(gameProperties.getPriceByPositionAndLevel(position, level))
                    .build();
            upgrades.add(upgradeDto);
        }
        return upgrades;
    }

    @Override
    public List<ArmySpendingDto> getArmySpendings() {
        List<ArmySpendingDto> armySpendings = new ArrayList<>();
        for (ArmySpending armySpending : ArmySpending.values()) {
            ArmySpendingDto armySpendingDto = ArmySpendingDto.builder()
                    .armySpending(armySpending)
                    .gold(gameProperties.getGoldFromArmySpending(armySpending))
                    .strength(gameProperties.getStrengthFromArmySpending(armySpending))
                    .build();
            armySpendings.add(armySpendingDto);
        }
        return armySpendings;
    }

    @Override
    public List<ProjectSettingsDto> getProjectSettings() {
        List<ProjectSettingsDto> projectSettings = new ArrayList<>();
        for (ProjectType projectType : ProjectType.values()) {
            Map<String, StatsDto> stats = new HashMap<>();
            for (Property.Upgrade level : List.of(Property.Upgrade.LEVEL_1, Property.Upgrade.LEVEL_2,
                    Property.Upgrade.LEVEL_3, Property.Upgrade.LEVEL_4)) {
                StatsDto statsDto = StatsDto.builder()
                        .gold(getProjectGoldByLevel(projectType, level))
                        .strength(getProjectStrengthByLevel(projectType, level))
                        .tourism(getProjectTourismByLevel(projectType, level))
                        .build();
                if (projectType == ProjectType.BREAD_AND_CIRCUSES) {
                    statsDto.setPlus(getBreadAndCircusesByLevel(level, true));
                    statsDto.setMinus(getBreadAndCircusesByLevel(level, false));
                }
                if (projectType == ProjectType.COMMERCIAL_HUB_INVESTMENT) {
                    statsDto.setGoldPerTurn(getGoldPerTurnByAdditionalEffect(switch (level) {
                        case LEVEL_1 -> AdditionalEffect.AdditionalEffectType.COMMERCIAL_HUB_INVESTMENT_1;
                        case LEVEL_2 -> AdditionalEffect.AdditionalEffectType.COMMERCIAL_HUB_INVESTMENT_2;
                        case LEVEL_3 -> AdditionalEffect.AdditionalEffectType.COMMERCIAL_HUB_INVESTMENT_3;
                        case LEVEL_4 -> AdditionalEffect.AdditionalEffectType.COMMERCIAL_HUB_INVESTMENT_4;
                        default -> null;
                    }));
                }
                if (projectType == ProjectType.INDUSTRIAL_ZONE_LOGISTICS) {
                    statsDto.setDiscount((int) (getDiscountByAdditionalEffect(switch (level) {
                        case LEVEL_1 -> AdditionalEffect.AdditionalEffectType.WONDER_DISCOUNT_1;
                        case LEVEL_2 -> AdditionalEffect.AdditionalEffectType.WONDER_DISCOUNT_2;
                        case LEVEL_3 -> AdditionalEffect.AdditionalEffectType.WONDER_DISCOUNT_3;
                        case LEVEL_4 -> AdditionalEffect.AdditionalEffectType.WONDER_DISCOUNT_4;
                        default -> null;
                    }) * 100));
                }
                stats.put(level.toString(), statsDto);
            }
            ProjectSettingsDto projectSettingsDto = ProjectSettingsDto.builder()
                    .type(projectType)
                    .stats(stats)
                    .build();
            projectSettings.add(projectSettingsDto);
        }
        return projectSettings;
    }

    @Override
    public int getEventGold(Event.EventType eventType) {
        return gameProperties.getEventGold(eventType);
    }

    @Override
    public int getEventStrength(Event.EventType eventType) {
        return gameProperties.getEventStrength(eventType);
    }

    @Override
    public int getEventDice(Event.EventType eventType) {
        return gameProperties.getEventDice(eventType);
    }

    @Override
    public int getHireIncome(Event.EventType eventType) {
        return gameProperties.getHireIncome(eventType);
    }

    @Override
    public int getHirePrice(Event.EventType eventType) {
        return gameProperties.getHirePrice(eventType);
    }

    @Override
    public int getBreadAndCircusesByLevel(Property.Upgrade level, boolean isPlus) {
        if (isPlus) {
            return gameProperties.getBreadAndCircusesByLevelPlus(level);
        } else {
            return gameProperties.getBreadAndCircusesByLevelMinus(level);
        }
    }

    @Override
    public int getProjectGoldByLevel(ProjectType type, Property.Upgrade level) {
        return gameProperties.getProjectGoldByLevel(type, level);
    }

    @Override
    public int getProjectStrengthByLevel(ProjectType type, Property.Upgrade level) {
        return gameProperties.getProjectStrengthByLevel(type, level);
    }

    @Override
    public int getProjectTourismByLevel(ProjectType type, Property.Upgrade level) {
        return gameProperties.getProjectTourismByLevel(type, level);
    }

    @Override
    public int getGoldPerTurnByAdditionalEffect(AdditionalEffect.AdditionalEffectType type) {
        return gameProperties.getGoldPerTurnByAdditionalEffect(type);
    }

    @Override
    public Property.Upgrade getHighestDistrictLevel(Member member, String districtType) {
        return member.getProperties().stream()
                .filter(property -> switch (districtType) {
                    case "ENTERTAINMENT" -> property.getPosition() == 22 || property.getPosition() == 38;
                    case "CAMPUS" -> property.getPosition() == 15 || property.getPosition() == 45;
                    case "COMMERCIAL_HUB" -> property.getPosition() == 19 || property.getPosition() == 43;
                    case "HARBOR" -> property.getPosition() == 17 || property.getPosition() == 31;
                    case "INDUSTRIAL_ZONE" -> property.getPosition() == 10 || property.getPosition() == 34;
                    case "THEATER_SQUARE" -> property.getPosition() == 21 || property.getPosition() == 39;
                    default -> false;
                })
                .flatMap(property -> property.getUpgrades().stream())
                .filter(upgrade -> upgrade == Property.Upgrade.LEVEL_1 ||
                        upgrade == Property.Upgrade.LEVEL_2 ||
                        upgrade == Property.Upgrade.LEVEL_3 ||
                        upgrade == Property.Upgrade.LEVEL_4)
                .max(Comparator.comparingInt(upgrade -> switch (upgrade) {
                    case LEVEL_1 -> 1;
                    case LEVEL_2 -> 2;
                    case LEVEL_3 -> 3;
                    case LEVEL_4 -> 4;
                    default -> 0;
                }))
                .orElse(Property.Upgrade.LEVEL_1);
    }

    private boolean calculateRequirement(RequirementDto.Requirement requirement, Integer position, Member member) {
        return switch (requirement) {
            case OWN_DEER_OR_FURS -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(3) ||
                            p.getPosition().equals(5));
            case OWN_CAMP -> member.getProperties().stream()
                    .anyMatch(p -> (p.getPosition().equals(3) || p.getPosition().equals(5)) &&
                            p.getUpgrades().contains(Property.Upgrade.LEVEL_2));
            case MAKE_ONE_ROUND -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(position) &&
                            p.getRoundOfLastChange() + 1 <= member.getFinishedRounds());
            case MAKE_TWO_ROUNDS -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(position) &&
                            p.getRoundOfLastChange() + 2 <= member.getFinishedRounds());
            case MAKE_THREE_ROUNDS -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(position) &&
                            p.getRoundOfLastChange() + 3 <= member.getFinishedRounds());
            case MAKE_FOUR_ROUNDS -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(position) &&
                            p.getRoundOfLastChange() + 4 <= member.getFinishedRounds());
            case MAKE_FIVE_ROUNDS -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(position) &&
                            p.getRoundOfLastChange() + 5 <= member.getFinishedRounds());
            case MAKE_ONE_TURN -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(position) &&
                            p.getTurnOfLastChange() + 1 <= member.getRoom().getTurn());
            case MAKE_TWO_TURNS -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(position) &&
                            p.getTurnOfLastChange() + 2 <= member.getRoom().getTurn());
            case MAKE_THREE_TURNS -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(position) &&
                            p.getTurnOfLastChange() + 3 <= member.getRoom().getTurn());
            case MAKE_FOUR_TURNS -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(position) &&
                            p.getTurnOfLastChange() + 4 <= member.getRoom().getTurn());
            case MAKE_FIVE_TURNS -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(position) &&
                            p.getTurnOfLastChange() + 5 <= member.getRoom().getTurn());
            case HAVE_LOW_GOLD_PER_TURN -> calculateGeneralGoldPerTurn(member) >= 50; // TODO: flexible
            case HAVE_MEDIUM_GOLD_PER_TURN -> calculateGeneralGoldPerTurn(member) >= 120; // TODO: flexible
            case HAVE_HIGH_GOLD_PER_TURN -> calculateGeneralGoldPerTurn(member) >= 200; // TODO: flexible
            case HAVE_LOW_GOLD_CAP -> member.getGold() >= 1800; // TODO: flexible
            case HAVE_MEDIUM_GOLD_CAP -> member.getGold() >= 2600; // TODO: flexible
            case HAVE_HIGH_GOLD_CAP -> member.getGold() >= 3400; // TODO: flexible
            case HAVE_LOW_TOURISM -> member.getTourism() >= 800; // TODO: flexible
            case HAVE_MEDIUM_TOURISM -> member.getTourism() >= 1200; // TODO: flexible
            case HAVE_HIGH_TOURISM -> member.getTourism() >= 2000; // TODO: flexible
            case HAVE_LOW_STRENGTH -> member.getStrength() >= 400; // TODO: flexible
            case HAVE_MEDIUM_STRENGTH -> member.getStrength() >= 800; // TODO: flexible
            case HAVE_HIGH_STRENGTH -> member.getStrength() >= 1200; // TODO: flexible
            case ON_CLASSICAL_ERA -> member.getRoom().getTurn() > 10;
            case ON_MEDIEVAL_ERA -> member.getRoom().getTurn() > 25;
            case ON_RENAISSANCE_ERA -> member.getRoom().getTurn() > 40;
            case ON_INDUSTRIAL_ERA -> member.getRoom().getTurn() > 55;
            case ON_MODERN_ERA -> member.getRoom().getTurn() > 70;
            case ON_ATOMIC_ERA -> member.getRoom().getTurn() > 85;
            case ON_INFORMATION_ERA -> member.getRoom().getTurn() > 100;
            case HAVE_TWO_RESOURCES -> member.getProperties().stream()
                    .filter(p -> (p.getPosition().equals(1) ||
                            p.getPosition().equals(2) ||
                            p.getPosition().equals(3) ||
                            p.getPosition().equals(5) ||
                            p.getPosition().equals(11) ||
                            p.getPosition().equals(12) ||
                            p.getPosition().equals(25) ||
                            p.getPosition().equals(26) ||
                            p.getPosition().equals(28)) && p.getUpgrades().contains(Property.Upgrade.LEVEL_2))
                    .count() >= 2;
            case HAVE_TWO_WONDERS -> member.getProperties().stream()
                    .filter(p -> p.getPosition().equals(4) ||
                            p.getPosition().equals(8) ||
                            p.getPosition().equals(16) ||
                            p.getPosition().equals(20) ||
                            p.getPosition().equals(23) ||
                            p.getPosition().equals(27) ||
                            p.getPosition().equals(32) ||
                            p.getPosition().equals(36) ||
                            p.getPosition().equals(40) ||
                            p.getPosition().equals(42) ||
                            p.getPosition().equals(46))
                    .count() >= 2;
            case OWN_RESEARCH_GRANTS -> member.getFinishedScienceProjects() != null &&
                    member.getFinishedScienceProjects().stream()
                            .anyMatch((project) -> project.equals(Member.ScienceProject.CAMPUS));
            case WIDE_EMPIRE -> member.getProperties().size() >= 7; // TODO: flexible
            case SUPER_WIDE_EMPIRE -> member.getProperties().size() >= 11; // TODO: flexible
            case TALL_EMPIRE -> member.getProperties().stream()
                    .flatMap(property -> property.getUpgrades().stream())
                    .filter(upgrade -> upgrade == Property.Upgrade.LEVEL_2 ||
                            upgrade == Property.Upgrade.LEVEL_3 ||
                            upgrade == Property.Upgrade.LEVEL_4 ||
                            upgrade == Property.Upgrade.LEVEL_4_1 ||
                            upgrade == Property.Upgrade.LEVEL_4_2 ||
                            upgrade == Property.Upgrade.LEVEL_4_3)
                    .count() >= 9; // TODO: flexible
            case SUPER_TALL_EMPIRE -> member.getProperties().stream()
                    .flatMap(property -> property.getUpgrades().stream())
                    .filter(upgrade -> upgrade == Property.Upgrade.LEVEL_2 ||
                            upgrade == Property.Upgrade.LEVEL_3 ||
                            upgrade == Property.Upgrade.LEVEL_4 ||
                            upgrade == Property.Upgrade.LEVEL_4_1 ||
                            upgrade == Property.Upgrade.LEVEL_4_2 ||
                            upgrade == Property.Upgrade.LEVEL_4_3)
                    .count() >= 15; // TODO: flexible
            case OWN_ENCAMPMENT -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(7) || p.getPosition().equals(30));
            case OWN_CAMPUS -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(15) || p.getPosition().equals(45));
            case OWN_LIBRARY -> member.getProperties().stream()
                    .anyMatch(p -> (p.getPosition().equals(15) || p.getPosition().equals(45)) &&
                            p.getUpgrades().contains(Property.Upgrade.LEVEL_2));
            case OWN_GOVERNMENT_PLAZA -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(9) ||
                            p.getPosition().equals(18) ||
                            p.getPosition().equals(44));
            case NOT_OWN_GOVERNMENT_PLAZA -> member.getProperties().stream()
                    .noneMatch(p -> p.getPosition().equals(9) ||
                            p.getPosition().equals(18) ||
                            p.getPosition().equals(44));
            case OWN_ENTERTAINMENT_COMPLEX -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(22) || p.getPosition().equals(38));
            case OWN_ARENA -> member.getProperties().stream()
                    .anyMatch(p -> (p.getPosition().equals(22) || p.getPosition().equals(38)) &&
                            p.getUpgrades().contains(Property.Upgrade.LEVEL_2));
            case OWN_HARBOR -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(17) || p.getPosition().equals(31));
            case OWN_INDUSTRIAL_ZONE -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(10) || p.getPosition().equals(34));
            case OWN_FACTORY -> member.getProperties().stream()
                    .anyMatch(p -> (p.getPosition().equals(10) || p.getPosition().equals(34)) &&
                            p.getUpgrades().contains(Property.Upgrade.LEVEL_3));
            case OWN_STADIUM -> member.getProperties().stream()
                    .anyMatch(p -> (p.getPosition().equals(22) || p.getPosition().equals(38)) &&
                            p.getUpgrades().contains(Property.Upgrade.LEVEL_4));
            case OWN_COMMERCIAL_HUB -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(19) || p.getPosition().equals(43));
            case OWN_STOCK_EXCHANGE -> member.getProperties().stream()
                    .anyMatch(p -> (p.getPosition().equals(19) || p.getPosition().equals(43)) &&
                            p.getUpgrades().contains(Property.Upgrade.LEVEL_4));
            case OWN_UNIVERSITY -> member.getProperties().stream()
                    .anyMatch(p -> (p.getPosition().equals(15) || p.getPosition().equals(45)) &&
                            p.getUpgrades().contains(Property.Upgrade.LEVEL_3));
            case OWN_SPACEPORT_OR_LAB -> member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(47) ||
                            ((p.getPosition().equals(15) || p.getPosition().equals(45)) &&
                                    p.getUpgrades().contains(Property.Upgrade.LEVEL_4)));
        };
    }
}
