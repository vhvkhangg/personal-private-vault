package com.vhvkhangg.personalprivatevault;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Framework-level API exception handler providing canonical {@link ApiResponse} envelopes
 * for transport, deserialization, validation, and unexpected runtime failures.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private void abortIfCommitted(jakarta.servlet.http.HttpServletResponse response, Exception ex) {
        if (response != null && response.isCommitted()) {
            log.warn("Response already committed; aborting transfer without writing error envelope: exception={}", ex.getClass().getSimpleName());
            throw new IllegalStateException("Committed response transfer aborted: " + ex.getClass().getSimpleName());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        List<ApiFieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ApiFieldError(err.getField(), err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid value"))
                .toList();
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fieldErrors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(
            HandlerMethodValidationException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        List<ApiFieldError> fieldErrors = ex.getAllErrors().stream()
                .map(err -> new ApiFieldError(
                        err instanceof org.springframework.validation.FieldError fe ? fe.getField() : "parameter",
                        err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid value"))
                .toList();
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        List<ApiFieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> new ApiFieldError(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Malformed or unreadable request payload");
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<Void>> handleRequestParameterErrors(
            Exception ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Invalid request parameters");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        return ApiResponses.of(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Requested resource not found");
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        return ApiResponses.of(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access is denied");
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchElement(
            java.util.NoSuchElementException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        return ApiResponses.of(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Requested resource not found");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        return ApiResponses.of(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "HTTP method not supported");
    }

    @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestPart(
            org.springframework.web.multipart.support.MissingServletRequestPartException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Required multipart part is missing: " + ex.getRequestPartName());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        return ApiResponses.of(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE", "Uploaded file exceeds maximum permitted size limit");
    }

    @ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(
            org.springframework.web.multipart.MultipartException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Failed to parse multipart request");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "Invalid argument provided");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception ex,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        abortIfCommitted(response, ex);
        log.error("Unexpected server failure: exception={}", ex.getClass().getName());
        return ApiResponses.of(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }
}
