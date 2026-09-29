package com.vhvkhangg.personalprivatevault.media.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Objects;

/**
 * Image metadata entity sharing identity with {@code vault_entries.id} for type {@code IMAGE}.
 */
@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "image_type", length = 100)
    private String imageType;

    @Column(name = "object_key", length = 1024, nullable = false, unique = true)
    private String objectKey;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "checksum_sha256", length = 64, unique = true)
    private String checksumSha256;

    @Column(name = "captured_at")
    private Instant capturedAt;

    @Column(name = "location_text", length = 500)
    private String locationText;

    @Transient
    private boolean isNew = false;

    public Image(
            Long id,
            Long albumId,
            String title,
            String imageType,
            String objectKey,
            String sourceUrl,
            String mimeType,
            Long sizeBytes,
            Integer widthPx,
            Integer heightPx,
            String checksumSha256,
            Instant capturedAt,
            String locationText,
            boolean isNew
    ) {
        this.id = Objects.requireNonNull(id, "Image id must not be null");
        this.albumId = albumId;
        this.title = title;
        this.imageType = imageType;
        this.objectKey = Objects.requireNonNull(objectKey, "Image objectKey must not be null");
        this.sourceUrl = sourceUrl;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
        this.checksumSha256 = checksumSha256;
        this.capturedAt = capturedAt;
        this.locationText = locationText;
        this.isNew = isNew;
    }

    public void updateMetadata(
            Long albumId,
            String title,
            String imageType,
            String sourceUrl,
            String mimeType,
            Long sizeBytes,
            Integer widthPx,
            Integer heightPx,
            Instant capturedAt,
            String locationText
    ) {
        this.albumId = albumId;
        this.title = title;
        this.imageType = imageType;
        this.sourceUrl = sourceUrl;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
        this.capturedAt = capturedAt;
        this.locationText = locationText;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Image other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
