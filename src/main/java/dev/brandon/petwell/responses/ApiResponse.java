package dev.brandon.petwell.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    @JsonProperty("status_code")
    private int statusCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String path;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String method;
    private String message;
    private boolean success;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    @JsonProperty("validation_errors")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T validationErrors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String timestamp;

    public ApiResponse(int statusCode, String statusDesc) {
        this.statusCode = statusCode;
        this.message = statusDesc;

        if (statusCode == HttpStatus.OK.value()) {
            success = true;
        }
    }

    public static <T> ApiResponse<T> failedResponse(int statusCode, String message, String path, String method) {
        return failedResponse(statusCode, message, path, method, null, null);
    }

    public static <T> ApiResponse<T> failedResponse(int statusCode, String message, String path, String method, T data, T validationErrors) {
        ApiResponse<T> response = new ApiResponse<>(statusCode, message);
        response.setSuccess(false);
        response.setPath(path);
        response.setMethod(method);
        response.setData(data);
        response.setValidationErrors(validationErrors);
        response.setTimestamp(LocalDateTime.now().toString());
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

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(T validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "statusCode=" + statusCode +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", message='" + message + '\'' +
                ", success=" + success +
                ", data=" + data +
                ", validationErrors=" + validationErrors +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
