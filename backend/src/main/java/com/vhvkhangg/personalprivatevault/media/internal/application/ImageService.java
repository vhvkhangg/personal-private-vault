package com.vhvkhangg.personalprivatevault.media.internal.application;

import com.vhvkhangg.personalprivatevault.media.album.AlbumNotFoundException;
import com.vhvkhangg.personalprivatevault.media.image.CreateImageCommand;
import com.vhvkhangg.personalprivatevault.media.image.ImageConflictException;
import com.vhvkhangg.personalprivatevault.media.image.ImageNotFoundException;
import com.vhvkhangg.personalprivatevault.media.image.ImageOperations;
import com.vhvkhangg.personalprivatevault.media.image.InvalidImageException;
import com.vhvkhangg.personalprivatevault.media.image.UpdateImageMetadataCommand;
import com.vhvkhangg.personalprivatevault.media.internal.domain.Image;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence.AlbumRepository;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence.ImageRepository;
import com.vhvkhangg.personalprivatevault.media.view.ImageView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ImageService implements ImageOperations {

    private final VaultEntryOperations vaultEntryOperations;
    private final AlbumRepository albumRepository;
    private final ImageRepository imageRepository;

    @Override
    @Transactional
    public ImageView create(CreateImageCommand command) {
        if (command == null) {
            throw new InvalidImageException("CreateImageCommand must not be null");
        }
        validateObjectKey(command.objectKey());
        validateMetadata(
                command.albumId(),
                command.title(),
                command.imageType(),
                command.sourceUrl(),
                command.mimeType(),
                command.sizeBytes(),
                command.widthPx(),
                command.heightPx(),
                command.locationText()
        );
        validateChecksum(command.checksumSha256());

        // Precheck duplicate object_key
        if (imageRepository.findByObjectKey(command.objectKey().trim()).isPresent()) {
            throw new ImageConflictException("Image with object key '" + command.objectKey().trim() + "' already exists");
        }

        // Precheck duplicate checksum if present
        if (command.checksumSha256() != null && !command.checksumSha256().isBlank()) {
            if (imageRepository.findByChecksumSha256(command.checksumSha256().trim()).isPresent()) {
                throw new ImageConflictException("Image with checksum '" + command.checksumSha256().trim() + "' already exists");
            }
        }

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.IMAGE);

        Image image = new Image(
                vaultEntry.id(),
                command.albumId(),
                trimOrNull(command.title()),
                trimOrNull(command.imageType()),
                command.objectKey().trim(),
                trimOrNull(command.sourceUrl()),
                trimOrNull(command.mimeType()),
                command.sizeBytes(),
                command.widthPx(),
                command.heightPx(),
                trimOrNull(command.checksumSha256()),
                command.capturedAt(),
                trimOrNull(command.locationText()),
                true
        );

        try {
            imageRepository.saveAndFlush(image);
        } catch (DataIntegrityViolationException ex) {
            String constraint = extractConstraintName(ex);
            if ("images_object_key_key".equals(constraint) || constraint.contains("object_key")) {
                throw new ImageConflictException("Image with object key '" + command.objectKey().trim() + "' already exists", ex);
            }
            if ("images_checksum_sha256_key".equals(constraint) || constraint.contains("checksum")) {
                String checksum = command.checksumSha256() != null ? command.checksumSha256().trim() : "";
                throw new ImageConflictException("Image with checksum '" + checksum + "' already exists", ex);
            }
            throw new ImageConflictException("Image metadata conflict occurred during creation", ex);
        }

        return toView(image);
    }

    @Override
    @Transactional
    public ImageView updateMetadata(UpdateImageMetadataCommand command) {
        if (command == null) {
            throw new InvalidImageException("UpdateImageMetadataCommand must not be null");
        }
        if (command.id() == null) {
            throw new InvalidImageException("Image ID must not be null");
        }
        validateMetadata(
                command.albumId(),
                command.title(),
                command.imageType(),
                command.sourceUrl(),
                command.mimeType(),
                command.sizeBytes(),
                command.widthPx(),
                command.heightPx(),
                command.locationText()
        );

        Image image = imageRepository.findById(command.id())
                .orElseThrow(() -> new ImageNotFoundException(command.id()));

        image.updateMetadata(
                command.albumId(),
                trimOrNull(command.title()),
                trimOrNull(command.imageType()),
                trimOrNull(command.sourceUrl()),
                trimOrNull(command.mimeType()),
                command.sizeBytes(),
                command.widthPx(),
                command.heightPx(),
                command.capturedAt(),
                trimOrNull(command.locationText())
        );

        imageRepository.save(image);
        return toView(image);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageView findById(Long id) {
        if (id == null) {
            throw new ImageNotFoundException("Image ID must not be null");
        }
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException(id));
        return toView(image);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageView> findByAlbumId(Long albumId, int limit, int offset) {
        if (albumId == null) {
            throw new AlbumNotFoundException("Album ID must not be null");
        }
        if (!albumRepository.existsById(albumId)) {
            throw new AlbumNotFoundException(albumId);
        }
        int effectiveLimit = Math.clamp(limit, 1, 100);
        int effectiveOffset = Math.max(0, offset);
        return imageRepository.findByAlbumIdPaged(albumId, effectiveLimit, effectiveOffset)
                .stream()
                .map(this::toView)
                .toList();
    }

    private void validateObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new InvalidImageException("Image object_key must not be blank");
        }
        if (objectKey.length() > 1024) {
            throw new InvalidImageException("Image object_key must not exceed 1024 characters");
        }
    }

    private void validateChecksum(String checksumSha256) {
        if (checksumSha256 != null && !checksumSha256.isBlank()) {
            if (checksumSha256.length() > 64) {
                throw new InvalidImageException("Image checksum_sha256 must not exceed 64 characters");
            }
        }
    }

    private void validateMetadata(
            Long albumId,
            String title,
            String imageType,
            String sourceUrl,
            String mimeType,
            Long sizeBytes,
            Integer widthPx,
            Integer heightPx,
            String locationText
    ) {
        if (albumId != null && !albumRepository.existsById(albumId)) {
            throw new AlbumNotFoundException(albumId);
        }
        if (title != null && title.length() > 500) {
            throw new InvalidImageException("Image title must not exceed 500 characters");
        }
        if (imageType != null && imageType.length() > 100) {
            throw new InvalidImageException("Image image_type must not exceed 100 characters");
        }
        if (sourceUrl != null && sourceUrl.length() > 2048) {
            throw new InvalidImageException("Image source_url must not exceed 2048 characters");
        }
        if (mimeType != null && mimeType.length() > 100) {
            throw new InvalidImageException("Image mime_type must not exceed 100 characters");
        }
        if (sizeBytes != null && sizeBytes < 0) {
            throw new InvalidImageException("Image size_bytes must be non-negative");
        }
        if (widthPx != null && widthPx <= 0) {
            throw new InvalidImageException("Image width_px must be positive");
        }
        if (heightPx != null && heightPx <= 0) {
            throw new InvalidImageException("Image height_px must be positive");
        }
        if (locationText != null && locationText.length() > 500) {
            throw new InvalidImageException("Image location_text must not exceed 500 characters");
        }
    }

    private String trimOrNull(String str) {
        if (str == null) return null;
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ImageView toView(Image image) {
        return new ImageView(
                image.getId(),
                image.getAlbumId(),
                image.getTitle(),
                image.getImageType(),
                image.getObjectKey(),
                image.getSourceUrl(),
                image.getMimeType(),
                image.getSizeBytes(),
                image.getWidthPx(),
                image.getHeightPx(),
                image.getChecksumSha256(),
                image.getCapturedAt(),
                image.getLocationText()
        );
    }

    private String extractConstraintName(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException cve && cve.getConstraintName() != null) {
                return cve.getConstraintName().toLowerCase(Locale.ROOT);
            }
            cause = cause.getCause();
        }
        Throwable root = ex.getRootCause();
        String rootMsg = root != null && root.getMessage() != null ? root.getMessage().toLowerCase(Locale.ROOT) : "";
        if (rootMsg.contains("images_object_key_key")) {
            return "images_object_key_key";
        }
        if (rootMsg.contains("images_checksum_sha256_key")) {
            return "images_checksum_sha256_key";
        }
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase(Locale.ROOT) : "";
        if (msg.contains("images_object_key_key")) {
            return "images_object_key_key";
        }
        if (msg.contains("images_checksum_sha256_key")) {
            return "images_checksum_sha256_key";
        }
        return "";
    }
}
