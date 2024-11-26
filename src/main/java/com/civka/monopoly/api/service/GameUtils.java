package com.civka.monopoly.api.service;

import com.civka.monopoly.api.dto.*;
import com.civka.monopoly.api.entity.*;

import java.util.List;
import java.util.Map;

public interface GameUtils {

    int calculateGoldOnStep(Property property);

    int getGoldOnStepByLevel(Integer position, Property.Upgrade level);

    int calculateTourismOnStep(Property property);

    int getTourismOnStepByLevel(Integer position, Property.Upgrade upgrade);

    int calculateGoldPerTurn(Property property);

    int calculateGeneralGoldPerTurn(Member member);

    int getGoldPerTurnByLevel(Integer position, Property.Upgrade level);

    int getPriceByPositionAndLevel(Integer position, Property.Upgrade level);

    int getScoreByPositionAndLevel(Integer position, Property.Upgrade level);

    int getStrengthFromArmySpending(ArmySpending armySpending);

    int getGoldFromArmySpending(ArmySpending armySpending);

    double getDiscountByAdditionalEffect(AdditionalEffect.AdditionalEffectType type);

    Map<String, String> getUpgradeProperties();

    List<RequirementDto> getRequirements(Integer position, Member member);

    List<UpgradeDto> getUpgrades(Integer position, Property property);

    List<ArmySpendingDto> getArmySpendings();

    List<ProjectSettingsDto> getProjectSettings();

    int getEventGold(Event.EventType eventType);

    int getEventStrength(Event.EventType eventType);

    int getEventDice(Event.EventType eventType);

    int getHireIncome(Event.EventType eventType);

    int getHirePrice(Event.EventType eventType);

    int getBreadAndCircusesByLevel(Property.Upgrade level, boolean isPlus);

    int getProjectGoldByLevel(ProjectType type, Property.Upgrade level);

    int getProjectStrengthByLevel(ProjectType type, Property.Upgrade level);

    int getProjectTourismByLevel(ProjectType type, Property.Upgrade level);

    int getGoldPerTurnByAdditionalEffect(AdditionalEffect.AdditionalEffectType type);

    Property.Upgrade getHighestDistrictLevel(Member member, String districtType);
}
