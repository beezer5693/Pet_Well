package org.brandon.petwellbackend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.cache.CacheService;
import org.brandon.petwellbackend.employee.Employee;
import org.brandon.petwellbackend.employee.EmployeeRepository;
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
    private final EmployeeRepository employeeRepository;
    private final CacheService cacheService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LOGGER.info("Started logout request...");

        String authHeader = extractAuthorizationHeader(request);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warn("Invalid or missing Authorization header");
            return;
        }

        String accessToken = extractTokenFromAuthHeader(authHeader);
        String email = extractEmployeeEmailFromToken(accessToken);

        Employee loggedInEmployee = getEmployeeByEmail(email);

        if (loggedInEmployee == null) {
            LOGGER.warn("The username extracted from the token is not valid.");
            return;
        }

        blacklistToken(accessToken, loggedInEmployee);
        clearSecurityContext();
    }

    private static String extractTokenFromAuthHeader(String authHeader) {
        return authHeader.substring(7);
    }

    private void blacklistToken(String accessToken, Employee employee) {
        cacheService.cacheToken(accessToken, employee);
    }

    private Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email).orElse(null);
    }

    private static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private String extractEmployeeEmailFromToken(String accessToken) {
        LOGGER.info("Extracting employee email from token");
        return jwtService.extractUsername(accessToken);
    }

    private static String extractAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }
}
