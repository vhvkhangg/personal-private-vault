package com.vhvkhangg.personalprivatevault.importdata.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.ImportJobNotFoundException;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportTransitionException;
import com.vhvkhangg.personalprivatevault.importdata.view.InvalidImportJsonException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.importdata.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ImportDataExceptionAdvice {

    @ExceptionHandler(ImportJobNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ImportJobNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "IMPORT_JOB_NOT_FOUND", "Import job not found");
    }

    @ExceptionHandler(InvalidImportJobException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidJob(InvalidImportJobException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_IMPORT_JOB", "Invalid import job data");
    }

    @ExceptionHandler(InvalidImportTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTransition(InvalidImportTransitionException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_IMPORT_TRANSITION", "Invalid import job transition");
    }

    @ExceptionHandler(InvalidImportJsonException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidJson(InvalidImportJsonException ex) {
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "INVALID_IMPORT_JSON", "Invalid import JSON data");
    }

    @ExceptionHandler(com.vhvkhangg.personalprivatevault.knowledge.api.InvalidKnowledgeItemException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidKnowledgeItem(com.vhvkhangg.personalprivatevault.knowledge.api.InvalidKnowledgeItemException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_KNOWLEDGE_ITEM", "Invalid knowledge item data");
    }

    @ExceptionHandler(com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleKnowledgeConflict(com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeConflictException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "KNOWLEDGE_CONFLICT", "Knowledge item conflict");
    }

    @ExceptionHandler(com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleKnowledgeNotFound(com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "KNOWLEDGE_NOT_FOUND", "Knowledge item not found");
    }
}
