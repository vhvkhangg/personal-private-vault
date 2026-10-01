package com.vhvkhangg.personalprivatevault.feed.internal.domain;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.view.FeedJsonSnapshot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Saved resource entity sharing identity with {@code vault_entries.id} for type {@code SAVED_RESOURCE}.
 */
@Entity
@Table(name = "saved_resources")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedResource implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "feed_item_id")
    private Long feedItemId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "kind", nullable = false)
    private SavedResourceKind kind;

    @Column(name = "source_name", length = 500)
    private String sourceName;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Column(name = "external_id", length = 500)
    private String externalId;

    @Column(name = "title", length = 1000, nullable = false)
    private String title;

    @Column(name = "resource_url", length = 2048, nullable = false)
    private String resourceUrl;

    @Column(name = "resource_url_hash", length = 64, nullable = false, unique = true)
    private String resourceUrlHash;

    @Column(name = "author", length = 500)
    private String author;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "fetched_at")
    private Instant fetchedAt;

    @Column(name = "saved_at", nullable = false)
    private Instant savedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_metadata", columnDefinition = "jsonb")
    private Map<String, Object> rawMetadata;

    @Transient
    private boolean isNew;

    public SavedResource(
            Long id,
            Long feedItemId,
            SavedResourceKind kind,
            String sourceName,
            String sourceUrl,
            String externalId,
            String title,
            String resourceUrl,
            String resourceUrlHash,
            String author,
            String summary,
            Instant publishedAt,
            Instant fetchedAt,
            Map<String, Object> rawMetadata,
            boolean isNew
    ) {
        this.id = Objects.requireNonNull(id, "SavedResource id must not be null");
        this.feedItemId = feedItemId;
        this.kind = Objects.requireNonNull(kind, "SavedResource kind must not be null");
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.externalId = externalId;
        this.title = Objects.requireNonNull(title, "SavedResource title must not be null");
        this.resourceUrl = Objects.requireNonNull(resourceUrl, "SavedResource resourceUrl must not be null");
        this.resourceUrlHash = Objects.requireNonNull(resourceUrlHash, "SavedResource resourceUrlHash must not be null");
        this.author = author;
        this.summary = summary;
        this.publishedAt = publishedAt;
        this.fetchedAt = fetchedAt;
        this.savedAt = Instant.now();
        this.rawMetadata = FeedJsonSnapshot.deepCopy(rawMetadata);
        this.isNew = isNew;
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

    public Map<String, Object> getRawMetadata() {
        return FeedJsonSnapshot.toUnmodifiableSnapshot(this.rawMetadata);
    }
}
