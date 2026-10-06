package com.vhvkhangg.personalprivatevault.film.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.film.credit.exception.InvalidFilmCreditException;
import com.vhvkhangg.personalprivatevault.film.film.exception.FilmNotFoundException;
import com.vhvkhangg.personalprivatevault.film.film.exception.InvalidFilmException;
import com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNotFoundException;
import com.vhvkhangg.personalprivatevault.film.genre.exception.InvalidFilmGenreException;
import com.vhvkhangg.personalprivatevault.film.link.exception.FilmLinkNotFoundException;
import com.vhvkhangg.personalprivatevault.film.link.exception.InvalidFilmLinkException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.film.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FilmExceptionAdvice {

    @ExceptionHandler(FilmNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleFilmNotFound(FilmNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "FILM_NOT_FOUND", "Film not found");
    }

    @ExceptionHandler(InvalidFilmException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFilm(InvalidFilmException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "FILM_INVALID", "Invalid film data");
    }

    @ExceptionHandler(InvalidFilmCreditException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFilmCredit(InvalidFilmCreditException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "FILM_CREDIT_INVALID", "Invalid film credit data");
    }

    @ExceptionHandler(FilmGenreNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleGenreNotFound(FilmGenreNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "FILM_GENRE_NOT_FOUND", "Film genre not found");
    }

    @ExceptionHandler(FilmGenreNameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleGenreNameExists(FilmGenreNameAlreadyExistsException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "FILM_GENRE_NAME_EXISTS", "Film genre name already exists");
    }

    @ExceptionHandler(InvalidFilmGenreException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidGenre(InvalidFilmGenreException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "FILM_GENRE_INVALID", "Invalid film genre data");
    }

    @ExceptionHandler(FilmLinkNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleLinkNotFound(FilmLinkNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "FILM_LINK_NOT_FOUND", "Film link not found");
    }

    @ExceptionHandler(InvalidFilmLinkException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidLink(InvalidFilmLinkException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "FILM_LINK_INVALID", "Invalid film link data");
    }
}
