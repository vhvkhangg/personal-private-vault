package com.vhvkhangg.personalprivatevault.account.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountConflictException;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountNotFoundException;
import com.vhvkhangg.personalprivatevault.account.account.InvalidExternalAccountException;
import com.vhvkhangg.personalprivatevault.account.relationship.ExternalAccountRelationshipNotFoundException;
import com.vhvkhangg.personalprivatevault.account.relationship.InvalidExternalAccountRelationshipException;
import com.vhvkhangg.personalprivatevault.account.snapshot.FollowerSnapshotNotFoundException;
import com.vhvkhangg.personalprivatevault.account.snapshot.InvalidFollowerSnapshotException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.account.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccountExceptionAdvice {

    @ExceptionHandler(ExternalAccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ExternalAccountNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "EXTERNAL_ACCOUNT_NOT_FOUND", "External account not found");
    }

    @ExceptionHandler(ExternalAccountRelationshipNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRelNotFound(ExternalAccountRelationshipNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "RELATIONSHIP_NOT_FOUND", "External account relationship not found");
    }

    @ExceptionHandler(FollowerSnapshotNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSnapshotNotFound(FollowerSnapshotNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "SNAPSHOT_NOT_FOUND", "Follower snapshot not found");
    }

    @ExceptionHandler(ExternalAccountConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ExternalAccountConflictException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "EXTERNAL_ACCOUNT_CONFLICT", "External account conflict");
    }

    @ExceptionHandler(InvalidExternalAccountException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalid(InvalidExternalAccountException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_EXTERNAL_ACCOUNT", "Invalid external account data");
    }

    @ExceptionHandler(InvalidExternalAccountRelationshipException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRel(InvalidExternalAccountRelationshipException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_RELATIONSHIP", "Invalid relationship data");
    }

    @ExceptionHandler(InvalidFollowerSnapshotException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSnapshot(InvalidFollowerSnapshotException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_SNAPSHOT", "Invalid follower snapshot data");
    }
}
