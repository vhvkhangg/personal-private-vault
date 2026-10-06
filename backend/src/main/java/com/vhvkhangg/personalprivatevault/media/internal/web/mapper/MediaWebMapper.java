package com.vhvkhangg.personalprivatevault.media.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.media.album.CreateAlbumCommand;
import com.vhvkhangg.personalprivatevault.media.album.UpdateAlbumCommand;
import com.vhvkhangg.personalprivatevault.media.image.CreateImageCommand;
import com.vhvkhangg.personalprivatevault.media.image.UpdateImageMetadataCommand;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.AlbumResponse;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.CreateAlbumRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.CreateImageRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.ImageResponse;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.UpdateAlbumRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.UpdateImageRequest;
import com.vhvkhangg.personalprivatevault.media.view.AlbumView;
import com.vhvkhangg.personalprivatevault.media.view.ImageView;

public final class MediaWebMapper {

    private MediaWebMapper() {}

    public static AlbumResponse toResponse(AlbumView view) {
        if (view == null) return null;
        return new AlbumResponse(
                view.id(),
                view.title(),
                view.description(),
                view.imageCount()
        );
    }

    public static CreateAlbumCommand toCommand(CreateAlbumRequest request) {
        if (request == null) return null;
        return new CreateAlbumCommand(request.title(), request.description());
    }

    public static UpdateAlbumCommand toCommand(Long id, UpdateAlbumRequest request) {
        if (request == null) return null;
        return new UpdateAlbumCommand(id, request.title(), request.description());
    }

    public static ImageResponse toResponse(ImageView view) {
        if (view == null) return null;
        return new ImageResponse(
                view.id(),
                view.albumId(),
                view.title(),
                view.imageType(),
                view.objectKey(),
                view.sourceUrl(),
                view.mimeType(),
                view.sizeBytes(),
                view.widthPx(),
                view.heightPx(),
                view.checksumSha256(),
                view.capturedAt(),
                view.locationText()
        );
    }

    public static CreateImageCommand toCommand(CreateImageRequest request) {
        if (request == null) return null;
        return new CreateImageCommand(
                request.albumId(),
                request.title(),
                request.imageType(),
                request.objectKey(),
                request.sourceUrl(),
                request.mimeType(),
                request.sizeBytes(),
                request.widthPx(),
                request.heightPx(),
                request.checksumSha256(),
                request.capturedAt(),
                request.locationText()
        );
    }

    public static UpdateImageMetadataCommand toCommand(Long id, UpdateImageRequest request) {
        if (request == null) return null;
        return new UpdateImageMetadataCommand(
                id,
                request.albumId(),
                request.title(),
                request.imageType(),
                request.sourceUrl(),
                request.mimeType(),
                request.sizeBytes(),
                request.widthPx(),
                request.heightPx(),
                request.capturedAt(),
                request.locationText()
        );
    }
}
