package com.example.usermanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_keys", indexes = {
        @Index(name = "idx_api_keys_owner_user", columnList = "ownerUserId"),
        @Index(name = "idx_api_keys_issuer", columnList = "issuerUserId"),
        @Index(name = "idx_api_keys_status", columnList = "status")
})
public class ApiKey {

    public enum Status {
        ACTIVE,
        REVOKED,
        EXPIRED
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private Long issuerUserId;

    private Long ownerUserId;

    @Column(length = 64)
    private String serviceAccountRef;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Column(nullable = false, length = 16)
    private String keyPrefix;

    @Column(nullable = false, length = 64, unique = true)
    private String keyHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public Long getIssuerUserId() { return issuerUserId; }
    public void setIssuerUserId(Long issuerUserId) { this.issuerUserId = issuerUserId; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getServiceAccountRef() { return serviceAccountRef; }
    public void setServiceAccountRef(String serviceAccountRef) { this.serviceAccountRef = serviceAccountRef; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}
