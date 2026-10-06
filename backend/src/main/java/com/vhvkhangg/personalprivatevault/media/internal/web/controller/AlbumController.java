package com.vhvkhangg.personalprivatevault.media.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiPageMeta;
import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.media.album.AlbumOperations;
import com.vhvkhangg.personalprivatevault.media.image.ImageOperations;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.AlbumResponse;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.CreateAlbumRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.ImageResponse;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.UpdateAlbumRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.mapper.MediaWebMapper;
import com.vhvkhangg.personalprivatevault.media.view.AlbumView;
import com.vhvkhangg.personalprivatevault.media.view.ImageView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Validated
@Tag(name = "Albums", description = "Album collection management and album image listing")
public class AlbumController {

    private final AlbumOperations albumOperations;
    private final ImageOperations imageOperations;

    @PostMapping
    @Operation(summary = "Create album", operationId = "createAlbum")
    public ResponseEntity<ApiResponse<AlbumResponse>> create(@Valid @RequestBody CreateAlbumRequest request) {
        AlbumView created = albumOperations.create(MediaWebMapper.toCommand(request));
        return ApiResponses.created(MediaWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get album by ID", operationId = "getAlbum")
    public ResponseEntity<ApiResponse<AlbumResponse>> get(@PathVariable Long id) {
        AlbumView album = albumOperations.findById(id);
        return ApiResponses.ok(MediaWebMapper.toResponse(album));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update album", operationId = "updateAlbum")
    public ResponseEntity<ApiResponse<AlbumResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAlbumRequest request) {
        AlbumView updated = albumOperations.update(MediaWebMapper.toCommand(id, request));
        return ApiResponses.ok(MediaWebMapper.toResponse(updated));
    }

    @GetMapping("/{id}/image-count")
    @Operation(summary = "Get derived image count for album", operationId = "getAlbumImageCount")
    public ResponseEntity<ApiResponse<Long>> getImageCount(@PathVariable Long id) {
        long count = albumOperations.getImageCount(id);
        return ApiResponses.ok(count);
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "List images in album with pagination", operationId = "listAlbumImages")
    public ResponseEntity<ApiResponse<List<ImageResponse>>> listImages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit,
            @RequestParam(defaultValue = "0") @PositiveOrZero int offset) {
        long totalCount = albumOperations.getImageCount(id);
        List<ImageView> images = imageOperations.findByAlbumId(id, limit, offset);
        List<ImageResponse> responseList = images.stream().map(MediaWebMapper::toResponse).toList();

        boolean hasMore = (offset + responseList.size()) < totalCount;
        ApiPageMeta pageMeta = new ApiPageMeta(limit, offset, hasMore);

        return ApiResponses.ok(responseList, pageMeta);
    }
}
