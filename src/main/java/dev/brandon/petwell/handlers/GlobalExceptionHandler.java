package dev.brandon.petwell.handlers;

import dev.brandon.petwell.exceptions.ApplicationException;
import dev.brandon.petwell.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Object> handleApplicationException(HttpServletRequest req, ApplicationException ex) {
        log.error(ex.getMessage(), ex);

        ApiResponse<String> response = ApiResponse.failedResponse(ex.getHttpStatus().value(), ex.getMessage());

        return ResponseEntity.status(ex.getHttpStatus()).body(generateErrorResponse(req, response));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<Object> handleValidationException(HttpServletRequest req, MethodArgumentNotValidException ex) {
        log.error("Validation failed", ex);

        Map<String, Object> errorMap = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiResponse<Map<String, Object>> response = ApiResponse.failedResponse(ex.getStatusCode().value(), "Failed To Validate Request", null, errorMap);

        return ResponseEntity.status(ex.getStatusCode()).body(generateErrorResponse(req, response));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleAllException(HttpServletRequest req, Exception ex) {
        log.error(ex.getMessage(), ex);

        if (ex.getCause() instanceof UnknownHostException) {
            ApiResponse<String> error = ApiResponse.failedResponse(HttpStatus.NOT_FOUND.value(), ex.getLocalizedMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        ApiResponse<String> response = ApiResponse.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getLocalizedMessage());

        return new ResponseEntity<>(generateErrorResponse(req, response), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static Map<String, Object> generateErrorResponse(HttpServletRequest req, Object data) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("path", req.getServletPath());
        errorMap.put("method", req.getMethod());
        errorMap.put("timestamp", Instant.now());
        errorMap.put("response", data);
        return errorMap;
    }
}
