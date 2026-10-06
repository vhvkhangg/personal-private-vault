package com.vhvkhangg.personalprivatevault.people.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.CreateCreatorGroupRequest;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.CreatorGroupMemberResponse;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.CreatorGroupResponse;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.UpdateCreatorGroupRequest;
import com.vhvkhangg.personalprivatevault.people.internal.web.mapper.PeopleWebMapper;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupView;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/people/creator-groups")
@RequiredArgsConstructor
@Tag(name = "Creator Groups", description = "Creator group management and memberships")
public class CreatorGroupController {

    private final CreatorGroupOperations creatorGroupOperations;

    @PostMapping
    @Operation(summary = "Create creator group", operationId = "createCreatorGroup")
    public ResponseEntity<ApiResponse<CreatorGroupResponse>> createGroup(@Valid @RequestBody CreateCreatorGroupRequest request) {
        CreatorGroupView created = creatorGroupOperations.create(PeopleWebMapper.toCommand(request));
        return ApiResponses.created(PeopleWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get creator group by ID", operationId = "getCreatorGroup")
    public ResponseEntity<ApiResponse<CreatorGroupResponse>> getGroup(@PathVariable Long id) {
        return creatorGroupOperations.find(id)
                .map(view -> ApiResponses.ok(PeopleWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "CREATOR_GROUP_NOT_FOUND", "Creator group not found"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update creator group", operationId = "updateCreatorGroup")
    public ResponseEntity<ApiResponse<CreatorGroupResponse>> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCreatorGroupRequest request) {
        CreatorGroupView updated = creatorGroupOperations.update(PeopleWebMapper.toCommand(id, request));
        return ApiResponses.ok(PeopleWebMapper.toResponse(updated));
    }

    @PutMapping("/{id}/members/{personId}")
    @Operation(summary = "Add person to creator group", operationId = "addCreatorGroupMember")
    public ResponseEntity<ApiResponse<Void>> addMember(@PathVariable Long id, @PathVariable Long personId) {
        creatorGroupOperations.addMember(id, personId);
        return ApiResponses.ok(null);
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get members of creator group", operationId = "getCreatorGroupMembers")
    public ResponseEntity<ApiResponse<List<CreatorGroupMemberResponse>>> getMembers(@PathVariable Long id) {
        List<CreatorGroupMemberResponse> members = creatorGroupOperations.getMembers(id).stream()
                .map(PeopleWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(members);
    }
}
