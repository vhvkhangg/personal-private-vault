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

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image metadata management")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final ImageOperations imageOperations;
    private final com.vhvkhangg.personalprivatevault.media.internal.application.storage.ImageUploadService imageUploadService;
    private final com.vhvkhangg.personalprivatevault.media.internal.application.storage.ImageDownloadService imageDownloadService;

    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image binary and create metadata", operationId = "uploadImage")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Image uploaded and metadata created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Malformed multipart request or invalid parameters",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "413",
                    description = "Payload Too Large - File exceeds maximum configured upload size limit",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ErrorResponse")
                    )
            )
    })
    public ResponseEntity<ApiResponse<ImageResponse>> upload(
            @org.springframework.web.bind.annotation.RequestPart("file") org.springframework.web.multipart.MultipartFile file,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long albumId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String title,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String imageType,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String sourceUrl,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer widthPx,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer heightPx,
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.time.Instant capturedAt,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String locationText
    ) {
        ImageView created = imageUploadService.uploadImage(
                file,
                albumId,
                title,
                imageType,
                sourceUrl,
                widthPx,
                heightPx,
                capturedAt,
                locationText
        );
        return ApiResponses.created(MediaWebMapper.toResponse(created));
    }

    @GetMapping("/{id}/content")
    @Operation(summary = "Download image binary content", operationId = "downloadImageContent")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Image binary content",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "*/*",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary")
                    )
            )
    })
    public ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody> downloadContent(
            @PathVariable Long id,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        var download = imageDownloadService.downloadImage(id);
        var closed = new java.util.concurrent.atomic.AtomicBoolean(false);
        Runnable cleanup = () -> {
            if (closed.compareAndSet(false, true)) {
                try {
                    download.close();
                } catch (IOException ignored) {
                }
            }
        };

        org.springframework.web.context.request.async.WebAsyncManager asyncManager =
                org.springframework.web.context.request.async.WebAsyncUtils.getAsyncManager(request);
        asyncManager.registerCallableInterceptor("imageDownloadCleanup",
                new org.springframework.web.context.request.async.CallableProcessingInterceptor() {
                    @Override
                    public <T> void afterCompletion(org.springframework.web.context.request.NativeWebRequest req, java.util.concurrent.Callable<T> task) {
                        cleanup.run();
                    }
                    @Override
                    public <T> Object handleTimeout(org.springframework.web.context.request.NativeWebRequest req, java.util.concurrent.Callable<T> task) {
                        cleanup.run();
                        return RESULT_NONE;
                    }
                    @Override
                    public <T> Object handleError(org.springframework.web.context.request.NativeWebRequest req, java.util.concurrent.Callable<T> task, Throwable t) {
                        cleanup.run();
                        return RESULT_NONE;
                    }
                });

        org.springframework.web.context.request.async.AsyncWebRequest asyncWebRequest = asyncManager.getAsyncWebRequest();
        if (asyncWebRequest != null) {
            asyncWebRequest.addCompletionHandler(cleanup);
            asyncWebRequest.addErrorHandler(t -> cleanup.run());
            asyncWebRequest.addTimeoutHandler(cleanup);
        }

        String mimeType = download.metadata().mimeType();
        org.springframework.http.MediaType mediaType = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
        if (mimeType != null && !mimeType.isBlank()) {
            try {
                mediaType = org.springframework.http.MediaType.parseMediaType(mimeType);
            } catch (Exception ignored) {
            }
        }

        var builder = ResponseEntity.ok()
                .contentType(mediaType)
                .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "private, no-store");

        Long sizeBytes = download.metadata().sizeBytes();
        if (sizeBytes != null && sizeBytes > 0) {
            builder.contentLength(sizeBytes);
        } else if (download.downloadResult().sizeBytes() != null && download.downloadResult().sizeBytes() > 0) {
            builder.contentLength(download.downloadResult().sizeBytes());
        }

        return builder.body(outputStream -> {
            try {
                download.downloadResult().inputStream().transferTo(outputStream);
                outputStream.flush();
            } catch (Exception ex) {
                if (response.isCommitted()) {
                    log.warn("Image transfer aborted after response commitment: exception={}", ex.getClass().getSimpleName());
                }
                throw new IOException("Image transfer stream aborted: " + ex.getClass().getSimpleName());
            } finally {
                cleanup.run();
            }
        });
    }

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
