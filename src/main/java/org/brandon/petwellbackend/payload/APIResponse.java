package org.brandon.petwellbackend.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class APIResponse<T> {

    private String message;
    private Integer status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> APIResponse<T> ok(T data) {
        return APIResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message(Result.SUCCESS.getValue())
                .data(data)
                .build();
    }

    public static <T> APIResponse<T> created(T data) {
        return APIResponse.<T>builder()
                .status(HttpStatus.CREATED.value())
                .message(Result.SUCCESS.getValue())
                .data(data)
                .build();
    }

    @Getter
    @RequiredArgsConstructor
    private enum Result {
        SUCCESS("Success"),
        ERROR("Error");

        private final String value;
    }
}
