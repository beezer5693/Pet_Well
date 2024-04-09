package org.brandon.petwellbackend.security;

import io.github.bucket4j.Bucket;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Component
public class RateLimitBucket {
    private static final int TOKEN_CAPACITY = 10;
    private static final int REFILL_TOKENS = 10;

    private final Bucket bucket;

    public RateLimitBucket() {
        bucket = Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(TOKEN_CAPACITY)
                        .refillIntervally(REFILL_TOKENS,
                                Duration.ofMinutes(1L))
                )
                .build();
    }
}
