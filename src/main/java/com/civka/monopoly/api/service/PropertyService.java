package com.civka.monopoly.api.service;

import com.civka.monopoly.api.dto.PropertyDto;
import com.civka.monopoly.api.entity.Property;
import com.civka.monopoly.api.entity.Room;

import java.util.List;

public interface PropertyService {

    Property save(Property property);

    Boolean existsByRoomAndPosition(Room room, Integer position);

    Property findByRoomAndPosition(Room room, Integer position);

    List<PropertyDto> findByRoom(Room room);

    void deleteById(Long id);
}
