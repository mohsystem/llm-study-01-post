package com.example.usermanagement.service;

import com.example.usermanagement.config.AuthFlowProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestNotificationClient implements NotificationClient {

    private final RestClient restClient;
    private final AuthFlowProperties authFlowProperties;

    public RestNotificationClient(RestClient restClient, AuthFlowProperties authFlowProperties) {
        this.restClient = restClient;
        this.authFlowProperties = authFlowProperties;
    }

    @Override
    public void sendOtp(String destination, String otp) {
        restClient.post()
                .uri(authFlowProperties.notificationEndpoint())
                .body(new OtpPayload(destination, otp))
                .retrieve()
                .toBodilessEntity();
    }

    private record OtpPayload(String destination, String otp) {
    }
}
