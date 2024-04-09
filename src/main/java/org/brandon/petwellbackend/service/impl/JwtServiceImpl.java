package org.brandon.petwellbackend.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.domain.Token;
import org.brandon.petwellbackend.security.JwtConfig;
import org.brandon.petwellbackend.service.JwtService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.apache.tomcat.util.http.SameSiteCookies.NONE;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl extends JwtConfig implements JwtService {

    private final Supplier<SecretKey> secretKey = () -> Keys.hmacShaKeyFor(Decoders.BASE64.decode(getSecretKey()));

    private final Function<String, Claims> extractAllClaims = token ->
            Jwts.parser()
                    .verifyWith(secretKey.get())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

    private final Supplier<JwtBuilder> builder = () ->
            Jwts.builder()
                    .id(UUID.randomUUID().toString())
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusSeconds(getTokenExpiration())))
                    .signWith(secretKey.get(), Jwts.SIG.HS256);

    private final Function<UserDetails, String> buildToken = user ->
            builder.get()
                    .subject(user.getUsername())
                    .claim("role", user.getAuthorities())
                    .compact();

    private final BiFunction<Map<String, Object>, UserDetails, String> buildTokenWithExtraClaims = (extraClaims, user) ->
            builder.get()
                    .subject(user.getUsername())
                    .claim("role", user.getAuthorities())
                    .claims(extraClaims)
                    .compact();

    private final BiConsumer<HttpServletResponse, UserDetails> createJWTCookie = (response, user) -> {
        Cookie cookie = new Cookie("access_token", generateJwtTokenForCookie(user, Token::accessToken));
        cookie.setHttpOnly(true);
        cookie.setMaxAge(2 * 60 * 60);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", NONE.name());
        response.addCookie(cookie);
    };

    @Override
    public void addCookie(HttpServletResponse response, UserDetails user) {
        createJWTCookie.accept(response, user);
    }

    @Override
    public String extractUsername(String token) {
        return getClaimsValue(token, Claims::getSubject);
    }

    @Override
    public Claims extractClaim(String token) {
        return extractAllClaims.apply(token);
    }

    @Override
    public String generateJwtTokenForCookie(UserDetails userDetails, Function<Token, String> tokenFunction) {
        Token accessToken = Token.builder().accessToken(buildToken.apply(userDetails)).build();
        return tokenFunction.apply(accessToken);
    }

    @Override
    public String generateJwtToken(UserDetails userDetails) {
        return buildToken.apply(userDetails);
    }

    @Override
    public String generateJwtTokenWithExtraClaims(UserDetails userDetails, Map<String, Object> claims) {
        return buildTokenWithExtraClaims.apply(claims, userDetails);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getClaimsValue(token, Claims::getSubject);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(Date.from(Instant.now()));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token).getExpiration();
    }

    private <T> T getClaimsValue(String token, Function<Claims, T> claimsExtractor) {
        return extractAllClaims.andThen(claimsExtractor).apply(token);
    }
}
