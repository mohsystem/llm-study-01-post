package com.example.usermanagement.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
        byte[] keyBytes = resolveSecret(jwtProperties.secretBase64());
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            JwtProperties jwtProperties,
            RevokedTokenJwtValidator revokedTokenJwtValidator
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefaultWithIssuer(jwtProperties.issuer());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, revokedTokenJwtValidator));
        return decoder;
    }

    @Bean
    public MacAlgorithm jwtMacAlgorithm() {
        return MacAlgorithm.HS256;
    }

    private byte[] resolveSecret(String configuredSecretBase64) {
        if (configuredSecretBase64 != null && !configuredSecretBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(configuredSecretBase64);
            if (decoded.length < 32) {
                throw new IllegalStateException("JWT secret must be at least 256 bits");
            }
            return decoded;
        }

        byte[] generated = new byte[32];
        new SecureRandom().nextBytes(generated);
        return generated;
    }
}
