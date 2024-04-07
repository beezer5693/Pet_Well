package org.brandon.petwellbackend.exceptions;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.brandon.petwellbackend.util.DateTimeUtil.parseAndFormatDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleEntityAlreadyExistsException(HttpServletRequest req, EntityAlreadyExistsException e) {
        LOGGER.error(e.getMessage(), e);
        ProblemDetail problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problemDetails.setType(URI.create("http://localhost:8080/errors/employee-already-exists"));
        problemDetails.setInstance(URI.create(req.getServletPath()));
        problemDetails.setProperty("email", e.getEmail());
        problemDetails.setProperty("timestamp", parseAndFormatDateTime(LocalDateTime.now()));
        return problemDetails;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotFoundException(HttpServletRequest req, EntityNotFoundException e) {
        LOGGER.error(e.getMessage(), e);
        ProblemDetail problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problemDetails.setType(URI.create("http://localhost:8080/errors/employee-not-found"));
        problemDetails.setInstance(URI.create(req.getServletPath()));
        problemDetails.setProperty("id", e.getId());
        problemDetails.setProperty("timestamp", parseAndFormatDateTime(LocalDateTime.now()));
        return problemDetails;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handleMethodArgumentNotValidException(HttpServletRequest req, MethodArgumentNotValidException e) {
        LOGGER.error(e.getMessage(), e);
        ProblemDetail problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetails.setType(URI.create("http://localhost:8080/errors/method-argument-not-valid"));
        problemDetails.setInstance(URI.create(req.getServletPath()));
        problemDetails.setProperty("invalid_args", getFieldErrors(e));
        problemDetails.setProperty("timestamp", parseAndFormatDateTime(LocalDateTime.now()));
        return problemDetails;
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleJWTException(HttpServletRequest req, JwtException e) {
        LOGGER.error(e.getMessage(), e);
        ProblemDetail problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Not authorized");
        problemDetails.setType(URI.create("https://localhost:8080/errors/jwt"));
        problemDetails.setInstance(URI.create(req.getServletPath()));
        problemDetails.setProperty("timestamp", parseAndFormatDateTime(LocalDateTime.now()));
        return problemDetails;
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ProblemDetail handleRateLimitExceededException(HttpServletRequest req, RateLimitExceededException e) {
        LOGGER.error(e.getMessage(), e);
        ProblemDetail problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
        problemDetails.setType(URI.create("http://localhost:8080/errors/rate-limit-exceeded"));
        problemDetails.setInstance(URI.create(req.getServletPath()));
        problemDetails.setProperty("timestamp", parseAndFormatDateTime(LocalDateTime.now()));
        return problemDetails;
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ProblemDetail> handleApplicationException(HttpServletRequest req, ApplicationException e) {
        LOGGER.error(e.getMessage(), e);
        ProblemDetail problemDetails = ProblemDetail.forStatusAndDetail(e.getHttpStatus(), e.getMessage());
        problemDetails.setType(URI.create("http://localhost:8080/errors/application"));
        problemDetails.setInstance(URI.create(req.getServletPath()));
        problemDetails.setProperty("timestamp", parseAndFormatDateTime(LocalDateTime.now()));
        return new ResponseEntity<>(problemDetails, e.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(HttpServletRequest req, Exception e) {
        HttpStatus status;
        ProblemDetail problemDetails;

        switch (e) {
            case AccountStatusException _ -> {
                LOGGER.error(e.getMessage(), e);
                status = HttpStatus.UNAUTHORIZED;
                problemDetails = ProblemDetail.forStatusAndDetail(status, e.getMessage());
                problemDetails.setType(URI.create("http://localhost:8080/errors/account-status"));
                problemDetails.setInstance(URI.create(req.getServletPath()));
                problemDetails.setProperty("timestamp", parseAndFormatDateTime(LocalDateTime.now()));
            }
            case BadCredentialsException _ -> {
                LOGGER.error(e.getMessage(), e);
                status = HttpStatus.UNAUTHORIZED;
                problemDetails = ProblemDetail.forStatusAndDetail(status, "Invalid email or password");
                problemDetails.setType(URI.create("http://localhost:8080/errors/bad-credentials"));
                problemDetails.setInstance(URI.create(req.getServletPath()));
                problemDetails.setProperty("timestamp", parseAndFormatDateTime(LocalDateTime.now()));
            }
            case AccessDeniedException _ -> {
                LOGGER.error(e.getMessage(), e);
                status = HttpStatus.FORBIDDEN;
                problemDetails = ProblemDetail.forStatusAndDetail(status, e.getMessage());
                problemDetails.setType(URI.create("http://localhost:8080/errors/access-denied"));
                problemDetails.setInstance(URI.create(req.getServletPath()));
                problemDetails.setProperty("timestamp", parseAndFormatDateTime(LocalDateTime.now()));
            }
            default -> {
                LOGGER.error(e.getMessage(), e);
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                problemDetails = ProblemDetail.forStatusAndDetail(status, e.getMessage());
                problemDetails.setType(URI.create("http://localhost:8080/errors/generic"));
                problemDetails.setInstance(URI.create(req.getServletPath()));
                problemDetails.setProperty("timestamp", parseAndFormatDateTime(LocalDateTime.now()));
            }
        }

        return new ResponseEntity<>(problemDetails, status);
    }

    private static Map<String, Map<String, String>> getFieldErrors(MethodArgumentNotValidException e) {
        Map<String, Map<String, String>> fieldErrors = new HashMap<>();

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("reason", fieldError.getDefaultMessage());
            errorDetails.put("rejected_value", String.valueOf(fieldError.getRejectedValue()));
            fieldErrors.put(fieldError.getField(), errorDetails);
        }

        return fieldErrors;
    }
}
