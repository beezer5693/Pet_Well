package org.brandon.petwellbackend.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@ToString
public class ApplicationException extends RuntimeException {
    private HttpStatus httpStatus;
    private Object data;
    private Throwable cause;

    public ApplicationException(HttpStatus httpStatus, String message) {
        this(httpStatus, message, null, null);
    }

    public ApplicationException(HttpStatus httpStatus, String message, Object data) {
        this(httpStatus, message, data, null);
    }

    public ApplicationException(HttpStatus httpStatus, String message, Object data, Throwable cause) {
        super(message);
        this.httpStatus = httpStatus;
        this.data = data;
        this.cause = cause;
    }
}
