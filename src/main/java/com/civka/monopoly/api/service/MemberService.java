package com.civka.monopoly.api.service;

import com.civka.monopoly.api.dto.PropertyDto;
import com.civka.monopoly.api.entity.Member;
import com.civka.monopoly.api.entity.Property;
import com.civka.monopoly.api.payload.DiceMessage;

public interface MemberService {

    Member save(Member member);

    void delete(Member member);

    void deleteById(Long id);

    DiceMessage rollDice(Member member);

    PropertyDto buyProperty(Member member, Integer position);

    PropertyDto upgradeProperty(Member member, Integer position, Property.Upgrade upgradeChoice);

    PropertyDto upgradeProperty(Member member, Integer position);

    PropertyDto downgradeProperty(Member member, Integer position, Property.Upgrade downgradeChoice);

    PropertyDto downgradeProperty(Member member, Integer position);

    PropertyDto payRent(Member member, Integer position);
}
