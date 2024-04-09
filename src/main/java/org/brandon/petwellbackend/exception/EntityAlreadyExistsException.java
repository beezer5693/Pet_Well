package org.brandon.petwellbackend.exception;

import lombok.Getter;

@Getter
public class EntityAlreadyExistsException extends RuntimeException {
    private final String email;

    public EntityAlreadyExistsException(String message, String email) {
        super(message);
        this.email = email;
    }
}
