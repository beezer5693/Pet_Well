package dev.brandon.petwell.employee;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record NewEmployeeRequest(
        @NotNull(message = "First name cannot be null")
        @NotEmpty(message = "First name cannot be empty")
        @NotBlank(message = "First name cannot be blank")
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z]*$", message = "First name must contain only letters")
        @JsonProperty("first_name")
        String firstName,
        @NotNull(message = "Last name cannot be null")
        @NotEmpty(message = "Last name cannot be empty")
        @NotBlank(message = "Last name cannot be blank")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z]*$", message = "Last name must contain only letters")
        @JsonProperty("last_name")
        String lastName,
        @NotNull(message = "Email cannot be null")
        @NotEmpty(message = "Email is required")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email address")
        String email,
        @NotNull(message = "Password cannot be null")
        @NotEmpty(message = "Password cannot be empty")
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,
        @NotNull(message = "Job title cannot be null")
        @NotEmpty(message = "Job title cannot be empty")
        @NotBlank(message = "Job title cannot be blank")
        @JsonProperty("job_title")
        String jobTitle,
        @NotNull(message = "Role cannot be null")
        @NotEmpty(message = "Role cannot be empty")
        @NotBlank(message = "Role cannot be blank")
        String role
) {
}
