package com.civka.monopoly.api.service;

import com.civka.monopoly.api.dto.GameSettingsDto;
import com.civka.monopoly.api.dto.RoomDto;
import com.civka.monopoly.api.entity.ArmySpending;
import com.civka.monopoly.api.entity.Event;
import com.civka.monopoly.api.entity.Member;
import com.civka.monopoly.api.entity.Room;

import java.util.List;

public interface RoomService {

    Room create(RoomDto roomDto, String username);

    Room findByName(String roomName);

    List<Room> findAll();

    Room deleteByName(String roomName, String username);

    Room addMember(Room room, String username);

    Room removeMember(Room room, String username);

    Room addMember(String roomName, String username);

    Room removeMember(String roomName, String username);

    Room kickMember(String roomName, String member, String username);

    void handlePassword(String roomName, String password);

    Room startGame(String roomName, String username);

    Room endTurn(Member member, ArmySpending armySpending);

    Room forceEndTurn(Member member, ArmySpending armySpending);

    Room addGold(Member member, Integer gold, String admin);

    Room addStrength(Member member, Integer strength, String admin);

    Room addEvent(Member member, Event.EventType eventType, String admin);

    Room goToPosition(Member member, Integer position, String admin);

    GameSettingsDto getGameSettings();
}
