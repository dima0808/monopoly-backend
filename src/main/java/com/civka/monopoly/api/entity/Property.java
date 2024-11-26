package com.civka.monopoly.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Room room;

    private List<Upgrade> upgrades;

    private Integer position;

    private Integer mortgage;

    private Integer roundOfLastChange;
    private Integer turnOfLastChange;

    public enum Upgrade {
        LEVEL_1,
        LEVEL_2,
        LEVEL_3,
        LEVEL_4,
        LEVEL_4_1,
        LEVEL_4_2,
        LEVEL_4_3,
        WONDER_TEMPLE_OF_ARTEMIS,
        WONDER_CASA_DE_CONTRATACION,
        WONDER_COLOSSEUM,
        WONDER_ETEMENANKI,
        WONDER_MAUSOLEUM_AT_HALICARNASSUS,
        WONDER_RUHR_VALLEY,
        WONDER_ESTADIO_DO_MARACANA,
        ADJACENCY_GOVERNMENT_PLAZA,
        ADJACENCY_IRON,
        ADJACENCY_FABRIC,
        ADJACENCY_SHIPYARD,
        ADJACENCY_REEF,
        ADJACENCY_WONDER,
        ADJACENCY_ENTERTAINMENT_COMPLEX,
        ADJACENCY_FARMS,
        ADJACENCY_AQUEDUCT,
        ADJACENCY_DAM,
    }
}
