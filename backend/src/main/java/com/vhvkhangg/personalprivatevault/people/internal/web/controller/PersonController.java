package com.vhvkhangg.personalprivatevault.people.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.AddRoleRequest;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.CreatePersonRequest;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.PersonResponse;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.UpdatePersonRequest;
import com.vhvkhangg.personalprivatevault.people.internal.web.mapper.PeopleWebMapper;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/people")
@RequiredArgsConstructor
@Tag(name = "People", description = "Person profile management and role assignments")
public class PersonController {

    private final PersonOperations personOperations;

    @PostMapping
    @Operation(summary = "Create person profile", operationId = "createPerson")
    public ResponseEntity<ApiResponse<PersonResponse>> createPerson(@Valid @RequestBody CreatePersonRequest request) {
        PersonView created = personOperations.create(PeopleWebMapper.toCommand(request));
        return ApiResponses.created(PeopleWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get person profile by ID", operationId = "getPerson")
    public ResponseEntity<ApiResponse<PersonResponse>> getPerson(@PathVariable Long id) {
        return personOperations.find(id)
                .map(view -> ApiResponses.ok(PeopleWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "PERSON_NOT_FOUND", "Person not found"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update person profile", operationId = "updatePerson")
    public ResponseEntity<ApiResponse<PersonResponse>> updatePerson(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePersonRequest request) {
        PersonView updated = personOperations.update(PeopleWebMapper.toCommand(id, request));
        return ApiResponses.ok(PeopleWebMapper.toResponse(updated));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Assign role to person", operationId = "addPersonRole")
    public ResponseEntity<ApiResponse<Void>> addRole(
            @PathVariable Long id,
            @Valid @RequestBody AddRoleRequest request) {
        personOperations.addRole(id, request.role());
        return ApiResponses.ok(null);
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "Get assigned roles for person", operationId = "getPersonRoles")
    public ResponseEntity<ApiResponse<Set<PersonRole>>> getRoles(@PathVariable Long id) {
        Set<PersonRole> roles = personOperations.getRoles(id);
        return ApiResponses.ok(roles);
    }
}
