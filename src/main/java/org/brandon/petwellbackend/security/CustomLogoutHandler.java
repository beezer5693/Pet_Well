package org.brandon.petwellbackend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.cache.CacheStore;
import org.brandon.petwellbackend.entity.UserEntity;
import org.brandon.petwellbackend.repository.UserEntityRepository;
import org.brandon.petwellbackend.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(CustomLogoutHandler.class);

    private final JwtService jwtService;
    private final UserEntityRepository userEntityRepository;
    private final CacheStore<String, String> tokenCache;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LOGGER.info("Started logout request...");
        String authHeader = extractAuthorizationHeader(request);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warn("Invalid or missing Authorization header");
            return;
        }
        String accessToken = extractTokenFromAuthHeader(authHeader);
        String email = getUserEmailFromToken(accessToken);
        UserEntity loggedInUserEntity = getUserByEmail(email);
        if (loggedInUserEntity == null) {
            LOGGER.warn("The username extracted from the token is not valid.");
            return;
        }
        blacklistToken(accessToken, loggedInUserEntity);
        clearSecurityContext();
    }

    private static String extractTokenFromAuthHeader(String authHeader) {
        return authHeader.substring(7);
    }

    private void blacklistToken(String accessToken, UserEntity userEntity) {
        tokenCache.put("token_" + userEntity.getEmail(), accessToken);
    }

    private UserEntity getUserByEmail(String email) {
        return userEntityRepository.findByEmail(email).orElse(null);
    }

    private static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private String getUserEmailFromToken(String accessToken) {
        LOGGER.info("Extracting user email from token");
        return jwtService.extractUsername(accessToken);
    }

    private static String extractAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }
}
