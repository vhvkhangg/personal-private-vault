package com.vhvkhangg.personalprivatevault.knowledge.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.knowledge.api.InvalidKnowledgeItemException;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.knowledge.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class KnowledgeExceptionAdvice {

    @ExceptionHandler(KnowledgeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleKnowledgeNotFound(KnowledgeNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "KNOWLEDGE_NOT_FOUND", "Knowledge item not found");
    }

    @ExceptionHandler(KnowledgeConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleKnowledgeConflict(KnowledgeConflictException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "KNOWLEDGE_CONFLICT", "Knowledge item conflict");
    }

    @ExceptionHandler(InvalidKnowledgeItemException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidKnowledge(InvalidKnowledgeItemException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "KNOWLEDGE_INVALID", "Invalid knowledge item data");
    }
}
