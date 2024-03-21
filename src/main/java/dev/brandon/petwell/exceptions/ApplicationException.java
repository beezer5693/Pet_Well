package dev.brandon.petwell.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
public class ApplicationException extends RuntimeException {

    private HttpStatus httpStatus;
    private String instance;
    private List<String> errors;
    private Object data;

    public ApplicationException(String message) {
        this(HttpStatus.BAD_REQUEST, message);
    }

    public ApplicationException(HttpStatus httpStatus, String message) {
        this(httpStatus, message, Collections.singletonList(message), null);
    }

    public ApplicationException(HttpStatus httpStatus, String message, Object data) {
        this(httpStatus, message, Collections.singletonList(message), data);
    }

    public ApplicationException(HttpStatus httpStatus, String message, List<String> errors, Object data) {
        super(message);
        this.httpStatus = httpStatus;
        this.errors = errors;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationException that = (ApplicationException) o;
        return httpStatus == that.httpStatus && Objects.equals(errors, that.errors) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpStatus, errors, data);
    }
}
