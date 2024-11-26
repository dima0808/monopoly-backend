package com.civka.monopoly.api.controller;

import com.civka.monopoly.api.dto.GameSettingsDto;
import com.civka.monopoly.api.dto.PropertyDto;
import com.civka.monopoly.api.entity.*;
import com.civka.monopoly.api.payload.DiceMessage;
import com.civka.monopoly.api.payload.PlayerMessage;
import com.civka.monopoly.api.payload.RoomMessage;
import com.civka.monopoly.api.service.MemberService;
import com.civka.monopoly.api.service.PropertyService;
import com.civka.monopoly.api.service.RoomService;
import com.civka.monopoly.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LobbyController {

    private final RoomService roomService;
    private final UserService userService;
    private final MemberService memberService;
    private final PropertyService propertyService;

    @MessageMapping("/rooms/{roomName}/changeCivilization/{civilization}")
    @SendTo("/topic/public/{roomName}/players")
    public PlayerMessage changeCivilization(@DestinationVariable Member.Civilization civilization,
                                            @Header("username") String username) {
        Member member = userService.findByUsername(username).getMember();
        member.setCivilization(civilization);
        return PlayerMessage.builder()
                .type(PlayerMessage.MessageType.CHANGE_CIVILIZATION)
                .content("Member " + username + " changed civilization to " + civilization)
                .member(memberService.save(member))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/changeColor/{color}")
    @SendTo({"/topic/public/{roomName}/players"})
    public PlayerMessage changeColor(@DestinationVariable Member.Color color,
                                            @Header("username") String username) {
        Member member = userService.findByUsername(username).getMember();
        member.setColor(color);
        return PlayerMessage.builder()
                .type(PlayerMessage.MessageType.CHANGE_COLOR)
                .content("Member " + username + " changed color to " + color)
                .member(memberService.save(member))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/startGame")
    @SendTo({"/topic/public", "/topic/public/{roomName}/game"})
    public RoomMessage startGame(@DestinationVariable String roomName,
                                     @Header("username") String username) {
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.START)
                .content("Game started")
                .room(roomService.startGame(roomName, username))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/rollDice")
    @SendTo("/topic/public/{roomName}/game")
    public DiceMessage rollDice(@Header("username") String username) {
        Member member = userService.findByUsername(username).getMember();
        return memberService.rollDice(member);
    }

    @MessageMapping("/rooms/{roomName}/endTurn")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage endTurn(@Header("username") String username,
                               @Header("armySpending") ArmySpending armySpending) {
        Member member = userService.findByUsername(username).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.END_TURN)
                .content(username + "'s turn ended")
                .room(roomService.endTurn(member, armySpending))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/forceEndTurn")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage forceEndTurn(@Header("username") String username,
                               @Header("armySpending") ArmySpending armySpending) {
        Member member = userService.findByUsername(username).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.FORCE_END_TURN)
                .content(username + "'s turn ended")
                .room(roomService.forceEndTurn(member, armySpending))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/buyProperty/{position}")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage buyProperty(@DestinationVariable Integer position,
                                   @Header("username") String username) {
        Member member = userService.findByUsername(username).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.BUY_PROPERTY)
                .content("Member " + username + " bought property on position " + position)
                .property(memberService.buyProperty(member, position))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/upgradeProperty/{position}")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage upgradeProperty(@DestinationVariable Integer position,
                                       @Header("username") String username) {
        Member member = userService.findByUsername(username).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.UPGRADE_PROPERTY)
                .content("Member " + username + " upgraded property on position " + position)
                .property(memberService.upgradeProperty(member, position))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/downgradeProperty/{position}")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage downgradeProperty(@DestinationVariable Integer position,
                                         @Header("username") String username) {
        Member member = userService.findByUsername(username).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.DOWNGRADE_PROPERTY)
                .content("Member " + username + " downgraded property on position " + position)
                .property(memberService.downgradeProperty(member, position))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/upgradeGovernmentPlazaChoice/{position}")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage upgradeGovernmentPlazaChoice(@DestinationVariable Integer position,
                                                    @Header("username") String username,
                                                    @Header("choice") Property.Upgrade upgradeChoice) {
        Member member = userService.findByUsername(username).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.UPGRADE_PROPERTY)
                .content("Member " + username + " upgraded Government Plaza with " + upgradeChoice)
                .property(memberService.upgradeProperty(member, position, upgradeChoice))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/downgradeGovernmentPlazaChoice/{position}")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage downgradeGovernmentPlazaChoice(@DestinationVariable Integer position,
                                                      @Header("username") String username,
                                                      @Header("choice") Property.Upgrade upgradeChoice) {
        Member member = userService.findByUsername(username).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.DOWNGRADE_PROPERTY)
                .content("Member " + username + " downgraded property on position " + position)
                .property(memberService.downgradeProperty(member, position, upgradeChoice))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/payRent/{position}")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage payRent(@DestinationVariable Integer position,
                                   @Header("username") String username) {
        Member member = userService.findByUsername(username).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.PAY_RENT)
                .content("Member " + username + " paid rent on position " + position)
                .property(memberService.payRent(member, position))
                .room(roomService.findByName(member.getRoom().getName()))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/addGold/{nickname}")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage addGold(@DestinationVariable String nickname,
                               @Header("username") String admin,
                               @Header("gold") Integer gold) {
        Member member = userService.findByNickname(nickname).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.CHEAT_ADD_GOLD)
                .content("Admin added " + gold + " gold for " + nickname)
                .room(roomService.addGold(member, gold, admin))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/addStrength/{nickname}")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage addStrength(@DestinationVariable String nickname,
                               @Header("username") String admin,
                               @Header("strength") Integer strength) {
        Member member = userService.findByNickname(nickname).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.CHEAT_ADD_STRENGTH)
                .content("Admin added " + strength + " strength for " + nickname)
                .room(roomService.addStrength(member, strength, admin))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/addEvent/{nickname}")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage addEvent(@DestinationVariable String nickname,
                                   @Header("username") String admin,
                                   @Header("event") Integer event) {
        Member member = userService.findByNickname(nickname).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.CHEAT_ADD_EVENT)
                .content("Admin added event " + event + " for " + nickname)
                .room(roomService.addEvent(member, Event.EventType.values()[event - 1], admin))
                .build();
    }

    @MessageMapping("/rooms/{roomName}/goToPosition/{nickname}")
    @SendTo("/topic/public/{roomName}/game")
    public RoomMessage goToPosition(@DestinationVariable String nickname,
                                @Header("username") String admin,
                                @Header("position") Integer position) {
        Member member = userService.findByNickname(nickname).getMember();
        return RoomMessage.builder()
                .type(RoomMessage.MessageType.CHEAT_ADD_EVENT)
                .content("Admin moved " + nickname + " to position " + position)
                .room(roomService.goToPosition(member, position, admin))
                .build();
    }

    @GetMapping("/api/rooms/{roomName}/members")
    public ResponseEntity<List<Member>> getMembers(@PathVariable String roomName) {
        return ResponseEntity.ok(roomService.findByName(roomName).getMembers());
    }

    @GetMapping("/api/rooms/{roomName}/properties")
    public ResponseEntity<List<PropertyDto>> getProperties(@PathVariable String roomName) {
        Room room = roomService.findByName(roomName);
        return ResponseEntity.ok(propertyService.findByRoom(room));
    }

    @GetMapping("/api/rooms/settings")
    public ResponseEntity<GameSettingsDto> getGameSettings() {
        return ResponseEntity.ok(roomService.getGameSettings());
    }
}
