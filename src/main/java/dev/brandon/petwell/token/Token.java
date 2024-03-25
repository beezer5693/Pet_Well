package dev.brandon.petwell.token;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import static dev.brandon.petwell.token.TokenType.BEARER;

public class Token {

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType = BEARER;

    private boolean isRevoked;
    private boolean isExpired;

    public Token() {
    }

    public Token(String token, TokenType tokenType, boolean isRevoked, boolean isExpired) {
        this.token = token;
        this.tokenType = tokenType;
        this.isRevoked = isRevoked;
        this.isExpired = isExpired;
    }

    public String getToken() {
        return token;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public boolean isExpired() {
        return isExpired;
    }

    @Override
    public String toString() {
        return "Token{" +
                "token='" + token + '\'' +
                ", tokenType=" + tokenType +
                ", isRevoked=" + isRevoked +
                ", isExpired=" + isExpired +
                '}';
    }
}
