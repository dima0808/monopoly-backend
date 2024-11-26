package com.civka.monopoly.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer size;

    private String password;

    @OneToMany(mappedBy = "room", fetch = FetchType.EAGER)
    @OrderBy("id")
    private List<Member> members = new ArrayList<>();

    private Boolean isStarted;

    private String currentTurn; // username of the member whose turn it is
    private String winner; // username of the member who won the game
    private VictoryType victoryType;

    private Integer turn;

    private Integer randomMemberIndex;

    @OneToMany(mappedBy = "room", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Property> properties = new ArrayList<>();

    public enum VictoryType {
        MILITARY,
        CULTURE,
        SCIENCE,
        SCORE
    }
}
