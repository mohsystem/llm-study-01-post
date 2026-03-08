package com.example.usermanagement.repository;

import com.example.usermanagement.entity.ApiKey;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {

    List<ApiKey> findByIssuerUserIdOrOwnerUserIdOrderByCreatedAtDesc(Long issuerUserId, Long ownerUserId);

    @Query("select k from ApiKey k where k.id = :id and (k.issuerUserId = :uid or k.ownerUserId = :uid)")
    Optional<ApiKey> findManagedKeyByIdAndUserId(@Param("id") String id, @Param("uid") Long uid);

    List<ApiKey> findByStatusAndExpiresAtBefore(ApiKey.Status status, Instant cutoff);
}
