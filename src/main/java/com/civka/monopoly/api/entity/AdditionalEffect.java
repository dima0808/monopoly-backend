package com.civka.monopoly.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "additional_effects")
public class AdditionalEffect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Member member;

    private AdditionalEffectType type;

    private Integer turnsLeft;

    public enum AdditionalEffectType {
        COMMERCIAL_HUB_INVESTMENT_1,
        COMMERCIAL_HUB_INVESTMENT_2,
        COMMERCIAL_HUB_INVESTMENT_3,
        COMMERCIAL_HUB_INVESTMENT_4,
        GOODY_HUT_WONDER_DISCOUNT,
        WONDER_DISCOUNT_1,
        WONDER_DISCOUNT_2,
        WONDER_DISCOUNT_3,
        WONDER_DISCOUNT_4,
        ALLIANCE,
    }
}
