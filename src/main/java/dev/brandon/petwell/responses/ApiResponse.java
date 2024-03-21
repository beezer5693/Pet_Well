package dev.brandon.petwell.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;


@NoArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {

    private int statusCode;
    private String message;
    private boolean success;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    @JsonProperty("validation_errors")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T validationErrors;

    public ApiResponse(int statCode, String statusDesc) {
        statusCode = statCode;
        message = statusDesc;

        if (statusCode == HttpStatus.OK.value()) {
            success = true;
        }
    }

    public static <T> ApiResponse<T> failedResponse(String message) {
        return failedResponse(HttpStatus.BAD_REQUEST.value(), message, null, null);
    }

    public static <T> ApiResponse<T> failedResponse(T data) {
        return failedResponse(HttpStatus.BAD_REQUEST.value(), "Bad request", data, null);
    }

    public static <T> ApiResponse<T> failedResponse(int statusCode, String message) {
        return failedResponse(statusCode, message, null, null);
    }

    public static <T> ApiResponse<T> failedResponse(int statusCode, String message, T data, T validationErrors) {
        ApiResponse<T> response = new ApiResponse<>(statusCode, message);
        response.setSuccess(false);
        response.setData(data);
        response.setValidationErrors(validationErrors);
        return response;
    }

    public static <T> ApiResponse<T> successfulResponse(String message, T data) {
        return successfulResponse(HttpStatus.OK.value(), message, data);
    }

    public static <T> ApiResponse<T> successfulResponse(String message) {
        return successfulResponse(HttpStatus.OK.value(), message, null);
    }

    public static <T> ApiResponse<T> successfulResponse(int statusCode, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(statusCode, message);
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
}
