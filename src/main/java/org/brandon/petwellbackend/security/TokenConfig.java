package org.brandon.petwellbackend.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@Configuration
@ConfigurationProperties(prefix = "token")
public class TokenConfig {

    @NotNull
    @NotBlank
    private String secretKey;

    @NotNull
    private long tokenExpiration;
}
