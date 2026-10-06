package com.vhvkhangg.personalprivatevault.journal.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.journal.diary.exception.DiaryEntryNotFoundException;
import com.vhvkhangg.personalprivatevault.journal.diary.exception.InvalidDiaryEntryException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.journal.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JournalExceptionAdvice {

    @ExceptionHandler(DiaryEntryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(DiaryEntryNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "DIARY_ENTRY_NOT_FOUND", "Diary entry not found");
    }

    @ExceptionHandler(InvalidDiaryEntryException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalid(InvalidDiaryEntryException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_DIARY_ENTRY", "Invalid diary entry data");
    }
}
