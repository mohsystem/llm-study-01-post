package com.example.usermanagement.dto;

import java.time.Instant;

public record ApiKeyResponse(
        String keyId,
        String displayName,
        String ownerType,
        String ownerRef,
        String keyPrefix,
        String status,
        Instant createdAt,
        Instant expiresAt
) {
}
