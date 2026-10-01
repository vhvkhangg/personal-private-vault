package com.vhvkhangg.personalprivatevault.feed.internal.domain;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.view.FeedJsonSnapshot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
 * Feed source entity mapped to {@code feed_sources}.
 */
@Entity
@Table(name = "feed_sources")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false)
    private FeedSourceType type;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Column(name = "feed_url", length = 2048)
    private String feedUrl;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "scheduled_refresh_enabled", nullable = false)
    private boolean scheduledRefreshEnabled;

    @Column(name = "refresh_interval_minutes")
    private Integer refreshIntervalMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", columnDefinition = "jsonb")
    private Map<String, Object> config;

    @Column(name = "last_fetched_at")
    private Instant lastFetchedAt;

    @Column(name = "next_fetch_at")
    private Instant nextFetchAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public FeedSource(
            String name,
            FeedSourceType type,
            String sourceUrl,
            String feedUrl,
            boolean enabled,
            boolean scheduledRefreshEnabled,
            Integer refreshIntervalMinutes,
            Map<String, Object> config
    ) {
        this.name = Objects.requireNonNull(name, "FeedSource name must not be null");
        this.type = Objects.requireNonNull(type, "FeedSource type must not be null");
        this.sourceUrl = sourceUrl;
        this.feedUrl = feedUrl;
        this.enabled = enabled;
        this.scheduledRefreshEnabled = scheduledRefreshEnabled;
        this.refreshIntervalMinutes = refreshIntervalMinutes;
        this.config = FeedJsonSnapshot.deepCopy(config);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void update(
            String name,
            FeedSourceType type,
            String sourceUrl,
            String feedUrl,
            boolean enabled,
            boolean scheduledRefreshEnabled,
            Integer refreshIntervalMinutes,
            Map<String, Object> config
    ) {
        this.name = Objects.requireNonNull(name, "FeedSource name must not be null");
        this.type = Objects.requireNonNull(type, "FeedSource type must not be null");
        this.sourceUrl = sourceUrl;
        this.feedUrl = feedUrl;
        this.enabled = enabled;
        this.scheduledRefreshEnabled = scheduledRefreshEnabled;
        this.refreshIntervalMinutes = refreshIntervalMinutes;
        this.config = FeedJsonSnapshot.deepCopy(config);
    }

    public void recordFetch(Instant fetchedAt, Instant nextFetchAt) {
        this.lastFetchedAt = fetchedAt;
        this.nextFetchAt = nextFetchAt;
    }

    public void setNextFetchAt(Instant nextFetchAt) {
        this.nextFetchAt = nextFetchAt;
    }

    public Map<String, Object> getConfig() {
        return FeedJsonSnapshot.toUnmodifiableSnapshot(this.config);
    }

    @PrePersist
    void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
    }

    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = Instant.now();
    }
}
