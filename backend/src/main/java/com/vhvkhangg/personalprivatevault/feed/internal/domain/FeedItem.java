package com.vhvkhangg.personalprivatevault.feed.internal.domain;

import com.vhvkhangg.personalprivatevault.feed.view.FeedJsonSnapshot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Feed item entity mapped to {@code feed_items}.
 */
@Entity
@Table(name = "feed_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "feed_source_id", nullable = false)
    private Long feedSourceId;

    @Column(name = "external_id", length = 500)
    private String externalId;

    @Column(name = "title", length = 1000, nullable = false)
    private String title;

    @Column(name = "url", length = 2048, nullable = false)
    private String url;

    @Column(name = "url_hash", length = 64, nullable = false)
    private String urlHash;

    @Column(name = "author", length = 500)
    private String author;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_metadata", columnDefinition = "jsonb")
    private Map<String, Object> rawMetadata;

    public FeedItem(
            Long feedSourceId,
            String externalId,
            String title,
            String url,
            String urlHash,
            String author,
            String summary,
            Instant publishedAt,
            Instant fetchedAt,
            Map<String, Object> rawMetadata
    ) {
        this.feedSourceId = Objects.requireNonNull(feedSourceId, "feedSourceId must not be null");
        this.externalId = externalId;
        this.title = Objects.requireNonNull(title, "FeedItem title must not be null");
        this.url = Objects.requireNonNull(url, "FeedItem url must not be null");
        this.urlHash = Objects.requireNonNull(urlHash, "FeedItem urlHash must not be null");
        this.author = author;
        this.summary = summary;
        this.publishedAt = publishedAt;
        this.fetchedAt = Objects.requireNonNull(fetchedAt, "FeedItem fetchedAt must not be null");
        this.rawMetadata = FeedJsonSnapshot.deepCopy(rawMetadata);
    }

    public void update(
            String externalId,
            String title,
            String url,
            String urlHash,
            String author,
            String summary,
            Instant publishedAt,
            Instant fetchedAt,
            Map<String, Object> rawMetadata
    ) {
        this.externalId = externalId;
        this.title = Objects.requireNonNull(title, "FeedItem title must not be null");
        this.url = Objects.requireNonNull(url, "FeedItem url must not be null");
        this.urlHash = Objects.requireNonNull(urlHash, "FeedItem urlHash must not be null");
        this.author = author;
        this.summary = summary;
        this.publishedAt = publishedAt;
        this.fetchedAt = Objects.requireNonNull(fetchedAt, "FeedItem fetchedAt must not be null");
        this.rawMetadata = FeedJsonSnapshot.deepCopy(rawMetadata);
    }

    public Map<String, Object> getRawMetadata() {
        return FeedJsonSnapshot.toUnmodifiableSnapshot(this.rawMetadata);
    }
}
