package com.example.usermanagement.config;

import com.example.usermanagement.repository.RevokedTokenRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class RevokedTokenJwtValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error TOKEN_REVOKED = new OAuth2Error("invalid_token", "Token is revoked", null);

    private final RevokedTokenRepository revokedTokenRepository;

    public RevokedTokenJwtValidator(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String jti = token.getId();
        if (jti == null || jti.isBlank()) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token missing jti", null));
        }

        if (revokedTokenRepository.existsByJti(jti)) {
            return OAuth2TokenValidatorResult.failure(TOKEN_REVOKED);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
