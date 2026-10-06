package com.vhvkhangg.personalprivatevault.media.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.media.album.AlbumNotFoundException;
import com.vhvkhangg.personalprivatevault.media.album.InvalidAlbumException;
import com.vhvkhangg.personalprivatevault.media.image.ImageConflictException;
import com.vhvkhangg.personalprivatevault.media.image.ImageNotFoundException;
import com.vhvkhangg.personalprivatevault.media.image.InvalidImageException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.media.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MediaExceptionAdvice {

    @ExceptionHandler(AlbumNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlbumNotFound(AlbumNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "ALBUM_NOT_FOUND", "Album not found");
    }

    @ExceptionHandler(InvalidAlbumException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidAlbum(InvalidAlbumException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "ALBUM_INVALID", "Invalid album data");
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageNotFound(ImageNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "IMAGE_NOT_FOUND", "Image not found");
    }

    @ExceptionHandler(ImageConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageConflict(ImageConflictException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "IMAGE_CONFLICT", "Image metadata conflicts with existing record");
    }

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidImage(InvalidImageException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "IMAGE_INVALID", "Invalid image data");
    }

    @ExceptionHandler(com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleStorageDisabled(com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageDisabledException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "STORAGE_DISABLED", "Managed image storage is not enabled");
    }

    @ExceptionHandler(com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageIntegrityException.class)
    public ResponseEntity<ApiResponse<Void>> handleStorageIntegrity(com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageIntegrityException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "STORAGE_INTEGRITY_ERROR", "Image binary is unavailable or missing in storage");
    }
}
