package org.brandon.petwellbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.brandon.petwellbackend.entity.Employee;
import org.brandon.petwellbackend.exception.ApplicationException;
import org.brandon.petwellbackend.payload.EmployeeLoginRequest;
import org.brandon.petwellbackend.payload.Response;
import org.brandon.petwellbackend.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@Component
public class LoginFilter extends AbstractAuthenticationProcessingFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFilter.class);

    private final JwtService jwtService;

    public LoginFilter(AuthenticationManager authenticationManager, JwtService jwtService) {
        super(new AntPathRequestMatcher("/api/v1/auth/employees/login", HttpMethod.POST.name()), authenticationManager);
        this.jwtService = jwtService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            EmployeeLoginRequest employeeLoginRequest = new ObjectMapper()
                    .configure(AUTO_CLOSE_SOURCE, true)
                    .readValue(request.getInputStream(), EmployeeLoginRequest.class);
            return getAuthenticationManager()
                    .authenticate(unauthenticated(employeeLoginRequest.email(), employeeLoginRequest.password()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            handleFailureResponse(request, response);
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        Employee employee = (Employee) authentication.getPrincipal();
        handleSuccessResponse(response, employee);
    }

    private void handleSuccessResponse(HttpServletResponse response, Employee employee) {
        try {
            jwtService.addCookie(response, employee);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.OK.value());
            new ObjectMapper().writeValue(response.getOutputStream(), Response.success(null, HttpStatus.OK));
            response.getOutputStream().flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleFailureResponse(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            new ObjectMapper().writeValue(response.getOutputStream(),
                    Response.error(HttpStatus.UNAUTHORIZED, "Not authorized", null, request));
            response.getOutputStream().flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
