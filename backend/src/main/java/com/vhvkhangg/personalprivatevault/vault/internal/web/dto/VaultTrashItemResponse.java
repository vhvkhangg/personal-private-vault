package com.vhvkhangg.personalprivatevault.vault.internal.web.dto;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Item in the vault trash page response.
 */
@Schema(name = "VaultTrashItemResponse", description = "Item in vault recycle bin containing minimal identity metadata")
public record VaultTrashItemResponse(
        @Schema(description = "Vault entry ID", example = "142", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Vault entry type", example = "FILM", requiredMode = Schema.RequiredMode.REQUIRED)
        VaultEntryType entryType,

        @Schema(description = "Deletion timestamp", example = "2026-11-14T08:12:01Z", requiredMode = Schema.RequiredMode.REQUIRED)
        Instant deletedAt
) {
}
