package dev.brandon.petwell.handlers;

import dev.brandon.petwell.exceptions.ApplicationException;
import dev.brandon.petwell.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Object> handleApplicationException(HttpServletRequest req, ApplicationException ex) {
        LOGGER.error(ex.getMessage(), ex);

        ApiResponse<String> response = ApiResponse.failedResponse(ex.getHttpStatus().value(), ex.getMessage(), req.getServletPath(), req.getMethod());

        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Object> handleValidationException(HttpServletRequest req, MethodArgumentNotValidException ex) {
        LOGGER.error(ex.getMessage(), ex);

        Map<String, Object> errorMap = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiResponse<Map<String, Object>> response = ApiResponse.failedResponse(ex.getStatusCode().value(), "Failed To Validate Request", req.getServletPath(), req.getMethod(), null, errorMap);

        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Object> handleSecurityException(HttpServletRequest req, Exception ex) {
        switch (ex.getClass().getSimpleName()) {
            case "BadCredentialsException" -> {
                LOGGER.error("Username or password is incorrect", ex);
                ApiResponse<String> apiResponse = ApiResponse.failedResponse(HttpStatus.UNAUTHORIZED.value(), "Not Authorized", req.getServletPath(), req.getMethod());
                return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
            }
            case "AccountStatusException" -> {
                LOGGER.error("Account is locked", ex);
                ApiResponse<String> apiResponse = ApiResponse.failedResponse(HttpStatus.UNAUTHORIZED.value(), "Not Authorized", req.getServletPath(), req.getMethod());
                return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
            }
            case "AccessDeniedException" -> {
                LOGGER.error("Not authorized to access this resource", ex);
                ApiResponse<String> apiResponse = ApiResponse.failedResponse(HttpStatus.FORBIDDEN.value(), "Not Authorized", req.getServletPath(), req.getMethod());
                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
            case "SignatureException" -> {
                LOGGER.error("Invalid JWT signature", ex);
                ApiResponse<String> apiResponse = ApiResponse.failedResponse(HttpStatus.FORBIDDEN.value(), "Not Authorized", req.getServletPath(), req.getMethod());
                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
            case "ExpiredJwtException" -> {
                LOGGER.error("JWT token has expired", ex);
                ApiResponse<String> apiResponse = ApiResponse.failedResponse(HttpStatus.FORBIDDEN.value(), "Not Authorized", req.getServletPath(), req.getMethod());
                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
            case "MalformedJwtException" -> {
                LOGGER.error("Malformed JWT", ex);
                ApiResponse<String> apiResponse = ApiResponse.failedResponse(HttpStatus.FORBIDDEN.value(), "Not Authorized", req.getServletPath(), req.getMethod());
                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
            default -> {
                LOGGER.error(ex.getMessage(), ex);
                ApiResponse<String> apiResponse = ApiResponse.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unknown internal server error.", req.getServletPath(), req.getMethod());
                return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
