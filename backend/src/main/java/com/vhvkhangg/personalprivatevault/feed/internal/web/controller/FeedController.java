package com.vhvkhangg.personalprivatevault.feed.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CombinedFeedRadarResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CreateFeedSourceRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.FeedItemResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.FeedRadarResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.FeedRefreshResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.FeedSourceResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.RssFeedRefreshResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.UpdateFeedSourceRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.mapper.FeedWebMapper;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.view.FeedItemView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedRadarPageView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedRefreshResultView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.feed.view.RssFeedRefreshResultView;
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

    // --- Radar & Manual Refresh ---

    @PostMapping("/sources/{sourceId}/refresh")
    @Operation(summary = "Manually refresh a Hacker News feed source", operationId = "refreshFeedSource")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Source refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or binding failure",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid bearer token",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Feed source not found",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Refresh in-flight or source changed during refresh",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Unsupported feed source type",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Downstream Hacker News provider error or limit exceeded",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Upstream provider, refresh capacity, or finalization unavailable",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<ApiResponse<FeedRefreshResponse>> refreshFeedSource(
            @PathVariable @Positive Long sourceId,
            @RequestParam(defaultValue = "30") @Positive @Max(50)
            @io.swagger.v3.oas.annotations.Parameter(description = "Maximum number of items to select from topstories (1-50, default 30)") int maxItems
    ) {
        FeedRefreshResultView result = feedSourceOperations.refreshSource(sourceId, maxItems);
        return ApiResponses.ok(FeedWebMapper.toResponse(result));
    }

    @GetMapping("/radar")
    @Operation(summary = "Get rolling 24-hour feed radar preview", operationId = "getFeedRadar")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rolling 24-hour feed radar preview"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or binding failure",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid bearer token",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Filtered feed source is not a Hacker News source",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<ApiResponse<FeedRadarResponse>> getFeedRadar(
            @RequestParam(required = false) @Positive
            @io.swagger.v3.oas.annotations.Parameter(description = "Optional feed source ID to filter radar items") Long sourceId,
            @RequestParam(defaultValue = "50") @Positive @Max(100)
            @io.swagger.v3.oas.annotations.Parameter(description = "Page size limit between 1 and 100, default 50") int limit
    ) {
        FeedRadarPageView page = feedItemOperations.getRadar(sourceId, limit);
        return ApiResponses.ok(FeedWebMapper.toResponse(page));
    }

    @PostMapping("/sources/{sourceId}/refresh-rss")
    @Operation(summary = "Manually refresh an RSS/Atom feed source", operationId = "refreshRssFeedSource")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Source refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or binding failure",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid bearer token",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Feed source not found",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Refresh in-flight or source changed during refresh",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Disabled source or unsupported source type",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Upstream provider invalid response, limit exceeded, or destination rejected",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Upstream provider, refresh capacity, or finalization unavailable",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<ApiResponse<RssFeedRefreshResponse>> refreshRssFeedSource(
            @PathVariable @Positive Long sourceId
    ) {
        RssFeedRefreshResultView result = feedSourceOperations.refreshRssSource(sourceId);
        return ApiResponses.ok(FeedWebMapper.toResponse(result));
    }

    @GetMapping("/radar/combined")
    @Operation(summary = "Get rolling 24-hour combined feed radar preview", operationId = "getCombinedFeedRadar")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rolling 24-hour combined feed radar preview"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or binding failure",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid bearer token",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<ApiResponse<CombinedFeedRadarResponse>> getCombinedFeedRadar(
            @RequestParam(defaultValue = "50") @Positive @Max(100)
            @io.swagger.v3.oas.annotations.Parameter(description = "Page size limit between 1 and 100, default 50") int limit
    ) {
        FeedRadarPageView page = feedItemOperations.getCombinedRadar(limit);
        return ApiResponses.ok(FeedWebMapper.toCombinedResponse(page));
    }
}
