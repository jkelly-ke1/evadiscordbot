package io.jkelly.evadiscordbot.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "discord_user")
public class User implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "discord_id")
    private long discordId;

    @Column(name = "username")
    private String username;

    @Column(name = "penalty_point")
    private int penaltyPoint;

    @Column(name = "penalty_cooldown")
    private boolean onPenaltyCooldown;

    @Column(name = "roulette_cooldown")
    private boolean onRouletteCooldown;

    @Column(name = "punishment_amount")
    private int punishmentAmount;

    @Column(name = "barrel_capacity")
    private int barrelCapacity;
}
