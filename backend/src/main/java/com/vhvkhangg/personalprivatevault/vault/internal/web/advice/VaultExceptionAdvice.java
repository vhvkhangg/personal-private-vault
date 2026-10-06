package com.vhvkhangg.personalprivatevault.vault.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.vault.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class VaultExceptionAdvice {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "VAULT_CONFLICT", "Vault entry state does not allow this operation");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "VAULT_DATA_CONFLICT", "Vault metadata conflict");
    }
}
