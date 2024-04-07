package org.brandon.petwellbackend.cache;

import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.common.User;
import org.brandon.petwellbackend.exceptions.ApplicationException;
import org.brandon.petwellbackend.security.TokenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);
    private static final String KEY_PREFIX = "user_";

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenConfig tokenConfig;

    public String getCachedToken(User user) {
        LOGGER.debug("Retrieving cached token for user: {}", user.getEmail());

        String key = KEY_PREFIX.concat("" + user.getId());

        String token;

        try {
            token = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            LOGGER.error("An error occurred retrieving the cached token for user: {}", user.getEmail(), e);
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occured retrieving the token", e);
        }

        logIsTokenPresent(user, token);

        return token;
    }

    public void cacheToken(String accessToken, User user) {
        LOGGER.debug("Blacklisting token for user: {}", user.getEmail());

        String key = KEY_PREFIX.concat("" + user.getId());

        try {
            redisTemplate.opsForValue().set(key, accessToken, Duration.ofMillis(tokenConfig.getTokenExpiration()));
        } catch (Exception e) {
            LOGGER.error("An error occurred blacklisting the token for user: {}", user.getEmail(), e);
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occured blacklisting the token", e);
        }
    }

    private static void logIsTokenPresent(User user, String token) {
        if (isTokenPresent(token)) {
            LOGGER.warn("Token present for user: {}", user.getEmail());
        } else {
            LOGGER.info("No token present for user: {}", user.getEmail());
        }
    }

    private static boolean isTokenPresent(String token) {
        return token != null && !token.isEmpty();
    }
}
