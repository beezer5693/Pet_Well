package org.brandon.petwellbackend.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends RuntimeException {
    private final String id;

    public EntityNotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }
}
