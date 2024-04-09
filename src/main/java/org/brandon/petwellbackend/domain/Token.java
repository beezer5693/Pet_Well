package org.brandon.petwellbackend.domain;

import lombok.Builder;

@Builder
public record Token(String accessToken) {
}
