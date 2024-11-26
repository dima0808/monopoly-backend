package com.civka.monopoly.api.controller;

import com.civka.monopoly.api.dto.RoomDto;
import com.civka.monopoly.api.entity.Room;
import com.civka.monopoly.api.payload.PasswordMessage;
import com.civka.monopoly.api.payload.RoomMessage;
import com.civka.monopoly.api.service.RoomService;
import com.civka.monopoly.api.service.WrongLobbyPasswordException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @MessageMapping("/rooms/addRoom")
    @SendTo("/topic/public")
    public RoomMessage addRoom(@Payload RoomDto roomDto, @Header("username") String username) {
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.CREATE)
                .content("Room " + roomDto.getName() + " created")
                .room(roomService.create(roomDto, username))
                .build();
    }

    @MessageMapping("/rooms/joinRoom/{roomName}")
    @SendTo({"/topic/public", "/topic/public/{roomName}/players"})
    public RoomMessage joinRoom(@Payload PasswordMessage passwordMessage,
                                @DestinationVariable String roomName,
                                @Header("username") String username) {
        if (roomService.findByName(roomName).getPassword() != null) {
            if (passwordMessage.getPassword() == null) {
                throw new WrongLobbyPasswordException();
            }
            roomService.handlePassword(roomName, passwordMessage.getPassword());
        }
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.JOIN)
                .content("Member " + username + " joined the room " + roomName)
                .room(roomService.addMember(roomName, username))
                .build();
    }

    @MessageMapping("/rooms/leaveRoom/{roomName}")
    @SendTo({"/topic/public", "/topic/public/{roomName}/players"})
    public RoomMessage leaveRoom(@DestinationVariable String roomName, @Header("username") String username) {
        Room updatedRoom = roomService.removeMember(roomName, username);
        boolean deleteCondition = updatedRoom.getMembers().isEmpty();
        return RoomMessage.builder()
                .type(deleteCondition ? RoomMessage.MessageType.DELETE : RoomMessage.MessageType.LEAVE)
                .content("Member " + username + " left the room " + roomName +
                        (deleteCondition ? " and room was deleted" : ""))
                .room(updatedRoom)
                .build();
    }

    @MessageMapping("/rooms/kickMember/{roomName}/{member}")
    @SendTo({"/topic/public", "/topic/public/{roomName}/players"})
    public RoomMessage kickMember(@DestinationVariable String roomName,
                           @DestinationVariable String member,
                           @Header("username") String username) {
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.KICK)
                .content(String.format("Member %s was kicked from the room by %s",
                        member, username))
                .room(roomService.kickMember(roomName, member, username))
                .build();
    }

    @MessageMapping("/rooms/deleteRoom/{roomName}")
    @SendTo("/topic/public")
    public RoomMessage deleteRoom(@DestinationVariable String roomName, @Header("username") String username) {
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.DELETE)
                .content(String.format("Room %s deleted by %s and all members were kicked out",
                        roomName, username))
                .room(roomService.deleteByName(roomName, username))
                .build();
    }

    @GetMapping("/api/rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.findAll());
    }

    @GetMapping("/api/rooms/{roomName}")
    public ResponseEntity<Room> getRoom(@PathVariable String roomName) {
        return ResponseEntity.ok(roomService.findByName(roomName));
    }
}
