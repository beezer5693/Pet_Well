package org.brandon.petwellbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.cache.CacheService;
import org.brandon.petwellbackend.employee.Employee;
import org.brandon.petwellbackend.employee.EmployeeRepository;
import org.brandon.petwellbackend.exceptions.ApplicationException;
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
    private final EmployeeRepository employeeRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final CacheService cacheService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) {
        if (!isMatchingEmployeeRequest(request)) {
            continueFilterChain(request, response, filterChain);
            return;
        }

        try {
            LOGGER.info("Started jwt request filtering...");

            String accessToken = extractTokenFromHeader(request);
            String email = extractUsernameFromToken(accessToken);
            Optional<Employee> foundEmployee = employeeRepository.findByEmail(email);

            if (foundEmployee.isEmpty()) {
                LOGGER.warn("Employee {} not found", email);
                throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Not authorized");
            }
            if (isSecurityContextHolderNull()) {
                authenticateRequest(accessToken, foundEmployee.get(), request);
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

    private boolean isTokenValid(String accessToken, UserDetails userDetails, Employee employee) {
        return jwtService.isTokenValid(accessToken, userDetails) && !isTokenBlacklisted(employee, accessToken);
    }

    private UserDetails retrieveUserDetailsByEmail(String email) {
        return userDetailsService.loadUserByUsername(email);
    }

    private boolean isMatchingEmployeeRequest(HttpServletRequest request) {
        final RequestMatcher employeeRequestMatcher = new AntPathRequestMatcher("/api/v1/employees/**");
        return employeeRequestMatcher.matches(request);
    }

    private boolean isTokenBlacklisted(Employee employee, String accessToken) {
        String cachedToken = cacheService.getCachedToken(employee);
        boolean isTokenOnBlacklist = cachedToken != null && cachedToken.equals(accessToken);

        if (isTokenOnBlacklist) {
            LOGGER.warn("Access Token validation failed - token blacklisted");
        } else {
            LOGGER.info("Access token validation successful - token not blacklisted");
        }

        return isTokenOnBlacklist;
    }

    private void authenticateRequest(String accessToken, Employee employee, HttpServletRequest request) {
        UserDetails userDetails = retrieveUserDetailsByEmail(employee.getEmail());

        if (!isTokenValid(accessToken, userDetails, employee)) {
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

