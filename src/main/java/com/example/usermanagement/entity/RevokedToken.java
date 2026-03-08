package com.example.usermanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
        name = "revoked_tokens",
        uniqueConstraints = @UniqueConstraint(name = "uk_revoked_tokens_jti", columnNames = "jti"),
        indexes = {
                @Index(name = "idx_revoked_tokens_jti", columnList = "jti"),
                @Index(name = "idx_revoked_tokens_expires_at", columnList = "expiresAt")
        }
)
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String jti;

    @Column(nullable = false)
    private Instant expiresAt;

    public Long getId() {
        return id;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
