package com.vhvkhangg.personalprivatevault.rag.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.rag.internal.web.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RagExceptionAdvice {

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnavailableAuthority(org.springframework.dao.DataAccessException ex) {
        if (com.vhvkhangg.personalprivatevault.ApiExceptionHandler.isSourceBusy(ex)) {
            return ApiResponses.of(HttpStatus.CONFLICT, "RAG_SOURCE_BUSY", "Cancel indexing before editing this source");
        }
        return ApiResponses.of(HttpStatus.SERVICE_UNAVAILABLE,"RAG_SOURCE_UNVERIFIED","RAG authority unavailable");
    }

    @ExceptionHandler(com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException.class)
    public ResponseEntity<ApiResponse<Void>> handleSourceChanged(com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "SOURCE_CHANGED", "Review current source and consent state");
    }

    @ExceptionHandler(com.vhvkhangg.personalprivatevault.rag.api.RagJobBusyException.class)
    public ResponseEntity<ApiResponse<Void>> handleJobBusy(com.vhvkhangg.personalprivatevault.rag.api.RagJobBusyException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "RAG_JOB_BUSY", "An indexing attempt is still active or recovering");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoSuchElementException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "RAG_NOT_FOUND", "RAG resource unavailable");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "RAG_CONFLICT", "RAG operation denied");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "RAG_BAD_REQUEST", "Invalid RAG request");
    }
}
