package com.vhvkhangg.personalprivatevault.vault.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * HTTP response payload for paged vault trash items.
 */
@Schema(name = "VaultTrashPageResponse", description = "Paged vault recycle bin items")
public record VaultTrashPageResponse(
        @Schema(description = "Page items in descending ID order", requiredMode = Schema.RequiredMode.REQUIRED)
        List<VaultTrashItemResponse> items,

        @Schema(description = "Opaque continuation cursor for next page, or null if exhausted", requiredMode = Schema.RequiredMode.REQUIRED, nullable = true)
        String nextCursor
) {
    public VaultTrashPageResponse {
        items = items != null ? List.copyOf(items) : List.of();
    }
}
