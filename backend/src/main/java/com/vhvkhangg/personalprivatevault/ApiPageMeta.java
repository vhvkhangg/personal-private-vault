package com.vhvkhangg.personalprivatevault;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Truthful pagination metadata when windowed/paged results exist.
 *
 * @param limit maximum items requested or returned
 * @param offset non-negative item offset where applicable
 * @param hasMore boolean indicating if more items exist beyond this window; null if untracked
 */
@Schema(description = "Pagination metadata for windowed list responses")
public record ApiPageMeta(
        @Schema(description = "Requested page size limit", example = "50")
        Integer limit,

        @Schema(description = "Page offset", example = "0")
        Integer offset,

        @Schema(description = "Whether additional records exist beyond this page", example = "true")
        Boolean hasMore
) {}
