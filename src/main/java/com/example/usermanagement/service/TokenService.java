package com.example.usermanagement.service;

public interface TokenService {
    String generateToken(Long userId, String subject);
}
