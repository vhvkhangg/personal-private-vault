package com.vhvkhangg.personalprivatevault.media.internal.application.storage;

import com.vhvkhangg.personalprivatevault.media.album.AlbumNotFoundException;
import com.vhvkhangg.personalprivatevault.media.image.CreateImageCommand;
import com.vhvkhangg.personalprivatevault.media.image.ImageConflictException;
import com.vhvkhangg.personalprivatevault.media.image.ImageOperations;
import com.vhvkhangg.personalprivatevault.media.image.InvalidImageException;
import com.vhvkhangg.personalprivatevault.media.internal.domain.Image;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence.ImageRepository;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.storage.MediaStorageProperties;
import com.vhvkhangg.personalprivatevault.media.view.ImageView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final MediaStoragePort mediaStoragePort;
    private final ImageOperations imageOperations;
    private final ImageRepository imageRepository;
    private final MediaStorageProperties properties;

    public ImageView uploadImage(
            MultipartFile file,
            Long albumId,
            String title,
            String imageType,
            String sourceUrl,
            Integer widthPx,
            Integer heightPx,
            Instant capturedAt,
            String locationText
    ) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Managed image upload cannot be initiated within an active transaction; upload requires independent commit boundary");
        }

        if (properties != null && !properties.isEnabled()) {
            throw new StorageDisabledException("Managed image storage is not enabled");
        }

        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Upload file must not be null or empty");
        }

        Path tempFile = null;
        String objectKey = generateObjectKey(file.getOriginalFilename());

        try {
            // 1. Spool to temp file while computing SHA-256 and byte size
            tempFile = Files.createTempFile("ppv-upload-", ".tmp");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            long totalBytes = 0;
            try (InputStream in = new BufferedInputStream(file.getInputStream());
                 DigestInputStream dis = new DigestInputStream(in, digest);
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(tempFile))) {

                byte[] buffer = new byte[8192];
                int read;
                while ((read = dis.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    totalBytes += read;
                    if (totalBytes > properties.getMaxFileSize()) {
                        throw new InvalidImageException("File size exceeds maximum configured upload limit");
                    }
                }
                out.flush();
            }

            if (totalBytes == 0) {
                throw new InvalidImageException("Uploaded file contains 0 bytes");
            }

            String checksumSha256 = HexFormat.of().formatHex(digest.digest());
            String mimeType = file.getContentType();
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = "application/octet-stream";
            }

            // 2. Upload to storage
            try (InputStream uploadStream = new BufferedInputStream(Files.newInputStream(tempFile))) {
                mediaStoragePort.upload(objectKey, uploadStream, totalBytes, mimeType);
            } catch (Exception uploadEx) {
                log.warn("Storage upload encountered exception; probing for known attempt object: exception={}", uploadEx.getClass().getSimpleName());
                boolean objectExists = false;
                try {
                    objectExists = mediaStoragePort.exists(objectKey);
                } catch (Exception probeEx) {
                    log.warn("Storage exists probe failed; retaining unresolved outcome as accepted residual orphan risk");
                }
                if (objectExists) {
                    log.info("Storage object confirmed present despite upload exception; compensating since metadata creation was not initiated");
                    compensateObject(objectKey);
                }
                throw uploadEx;
            }

            // 3. Create metadata in database
            CreateImageCommand command = new CreateImageCommand(
                    albumId,
                    title,
                    imageType,
                    objectKey,
                    sourceUrl,
                    mimeType,
                    totalBytes,
                    widthPx,
                    heightPx,
                    checksumSha256,
                    capturedAt,
                    locationText
            );

            try {
                return imageOperations.create(command);
            } catch (InvalidImageException | ImageConflictException | AlbumNotFoundException | DataIntegrityViolationException | IllegalArgumentException e) {
                // Authoritative confirmed rollback from the metadata transaction boundary
                log.info("Metadata creation failed with confirmed rollback; initiating bounded compensation");
                compensateObject(objectKey);
                throw e;
            } catch (Exception e) {
                // Ambiguous / uncertain metadata outcome:
                log.warn("Ambiguous exception during image metadata commit; probing positive reconciliation: exception={}", e.getClass().getSimpleName());
                try {
                    Optional<Image> reconciled = imageRepository.findByObjectKey(objectKey);
                    if (reconciled.isPresent()) {
                        Image img = reconciled.get();
                        if (checksumSha256.equals(img.getChecksumSha256()) && Long.valueOf(totalBytes).equals(img.getSizeBytes())) {
                            log.info("Positive reconciliation succeeded; retaining committed image object");
                            return toView(img);
                        }
                    }
                } catch (Exception reconciliationEx) {
                    log.warn("Positive reconciliation check failed; retaining object: exception={}", reconciliationEx.getClass().getSimpleName());
                }
                // Absence or mismatch does NOT prove rollback! Never delete on uncertainty!
                log.warn("Metadata transaction outcome is uncertain; retaining object in storage as accepted residual orphan risk");
                throw e;
            }

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Failed to process upload stream", e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void compensateObject(String objectKey) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                mediaStoragePort.delete(objectKey);
                return;
            } catch (Exception e) {
                if (attempt == 3) {
                    log.warn("Upload compensation failed after maximum retries; manual cleanup required");
                } else {
                    try {
                        Thread.sleep(attempt * 50L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private String generateObjectKey(String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            String candidate = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            if (candidate.matches("^[a-z0-9]{1,10}$")) {
                ext = "." + candidate;
            }
        }
        return "images/managed/" + UUID.randomUUID() + ext;
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
}
