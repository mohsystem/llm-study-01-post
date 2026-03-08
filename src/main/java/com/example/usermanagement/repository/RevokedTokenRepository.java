package com.example.usermanagement.repository;

import com.example.usermanagement.entity.RevokedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    boolean existsByJti(String jti);

    void deleteByExpiresAtBefore(Instant cutoff);
}
