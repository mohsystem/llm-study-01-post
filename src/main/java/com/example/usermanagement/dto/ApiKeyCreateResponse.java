package com.example.usermanagement.dto;

public record ApiKeyCreateResponse(String keyId, String apiKey, String status, boolean accepted) {
}
