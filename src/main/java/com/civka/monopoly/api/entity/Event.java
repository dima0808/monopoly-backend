package com.civka.monopoly.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private EventType type;

    private Integer roll;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    public enum EventType {
        BUY_PROPERTY,
        UPGRADE_PROPERTY,
        PROJECTS,
        SCIENCE_PROJECTS,
        GIVE_CONCERT,
        BERMUDA,
        FOREIGN_PROPERTY,
        GOODY_HUT_FREE_GOLD,
        GOODY_HUT_FREE_STRENGTH,
        GOODY_HUT_FREE_GOLD_OR_STRENGTH,
        GOODY_HUT_HAPPY_BIRTHDAY,
        GOODY_HUT_WONDER_DISCOUNT,
        GOODY_HUT_DICE_BUFF,
        GOODY_HUT_JACKPOT,
        BARBARIANS_PAY_GOLD_OR_STRENGTH,
        BARBARIANS_PAY_GOLD_OR_HIRE,
        BARBARIANS_PAY_STRENGTH,
        BARBARIANS_ATTACK_NEIGHBOR,
        BARBARIANS_RAID, // pillage or fight
        BARBARIANS_PILLAGE, // 100% pillage
        BARBARIANS_RAGNAROK,
    }
}
