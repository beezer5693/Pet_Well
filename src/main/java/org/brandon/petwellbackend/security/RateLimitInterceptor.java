package org.brandon.petwellbackend.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.exceptions.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitBucket rateLimitBucket;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        Bucket bucket = rateLimitBucket.getBucket();

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            LOGGER.warn("Too many requests, tokens left: {}", bucket.getAvailableTokens());
            throw new RateLimitExceededException("Rate limit exceeded.");
        }
    }
}
