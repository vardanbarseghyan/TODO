package com.vardan.todo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity{
    @Column(nullable = false, unique = true)
    private String token; // We will store the hashed value here

    @Column(name = "expires_at", nullable = false) // Matches Lead's "expires_at" column name
    private Instant expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)//A user can have multiple refresh tokens (login from multiple devices, browsers, mobile phones).
    @JoinColumn(name = "user_id", nullable = false) // FK -> users.id
    private User user;
}
