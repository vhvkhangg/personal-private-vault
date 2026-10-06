package com.vhvkhangg.personalprivatevault.feed.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CreateFeedSourceRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.FeedItemResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.FeedSourceResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.UpdateFeedSourceRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.mapper.FeedWebMapper;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.view.FeedItemView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
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

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Validated
@Tag(name = "Feed", description = "Feed source management and normalized item catalog")
public class FeedController {

    private final FeedSourceOperations feedSourceOperations;
    private final FeedItemOperations feedItemOperations;

    // --- Sources ---

    @PostMapping("/sources")
    @Operation(summary = "Create feed source", operationId = "createFeedSource")
    public ResponseEntity<ApiResponse<FeedSourceResponse>> createSource(@Valid @RequestBody CreateFeedSourceRequest request) {
        FeedSourceView created = feedSourceOperations.createSource(FeedWebMapper.toCommand(request));
        return ApiResponses.created(FeedWebMapper.toResponse(created));
    }

    @GetMapping("/sources/{id}")
    @Operation(summary = "Get feed source by ID", operationId = "getFeedSource")
    public ResponseEntity<ApiResponse<FeedSourceResponse>> getSource(@PathVariable Long id) {
        return feedSourceOperations.findSourceById(id)
                .map(view -> ApiResponses.ok(FeedWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FEED_SOURCE_NOT_FOUND", "Feed source not found"));
    }

    @PutMapping("/sources/{id}")
    @Operation(summary = "Update feed source", operationId = "updateFeedSource")
    public ResponseEntity<ApiResponse<FeedSourceResponse>> updateSource(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFeedSourceRequest request) {
        FeedSourceView updated = feedSourceOperations.updateSource(id, FeedWebMapper.toCommand(request));
        return ApiResponses.ok(FeedWebMapper.toResponse(updated));
    }

    @GetMapping("/sources/due")
    @Operation(summary = "Find feed sources due for refresh", operationId = "findDueFeedSources")
    public ResponseEntity<ApiResponse<List<FeedSourceResponse>>> findDueSources(
            @RequestParam Instant cutoff,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<FeedSourceView> sources = feedSourceOperations.findDueSources(cutoff, limit);
        return ApiResponses.ok(sources.stream().map(FeedWebMapper::toResponse).toList());
    }

    // --- Items ---

    @GetMapping("/items/{id}")
    @Operation(summary = "Get feed item by ID", operationId = "getFeedItem")
    public ResponseEntity<ApiResponse<FeedItemResponse>> getItem(@PathVariable Long id) {
        return feedItemOperations.findItemById(id)
                .map(view -> ApiResponses.ok(FeedWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FEED_ITEM_NOT_FOUND", "Feed item not found"));
    }

    @GetMapping("/sources/{sourceId}/items")
    @Operation(summary = "Find recent items for a feed source", operationId = "findRecentFeedItemsBySource")
    public ResponseEntity<ApiResponse<List<FeedItemResponse>>> findRecentItemsBySource(
            @PathVariable Long sourceId,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<FeedItemView> items = feedItemOperations.findRecentItemsBySource(sourceId, limit);
        return ApiResponses.ok(items.stream().map(FeedWebMapper::toResponse).toList());
    }
}
