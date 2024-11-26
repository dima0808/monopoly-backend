package com.civka.monopoly.api.config;

import com.civka.monopoly.api.dto.ProjectType;
import com.civka.monopoly.api.entity.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "monopoly.app.room.game")
public class GameProperties {

    private Map<String, Integer> price;
    private Map<String, Integer> score;
    private Map<String, Integer> goldOnStep;
    private Map<String, Integer> tourismOnStep;
    private Map<String, Integer> perTurn;
    private Map<String, String> requirement;
    private Map<String, String> upgrade;
    private Map<String, Integer> armySpending;
    private Map<String, Integer> event;
    private Map<String, Integer> project;
    private Map<String, Integer> additionalGoldPerTurn;
    private Map<String, Double> wonderDiscount;

    public Integer getPriceByPositionAndLevel(Integer position, Property.Upgrade level) {
        Integer price = this.price.get(position + "." + level);
        return price == null ? 0 : price;
    }

    public Integer getScoreByPositionAndLevel(Integer position, Property.Upgrade level) {
        Integer score = this.score.get(position + "." + level);
        return score == null ? 0 : score;
    }

    public Integer getGoldOnStepByPositionAndLevel(Integer position, Property.Upgrade level) {
        Integer gold = goldOnStep.get(position + "." + level);
        return gold == null ? 0 : gold;
    }

    public Integer getTourismOnStepByPositionAndLevel(Integer position, Property.Upgrade level) {
        Integer tourism = tourismOnStep.get(position + "." + level);
        return tourism == null ? 0 : tourism;
    }

    public Integer getPerTurnByPositionAndLevel(Integer position, Property.Upgrade level) {
        Integer perTurn = this.perTurn.get(position + "." + level);
        return perTurn == null ? 0 : perTurn;
    }

    public int getStrengthFromArmySpending(ArmySpending armySpendingLevel) {
        return armySpending.get(armySpendingLevel + ".strength");
    }

    public int getGoldFromArmySpending(ArmySpending armySpendingLevel) {
        return armySpending.get(armySpendingLevel + ".gold");
    }

    public int getEventGold(Event.EventType eventType) {
        return event.get(eventType + ".gold");
    }

    public int getEventStrength(Event.EventType eventType) {
        return event.get(eventType + ".strength");
    }

    public int getEventDice(Event.EventType eventType) {
        return event.get(eventType + ".dice");
    }

    public int getHireIncome(Event.EventType eventType) {
        return event.get(eventType + ".hireIncome");
    }

    public int getHirePrice(Event.EventType eventType) {
        return event.get(eventType + ".hirePrice");
    }

    public int getBreadAndCircusesByLevelPlus(Property.Upgrade level) {
        return project.get("BREAD_AND_CIRCUSES." + level + ".plus");
    }

    public int getBreadAndCircusesByLevelMinus(Property.Upgrade level) {
        return project.get("BREAD_AND_CIRCUSES." + level + ".minus");
    }

    public int getProjectGoldByLevel(ProjectType type, Property.Upgrade level) {
        Integer gold = project.get(type + "." + level + ".gold");
        return gold == null ? 0 : gold;
    }

    public int getProjectStrengthByLevel(ProjectType type, Property.Upgrade level) {
        Integer strength = project.get(type + "." + level + ".strength");
        return strength == null ? 0 : strength;
    }

    public int getProjectTourismByLevel(ProjectType type, Property.Upgrade level) {
        Integer tourism = project.get(type + "." + level + ".tourism");
        return tourism == null ? 0 : tourism;
    }

    public int getGoldPerTurnByAdditionalEffect(AdditionalEffect.AdditionalEffectType type) {
        Integer goldPerTurn = additionalGoldPerTurn.get(type.toString());
        return goldPerTurn == null ? 0 : goldPerTurn;
    }

    public double getDiscountByAdditionalEffect(AdditionalEffect.AdditionalEffectType type) {
        Double discount = wonderDiscount.get(type.toString());
        return discount == null ? 0.0 : discount;
    }
}