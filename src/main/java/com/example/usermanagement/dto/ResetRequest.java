package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetRequest(
        @NotBlank
        @Size(min = 3, max = 254)
        @Pattern(regexp = "^[^\\s]+$", message = "identifier must not contain spaces")
        String identifier
) {
}
