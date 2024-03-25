package dev.brandon.petwell.auth;

import dev.brandon.petwell.token.Token;

public record AuthenticationResponse(Token token) {
}
