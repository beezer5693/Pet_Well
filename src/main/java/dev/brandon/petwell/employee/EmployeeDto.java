package dev.brandon.petwell.employee;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EmployeeDto(
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z]*$", message = "First name must contain only letters")
        @JsonProperty("first_name")
        String firstName,
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z]*$", message = "Last name must contain only letters")
        @JsonProperty("last_name")
        String lastName,
        @Email(message = "Invalid email")
        String email,
        @JsonProperty("job_title")
        String jobTitle,
        String role
) {
}
