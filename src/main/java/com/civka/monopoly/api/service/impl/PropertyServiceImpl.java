package com.civka.monopoly.api.service.impl;

import com.civka.monopoly.api.dto.PropertyDto;
import com.civka.monopoly.api.entity.Member;
import com.civka.monopoly.api.entity.Property;
import com.civka.monopoly.api.entity.Room;
import com.civka.monopoly.api.repository.PropertyRepository;
import com.civka.monopoly.api.service.GameUtils;
import com.civka.monopoly.api.service.PropertyService;
import com.civka.monopoly.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final GameUtils gameUtils;
    private final UserService userService;

    @Override
    public Property save(Property property) {
        return propertyRepository.save(property);
    }

    @Override
    public Boolean existsByRoomAndPosition(Room room, Integer position) {
        return propertyRepository.existsByRoomAndPosition(room, position);
    }

    @Override
    public Property findByRoomAndPosition(Room room, Integer position) {
        return propertyRepository.findByRoomAndPosition(room, position).orElse(null);
    }

    @Override
    public List<PropertyDto> findByRoom(Room room) {
        Member member = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .getMember();
        List<Property> properties = propertyRepository.findByRoom(room);
        Map<String, String> upgradeMap = gameUtils.getUpgradeProperties();

        return upgradeMap.keySet().stream()
                .map(position -> {
                    Integer positionInt = Integer.parseInt(position);
                    Property property = properties.stream()
                            .filter(p -> p.getPosition().equals(positionInt))
                            .findFirst()
                            .orElse(null);

                    if (property != null) {
                        return PropertyDto.builder()
                                .id(property.getId())
                                .member(property.getMember())
                                .upgrades(gameUtils.getUpgrades(property.getPosition(), property))
                                .mortgage(property.getMortgage())
                                .position(property.getPosition())
                                .goldOnStep(gameUtils.calculateGoldOnStep(property))
                                .tourismOnStep(gameUtils.calculateTourismOnStep(property))
                                .goldPerTurn(gameUtils.calculateGoldPerTurn(property))
                                .upgradeRequirements(gameUtils.getRequirements(property.getPosition(), member))
                                .build();
                    } else {
                        return PropertyDto.builder()
                                .upgrades(gameUtils.getUpgrades(positionInt, null))
                                .position(positionInt)
                                .goldOnStep(gameUtils.getGoldOnStepByLevel(positionInt, Property.Upgrade.LEVEL_1))
                                .goldOnStep(gameUtils.getTourismOnStepByLevel(positionInt, Property.Upgrade.LEVEL_1))
                                .goldPerTurn(gameUtils.getGoldPerTurnByLevel(positionInt, Property.Upgrade.LEVEL_1))
                                .upgradeRequirements(gameUtils.getRequirements(positionInt, member))
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        propertyRepository.deleteById(id);
    }
}
