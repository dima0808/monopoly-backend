package com.civka.monopoly.api.service.impl;

import com.civka.monopoly.api.dto.ChatMessageDto;
import com.civka.monopoly.api.dto.ProjectType;
import com.civka.monopoly.api.entity.*;
import com.civka.monopoly.api.payload.EventMessage;
import com.civka.monopoly.api.payload.PlayerMessage;
import com.civka.monopoly.api.payload.RoomMessage;
import com.civka.monopoly.api.repository.EventRepository;
import com.civka.monopoly.api.repository.MemberRepository;
import com.civka.monopoly.api.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameUtilsImpl gameUtils;
    private final MemberRepository memberRepository;
    private final ChatService chatService;
    private final ChatMessageService chatMessageService;
    private final PropertyService propertyService;

    @Value("${monopoly.app.room.game.science-project.basicTurnAmount}")
    private Integer basicTurnAmount;

    @Value("${monopoly.app.room.game.science-project.expeditionTurnAmount}")
    private Integer expeditionTurnAmount;

    @Value("${monopoly.app.room.game.science-project.laserBoost}")
    private Integer laserBoost;

    @Value("${monopoly.app.room.game.wonder-effect.16}")
    private Integer goldForProjectGreatLibrary;

    @Value("${monopoly.app.room.game.science-project.cost}")
    private Integer scienceProjectCost;

    @Value("${monopoly.app.room.game.concert.cost}")
    private Integer concertCost;

    @Value("${monopoly.app.room.game.concert.tourismLowerBound}")
    private Integer concertTourismLowerBound;

    @Value("${monopoly.app.room.game.concert.tourismUpperBound}")
    private Integer concertTourismUpperBound;

    @Override
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Event findByMemberAndType(Member member, Event.EventType type) {
        return eventRepository.findByMemberAndType(member, type).orElse(null);
    }

    @Override
    public Event add(Member member, Event.EventType type, Integer roll) {
        if (eventRepository.existsByMemberAndType(member, type)) {
            throw new UserNotAllowedException();
        }
        if (type == Event.EventType.FOREIGN_PROPERTY) {
            Property property = propertyService.findByRoomAndPosition(member.getRoom(), member.getPosition());
            Member owner = property.getMember();
            owner.setTourism(owner.getTourism() + gameUtils.calculateTourismOnStep(property));
            memberRepository.save(owner);

            PlayerMessage playerMessage = PlayerMessage.builder()
                    .type(PlayerMessage.MessageType.TOURIST)
                    .content("Member " + member.getUser().getUsername() + " visited " +
                            owner.getUser().getUsername() + "'s property")
                    .member(owner)
                    .build();
            messagingTemplate.convertAndSend("/topic/public/" + member.getRoom().getName() + "/game", playerMessage);
        }
        Event event = Event.builder()
                .member(member)
                .type(type)
                .roll(roll)
                .build();
        EventMessage eventMessage = EventMessage.builder()
                .type(EventMessage.MessageType.ADD_EVENT)
                .content("Event added: " + type)
                .event(event)
                .build();
        messagingTemplate.convertAndSendToUser(member.getUser().getUsername(), "/queue/events", eventMessage);
        return eventRepository.save(event);
    }

    @Override
    public Event add(Member member, Event.EventType type) {
        return add(member, type, null);
    }

    @Override
    public Event delete(Member member, Event.EventType type) {
        Event event = member.getEvents().stream()
                .filter(e -> e.getType().equals(type))
                .findFirst()
                .orElse(null);

        if (event != null) {
            if (type == Event.EventType.BERMUDA) {
                handleBermudaTriangle(member, -1);
            }
            member.getEvents().removeIf(e -> e.getType().equals(type));
            memberRepository.save(member);
            EventMessage eventMessage = EventMessage.builder()
                    .type(EventMessage.MessageType.DELETE_EVENT)
                    .content("Event deleted: " + type)
                    .event(event)
                    .build();
            messagingTemplate.convertAndSendToUser(member.getUser().getUsername(), "/queue/events", eventMessage);
        }
        return event;
    }

    @Override
    public Event makeChoice(Member member, Event.EventType type, Integer choice) {
        switch (type) {
            case GOODY_HUT_FREE_GOLD:
            case GOODY_HUT_JACKPOT:
                member.setGold(member.getGold() + gameUtils.getEventGold(type));
                break;
            case GOODY_HUT_FREE_STRENGTH:
                member.setStrength(member.getStrength() + gameUtils.getEventStrength(type));
                break;
            case GOODY_HUT_FREE_GOLD_OR_STRENGTH:
                if (choice == 1) {
                    member.setGold(member.getGold() + gameUtils.getEventGold(type));
                } else {
                    member.setStrength(member.getStrength() + gameUtils.getEventStrength(type));
                }
                break;
            case GOODY_HUT_HAPPY_BIRTHDAY:
                for (Member m : member.getRoom().getMembers()) {
                    m.setGold(Math.max(0, m.getGold() - gameUtils.getEventGold(type)));
                    member.setGold(member.getGold() + gameUtils.getEventGold(type));
                }
                break;
            case GOODY_HUT_WONDER_DISCOUNT:
                member.getAdditionalEffects().add(AdditionalEffect.builder()
                        .member(member)
                        .type(AdditionalEffect.AdditionalEffectType.GOODY_HUT_WONDER_DISCOUNT)
                        .turnsLeft(-1)
                        .build());
            case GOODY_HUT_DICE_BUFF:
                // todo
                break;
            case BARBARIANS_PAY_GOLD_OR_STRENGTH:
                if (choice == 1) {
                    member.setGold(member.getGold() - gameUtils.getEventGold(type));
                } else {
                    member.setStrength(Math.max(0, member.getStrength() - gameUtils.getEventStrength(type)));
                }
                break;
            case BARBARIANS_PAY_GOLD_OR_HIRE:
                if (choice == 0) {
                    member.setGold(member.getGold() - gameUtils.getEventGold(type));
                } else {
                    member.setStrength(member.getStrength() + gameUtils.getHireIncome(type));
                    member.setGold(member.getGold() - gameUtils.getHirePrice(type));
                }
                break;
            case BARBARIANS_PAY_STRENGTH:
                member.setStrength(Math.max(0, member.getStrength() - gameUtils.getEventStrength(type)));
                break;
            case BARBARIANS_ATTACK_NEIGHBOR:
            case BARBARIANS_RAID:
            case BARBARIANS_PILLAGE:
            case BARBARIANS_RAGNAROK:
                // todo
                break;
            default:
                break;
        }
        memberRepository.save(member);
        return delete(member, type);
    }

    @Override
    @Transactional
    public Event makeProjectChoice(Member member, ProjectType choice) {
        switch (choice) {
            case BREAD_AND_CIRCUSES -> {
                Room room = member.getRoom();
                Property.Upgrade highestDistrictLevel = gameUtils.getHighestDistrictLevel(member, "ENTERTAINMENT");
                List<Member> members = room.getMembers();

                Member updatedMember = null;
                for (Member m : members) {
                    boolean isCurrentMember = m.equals(member);
                    List<Property> properties = m.getProperties();
                    Iterator<Property> iterator = properties.iterator();
                    while (iterator.hasNext()) {
                        Property property = iterator.next();
                        if (property.getMortgage() != -1) {
                            property.setMortgage(property.getMortgage() +
                                    gameUtils.getBreadAndCircusesByLevel(highestDistrictLevel, isCurrentMember));
                            propertyService.save(property);
                            if (!isCurrentMember && property.getMortgage() < 1) {
                                iterator.remove();
                            }
                        }
                    }
                    if (!isCurrentMember) {
                        memberRepository.save(m);
                    } else {
                        updatedMember = memberRepository.save(m);
                    }
                }
                if (updatedMember != null) {
                    handleGreatLibrary(updatedMember);
                    handleBigBen(updatedMember, false);
                }

                RoomMessage roomMessage = RoomMessage.builder()
                        .type(RoomMessage.MessageType.PROJECTS)
                        .content("Bread and Circuses project completed!")
                        .room(room)
                        .build();
                messagingTemplate.convertAndSend("/topic/public/" + room.getName() + "/game", roomMessage);
            }

            case COMMERCIAL_HUB_INVESTMENT -> {
                Room room = member.getRoom();
                Property.Upgrade highestDistrictLevel = gameUtils.getHighestDistrictLevel(member, "COMMERCIAL_HUB");
                AdditionalEffect commercialHubInvestment = AdditionalEffect.builder()
                        .member(member)
                        .type(switch (highestDistrictLevel) {
                            case LEVEL_1 -> AdditionalEffect.AdditionalEffectType.COMMERCIAL_HUB_INVESTMENT_1;
                            case LEVEL_2 -> AdditionalEffect.AdditionalEffectType.COMMERCIAL_HUB_INVESTMENT_2;
                            case LEVEL_3 -> AdditionalEffect.AdditionalEffectType.COMMERCIAL_HUB_INVESTMENT_3;
                            case LEVEL_4 -> AdditionalEffect.AdditionalEffectType.COMMERCIAL_HUB_INVESTMENT_4;
                            default -> null;
                        })
                        .turnsLeft(10)
                        .build();
                member.getAdditionalEffects().add(commercialHubInvestment);
                Member updatedMember = memberRepository.save(member);
                handleGreatLibrary(updatedMember);
                handleBigBen(updatedMember, true);

                RoomMessage roomMessage = RoomMessage.builder()
                        .type(RoomMessage.MessageType.PROJECTS)
                        .content("Commercial Hub Investment project completed!")
                        .room(room)
                        .build();
                messagingTemplate.convertAndSend("/topic/public/" + room.getName() + "/game", roomMessage);
            }

            case ENCAMPMENT_TRAINING -> {
                Property.Upgrade highestDistrictLevel = gameUtils.getHighestDistrictLevel(member, "ENCAMPMENT");
                member.setStrength(member.getStrength() +
                        gameUtils.getProjectStrengthByLevel(choice, highestDistrictLevel));
                Member updatedMember = memberRepository.save(member);
                handleGreatLibrary(updatedMember);
                handleBigBen(updatedMember, false);
            }

            case HARBOR_SHIPPING -> {
                Property.Upgrade highestDistrictLevel = gameUtils.getHighestDistrictLevel(member, "HARBOR");
                member.setGold(member.getGold() + gameUtils.getProjectGoldByLevel(choice, highestDistrictLevel));
                Member updatedMember = memberRepository.save(member);
                handleGreatLibrary(updatedMember);
                handleBigBen(updatedMember, false);
            }

            case INDUSTRIAL_ZONE_LOGISTICS -> {
                Property.Upgrade highestDistrictLevel = gameUtils.getHighestDistrictLevel(member, "INDUSTRIAL_ZONE");
                AdditionalEffect.AdditionalEffectType discountType = switch (highestDistrictLevel) {
                    case LEVEL_1 -> AdditionalEffect.AdditionalEffectType.WONDER_DISCOUNT_1;
                    case LEVEL_2 -> AdditionalEffect.AdditionalEffectType.WONDER_DISCOUNT_2;
                    case LEVEL_3 -> AdditionalEffect.AdditionalEffectType.WONDER_DISCOUNT_3;
                    case LEVEL_4 -> AdditionalEffect.AdditionalEffectType.WONDER_DISCOUNT_4;
                    default -> null;
                };
                Double discount = gameUtils.getDiscountByAdditionalEffect(discountType);
                member.setDiscount(member.getDiscount() + discount);
                if (member.getProperties().stream().anyMatch(p -> p.getPosition().equals(32))) {
                    member.setDiscount(member.getDiscount() + discount);
                }
                Member updatedMember = memberRepository.save(member);
                handleGreatLibrary(updatedMember);
                handleBigBen(updatedMember, false);
            }

            case THEATER_SQUARE_PERFORMANCES -> {
                Property.Upgrade highestDistrictLevel = gameUtils.getHighestDistrictLevel(member, "THEATER_SQUARE");
                member.setTourism(member.getTourism() +
                        gameUtils.getProjectTourismByLevel(choice, highestDistrictLevel));
                Member updatedMember = memberRepository.save(member);
                handleGreatLibrary(updatedMember);
                handleBigBen(updatedMember, false);
            }

            case CAMPUS_RESEARCH_GRANTS -> {
                Room room = member.getRoom();
                if (member.getFinishedScienceProjects()
                        .stream()
                        .noneMatch((project) -> project.equals(Member.ScienceProject.CAMPUS))) {
                    List<Member.ScienceProject> finishedScienceProjects = member.getFinishedScienceProjects();
                    finishedScienceProjects.add(Member.ScienceProject.CAMPUS);
                    Member updatedMember = memberRepository.save(member);
                    handleGreatLibrary(updatedMember);
                }

                RoomMessage roomMessage = RoomMessage.builder()
                        .type(RoomMessage.MessageType.PROJECTS)
                        .content("Campus Research Grants project completed!")
                        .room(room)
                        .build();
                messagingTemplate.convertAndSend("/topic/public/" + room.getName() + "/game", roomMessage);
            }

            case LAUNCH_EARTH_SATELLITE,
                 LAUNCH_MOON_LANDING,
                 LAUNCH_MARS_COLONY,
                 EXOPLANET_EXPEDITION,
                 TERRESTRIAL_LASER_STATION -> {
                Member.ScienceProject spaceProject = switch (choice) {
                    case LAUNCH_EARTH_SATELLITE -> Member.ScienceProject.SATELLITE;
                    case LAUNCH_MOON_LANDING -> Member.ScienceProject.MOON;
                    case LAUNCH_MARS_COLONY -> Member.ScienceProject.MARS;
                    case EXOPLANET_EXPEDITION -> Member.ScienceProject.EXOPLANET;
                    case TERRESTRIAL_LASER_STATION -> Member.ScienceProject.LASER;
                    default -> null;
                };
                if ((spaceProject.equals(Member.ScienceProject.SATELLITE) ||
                        member.getFinishedScienceProjects()
                                .stream()
                                .anyMatch((project) -> project.ordinal() == spaceProject.ordinal() - 1)) &&
                        member.getProperties()
                                .stream()
                                .anyMatch(p -> p.getPosition().equals(47) ||
                                        (p.getPosition().equals(15) || p.getPosition().equals(45))
                                                && p.getUpgrades().contains(Property.Upgrade.LEVEL_4))) {
                    handleScienceLastPhase(member, spaceProject);
                    Member updatedMember = memberRepository.save(member);
                    handleGreatLibrary(updatedMember);
                } else {
                    throw new UserNotAllowedException();
                }
            }
            default -> {
                return null;
            }
        }
        return delete(member, Event.EventType.PROJECTS);
    }

    @Override
    public Event doScienceProject(Member member) {
        Member.ScienceProject nextProject = getScienceProject(member);
        handleScienceLastPhase(member, nextProject);
        member.setTurnsToNextScienceProject(basicTurnAmount);
        if (member.getGold() < scienceProjectCost) {
            throw new UserNotAllowedException();
        }
        member.setGold(member.getGold() - scienceProjectCost);
        Member updatedMember = memberRepository.save(member);
        handleGreatLibrary(updatedMember);

        return delete(member, Event.EventType.SCIENCE_PROJECTS);
    }

    @Override
    public Event doConcert(Member member) {
        if (member.getGold() < concertCost) {
            throw new UserNotAllowedException();
        }
        member.setGold(member.getGold() - concertCost);
        int tourism = concertTourismLowerBound
                + (int) (Math.random() * (concertTourismUpperBound - concertTourismLowerBound));
        member.setTourism(member.getTourism() + tourism);
        memberRepository.save(member);

        Chat roomChat = chatService.findByName(member.getRoom().getName());
        ChatMessageDto systemMessage = ChatMessageDto.builder()
                .type(ChatMessage.MessageType.SYSTEM_CONCERT)
                .content(member.getUser().getNickname() + " " + tourism)
                .timestamp(LocalDateTime.now())
                .build();
        ChatMessage chatMessage = chatMessageService.save(roomChat, systemMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + roomChat.getName(), chatMessage);
        return delete(member, Event.EventType.GIVE_CONCERT);
    }

    @Override
    public Event.EventType randomGoodyHutEvent() {
        List<Event.EventType> goodyHutEvents = Arrays.asList(
                Event.EventType.GOODY_HUT_FREE_GOLD,
                Event.EventType.GOODY_HUT_FREE_STRENGTH,
                Event.EventType.GOODY_HUT_FREE_GOLD_OR_STRENGTH,
                Event.EventType.GOODY_HUT_HAPPY_BIRTHDAY,
                Event.EventType.GOODY_HUT_WONDER_DISCOUNT,
                Event.EventType.GOODY_HUT_DICE_BUFF,
                Event.EventType.GOODY_HUT_JACKPOT
        );
        return goodyHutEvents.get((int) (Math.random() * goodyHutEvents.size()));
    }

    @Override
    public Event.EventType randomBarbariansEvent() {
        List<Event.EventType> barbariansEvents = Arrays.asList(
                Event.EventType.BARBARIANS_PAY_GOLD_OR_STRENGTH,
                Event.EventType.BARBARIANS_PAY_GOLD_OR_HIRE,
                Event.EventType.BARBARIANS_PAY_STRENGTH,
                Event.EventType.BARBARIANS_ATTACK_NEIGHBOR,
                Event.EventType.BARBARIANS_RAID,
                Event.EventType.BARBARIANS_PILLAGE,
                Event.EventType.BARBARIANS_RAGNAROK
        );
        return barbariansEvents.get((int) (Math.random() * barbariansEvents.size()));
    }

    @Override
    public void handleNewPosition(int newPosition, Member member, int firstRoll, int secondRoll) {
        if (newPosition == 0) {
            // nothing happens
        } else if (newPosition == 13 || newPosition == 37) {
            if (hasDistrictForProjects(member)) {
                add(member, Event.EventType.PROJECTS);
            }
        } else if (newPosition == 24) {
            add(member, Event.EventType.BERMUDA);
        } else if (newPosition == 6) {
            add(member, randomGoodyHutEvent());
        } else if (newPosition == 29) {
            add(member, randomBarbariansEvent());
        } else if (member.getRoom().getProperties().stream()
                .noneMatch(property -> property.getPosition().equals(newPosition))) {
            add(member, Event.EventType.BUY_PROPERTY);
        } else if (member.getProperties().stream()
                .noneMatch(property -> property.getPosition().equals(newPosition))) {
            if (newPosition == 7 || newPosition == 30) {
                add(member, Event.EventType.FOREIGN_PROPERTY, firstRoll + secondRoll);
            } else {
                add(member, Event.EventType.FOREIGN_PROPERTY);
            }
            if (member.getProperties().stream()
                    .anyMatch(property -> List.of(9, 18, 44).contains(property.getPosition()) &&
                            property.getUpgrades().contains(Property.Upgrade.LEVEL_4_3))) {
                add(member, Event.EventType.GIVE_CONCERT);
            }
        }
    }

    @Override
    public Room handleBermudaTriangle(Member member, int requiredPosition) {
        int newPosition = requiredPosition == -1 ? (int) (Math.random() * 48) : requiredPosition;
        if (newPosition == 24 && requiredPosition == -1) {
            newPosition = 29; // Easter Egg with barbs
        }
        int oldPosition = member.getPosition();
        member.setPosition(newPosition);
        Member updatedMember = memberRepository.save(member);
        handleNewPosition(newPosition, updatedMember, 0, requiredPosition == -1 ? 1 : newPosition - oldPosition);

        String roomName = updatedMember.getRoom().getName();
        Chat roomChat = chatService.findByName(roomName);
        ChatMessageDto systemMessage = ChatMessageDto.builder()
                .type(ChatMessage.MessageType.SYSTEM_BERMUDA)
                .content(updatedMember.getUser().getNickname() + " " + newPosition)
                .timestamp(LocalDateTime.now())
                .build();
        ChatMessage chatMessage = chatMessageService.save(roomChat, systemMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + roomChat.getName(), chatMessage);

        PlayerMessage playerMessage = PlayerMessage.builder()
                .type(PlayerMessage.MessageType.BERMUDA)
                .content("Member " + updatedMember.getUser().getUsername() + " teleported to " + newPosition)
                .member(updatedMember)
                .build();
        messagingTemplate.convertAndSend("/topic/public/" + roomName + "/game", playerMessage);
        return member.getRoom();
    }

    private void handleGreatLibrary(Member member) {
        Property property = propertyService.findByRoomAndPosition(member.getRoom(), 16);
        if (property != null && !member.equals(property.getMember())) {
            property.getMember().setGold(property.getMember().getGold() + goldForProjectGreatLibrary);
            Property updatedProperty = propertyService.save(property);

            RoomMessage playerMessage = RoomMessage.builder()
                    .type(RoomMessage.MessageType.GREAT_LIBRARY_PAYMENT)
                    .content("Member " + updatedProperty.getMember().getUser().getUsername() + " received money for Great Library")
                    .room(updatedProperty.getMember().getRoom())
                    .build();
            messagingTemplate.convertAndSend("/topic/public/" + updatedProperty.getMember().getRoom().getName() + "/game", playerMessage);
        }
    }

    private void handleBigBen(Member member, boolean isCommercialHubInvestment) {
        if (member.getProperties().stream()
                .anyMatch(p -> p.getPosition().equals(42))) {
            int income = 0;
            int change;
            for (Member m : member.getRoom().getMembers()) {
                if (!m.equals(member)) {
                    change = isCommercialHubInvestment ? m.getGold() : (int) (m.getGold() * 0.5);
                    income += change;
                    m.setGold(m.getGold() - change);
                    memberRepository.save(m);
                }
            }
            member.setGold(member.getGold() + income);
            Member updatedMember = memberRepository.save(member);
            RoomMessage playerMessage = RoomMessage.builder()
                    .type(RoomMessage.MessageType.BIG_BEN_PAYMENT)
                    .content("Member " + updatedMember.getUser().getUsername() + " stole money using Big Ben")
                    .room(updatedMember.getRoom())
                    .build();
            messagingTemplate.convertAndSend("/topic/public/" + updatedMember.getRoom().getName() + "/game", playerMessage);
            Chat roomChat = chatService.findByName(updatedMember.getRoom().getName());
            ChatMessageDto systemMessage = ChatMessageDto.builder()
                    .type(ChatMessage.MessageType.SYSTEM_BIG_BEN)
                    .content(updatedMember.getUser().getNickname() + " " + income)
                    .timestamp(LocalDateTime.now())
                    .build();
            ChatMessage chatMessage = chatMessageService.save(roomChat, systemMessage);
            messagingTemplate.convertAndSend("/topic/chat/" + roomChat.getName(), chatMessage);
        }
    }

    private void handleScienceLastPhase(Member member, Member.ScienceProject nextProject) {
        List<Member.ScienceProject> finishedScienceProjects = member.getFinishedScienceProjects();
        finishedScienceProjects.add(nextProject);
        if (nextProject.equals(Member.ScienceProject.EXOPLANET)) {
            member.setExpeditionTurns(expeditionTurnAmount);
        } else if (nextProject.equals(Member.ScienceProject.LASER)) {
            member.setExpeditionTurns(Math.max(member.getExpeditionTurns() - laserBoost, 0));
        }
        Chat roomChat = chatService.findByName(member.getRoom().getName());
        ChatMessageDto systemMessage = ChatMessageDto.builder()
                .type(ChatMessage.MessageType.SYSTEM_SCIENCE_PROJECT)
                .content(member.getUser().getNickname() + " " + nextProject)
                .timestamp(LocalDateTime.now())
                .build();
        ChatMessage chatMessage = chatMessageService.save(roomChat, systemMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + roomChat.getName(), chatMessage);
    }

    private Member.ScienceProject getScienceProject(Member member) {
        List<Member.ScienceProject> finishedScienceProjects = member.getFinishedScienceProjects();
        Member.ScienceProject nextProject;

        if (finishedScienceProjects.contains(Member.ScienceProject.EXOPLANET)) {
            nextProject = Member.ScienceProject.LASER;
        } else if (finishedScienceProjects.contains(Member.ScienceProject.MARS)) {
            nextProject = Member.ScienceProject.EXOPLANET;
        } else if (finishedScienceProjects.contains(Member.ScienceProject.MOON)) {
            nextProject = Member.ScienceProject.MARS;
        } else if (finishedScienceProjects.contains(Member.ScienceProject.SATELLITE)) {
            nextProject = Member.ScienceProject.MOON;
        } else {
            nextProject = Member.ScienceProject.SATELLITE;
        }
        return nextProject;
    }

    private boolean hasDistrictForProjects(Member member) {
        return member.getProperties().stream()
                .filter(property -> property.getPosition() == 7 ||
                        property.getPosition() == 10 ||
                        property.getPosition() == 15 ||
                        property.getPosition() == 17 ||
                        property.getPosition() == 19 ||
                        property.getPosition() == 21 ||
                        property.getPosition() == 22 ||
                        property.getPosition() == 30 ||
                        property.getPosition() == 31 ||
                        property.getPosition() == 34 ||
                        property.getPosition() == 38 ||
                        property.getPosition() == 39 ||
                        property.getPosition() == 43 ||
                        property.getPosition() == 45 ||
                        property.getPosition() == 47
                )
                .count() >= 3;
    }
}
