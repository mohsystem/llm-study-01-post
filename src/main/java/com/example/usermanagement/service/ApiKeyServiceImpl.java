package com.example.usermanagement.service;

import com.example.usermanagement.dto.ApiKeyCreateRequest;
import com.example.usermanagement.dto.ApiKeyCreateResponse;
import com.example.usermanagement.dto.ApiKeyResponse;
import com.example.usermanagement.dto.StatusResponse;
import com.example.usermanagement.entity.ApiKey;
import com.example.usermanagement.repository.ApiKeyRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyServiceImpl(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    @Transactional
    public ApiKeyCreateResponse createKey(Jwt jwt, ApiKeyCreateRequest request) {
        Long uid = extractUid(jwt);
        String serviceAccountRef = normalizeServiceRef(request.serviceAccountRef());
        if (serviceAccountRef != null && !hasAdminScope(jwt)) {
            throw new AccessDeniedException("Insufficient privileges");
        }

        String rawSecret = generateRawSecret();
        ApiKey key = new ApiKey();
        key.setIssuerUserId(uid);
        key.setOwnerUserId(serviceAccountRef == null ? uid : null);
        key.setServiceAccountRef(serviceAccountRef);
        key.setDisplayName(request.displayName().trim());
        key.setKeyPrefix(rawSecret.substring(0, 12));
        key.setKeyHash(sha256(rawSecret));
        key.setStatus(ApiKey.Status.ACTIVE);
        key.setExpiresAt(Instant.now().plus(request.expiresInDays(), ChronoUnit.DAYS));

        ApiKey saved = apiKeyRepository.save(key);
        String exposedKey = "ak_" + saved.getId() + "." + rawSecret;
        return new ApiKeyCreateResponse(saved.getId(), exposedKey, "ACCEPTED", true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKeyResponse> listKeys(Jwt jwt) {
        Long uid = extractUid(jwt);
        Instant now = Instant.now();

        return apiKeyRepository.findByIssuerUserIdOrOwnerUserIdOrderByCreatedAtDesc(uid, uid).stream()
                .map(k -> {
                    String status = k.getStatus().name();
                    if (k.getStatus() == ApiKey.Status.ACTIVE && k.getExpiresAt().isBefore(now)) {
                        status = ApiKey.Status.EXPIRED.name();
                    }
                    String ownerType = k.getServiceAccountRef() == null ? "USER" : "SERVICE";
                    String ownerRef = k.getServiceAccountRef() == null ? String.valueOf(k.getOwnerUserId()) : k.getServiceAccountRef();
                    return new ApiKeyResponse(
                            k.getId(),
                            k.getDisplayName(),
                            ownerType,
                            ownerRef,
                            k.getKeyPrefix(),
                            status,
                            k.getCreatedAt(),
                            k.getExpiresAt()
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public StatusResponse revokeKey(Jwt jwt, String keyId) {
        Long uid = extractUid(jwt);
        ApiKey key = apiKeyRepository.findManagedKeyByIdAndUserId(keyId, uid)
                .orElseThrow(() -> new AccessDeniedException("Key not found"));

        key.setStatus(ApiKey.Status.REVOKED);
        key.setRevokedAt(Instant.now());
        apiKeyRepository.save(key);

        expireElapsedKeys();
        return new StatusResponse("ACCEPTED", true);
    }

    private void expireElapsedKeys() {
        Instant now = Instant.now();
        List<ApiKey> activeExpired = apiKeyRepository.findByStatusAndExpiresAtBefore(ApiKey.Status.ACTIVE, now);
        for (ApiKey key : activeExpired) {
            key.setStatus(ApiKey.Status.EXPIRED);
        }
        apiKeyRepository.saveAll(activeExpired);
    }

    private Long extractUid(Jwt jwt) {
        Long uid = jwt.getClaim("uid");
        if (uid == null) {
            throw new AccessDeniedException("Invalid principal");
        }
        return uid;
    }

    private boolean hasAdminScope(Jwt jwt) {
        Object scope = jwt.getClaims().get("scope");
        if (scope instanceof String s) {
            return s.contains("admin");
        }
        return false;
    }

    private String normalizeServiceRef(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    private String generateRawSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
