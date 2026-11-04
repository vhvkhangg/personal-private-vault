package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;

import java.time.Instant;
import java.util.Map;

/**
 * Spring Data projection for bounded Feed Radar queries joined to FeedSource.
 */
public interface FeedRadarProjection {

    Long getId();

    Long getFeedSourceId();

    String getSourceName();

    FeedSourceType getSourceType();

    String getTitle();

    String getUrl();

    Instant getPublishedAt();

    Instant getFetchedAt();

    Map<String, Object> getRawMetadata();
}
