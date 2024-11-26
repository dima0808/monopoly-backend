package com.civka.monopoly.api.repository;

import com.civka.monopoly.api.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Boolean existsByName(String name);

    Optional<Room> findByName(String name);
}
