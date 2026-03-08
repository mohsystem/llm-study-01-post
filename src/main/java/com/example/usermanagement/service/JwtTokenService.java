package com.example.usermanagement.service;

import com.example.usermanagement.config.JwtProperties;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService implements TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final MacAlgorithm macAlgorithm;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties, MacAlgorithm macAlgorithm) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.macAlgorithm = macAlgorithm;
    }

    @Override
    public String generateToken(Long userId, String subject) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.ttlMinutes() * 60);

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(subject)
                .claim("uid", userId)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(macAlgorithm).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }
}
