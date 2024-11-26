package com.civka.monopoly.api.service.impl;

import com.civka.monopoly.api.dto.AdditionalEffectDto;
import com.civka.monopoly.api.entity.AdditionalEffect;
import com.civka.monopoly.api.entity.Member;
import com.civka.monopoly.api.repository.AdditionalEffectRepository;
import com.civka.monopoly.api.service.AdditionalEffectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdditionalEffectServiceImpl implements AdditionalEffectService {

    private final AdditionalEffectRepository additionalEffectRepository;
    private final GameUtilsImpl gameUtils;

    @Override
    public AdditionalEffect save(AdditionalEffect additionalEffect) {
        return additionalEffectRepository.save(additionalEffect);
    }

    @Override
    public List<AdditionalEffectDto> findByMember(Member member) {
        List<AdditionalEffect> additionalEffects = member.getAdditionalEffects();

        return additionalEffects.stream()
                .map(additionalEffect -> AdditionalEffectDto.builder()
                        .type(additionalEffect.getType())
                        .turnsLeft(additionalEffect.getTurnsLeft())
                        .goldPerTurn(gameUtils.getGoldPerTurnByAdditionalEffect(additionalEffect.getType()))
                        .build())
                .collect(Collectors.toList());
    }
}
