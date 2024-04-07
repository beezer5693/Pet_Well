package org.brandon.petwellbackend.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuthResponse(@JsonProperty("access_token") String accessToken) {
}
