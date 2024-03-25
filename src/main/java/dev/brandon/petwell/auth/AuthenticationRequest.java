package dev.brandon.petwell.auth;

import jakarta.validation.constraints.*;

public record AuthenticationRequest(
        @NotEmpty(message = "Email cannot be empty")
        @NotBlank(message = "Email cannot be blank")
        @NotNull(message = "Email cannot be null")
        @Email(message = "Invalid email")
        String email,
        @NotEmpty(message = "Password cannot be empty")
        @NotBlank(message = "Password cannot be blank")
        @NotNull(message = "Password cannot be null")
        @Size(min = 8, message = "Password must be at least 8 character long")
        String password
) {
}
