package com.vhvkhangg.personalprivatevault.drive.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = DriveController.class)
@ConditionalOnProperty(prefix = "ppv.drive", name = "enabled", havingValue = "true")
public class DriveControllerAdvice {

    @ExceptionHandler(DriveException.class)
    public ResponseEntity<ApiResponse<Void>> handleDriveException(DriveException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponses.error(ex.getErrorCode(), ex.getMessage()));
    }
}
