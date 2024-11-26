package com.civka.monopoly.api.repository;

import com.civka.monopoly.api.entity.Event;
import com.civka.monopoly.api.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByMemberAndType(Member member, Event.EventType type);

    boolean existsByMemberAndType(Member member, Event.EventType type);
}
