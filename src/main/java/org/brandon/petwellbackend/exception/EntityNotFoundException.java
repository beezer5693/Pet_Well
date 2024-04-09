package org.brandon.petwellbackend.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends RuntimeException {
    private final Long id;

    public EntityNotFoundException(String message, Long id) {
        super(message);
        this.id = id;
    }
}
