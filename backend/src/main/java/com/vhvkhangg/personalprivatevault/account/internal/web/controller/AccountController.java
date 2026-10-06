package com.vhvkhangg.personalprivatevault.account.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountOperations;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.CreateExternalAccountRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.CreateFollowerSnapshotRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.ExternalAccountRelationshipResponse;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.ExternalAccountResponse;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.FollowerSnapshotEntryResponse;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.FollowerSnapshotResponse;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.SetExternalAccountRelationshipRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.UpdateExternalAccountRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.mapper.AccountWebMapper;
import com.vhvkhangg.personalprivatevault.account.relationship.ExternalAccountRelationshipOperations;
import com.vhvkhangg.personalprivatevault.account.snapshot.FollowerSnapshotOperations;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountRelationshipView;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountView;
import com.vhvkhangg.personalprivatevault.account.view.FollowerSnapshotEntryView;
import com.vhvkhangg.personalprivatevault.account.view.FollowerSnapshotView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Account", description = "External account catalog, relationships, and follower snapshot history")
public class AccountController {

    private final ExternalAccountOperations accountOperations;
    private final ExternalAccountRelationshipOperations relationshipOperations;
    private final FollowerSnapshotOperations snapshotOperations;

    // --- Accounts ---

    @PostMapping
    @Operation(summary = "Create external account", operationId = "createExternalAccount")
    public ResponseEntity<ApiResponse<ExternalAccountResponse>> create(@Valid @RequestBody CreateExternalAccountRequest request) {
        ExternalAccountView created = accountOperations.create(AccountWebMapper.toCommand(request));
        return ApiResponses.created(AccountWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get external account by ID", operationId = "getExternalAccount")
    public ResponseEntity<ApiResponse<ExternalAccountResponse>> get(@PathVariable Long id) {
        return accountOperations.findById(id)
                .map(view -> ApiResponses.ok(AccountWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "EXTERNAL_ACCOUNT_NOT_FOUND", "External account not found"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update external account", operationId = "updateExternalAccount")
    public ResponseEntity<ApiResponse<ExternalAccountResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExternalAccountRequest request) {
        ExternalAccountView updated = accountOperations.update(id, AccountWebMapper.toCommand(request));
        return ApiResponses.ok(AccountWebMapper.toResponse(updated));
    }

    @GetMapping
    @Operation(summary = "Find accounts by platform", operationId = "findAccountsByPlatform")
    public ResponseEntity<ApiResponse<List<ExternalAccountResponse>>> findRecentByPlatform(
            @RequestParam Long platformId,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<ExternalAccountView> accounts = accountOperations.findRecentByPlatformId(platformId, limit);
        return ApiResponses.ok(accounts.stream().map(AccountWebMapper::toResponse).toList());
    }

    // --- Relationships ---

    @PutMapping("/{ownerAccountId}/relationships/{targetAccountId}")
    @Operation(summary = "Set relationship between owner and target account", operationId = "setAccountRelationship")
    public ResponseEntity<ApiResponse<ExternalAccountRelationshipResponse>> setRelationship(
            @PathVariable Long ownerAccountId,
            @PathVariable Long targetAccountId,
            @Valid @RequestBody SetExternalAccountRelationshipRequest request) {
        ExternalAccountRelationshipView view = relationshipOperations.setRelationship(
                AccountWebMapper.toCommand(ownerAccountId, targetAccountId, request));
        return ApiResponses.ok(AccountWebMapper.toResponse(view));
    }

    @GetMapping("/{ownerAccountId}/relationships/{targetAccountId}")
    @Operation(summary = "Get relationship between owner and target account", operationId = "getAccountRelationship")
    public ResponseEntity<ApiResponse<ExternalAccountRelationshipResponse>> getRelationship(
            @PathVariable Long ownerAccountId,
            @PathVariable Long targetAccountId) {
        return relationshipOperations.findByPair(ownerAccountId, targetAccountId)
                .map(view -> ApiResponses.ok(AccountWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "RELATIONSHIP_NOT_FOUND", "External account relationship not found"));
    }

    @GetMapping("/{ownerAccountId}/relationships")
    @Operation(summary = "Find recent relationships by owner account", operationId = "findRelationshipsByOwner")
    public ResponseEntity<ApiResponse<List<ExternalAccountRelationshipResponse>>> findRelationshipsByOwner(
            @PathVariable Long ownerAccountId,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<ExternalAccountRelationshipView> views = relationshipOperations.findRecentByOwner(ownerAccountId, limit);
        return ApiResponses.ok(views.stream().map(AccountWebMapper::toResponse).toList());
    }

    // --- Follower Snapshots ---

    @PostMapping("/{ownerAccountId}/snapshots")
    @Operation(summary = "Create follower snapshot for owner account", operationId = "createFollowerSnapshot")
    public ResponseEntity<ApiResponse<FollowerSnapshotResponse>> createSnapshot(
            @PathVariable Long ownerAccountId,
            @Valid @RequestBody CreateFollowerSnapshotRequest request) {
        FollowerSnapshotView created = snapshotOperations.createSnapshot(
                AccountWebMapper.toCommand(ownerAccountId, request));
        return ApiResponses.created(AccountWebMapper.toResponse(created));
    }

    @GetMapping("/snapshots/{id}")
    @Operation(summary = "Get follower snapshot by ID", operationId = "getFollowerSnapshot")
    public ResponseEntity<ApiResponse<FollowerSnapshotResponse>> getSnapshot(@PathVariable Long id) {
        return snapshotOperations.findById(id)
                .map(view -> ApiResponses.ok(AccountWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "SNAPSHOT_NOT_FOUND", "Follower snapshot not found"));
    }

    @GetMapping("/{ownerAccountId}/snapshots")
    @Operation(summary = "Find recent follower snapshots by owner account", operationId = "findSnapshotsByOwner")
    public ResponseEntity<ApiResponse<List<FollowerSnapshotResponse>>> findSnapshotsByOwner(
            @PathVariable Long ownerAccountId,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<FollowerSnapshotView> views = snapshotOperations.findRecentByOwner(ownerAccountId, limit);
        return ApiResponses.ok(views.stream().map(AccountWebMapper::toResponse).toList());
    }

    @GetMapping("/snapshots/{snapshotId}/entries")
    @Operation(summary = "Find entries for a follower snapshot", operationId = "findSnapshotEntries")
    public ResponseEntity<ApiResponse<List<FollowerSnapshotEntryResponse>>> findSnapshotEntries(
            @PathVariable Long snapshotId,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<FollowerSnapshotEntryView> entries = snapshotOperations.findEntriesBySnapshotId(snapshotId, limit);
        return ApiResponses.ok(entries.stream().map(AccountWebMapper::toResponse).toList());
    }
}
