package com.example.usermanagement.service;

public interface NotificationClient {
    void sendOtp(String destination, String otp);
}
