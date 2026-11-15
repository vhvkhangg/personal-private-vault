package com.vhvkhangg.personalprivatevault.vault.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.internal.web.dto.CreateTagRequest;
import com.vhvkhangg.personalprivatevault.vault.internal.web.dto.SetRatingRequest;
import com.vhvkhangg.personalprivatevault.vault.internal.web.dto.TagResponse;
import com.vhvkhangg.personalprivatevault.vault.internal.web.dto.VaultEntryResponse;
import com.vhvkhangg.personalprivatevault.vault.internal.web.dto.VaultMetadataResponse;
import com.vhvkhangg.personalprivatevault.vault.internal.web.dto.VaultTrashPageResponse;
import com.vhvkhangg.personalprivatevault.vault.internal.web.mapper.VaultWebMapper;
import com.vhvkhangg.personalprivatevault.vault.metadata.VaultMetadataOperations;
import com.vhvkhangg.personalprivatevault.vault.view.TagView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultMetadataView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultTrashPageView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vault")
@RequiredArgsConstructor
@Tag(name = "Vault", description = "Vault entry lifecycle, recycle bin, favorites, ratings, and tags")
public class VaultController {

    private final VaultEntryOperations vaultEntryOperations;
    private final VaultMetadataOperations vaultMetadataOperations;

    @GetMapping("/entries/{id}")
    @Operation(summary = "Get vault entry by ID", operationId = "getVaultEntry")
    public ResponseEntity<ApiResponse<VaultEntryResponse>> getEntry(@PathVariable Long id) {
        return vaultEntryOperations.find(id)
                .map(view -> ApiResponses.ok(VaultWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "VAULT_ENTRY_NOT_FOUND", "Vault entry not found"));
    }

    @DeleteMapping("/entries/{id}")
    @Operation(summary = "Move vault entry to trash", operationId = "trashVaultEntry")
    public ResponseEntity<ApiResponse<VaultEntryResponse>> moveToTrash(@PathVariable Long id) {
        VaultEntryView trashed = vaultEntryOperations.moveToTrash(id);
        return ApiResponses.ok(VaultWebMapper.toResponse(trashed));
    }

    @PostMapping("/entries/{id}/restore")
    @Operation(summary = "Restore vault entry from trash", operationId = "restoreVaultEntry")
    public ResponseEntity<ApiResponse<VaultEntryResponse>> restore(@PathVariable Long id) {
        VaultEntryView restored = vaultEntryOperations.restore(id);
        return ApiResponses.ok(VaultWebMapper.toResponse(restored));
    }

    @GetMapping("/trash")
    @Operation(
            summary = "List trashed vault entries",
            operationId = "listVaultTrash",
            description = "Paged vault recycle bin items in descending ID order. Accessible only to authenticated PROFILE_PRIVATE sessions."
    )
    public ResponseEntity<ApiResponse<VaultTrashPageResponse>> listTrash(
            @Parameter(
                    name = "limit",
                    description = "Maximum number of items to return (1-100, default 25)",
                    schema = @Schema(type = "integer", format = "int32", defaultValue = "25", minimum = "1", maximum = "100")
            )
            @RequestParam(name = "limit", required = false) String limitRaw,
            @Parameter(
                    name = "cursor",
                    description = "Opaque continuation cursor for next page",
                    schema = @Schema(type = "string", maxLength = 128)
            )
            @RequestParam(name = "cursor", required = false) String cursor
    ) {
        int limit = 25;
        if (limitRaw != null) {
            if (!limitRaw.matches("^-?\\d+$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .cacheControl(CacheControl.noStore())
                        .body(ApiResponses.error("MALFORMED_REQUEST", "Invalid request parameters"));
            }
            try {
                limit = Integer.parseInt(limitRaw);
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .cacheControl(CacheControl.noStore())
                        .body(ApiResponses.error("MALFORMED_REQUEST", "Invalid request parameters"));
            }
            if (limit < 1 || limit > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .cacheControl(CacheControl.noStore())
                        .body(ApiResponses.error("INVALID_ARGUMENT", "Invalid argument provided"));
            }
        }

        try {
            VaultTrashPageView pageView = vaultEntryOperations.listTrash(limit, cursor);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(ApiResponses.success(VaultWebMapper.toTrashPageResponse(pageView)));
        } catch (VaultEntryOperations.InvalidTrashInputException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .cacheControl(CacheControl.noStore())
                    .body(ApiResponses.error("INVALID_ARGUMENT", "Invalid argument provided"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .cacheControl(CacheControl.noStore())
                    .body(ApiResponses.error("INTERNAL_ERROR", "An unexpected error occurred"));
        }
    }

    @GetMapping("/entries/{id}/metadata")
    @Operation(summary = "Get vault entry metadata (favorite, rating, tags)", operationId = "getVaultEntryMetadata")
    public ResponseEntity<ApiResponse<VaultMetadataResponse>> getMetadata(@PathVariable Long id) {
        VaultMetadataView metadata = vaultMetadataOperations.metadata(id);
        return ApiResponses.ok(VaultWebMapper.toResponse(metadata));
    }

    @PutMapping("/entries/{id}/favorite")
    @Operation(summary = "Mark vault entry as favorite", operationId = "favoriteVaultEntry")
    public ResponseEntity<ApiResponse<Void>> favorite(@PathVariable Long id) {
        vaultMetadataOperations.favorite(id);
        return ApiResponses.ok(null);
    }

    @DeleteMapping("/entries/{id}/favorite")
    @Operation(summary = "Unmark vault entry as favorite", operationId = "unfavoriteVaultEntry")
    public ResponseEntity<ApiResponse<Void>> unfavorite(@PathVariable Long id) {
        vaultMetadataOperations.unfavorite(id);
        return ApiResponses.ok(null);
    }

    @PutMapping("/entries/{id}/rating")
    @Operation(summary = "Set rating for vault entry", operationId = "setVaultEntryRating")
    public ResponseEntity<ApiResponse<Void>> setRating(@PathVariable Long id, @Valid @RequestBody SetRatingRequest request) {
        vaultMetadataOperations.setRating(id, request.grade());
        return ApiResponses.ok(null);
    }

    @DeleteMapping("/entries/{id}/rating")
    @Operation(summary = "Remove rating from vault entry", operationId = "removeVaultEntryRating")
    public ResponseEntity<ApiResponse<Void>> removeRating(@PathVariable Long id) {
        vaultMetadataOperations.removeRating(id);
        return ApiResponses.ok(null);
    }

    @PostMapping("/tags")
    @Operation(summary = "Create global tag if not existing", operationId = "createTag")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@Valid @RequestBody CreateTagRequest request) {
        TagView tag = vaultMetadataOperations.createTag(request.name());
        return ApiResponses.created(VaultWebMapper.toResponse(tag));
    }

    @PutMapping("/entries/{id}/tags/{tagId}")
    @Operation(summary = "Attach tag to vault entry", operationId = "attachVaultEntryTag")
    public ResponseEntity<ApiResponse<Void>> attachTag(@PathVariable Long id, @PathVariable Long tagId) {
        vaultMetadataOperations.attachTag(id, tagId);
        return ApiResponses.ok(null);
    }

    @DeleteMapping("/entries/{id}/tags/{tagId}")
    @Operation(summary = "Detach tag from vault entry", operationId = "detachVaultEntryTag")
    public ResponseEntity<ApiResponse<Void>> detachTag(@PathVariable Long id, @PathVariable Long tagId) {
        vaultMetadataOperations.detachTag(id, tagId);
        return ApiResponses.ok(null);
    }
}
