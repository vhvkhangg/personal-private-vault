package com.vhvkhangg.personalprivatevault.search.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiPageMeta;
import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.search.enums.SearchDomain;
import com.vhvkhangg.personalprivatevault.search.internal.web.dto.GlobalSearchResultResponse;
import com.vhvkhangg.personalprivatevault.search.internal.web.mapper.SearchWebMapper;
import com.vhvkhangg.personalprivatevault.search.query.GlobalSearchOperations;
import com.vhvkhangg.personalprivatevault.search.query.GlobalSearchQuery;
import com.vhvkhangg.personalprivatevault.search.view.GlobalSearchPage;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Validated
@Tag(name = "Search", description = "Cross-module PostgreSQL-first global discovery")
public class GlobalSearchController {

    private final GlobalSearchOperations globalSearchOperations;

    @GetMapping
    @Operation(summary = "Search vault entries across modules", operationId = "globalSearch")
    public ResponseEntity<ApiResponse<List<GlobalSearchResultResponse>>> search(
            @RequestParam("q") String q,
            @RequestParam(required = false) Set<SearchDomain> domain,
            @RequestParam(required = false) Set<VaultEntryType> entryType,
            @RequestParam(required = false) Set<Long> tagId,
            @RequestParam(defaultValue = "0") @PositiveOrZero @Max(500) int offset,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        GlobalSearchQuery query = new GlobalSearchQuery(q, domain, entryType, tagId, offset, limit);
        GlobalSearchPage page = globalSearchOperations.search(query);
        List<GlobalSearchResultResponse> results = page.items().stream()
                .map(SearchWebMapper::toResponse)
                .toList();
        ApiPageMeta pageMeta = new ApiPageMeta(page.limit(), page.offset(), page.hasMore());
        return ApiResponses.ok(results, pageMeta);
    }
}
