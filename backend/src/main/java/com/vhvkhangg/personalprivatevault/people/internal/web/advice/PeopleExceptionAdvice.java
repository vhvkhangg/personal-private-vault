package com.vhvkhangg.personalprivatevault.people.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.people.group.exception.CreatorGroupNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.people.group.exception.CreatorGroupNotFoundException;
import com.vhvkhangg.personalprivatevault.people.group.exception.InvalidCreatorGroupException;
import com.vhvkhangg.personalprivatevault.people.person.exception.InvalidPersonException;
import com.vhvkhangg.personalprivatevault.people.person.exception.PersonNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.people.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PeopleExceptionAdvice {

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePersonNotFound(PersonNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "PERSON_NOT_FOUND", "Person not found");
    }

    @ExceptionHandler(InvalidPersonException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPerson(InvalidPersonException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "PERSON_INVALID", "Invalid person data");
    }

    @ExceptionHandler(CreatorGroupNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCreatorGroupNotFound(CreatorGroupNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "CREATOR_GROUP_NOT_FOUND", "Creator group not found");
    }

    @ExceptionHandler(CreatorGroupNameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleCreatorGroupNameExists(CreatorGroupNameAlreadyExistsException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "CREATOR_GROUP_NAME_EXISTS", "Creator group name already exists");
    }

    @ExceptionHandler(InvalidCreatorGroupException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCreatorGroup(InvalidCreatorGroupException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "CREATOR_GROUP_INVALID", "Invalid creator group data");
    }
}
