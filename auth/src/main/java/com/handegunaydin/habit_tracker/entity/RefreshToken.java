package com.handegunaydin.habit_tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Table(name = "refreshtokens")
@Entity
@Getter
@Setter
public class RefreshToken extends Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column
    private String tokenHashed;
    @Column
    private String email;
    @Column
    private Instant expiresAt;
    @Column
    private boolean revoked = false;
    @Column
    private UUID replacedByTokenId;

}
