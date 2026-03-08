package com.example.usermanagement.service;

import com.example.usermanagement.dto.ApiKeyCreateRequest;
import com.example.usermanagement.dto.StatusResponse;
import com.example.usermanagement.repository.ApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ApiKeyServiceImpl.class)
class ApiKeyServiceImplTest {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Test
    void createAndListAndRevokeApiKey() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("uid", 1L)
                .subject("user1")
                .build();

        var created = apiKeyService.createKey(jwt, new ApiKeyCreateRequest("key-one", "", 30));
        assertThat(created.accepted()).isTrue();
        assertThat(created.apiKey()).startsWith("ak_");

        var listed = apiKeyService.listKeys(jwt);
        assertThat(listed).hasSize(1);
        assertThat(listed.getFirst().keyId()).isEqualTo(created.keyId());

        StatusResponse revoked = apiKeyService.revokeKey(jwt, created.keyId());
        assertThat(revoked.accepted()).isTrue();
        assertThat(apiKeyRepository.findById(created.keyId()).orElseThrow().getStatus().name()).isEqualTo("REVOKED");
    }
}
