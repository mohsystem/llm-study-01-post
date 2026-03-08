package com.example.usermanagement.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
        byte[] keyBytes = resolveSecret(jwtProperties.secretBase64());
        var secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
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
