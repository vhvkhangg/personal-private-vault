package com.vhvkhangg.personalprivatevault.personal.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.personal.profile.exception.InvalidPersonalProfileException;
import com.vhvkhangg.personalprivatevault.personal.profile.exception.PersonalProfileConflictException;
import com.vhvkhangg.personalprivatevault.personal.profile.exception.PersonalProfileNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.personal.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PersonalExceptionAdvice {

    @ExceptionHandler(PersonalProfileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(PersonalProfileNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "PERSONAL_PROFILE_NOT_FOUND", "Personal profile not found");
    }

    @ExceptionHandler(PersonalProfileConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(PersonalProfileConflictException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "PERSONAL_PROFILE_CONFLICT", "Personal profile conflict");
    }

    @ExceptionHandler(InvalidPersonalProfileException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalid(InvalidPersonalProfileException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_PERSONAL_PROFILE", "Invalid personal profile data");
    }
}
