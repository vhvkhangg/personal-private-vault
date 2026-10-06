package com.vhvkhangg.personalprivatevault.collection.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.api.InvalidCollectionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.collection.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CollectionExceptionAdvice {

    @ExceptionHandler(CollectionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(CollectionNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "COLLECTION_NOT_FOUND", "Collection item not found");
    }

    @ExceptionHandler(InvalidCollectionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalid(InvalidCollectionException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_COLLECTION", "Invalid collection data");
    }
}
