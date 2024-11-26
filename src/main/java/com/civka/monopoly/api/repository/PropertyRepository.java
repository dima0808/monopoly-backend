package com.civka.monopoly.api.repository;

import com.civka.monopoly.api.entity.Property;
import com.civka.monopoly.api.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    Boolean existsByRoomAndPosition(Room room, Integer position);

    Optional<Property> findByRoomAndPosition(Room room, Integer position);

    List<Property> findByRoom(Room room);
}
