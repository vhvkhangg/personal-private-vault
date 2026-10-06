package com.vhvkhangg.personalprivatevault.media.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.media.image.ImageOperations;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.CreateImageRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.ImageResponse;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.UpdateImageRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.mapper.MediaWebMapper;
import com.vhvkhangg.personalprivatevault.media.view.ImageView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image metadata management")
public class ImageController {

    private final ImageOperations imageOperations;

    @PostMapping
    @Operation(summary = "Create image metadata record", operationId = "createImage")
    public ResponseEntity<ApiResponse<ImageResponse>> create(@Valid @RequestBody CreateImageRequest request) {
        ImageView created = imageOperations.create(MediaWebMapper.toCommand(request));
        return ApiResponses.created(MediaWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get image metadata by ID", operationId = "getImage")
    public ResponseEntity<ApiResponse<ImageResponse>> get(@PathVariable Long id) {
        ImageView image = imageOperations.findById(id);
        return ApiResponses.ok(MediaWebMapper.toResponse(image));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update image metadata", operationId = "updateImage")
    public ResponseEntity<ApiResponse<ImageResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateImageRequest request) {
        ImageView updated = imageOperations.updateMetadata(MediaWebMapper.toCommand(id, request));
        return ApiResponses.ok(MediaWebMapper.toResponse(updated));
    }
}
