package org.brandon.petwellbackend.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EmployeeRegistrationRequest(
        @NotBlank(message = "First name is required")
        @JsonProperty("first_name")
        String firstname,
        @NotBlank(message = "Last name is required")
        @JsonProperty("last_name")
        String lastname,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,
        @NotBlank(message = "Job title is required")
        @JsonProperty("job_title")
        String jobTitle,
        @NotBlank(message = "Role is required")
        String role
) {
}
