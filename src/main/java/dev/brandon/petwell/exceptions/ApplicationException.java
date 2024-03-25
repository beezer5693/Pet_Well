package dev.brandon.petwell.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

public class ApplicationException extends RuntimeException {

    private HttpStatus httpStatus;
    private String instance;
    private List<String> errors;
    private Object data;

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

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ApplicationException{" +
                "httpStatus=" + httpStatus +
                ", instance='" + instance + '\'' +
                ", errors=" + errors +
                ", data=" + data +
                '}';
    }
}
