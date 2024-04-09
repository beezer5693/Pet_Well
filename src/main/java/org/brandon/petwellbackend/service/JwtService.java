package org.brandon.petwellbackend.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.brandon.petwellbackend.domain.Token;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

public interface JwtService {

    void addCookie(HttpServletResponse response, UserDetails user);

    String extractUsername(String token);

    Claims extractClaim(String token);

    String generateJwtTokenForCookie(UserDetails userDetails, Function<Token, String> tokenFunction);

    String generateJwtToken(UserDetails userDetails);

    String generateJwtTokenWithExtraClaims(UserDetails userDetails, Map<String, Object> claims);

    boolean isTokenValid(String token, UserDetails userDetails);
}
