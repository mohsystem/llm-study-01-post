package com.example.usermanagement.dto;

public record MfaVerifyResponse(String status, boolean authenticated) {
}
