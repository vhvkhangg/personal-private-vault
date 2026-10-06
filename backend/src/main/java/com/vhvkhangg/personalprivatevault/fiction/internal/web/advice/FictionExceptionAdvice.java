package com.vhvkhangg.personalprivatevault.fiction.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.FictionNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.InvalidFictionException;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.InvalidFictionGenreException;
import com.vhvkhangg.personalprivatevault.fiction.link.exception.FictionLinkNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.link.exception.InvalidFictionLinkException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.fiction.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FictionExceptionAdvice {

    @ExceptionHandler(FictionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleFictionNotFound(FictionNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "FICTION_NOT_FOUND", "Fiction not found");
    }

    @ExceptionHandler(InvalidFictionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFiction(InvalidFictionException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "FICTION_INVALID", "Invalid fiction data");
    }

    @ExceptionHandler(FictionGenreNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleGenreNotFound(FictionGenreNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "FICTION_GENRE_NOT_FOUND", "Fiction genre not found");
    }

    @ExceptionHandler(FictionGenreNameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleGenreNameExists(FictionGenreNameAlreadyExistsException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "FICTION_GENRE_NAME_EXISTS", "Fiction genre name already exists");
    }

    @ExceptionHandler(InvalidFictionGenreException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidGenre(InvalidFictionGenreException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "FICTION_GENRE_INVALID", "Invalid fiction genre data");
    }

    @ExceptionHandler(FictionLinkNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleLinkNotFound(FictionLinkNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "FICTION_LINK_NOT_FOUND", "Fiction link not found");
    }

    @ExceptionHandler(InvalidFictionLinkException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidLink(InvalidFictionLinkException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "FICTION_LINK_INVALID", "Invalid fiction link data");
    }
}
