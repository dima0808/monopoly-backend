package com.civka.monopoly.api.service;

import com.civka.monopoly.api.dto.AdditionalEffectDto;
import com.civka.monopoly.api.entity.AdditionalEffect;
import com.civka.monopoly.api.entity.Member;

import java.util.List;

public interface AdditionalEffectService {

    AdditionalEffect save(AdditionalEffect additionalEffect);

    List<AdditionalEffectDto> findByMember(Member member);
}
