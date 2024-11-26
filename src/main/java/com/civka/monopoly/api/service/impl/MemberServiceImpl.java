package com.civka.monopoly.api.service.impl;

import com.civka.monopoly.api.dto.ChatMessageDto;
import com.civka.monopoly.api.dto.PropertyDto;
import com.civka.monopoly.api.entity.*;
import com.civka.monopoly.api.payload.DiceMessage;
import com.civka.monopoly.api.payload.PlayerMessage;
import com.civka.monopoly.api.repository.MemberRepository;
import com.civka.monopoly.api.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final ChatMessageService chatMessageService;
    private final EventService eventService;
    private final PropertyService propertyService;
    private final GameUtils gameUtils;

    @Value("${monopoly.app.room.game.goldForBypassingStart}")
    private Integer goldForBypassingStart;

    @Value("${monopoly.app.room.game.demoteGoldCoefficient}")
    private Double demoteGoldCoefficient;

    @Value("${monopoly.app.room.game.mortgageGoldCoefficient}")
    private Double mortgageGoldCoefficient;

    @Value("${monopoly.app.room.game.redemptionCoefficient}")
    private Double redemptionCoefficient;

    @Value("${monopoly.app.room.game.science-project.basicTurnAmount}")
    private Integer basicTurnAmount;

    @Value("${monopoly.app.room.game.science-project.labBoost}")
    private Integer labBoost;

    @Value("${monopoly.app.room.game.science-project.governmentBoost}")
    private Integer governmentBoost;

    @Value("${monopoly.app.room.game.science-project.oxfordBoost}")
    private Integer oxfordBoost;

    @Value("${monopoly.app.room.game.science-project.spaceportBoost}")
    private Integer spaceportBoost;

    @Value("${monopoly.app.room.game.science-project.expeditionTurnAmount}")
    private Integer expeditionTurnAmount;

    @Value("${monopoly.app.room.game.science-project.laserBoost}")
    private Integer laserBoost;

    @Override
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public void delete(Member member) {
        memberRepository.delete(member);
    }

    @Override
    public void deleteById(Long id) {
        memberRepository.deleteById(id);
    }

    @Override
    public DiceMessage rollDice(Member member) {
        if (!member.getRoom().getCurrentTurn().equals(member.getUser().getUsername()) || member.getHasRolledDice()) {
            throw new UserNotAllowedException();
        }
        Room room = member.getRoom();
        int firstRoll = (int) (Math.random() * 6) + 1;
        int secondRoll = (int) (Math.random() * 6) + 1;
        int newPosition = member.getPosition() + firstRoll + secondRoll;
        if (newPosition > 47) {
            member.setGold(member.getGold() + goldForBypassingStart);
            member.setFinishedRounds(member.getFinishedRounds() + 1);
            Chat roomChat = chatService.findByName(room.getName());
            ChatMessageDto systemMessage = ChatMessageDto.builder()
                    .type(ChatMessage.MessageType.SYSTEM_BYPASS_START)
                    .content(member.getUser().getNickname() + " " + goldForBypassingStart)
                    .timestamp(LocalDateTime.now())
                    .build();
            ChatMessage chatMessage = chatMessageService.save(roomChat, systemMessage);
            messagingTemplate.convertAndSend("/topic/chat/" + roomChat.getName(), chatMessage);

            PlayerMessage playerMessage = PlayerMessage.builder()
                    .type(PlayerMessage.MessageType.BYPASS_START)
                    .content("Member " + member.getUser().getUsername() + " bypassed start")
                    .member(member)
                    .build();
            messagingTemplate.convertAndSend("/topic/public/" + room.getName() + "/game", playerMessage);

            newPosition -= 48;
        }
        member.setPosition(newPosition);
        member.setGold(member.getGold() + gameUtils.calculateGeneralGoldPerTurn(member));
        for (AdditionalEffect additionalEffect : member.getAdditionalEffects()) {
            member.setGold(member.getGold() + gameUtils.getGoldPerTurnByAdditionalEffect(additionalEffect.getType()));
        }
        member.setHasRolledDice(true);
        if (calculateMemberTurnsToScienceProject(member) && isMemberAbleToSpace(member)) {
            eventService.add(member, Event.EventType.SCIENCE_PROJECTS);
        }
        Member updatedMember = memberRepository.save(member);

        eventService.handleNewPosition(newPosition, updatedMember, firstRoll, secondRoll);

        Chat roomChat = chatService.findByName(room.getName());
        ChatMessageDto systemMessage = ChatMessageDto.builder()
                .type(ChatMessage.MessageType.SYSTEM_ROLL_DICE)
                .content(updatedMember.getUser().getNickname() + " " + firstRoll + " " + secondRoll)
                .timestamp(LocalDateTime.now())
                .build();
        ChatMessage chatMessage = chatMessageService.save(roomChat, systemMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + roomChat.getName(), chatMessage);

        return DiceMessage.builder()
                .type(PlayerMessage.MessageType.ROLL_DICE)
                .content("Player " + updatedMember.getUser().getNickname() + " rolled dice: " + firstRoll + " and " + secondRoll)
                .firstRoll(firstRoll)
                .secondRoll(secondRoll)
                .member(updatedMember)
                .build();
    }

    @Override
    public PropertyDto buyProperty(Member member, Integer position) {
        if (!member.getPosition().equals(position) ||
                propertyService.existsByRoomAndPosition(member.getRoom(), position)) {
            throw new UserNotAllowedException();
        }
        int price = gameUtils.getPriceByPositionAndLevel(position, Property.Upgrade.LEVEL_1);
        if (isPropertyWonder(position)) {
            if (member.getDiscount() > 0) {
                price = (int) (price * (1 - Math.min(1, member.getDiscount())));
                member.setDiscount(member.getDiscount() - Math.min(1, member.getDiscount()));
            }
            if (position == 46) {
                List<Member.ScienceProject> finishedScienceProjects = member.getFinishedScienceProjects();
                if (member.getFinishedScienceProjects()
                        .stream()
                        .noneMatch((project) -> project.equals(Member.ScienceProject.CAMPUS))) {
                    finishedScienceProjects.add(Member.ScienceProject.CAMPUS);
                }
                List<Member.ScienceProject> scienceProjects = List.of(
                        Member.ScienceProject.SATELLITE,
                        Member.ScienceProject.MOON,
                        Member.ScienceProject.MARS,
                        Member.ScienceProject.EXOPLANET
                );
                for (Member.ScienceProject project : scienceProjects) {
                    if (!member.getFinishedScienceProjects().contains(project)) {
                        finishedScienceProjects.add(project);
                        if (project.equals(Member.ScienceProject.EXOPLANET)) {
                            member.setExpeditionTurns(expeditionTurnAmount);
                        } else if (project.equals(Member.ScienceProject.LASER)) {
                            member.setExpeditionTurns(Math.max(member.getExpeditionTurns() - laserBoost, 0));
                        }
                        break;
                    }
                }
            }
        }
        if (member.getGold() < price) {
            throw new UserNotAllowedException();
        }
        member.setGold(member.getGold() - price);
        member.setScore(member.getScore() + gameUtils.getScoreByPositionAndLevel(position, Property.Upgrade.LEVEL_1));
        if (position == 47 && member.getTurnsToNextScienceProject() == -1) {
            member.setTurnsToNextScienceProject(basicTurnAmount);
        }
        Room room = member.getRoom();
        List<Property.Upgrade> upgradeList = new ArrayList<>();
        upgradeList.add(Property.Upgrade.LEVEL_1);
        List<Property.Upgrade> uniqueEffects = checkForUniqueUpgradesOnBuy(position, member);
        if (!uniqueEffects.isEmpty()) {
            upgradeList.addAll(uniqueEffects);
        }
        Property property = Property.builder()
                .member(member)
                .room(room)
                .upgrades(upgradeList)
                .position(position)
                .roundOfLastChange(member.getFinishedRounds())
                .turnOfLastChange(room.getTurn())
                .mortgage(-1)
                .build();
        Property updatedProperty = checkForUniqueUpgradesOnUpgrade(property, member);
        Member updatedMember = updatedProperty.getMember();
        eventService.delete(updatedMember, Event.EventType.BUY_PROPERTY);

        Chat roomChat = chatService.findByName(room.getName());
        ChatMessageDto systemMessage = ChatMessageDto.builder()
                .type(ChatMessage.MessageType.SYSTEM_BUY_PROPERTY)
                .content(updatedMember.getUser().getNickname() + " " + position + " " + price)
                .timestamp(LocalDateTime.now())
                .build();
        ChatMessage chatMessage = chatMessageService.save(roomChat, systemMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + roomChat.getName(), chatMessage);

        return PropertyDto.builder()
                .id(updatedProperty.getId())
                .member(updatedProperty.getMember())
                .upgrades(gameUtils.getUpgrades(updatedProperty.getPosition(), updatedProperty))
                .mortgage(updatedProperty.getMortgage())
                .position(updatedProperty.getPosition())
                .goldOnStep(gameUtils.calculateGoldOnStep(updatedProperty))
                .tourismOnStep(gameUtils.calculateTourismOnStep(property))
                .goldPerTurn(gameUtils.calculateGoldPerTurn(property))
                .upgradeRequirements(gameUtils.getRequirements(updatedProperty.getPosition(), member))
                .build();
    }

    @Override
    public PropertyDto upgradeProperty(Member member, Integer position, Property.Upgrade upgradeChoice) {
        if (!propertyService.existsByRoomAndPosition(member.getRoom(), position) || member.getProperties().stream()
                .noneMatch(property -> property.getPosition().equals(position))) {
            throw new UserNotAllowedException();
        }
        Property property = propertyService.findByRoomAndPosition(member.getRoom(), position);
        Chat roomChat = chatService.findByName(member.getRoom().getName());
        ChatMessageDto systemMessage;
        if (property.getMortgage() != -1) {
            property.setMortgage(-1);
            int price = gameUtils.getPriceByPositionAndLevel(position, Property.Upgrade.LEVEL_1);
            member.setGold(member.getGold() - (int) (price * redemptionCoefficient));
            member.setScore(member.getScore() + gameUtils.getScoreByPositionAndLevel(position, Property.Upgrade.LEVEL_1));
            memberRepository.save(member);
            systemMessage = ChatMessageDto.builder()
                    .type(ChatMessage.MessageType.SYSTEM_REDEMPTION_PROPERTY)
                    .content(member.getUser().getNickname() + " " + position)
                    .timestamp(LocalDateTime.now())
                    .build();
        } else {
            Property.Upgrade nextLevel;
            if (upgradeChoice != null) {
                nextLevel = upgradeChoice;
            } else {
                nextLevel = Property.Upgrade.LEVEL_1;
                for (Property.Upgrade upgrade : List.of(Property.Upgrade.LEVEL_1, Property.Upgrade.LEVEL_2,
                        Property.Upgrade.LEVEL_3, Property.Upgrade.LEVEL_4)) {
                    if (!property.getUpgrades().contains(upgrade)) {
                        nextLevel = upgrade;
                        break;
                    }
                }
                if ((property.getPosition() == 15 || property.getPosition() == 45)
                        && nextLevel == Property.Upgrade.LEVEL_4 && member.getTurnsToNextScienceProject() == -1) {
                    member.setTurnsToNextScienceProject(basicTurnAmount);
                }
            }
            int price = gameUtils.getPriceByPositionAndLevel(position, nextLevel);
            if (member.getGold() < price) {
                throw new UserNotAllowedException();
            }
            member.setGold(member.getGold() - price);
            member.setScore(member.getScore() + gameUtils.getScoreByPositionAndLevel(position, nextLevel));
            memberRepository.save(member);
            property.getUpgrades().add(nextLevel);

            systemMessage = ChatMessageDto.builder()
                    .type(ChatMessage.MessageType.SYSTEM_UPGRADE_PROPERTY)
                    .content(member.getUser().getNickname() + " " + position + " " + price)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        property.setRoundOfLastChange(member.getFinishedRounds());
        property.setTurnOfLastChange(member.getRoom().getTurn());
        Property updatedProperty = checkForUniqueUpgradesOnUpgrade(property, member);

        ChatMessage chatMessage = chatMessageService.save(roomChat, systemMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + roomChat.getName(), chatMessage);

        return PropertyDto.builder()
                .id(updatedProperty.getId())
                .member(updatedProperty.getMember())
                .upgrades(gameUtils.getUpgrades(updatedProperty.getPosition(), updatedProperty))
                .mortgage(updatedProperty.getMortgage())
                .position(updatedProperty.getPosition())
                .goldOnStep(gameUtils.calculateGoldOnStep(updatedProperty))
                .tourismOnStep(gameUtils.calculateTourismOnStep(property))
                .goldPerTurn(gameUtils.calculateGoldPerTurn(updatedProperty))
                .upgradeRequirements(gameUtils.getRequirements(updatedProperty.getPosition(), member))
                .build();
    }

    @Override
    public PropertyDto upgradeProperty(Member member, Integer position) {
        return upgradeProperty(member, position, null);
    }

    @Override
    public PropertyDto downgradeProperty(Member member, Integer position, Property.Upgrade downgradeChoice) {
        if (!propertyService.existsByRoomAndPosition(member.getRoom(), position) || member.getProperties().stream()
                .noneMatch(property -> property.getPosition().equals(position))) {
            throw new UserNotAllowedException();
        }
        Property property = propertyService.findByRoomAndPosition(member.getRoom(), position);
        Chat roomChat = chatService.findByName(member.getRoom().getName());
        ChatMessageDto systemMessage = null;

        List<Property.Upgrade> upgrades = property.getUpgrades();
        if (downgradeChoice == null) {
            List<Property.Upgrade> validUpgrades = upgrades.stream()
                    .filter(upgrade -> List.of(Property.Upgrade.LEVEL_1, Property.Upgrade.LEVEL_2,
                            Property.Upgrade.LEVEL_3, Property.Upgrade.LEVEL_4,
                            Property.Upgrade.LEVEL_4_1, Property.Upgrade.LEVEL_4_2,
                            Property.Upgrade.LEVEL_4_3).contains(upgrade))
                    .toList();
            if (validUpgrades.size() > 1) {
                Property.Upgrade levelToDowngrade = validUpgrades.get(validUpgrades.size() - 1);
                upgrades.remove(levelToDowngrade);
                int price = gameUtils.getPriceByPositionAndLevel(position, levelToDowngrade);
                member.setGold(member.getGold() + (int) (price * demoteGoldCoefficient));
                member.setScore(member.getScore() - gameUtils.getScoreByPositionAndLevel(position, levelToDowngrade));
                memberRepository.save(member);
                systemMessage = ChatMessageDto.builder()
                        .type(ChatMessage.MessageType.SYSTEM_DOWNGRADE_PROPERTY)
                        .content(member.getUser().getNickname() + " " + position)
                        .timestamp(LocalDateTime.now())
                        .build();
            } else if (validUpgrades.size() == 1 && validUpgrades.contains(Property.Upgrade.LEVEL_1) &&
                    property.getMortgage() == -1) {
                property.setMortgage(5);
                int price = gameUtils.getPriceByPositionAndLevel(position, Property.Upgrade.LEVEL_1);
                member.setGold(member.getGold() + (int) (price * mortgageGoldCoefficient));
                member.setScore(member.getScore() -
                        gameUtils.getScoreByPositionAndLevel(position, Property.Upgrade.LEVEL_1));
                memberRepository.save(member);
                systemMessage = ChatMessageDto.builder()
                        .type(ChatMessage.MessageType.SYSTEM_MORTGAGE_PROPERTY)
                        .content(member.getUser().getNickname() + " " + position)
                        .timestamp(LocalDateTime.now())
                        .build();
            }
        } else {
            upgrades.remove(downgradeChoice);
            int price = gameUtils.getPriceByPositionAndLevel(position, downgradeChoice);
            member.setGold(member.getGold() + (int) (price * mortgageGoldCoefficient));
            member.setScore(member.getScore() - gameUtils.getScoreByPositionAndLevel(position, downgradeChoice));
            memberRepository.save(member);
            systemMessage = ChatMessageDto.builder()
                    .type(ChatMessage.MessageType.SYSTEM_MORTGAGE_PROPERTY)
                    .content(member.getUser().getNickname() + " " + position)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        property.setRoundOfLastChange(member.getFinishedRounds());
        property.setTurnOfLastChange(member.getRoom().getTurn());
        Property updatedProperty = checkForUniqueUpgradesOnDowngrade(property, member);

        ChatMessage chatMessage = chatMessageService.save(roomChat, systemMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + roomChat.getName(), chatMessage);

        return PropertyDto.builder()
                .id(updatedProperty.getId())
                .member(updatedProperty.getMember())
                .upgrades(gameUtils.getUpgrades(updatedProperty.getPosition(), updatedProperty))
                .mortgage(updatedProperty.getMortgage())
                .position(updatedProperty.getPosition())
                .goldOnStep(gameUtils.calculateGoldOnStep(updatedProperty))
                .tourismOnStep(gameUtils.calculateTourismOnStep(property))
                .goldPerTurn(gameUtils.calculateGoldPerTurn(updatedProperty))
                .upgradeRequirements(gameUtils.getRequirements(updatedProperty.getPosition(), member))
                .build();
    }

    @Override
    public PropertyDto downgradeProperty(Member member, Integer position) {
        return downgradeProperty(member, position, null);
    }

    @Override
    public PropertyDto payRent(Member member, Integer position) {
        if (!member.getPosition().equals(position) ||
                !propertyService.existsByRoomAndPosition(member.getRoom(), position)) {
            throw new UserNotAllowedException();
        }
        Property property = propertyService.findByRoomAndPosition(member.getRoom(), position);
        int onStep = gameUtils.calculateGoldOnStep(property);
        if (member.getGold() < onStep) {
            throw new UserNotAllowedException();
        }
        if (position == 7 || position == 30) {
            Event event = eventService.findByMemberAndType(member, Event.EventType.FOREIGN_PROPERTY);
            onStep = onStep * event.getRoll();
        }
        member.setGold(member.getGold() - onStep);
        memberRepository.save(member);
        Member owner = property.getMember();
        owner.setGold(owner.getGold() + onStep);
        memberRepository.save(owner);

        Chat roomChat = chatService.findByName(member.getRoom().getName());
        ChatMessageDto systemMessage = ChatMessageDto.builder()
                .type(ChatMessage.MessageType.SYSTEM_PAY_RENT)
                .content(member.getUser().getNickname() + " " + onStep + " " + property.getMember().getUser().getNickname())
                .timestamp(LocalDateTime.now())
                .build();
        ChatMessage chatMessage = chatMessageService.save(roomChat, systemMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + roomChat.getName(), chatMessage);

        eventService.delete(member, Event.EventType.FOREIGN_PROPERTY);

        return PropertyDto.builder()
                .id(property.getId())
                .member(property.getMember())
                .upgrades(gameUtils.getUpgrades(property.getPosition(), property))
                .position(property.getPosition())
                .goldOnStep(onStep)
                .tourismOnStep(gameUtils.calculateTourismOnStep(property))
                .goldPerTurn(gameUtils.calculateGoldPerTurn(property))
                .build();
    }

    private boolean calculateMemberTurnsToScienceProject(Member member) {
        int labBoostCount = 0;
        int governmentBoostCount = 0;
        int spaceportBoostCount = 0;
        int oxfordBoostCount = 0;

        for (Property property : member.getProperties()) {
            if ((property.getPosition() == 15 || property.getPosition() == 45) &&
                    property.getUpgrades().contains(Property.Upgrade.LEVEL_4)) {
                labBoostCount++;
            } else if ((property.getPosition() == 9 || property.getPosition() == 18 || property.getPosition() == 44) &&
                    property.getUpgrades().contains(Property.Upgrade.LEVEL_4_1)) {
                governmentBoostCount++;
            } else if (property.getPosition() == 46) {
                oxfordBoostCount++;
            } else if (property.getPosition() == 47) {
                spaceportBoostCount++;
            }
        }

        int totalLabBoost = labBoost * labBoostCount;
        int totalGovernmentBoost = governmentBoost * governmentBoostCount;
        int totalOxfordBoost = oxfordBoost * oxfordBoostCount;
        int totalSpaceportBoost = spaceportBoost * spaceportBoostCount;

        return member.getTurnsToNextScienceProject() != -1 &&
                member.getTurnsToNextScienceProject() - totalLabBoost - totalGovernmentBoost - totalOxfordBoost - totalSpaceportBoost <= 0;
    }

    private boolean isMemberAbleToSpace(Member member) {
        return member.getProperties().stream().anyMatch(p -> p.getPosition().equals(47) ||
                (p.getPosition().equals(15) || p.getPosition().equals(45)) &&
                        p.getUpgrades().contains(Property.Upgrade.LEVEL_4));
    }

    private List<Property.Upgrade> checkForUniqueUpgradesOnBuy(Integer position, Member member) {
        List<Property.Upgrade> uniqueUpgrades = new ArrayList<>();
        if (position == 10) {
            if (isMemberHasProperty(member, 9)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA);
            }
            if (isMemberHasProperty(member, 11)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_IRON);
            }
            if (isMemberHasProperty(member, 36)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_RUHR_VALLEY);
            }
        } else if (position == 12 || position == 14) {
            if (isMemberHasProperty(member, 32)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_MAUSOLEUM_AT_HALICARNASSUS);
            }
            if (isMemberHasProperty(member, Property.Upgrade.LEVEL_3, 17, 31)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_SHIPYARD);
            }
        } else if (position == 15 && isMemberHasProperty(member, 14)) {
            uniqueUpgrades.add(Property.Upgrade.ADJACENCY_REEF);
        } else if (position == 17) {
            if (isMemberHasProperty(member, 18)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA);
            }
            if (isMemberHasProperty(member, 23)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_COLOSSEUM);
            }
            if (isMemberHasProperty(member, 32)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_MAUSOLEUM_AT_HALICARNASSUS);
            }
        } else if (position == 18) {
            if (isMemberHasProperty(member, 23)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_COLOSSEUM);
            }
        } else if (position == 19) {
            if (isMemberHasProperty(member, 18)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA);
            }
            if (isMemberHasProperty(member, 23)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_COLOSSEUM);
            }
        } else if (position == 20) {
            if (isMemberHasProperty(member, 23)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_COLOSSEUM);
            }
        } else if (position == 21) {
            if (isMemberHasProperty(member, 20)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_WONDER);
            }
            if (isMemberHasProperty(member, 22)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_ENTERTAINMENT_COMPLEX);
            }
            if (isMemberHasProperty(member, 23)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_COLOSSEUM);
            }
        } else if ((position == 25 || position == 26 || position == 28)) {
            if (isMemberHasProperty(member, 23)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_COLOSSEUM);
            }
            if (isMemberHasProperty(member, 27)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_ETEMENANKI);
            }
        } else if (position == 31) {
            if (isMemberHasProperty(member, 23)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_COLOSSEUM);
            }
            if (isMemberHasProperty(member, 32)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_MAUSOLEUM_AT_HALICARNASSUS);
            }
        } else if (List.of(32, 33, 35, 36, 38, 40, 42, 46, 47).contains(position)) {
            if (isMemberHasProperty(member, 40)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_ESTADIO_DO_MARACANA);
            }
        } else if (position == 34) {
            if (isMemberHasProperty(member, 33)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_AQUEDUCT);
            }
            if (isMemberHasProperty(member, 35)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_DAM);
            }
            if (isMemberHasProperty(member, 36)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_RUHR_VALLEY);
            }
            if (isMemberHasProperty(member, 40)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_ESTADIO_DO_MARACANA);
            }
        } else if (position == 39) {
            if (isMemberHasProperty(member, 38)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_ENTERTAINMENT_COMPLEX);
            }
            if (isMemberHasProperty(member, 40)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_WONDER);
                uniqueUpgrades.add(Property.Upgrade.WONDER_ESTADIO_DO_MARACANA);
            }
        } else if (position == 41) {
            if (isMemberHasProperty(member, 40)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_WONDER);
                uniqueUpgrades.add(Property.Upgrade.WONDER_ESTADIO_DO_MARACANA);
            }
            if (isMemberHasProperty(member, 42)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_WONDER);
            }
        } else if (position == 43 || position == 45) {
            if (isMemberHasProperty(member, 40)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_ESTADIO_DO_MARACANA);
            }
            if (isMemberHasProperty(member, 44)) {
                uniqueUpgrades.add(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA);
            }
        }
        if (position != 20 && isMemberHasProperty(member, 20)) {
            if (member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(9)) && position >= 14) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_CASA_DE_CONTRATACION);
            } else if (member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(18)) && (position <= 12 || position >= 25)) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_CASA_DE_CONTRATACION);
            } else if (member.getProperties().stream()
                    .anyMatch(p -> p.getPosition().equals(44)) && position <= 36) {
                uniqueUpgrades.add(Property.Upgrade.WONDER_CASA_DE_CONTRATACION);
            }
        }
        return uniqueUpgrades;
    }

    private Property checkForUniqueUpgradesOnUpgrade(Property property, Member member) {
        if (List.of(1, 2, 3, 5).contains(property.getPosition()) &&
                property.getUpgrades().contains(Property.Upgrade.LEVEL_2)) {
            if (isMemberHasProperty(member, 4)) {
                property.getUpgrades().add(Property.Upgrade.WONDER_TEMPLE_OF_ARTEMIS);
            }
        } else if (property.getPosition() == 4) {
            member.getProperties().forEach(p -> {
                if (List.of(1, 2, 3, 5).contains(p.getPosition()) &&
                        p.getUpgrades().contains(Property.Upgrade.LEVEL_2)) {
                    p.getUpgrades().add(Property.Upgrade.WONDER_TEMPLE_OF_ARTEMIS);
                }
            });
        } else if (property.getPosition() == 9) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 10 && !p.getUpgrades().contains(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA)) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA);
                }
            });
        } else if ((property.getPosition() == 10 || property.getPosition() == 34) &&
                property.getUpgrades().contains(Property.Upgrade.LEVEL_3)) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 11 && p.getUpgrades().contains(Property.Upgrade.LEVEL_2) &&
                        !p.getUpgrades().contains(Property.Upgrade.ADJACENCY_FABRIC)) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_FABRIC);
                }
            });
        } else if (property.getPosition() == 11) {
            if (property.getUpgrades().contains(Property.Upgrade.LEVEL_2)) {
                if (isMemberHasProperty(member, Property.Upgrade.LEVEL_3, 10, 34) &&
                        !property.getUpgrades().contains(Property.Upgrade.ADJACENCY_FABRIC)) {
                    property.getUpgrades().add(Property.Upgrade.ADJACENCY_FABRIC);
                }
                if (isMemberHasProperty(member, 36) &&
                        !property.getUpgrades().contains(Property.Upgrade.WONDER_RUHR_VALLEY)) {
                    property.getUpgrades().add(Property.Upgrade.WONDER_RUHR_VALLEY);
                }
            } else {
                member.getProperties().forEach(p -> {
                    if (p.getPosition() == 10 && !p.getUpgrades().contains(Property.Upgrade.ADJACENCY_IRON)) {
                        p.getUpgrades().add(Property.Upgrade.ADJACENCY_IRON);
                    }
                });
            }
        } else if (property.getPosition() == 14) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 15) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_REEF);
                }
            });
        } else if ((property.getPosition() == 17 || property.getPosition() == 31) &&
                property.getUpgrades().contains(Property.Upgrade.LEVEL_3)) {
            member.getProperties().forEach(p -> {
                if (List.of(12, 14).contains(p.getPosition()) &&
                        !p.getUpgrades().contains(Property.Upgrade.ADJACENCY_SHIPYARD)) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_SHIPYARD);
                }
            });
        } else if (property.getPosition() == 18) {
            member.getProperties().forEach(p -> {
                if (List.of(17, 19).contains(p.getPosition()) &&
                        !p.getUpgrades().contains(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA)) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA);
                }
            });
        } else if (property.getPosition() == 20) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 21) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_WONDER);
                }
                if (member.getProperties().stream()
                        .anyMatch(prop -> prop.getPosition().equals(9) && prop.getMortgage() == -1) &&
                        p.getPosition() >= 14) {
                    p.getUpgrades().add(Property.Upgrade.WONDER_CASA_DE_CONTRATACION);
                } else if (member.getProperties().stream()
                        .anyMatch(prop -> prop.getPosition().equals(18) && prop.getMortgage() == -1) &&
                        (p.getPosition() <= 12 || p.getPosition() >= 25)) {
                    p.getUpgrades().add(Property.Upgrade.WONDER_CASA_DE_CONTRATACION);
                } else if (member.getProperties().stream()
                        .anyMatch(prop -> prop.getPosition().equals(44) && prop.getMortgage() == -1) &&
                        p.getPosition() <= 36) {
                    p.getUpgrades().add(Property.Upgrade.WONDER_CASA_DE_CONTRATACION);
                }
            });
        } else if (property.getPosition() == 22) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 21 && !p.getUpgrades().contains(Property.Upgrade.ADJACENCY_ENTERTAINMENT_COMPLEX)) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_ENTERTAINMENT_COMPLEX);
                }
            });
        } else if (property.getPosition() == 23) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() >= 17 && p.getPosition() <= 31 && p.getPosition() != 23) {
                    p.getUpgrades().add(Property.Upgrade.WONDER_COLOSSEUM);
                }
            });
        } else if (List.of(25, 26, 28).contains(property.getPosition()) &&
                property.getUpgrades().contains(Property.Upgrade.LEVEL_2)) {
            List<Property> farmProperties = member.getProperties().stream()
                    .filter(p -> !p.getPosition().equals(property.getPosition()) &&
                            List.of(25, 26, 28).contains(p.getPosition()) &&
                            p.getUpgrades().contains(Property.Upgrade.LEVEL_2))
                    .toList();
            if (!farmProperties.isEmpty()) {
                farmProperties.forEach(p -> {
                    if (!p.getUpgrades().contains(Property.Upgrade.ADJACENCY_FARMS)) {
                        p.getUpgrades().add(Property.Upgrade.ADJACENCY_FARMS);
                    }
                });
                property.getUpgrades().add(Property.Upgrade.ADJACENCY_FARMS);
            }
        } else if (property.getPosition() == 27) {
            member.getProperties().forEach(p -> {
                if (List.of(25, 26, 28).contains(p.getPosition())) {
                    p.getUpgrades().add(Property.Upgrade.WONDER_ETEMENANKI);
                }
            });
        } else if (property.getPosition() == 32) {
            member.getProperties().forEach(p -> {
                if (List.of(12, 14, 17, 31).contains(p.getPosition())) {
                    p.getUpgrades().add(Property.Upgrade.WONDER_MAUSOLEUM_AT_HALICARNASSUS);
                }
            });
        } else if (property.getPosition() == 33) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 34) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_AQUEDUCT);
                }
            });
        } else if (property.getPosition() == 35) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 34 && !p.getUpgrades().contains(Property.Upgrade.ADJACENCY_DAM)) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_DAM);
                }
            });
        } else if (property.getPosition() == 36) {
            member.getProperties().forEach(p -> {
                if (List.of(10, 34).contains(p.getPosition())) {
                    p.getUpgrades().add(Property.Upgrade.WONDER_RUHR_VALLEY);
                }
                if (p.getPosition() == 11 && p.getUpgrades().contains(Property.Upgrade.LEVEL_2)) {
                    p.getUpgrades().add(Property.Upgrade.WONDER_RUHR_VALLEY);
                }
            });
        } else if (property.getPosition() == 38) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 39 && !p.getUpgrades().contains(Property.Upgrade.ADJACENCY_ENTERTAINMENT_COMPLEX)) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_ENTERTAINMENT_COMPLEX);
                }
            });
        } else if (property.getPosition() == 40) {
            member.getProperties().forEach(p -> {
                if (List.of(39, 41).contains(p.getPosition())) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_WONDER);
                }
                if (p.getPosition() >= 32 && p.getPosition() <= 47 && p.getPosition() != 40) {
                    p.getUpgrades().add(Property.Upgrade.WONDER_ESTADIO_DO_MARACANA);
                }
            });
        } else if (property.getPosition() == 42) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 41) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_WONDER);
                }
            });
        } else if (property.getPosition() == 44) {
            member.getProperties().forEach(p -> {
                if (List.of(43, 45).contains(p.getPosition()) &&
                        !p.getUpgrades().contains(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA)) {
                    p.getUpgrades().add(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA);
                }
            });
        }
        if (List.of(9, 18, 44).contains(property.getPosition()) &&
                !property.getUpgrades().contains(Property.Upgrade.LEVEL_2)) {
            boolean hasCasa = member.getProperties().stream()
                    .anyMatch(prop -> prop.getPosition().equals(20));
            if (hasCasa) {
                member.getProperties().forEach(p -> p.getUpgrades().add(Property.Upgrade.WONDER_CASA_DE_CONTRATACION));
            }
        }
        Member updatedMember = memberRepository.save(member);
        property.setMember(updatedMember);
        return propertyService.save(property);
    }

    private Property checkForUniqueUpgradesOnDowngrade(Property property, Member member) {
        if (List.of(1, 2, 3, 5).contains(property.getPosition()) &&
                !property.getUpgrades().contains(Property.Upgrade.LEVEL_2)) {
            if (isMemberHasProperty(member, 4)) {
                property.getUpgrades().remove(Property.Upgrade.WONDER_TEMPLE_OF_ARTEMIS);
            }
        } else if (property.getPosition() == 4) {
            member.getProperties().forEach(p -> {
                if (List.of(1, 2, 3, 5).contains(p.getPosition())) {
                    p.getUpgrades().remove(Property.Upgrade.WONDER_TEMPLE_OF_ARTEMIS);
                }
            });
        } else if (property.getPosition() == 9 && property.getMortgage() != -1) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 10) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA);
                }
            });
        } else if ((property.getPosition() == 10 || property.getPosition() == 34) &&
                !property.getUpgrades().contains(Property.Upgrade.LEVEL_3)) {
            boolean isMaxLevel3 = member.getProperties().stream()
                    .filter(p -> p.getPosition() == 10 || p.getPosition() == 34)
                    .anyMatch(p -> p.getUpgrades().contains(Property.Upgrade.LEVEL_3) &&
                            !p.getPosition().equals(property.getPosition()));
            if (!isMaxLevel3) {
                member.getProperties().forEach(p -> {
                    if (p.getPosition() == 11) {
                        p.getUpgrades().remove(Property.Upgrade.ADJACENCY_FABRIC);
                    }
                });
            }
        } else if (property.getPosition() == 11) {
            if (property.getMortgage() != -1) {
                member.getProperties().forEach(p -> {
                    if (p.getPosition() == 10) {
                        p.getUpgrades().remove(Property.Upgrade.ADJACENCY_IRON);
                    }
                });
            } else if (!property.getUpgrades().contains(Property.Upgrade.LEVEL_2)) {
                property.getUpgrades().remove(Property.Upgrade.ADJACENCY_FABRIC);
                property.getUpgrades().remove(Property.Upgrade.WONDER_RUHR_VALLEY);
            }
        } else if (property.getPosition() == 14) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 15) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_REEF);
                }
            });
        } else if ((property.getPosition() == 17 || property.getPosition() == 31) &&
                !property.getUpgrades().contains(Property.Upgrade.LEVEL_3)) {
            boolean isMaxLevel3 = member.getProperties().stream()
                    .filter(p -> p.getPosition() == 17 || p.getPosition() == 31)
                    .anyMatch(p -> p.getUpgrades().contains(Property.Upgrade.LEVEL_3));
            if (!isMaxLevel3) {
                member.getProperties().forEach(p -> {
                    if (List.of(12, 14).contains(p.getPosition())) {
                        p.getUpgrades().remove(Property.Upgrade.ADJACENCY_SHIPYARD);
                    }
                });
            }
        } else if (property.getPosition() == 18 && property.getMortgage() != -1) {
            member.getProperties().forEach(p -> {
                if (List.of(17, 19).contains(p.getPosition())) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA);
                }
            });
        } else if (property.getPosition() == 20) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 21) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_WONDER);
                }
                if (member.getProperties().stream()
                        .anyMatch(prop -> prop.getPosition().equals(9)) && p.getPosition() >= 14) {
                    p.getUpgrades().remove(Property.Upgrade.WONDER_CASA_DE_CONTRATACION);
                } else if (member.getProperties().stream()
                        .anyMatch(prop -> prop.getPosition().equals(18)) && (p.getPosition() <= 12 || p.getPosition() >= 25)) {
                    p.getUpgrades().remove(Property.Upgrade.WONDER_CASA_DE_CONTRATACION);
                } else if (member.getProperties().stream()
                        .anyMatch(prop -> prop.getPosition().equals(44)) && p.getPosition() <= 36) {
                    p.getUpgrades().remove(Property.Upgrade.WONDER_CASA_DE_CONTRATACION);
                }
            });
        } else if (property.getPosition() == 22) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 21) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_ENTERTAINMENT_COMPLEX);
                }
            });
        } else if (property.getPosition() == 23) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() >= 17 && p.getPosition() <= 31 && p.getPosition() != 23) {
                    p.getUpgrades().remove(Property.Upgrade.WONDER_COLOSSEUM);
                }
            });
        } else if (List.of(25, 26, 28).contains(property.getPosition()) &&
                !property.getUpgrades().contains(Property.Upgrade.LEVEL_2)) {
            List<Property> farmProperties = member.getProperties().stream()
                    .filter(p -> !p.getPosition().equals(property.getPosition()) &&
                            List.of(25, 26, 28).contains(p.getPosition()) &&
                            p.getUpgrades().contains(Property.Upgrade.LEVEL_2))
                    .toList();
            if (farmProperties.size() == 1) {
                farmProperties.forEach(p -> p.getUpgrades().remove(Property.Upgrade.ADJACENCY_FARMS));
            }
            property.getUpgrades().remove(Property.Upgrade.ADJACENCY_FARMS);
        } else if (property.getPosition() == 27) {
            member.getProperties().forEach(p -> {
                if (List.of(25, 26, 28).contains(p.getPosition())) {
                    p.getUpgrades().remove(Property.Upgrade.WONDER_ETEMENANKI);
                }
            });
        } else if (property.getPosition() == 32) {
            member.getProperties().forEach(p -> {
                if (List.of(12, 14, 17, 31).contains(p.getPosition())) {
                    p.getUpgrades().remove(Property.Upgrade.WONDER_MAUSOLEUM_AT_HALICARNASSUS);
                }
            });
        } else if (property.getPosition() == 33) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 34) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_AQUEDUCT);
                }
            });
        } else if (property.getPosition() == 35) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 34) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_DAM);
                }
            });
        } else if (property.getPosition() == 36) {
            member.getProperties().forEach(p -> {
                if (List.of(10, 34).contains(p.getPosition())) {
                    p.getUpgrades().remove(Property.Upgrade.WONDER_RUHR_VALLEY);
                }
                if (p.getPosition() == 11) {
                    p.getUpgrades().remove(Property.Upgrade.WONDER_RUHR_VALLEY);
                }
            });
        } else if (property.getPosition() == 38) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 39) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_ENTERTAINMENT_COMPLEX);
                }
            });
        } else if (property.getPosition() == 40) {
            member.getProperties().forEach(p -> {
                if (List.of(39, 41).contains(p.getPosition())) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_WONDER);
                }
                if (p.getPosition() >= 32 && p.getPosition() <= 47 && p.getPosition() != 40) {
                    p.getUpgrades().remove(Property.Upgrade.WONDER_ESTADIO_DO_MARACANA);
                }
            });
        } else if (property.getPosition() == 42) {
            member.getProperties().forEach(p -> {
                if (p.getPosition() == 41) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_WONDER);
                }
            });
        } else if (property.getPosition() == 44) {
            member.getProperties().forEach(p -> {
                if (List.of(43, 45).contains(p.getPosition())) {
                    p.getUpgrades().remove(Property.Upgrade.ADJACENCY_GOVERNMENT_PLAZA);
                }
            });
        }
        if (List.of(9, 18, 44).contains(property.getPosition()) && property.getMortgage() != -1) {
            boolean hasCasa = member.getProperties().stream()
                    .anyMatch(prop -> prop.getPosition().equals(20));
            if (hasCasa) {
                member.getProperties()
                        .forEach(p -> p.getUpgrades().remove(Property.Upgrade.WONDER_CASA_DE_CONTRATACION));
            }
        }
        Member updatedMember = memberRepository.save(member);
        property.setMember(updatedMember);
        return propertyService.save(property);
    }

    private boolean isMemberHasProperty(Member member, Property.Upgrade upgrade, Integer... positions) {
        return member.getProperties().stream()
                .anyMatch(p -> List.of(positions).contains(p.getPosition()) &&
                        p.getMortgage() == -1 &&
                        p.getUpgrades().contains(upgrade));
    }

    private boolean isMemberHasProperty(Member member, Integer... positions) {
        return isMemberHasProperty(member, Property.Upgrade.LEVEL_1, positions);
    }

    private boolean isPropertyWonder(int position) {
        return List.of(4, 8, 16, 20, 23, 27, 32, 36, 40, 42, 46).contains(position);
    }
}
