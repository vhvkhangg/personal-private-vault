package com.vhvkhangg.personalprivatevault.feed.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.feed.conversion.exception.InvalidSavedResourceConversionException;
import com.vhvkhangg.personalprivatevault.feed.item.exception.FeedItemConflictException;
import com.vhvkhangg.personalprivatevault.feed.item.exception.FeedItemNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.item.exception.InvalidFeedItemException;
import com.vhvkhangg.personalprivatevault.feed.resource.exception.InvalidSavedResourceException;
import com.vhvkhangg.personalprivatevault.feed.resource.exception.SavedResourceConflictException;
import com.vhvkhangg.personalprivatevault.feed.resource.exception.SavedResourceNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedSourceNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.InvalidFeedSourceException;
import com.vhvkhangg.personalprivatevault.feed.view.InvalidFeedJsonException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.feed.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FeedExceptionAdvice {

    @ExceptionHandler(FeedSourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSourceNotFound(FeedSourceNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "FEED_SOURCE_NOT_FOUND", "Feed source not found");
    }

    @ExceptionHandler(InvalidFeedSourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSource(InvalidFeedSourceException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_FEED_SOURCE", "Invalid feed source data");
    }

    @ExceptionHandler(FeedItemNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleItemNotFound(FeedItemNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "FEED_ITEM_NOT_FOUND", "Feed item not found");
    }

    @ExceptionHandler(FeedItemConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleItemConflict(FeedItemConflictException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "FEED_ITEM_CONFLICT", "Feed item conflict");
    }

    @ExceptionHandler(InvalidFeedItemException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidItem(InvalidFeedItemException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_FEED_ITEM", "Invalid feed item data");
    }

    @ExceptionHandler(SavedResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(SavedResourceNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "SAVED_RESOURCE_NOT_FOUND", "Saved resource not found");
    }

    @ExceptionHandler(SavedResourceConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceConflict(SavedResourceConflictException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "SAVED_RESOURCE_CONFLICT", "Saved resource conflict");
    }

    @ExceptionHandler(InvalidSavedResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidResource(InvalidSavedResourceException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_SAVED_RESOURCE", "Invalid saved resource data");
    }

    @ExceptionHandler(InvalidSavedResourceConversionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidConversion(InvalidSavedResourceConversionException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_SAVED_RESOURCE_CONVERSION", "Invalid saved resource conversion data");
    }

    @ExceptionHandler(InvalidFeedJsonException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidJson(InvalidFeedJsonException ex) {
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "INVALID_FEED_JSON", "Invalid feed JSON data");
    }
}
