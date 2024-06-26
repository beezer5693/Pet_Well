package org.brandon.petwellbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.cache.CacheStore;
import org.brandon.petwellbackend.entity.UserEntity;
import org.brandon.petwellbackend.exception.ApplicationException;
import org.brandon.petwellbackend.repository.UserEntityRepository;
import org.brandon.petwellbackend.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final UserDetailsService userDetailsService;
    private final UserEntityRepository userEntityRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final CacheStore<String, String> tokenCache;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) {
        LOGGER.info("Started jwt request filtering...");
        if (!isMatchingRequestUrl(request)) {
            continueFilterChain(request, response, filterChain);
            return;
        }
        try {
            String accessToken = extractTokenFromHeader(request);
            String email = extractUsernameFromToken(accessToken);
            Optional<UserEntity> foundUser = userEntityRepository.findByEmail(email);
            if (foundUser.isEmpty()) {
                LOGGER.warn("User {} not found", email);
                throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Not authorized");
            }
            if (isSecurityContextHolderNull()) {
                authenticateRequest(accessToken, foundUser.get(), request);
            }
            continueFilterChain(request, response, filterChain);
        } catch (Exception ex) {
            LOGGER.error("Exception while authenticating request: {}", ex.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private String extractUsernameFromToken(String accessToken) {
        return jwtService.extractUsername(accessToken);
    }

    private boolean isTokenValid(String accessToken, UserDetails userDetails, UserEntity userEntity) {
        return jwtService.isTokenValid(accessToken, userDetails) && !isTokenBlacklisted(userEntity, accessToken);
    }

    private UserDetails retrieveUserDetailsByEmail(String email) {
        return userDetailsService.loadUserByUsername(email);
    }

    private boolean isMatchingRequestUrl(HttpServletRequest request) {
        final RequestMatcher userRequestMatcher = new AntPathRequestMatcher("/api/v1/users/**");
        return userRequestMatcher.matches(request);
    }

    private boolean isTokenBlacklisted(UserEntity userEntity, String accessToken) {
        String cachedToken = tokenCache.get("token_" + userEntity.getEmail());
        boolean isTokenOnBlacklist = cachedToken != null && cachedToken.equals(accessToken);
        if (isTokenOnBlacklist) {
            LOGGER.warn("Access Token validation failed - token blacklisted");
        } else {
            LOGGER.info("Access token validation successful - token not blacklisted");
        }
        return isTokenOnBlacklist;
    }

    private void authenticateRequest(String accessToken, UserEntity userEntity, HttpServletRequest request) {
        UserDetails userDetails = retrieveUserDetailsByEmail(userEntity.getEmail());
        if (!isTokenValid(accessToken, userDetails, userEntity)) {
            LOGGER.warn("Failed to authenticate request");
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Not authorized");
        }
        setSecurityContextHolder(userDetails, request);
    }

    private static boolean isAuthHeaderMissingOrInvalid(String authHeader) {
        return authHeader == null || !authHeader.startsWith("Bearer ");
    }

    private static String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = getAuthorizationHeader(request);
        return authHeader.substring(7);
    }

    private static boolean isSecurityContextHolderNull() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    private static String getAuthorizationHeader(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (isAuthHeaderMissingOrInvalid(authHeader)) {
            LOGGER.warn("Invalid Authorization header: {}", authHeader);
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Not authorized");
        }
        return authHeader;
    }

    private static void setSecurityContextHolder(UserDetails userDetails, HttpServletRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private static void continueFilterChain(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        try {
            chain.doFilter(req, res);
        } catch (IOException | ServletException e) {
            LOGGER.error("Error proceeding with filter chain: {}", e.getMessage());
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}

