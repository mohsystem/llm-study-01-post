package com.example.usermanagement.service;

import com.example.usermanagement.dto.ApiKeyCreateRequest;
import com.example.usermanagement.dto.ApiKeyCreateResponse;
import com.example.usermanagement.dto.ApiKeyResponse;
import com.example.usermanagement.dto.StatusResponse;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;

public interface ApiKeyService {

    ApiKeyCreateResponse createKey(Jwt jwt, ApiKeyCreateRequest request);

    List<ApiKeyResponse> listKeys(Jwt jwt);

    StatusResponse revokeKey(Jwt jwt, String keyId);
}
