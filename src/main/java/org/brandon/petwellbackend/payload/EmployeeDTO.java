package org.brandon.petwellbackend.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EmployeeDTO(
        @JsonProperty("user_id")
        String userId,
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z]*$", message = "First name must contain only letters")
        @JsonProperty("first_name")
        String firstname,
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z]*$", message = "Last name must contain only letters")
        @JsonProperty("last_name")
        String lastname,
        @Email(message = "Invalid email")
        String email,
        @JsonProperty("job_title")
        String jobTitle,
        String role,
        @JsonProperty("is_account_non_expired")
        boolean isAccountNonExpired,
        @JsonProperty("is_account_non_locked")
        boolean isAccountNonLocked,
        @JsonProperty("is_credentials_non_expired")
        boolean isCredentialsNonExpired,
        @JsonProperty("is_enabled")
        boolean isEnabled
) {
}
