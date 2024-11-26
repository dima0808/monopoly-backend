package com.civka.monopoly.api.service;

import com.civka.monopoly.api.dto.ProjectType;
import com.civka.monopoly.api.entity.Event;
import com.civka.monopoly.api.entity.Member;
import com.civka.monopoly.api.entity.Room;

public interface EventService {

    Event save(Event event);

    Event findByMemberAndType(Member member, Event.EventType type);

    Event add(Member member, Event.EventType type, Integer roll);

    Event add(Member member, Event.EventType type);

    Event delete(Member member, Event.EventType type);

    Event makeChoice(Member member, Event.EventType type, Integer choice);

    Event makeProjectChoice(Member member, ProjectType type);

    Event doScienceProject(Member member);

    Event doConcert(Member member);

    Event.EventType randomGoodyHutEvent();

    Event.EventType randomBarbariansEvent();

    void handleNewPosition(int newPosition, Member member, int firstRoll, int secondRoll);

    Room handleBermudaTriangle(Member member, int requiredPosition);
}
