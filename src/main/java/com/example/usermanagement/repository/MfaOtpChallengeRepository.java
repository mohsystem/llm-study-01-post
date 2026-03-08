package com.example.usermanagement.repository;

import com.example.usermanagement.entity.MfaOtpChallenge;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MfaOtpChallengeRepository extends JpaRepository<MfaOtpChallenge, String> {

    Optional<MfaOtpChallenge> findByIdAndUserId(String id, Long userId);

    void deleteByExpiresAtBefore(Instant cutoff);
}
