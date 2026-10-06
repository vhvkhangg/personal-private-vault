package com.vhvkhangg.personalprivatevault.settings.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.settings.configuration.InvalidSettingsException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.settings.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SettingsExceptionAdvice {

    @ExceptionHandler(InvalidSettingsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSettings(InvalidSettingsException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "SETTINGS_INVALID", "Invalid application settings");
    }
}
