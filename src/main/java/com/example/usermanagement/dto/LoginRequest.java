package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Size(min = 3, max = 254)
        @Pattern(regexp = "^[^\\s]+$", message = "login identifier must not contain spaces")
        String usernameOrEmail,

        @NotBlank
        @Size(min = 12, max = 72)
        String password
) {
}
