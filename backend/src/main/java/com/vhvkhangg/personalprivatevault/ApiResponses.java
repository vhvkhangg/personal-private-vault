package com.vhvkhangg.personalprivatevault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

/**
 * Factory and utility methods for constructing unified {@link ApiResponse} envelopes.
 */
public final class ApiResponses {

    private ApiResponses() {}

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, ApiPageMeta page) {
        return new ApiResponse<>(data, null, page != null ? new ApiMeta(page) : null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(null, new ApiError(code, message, List.of()), null);
    }

    public static <T> ApiResponse<T> error(String code, String message, List<ApiFieldError> fieldErrors) {
        return new ApiResponse<>(null, new ApiError(code, message, fieldErrors), null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, ApiPageMeta page) {
        return ResponseEntity.ok(success(data, page));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, URI location) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.CREATED);
        if (location != null) {
            builder.location(location);
        }
        return builder.body(success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return created(data, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> of(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(error(code, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> of(HttpStatus status, String code, String message, List<ApiFieldError> fieldErrors) {
        return ResponseEntity.status(status).body(error(code, message, fieldErrors));
    }
}
