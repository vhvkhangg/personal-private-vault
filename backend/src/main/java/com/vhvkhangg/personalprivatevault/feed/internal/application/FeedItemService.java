package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedItem;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.Sha256Util;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedItemRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.item.exception.FeedItemConflictException;
import com.vhvkhangg.personalprivatevault.feed.item.exception.FeedItemNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.item.exception.InvalidFeedItemException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedSourceNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.InvalidFeedSourceException;
import com.vhvkhangg.personalprivatevault.feed.view.FeedItemView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Service implementing feed item ingestion and querying.
 */
@Service
@Slf4j
public class FeedItemService implements FeedItemOperations {

    private final FeedSourceRepository feedSourceRepository;
    private final FeedItemRepository feedItemRepository;
    private final jakarta.persistence.EntityManager entityManager;
    private final java.time.Clock clock;

    public FeedItemService(
            FeedSourceRepository feedSourceRepository,
            FeedItemRepository feedItemRepository,
            jakarta.persistence.EntityManager entityManager,
            @org.springframework.beans.factory.annotation.Autowired(required = false) java.time.Clock clock
    ) {
        this.feedSourceRepository = feedSourceRepository;
        this.feedItemRepository = feedItemRepository;
        this.entityManager = entityManager;
        this.clock = clock != null ? clock : java.time.Clock.systemUTC();
    }

    @Override
    @Transactional
    public List<FeedItemView> ingestFetch(Long sourceId, Instant fetchedAt, List<NormalizedFeedItemInput> normalizedItems) {
        FeedSource source = feedSourceRepository.findById(sourceId)
                .orElseThrow(() -> new FeedSourceNotFoundException(sourceId));

        if (!source.isEnabled()) {
            throw new InvalidFeedSourceException("Feed source is disabled");
        }
        if (fetchedAt == null) {
            throw new InvalidFeedItemException("fetchedAt must not be null");
        }

        List<ValidatedCandidate> candidates = validateAndDeduplicateBatch(normalizedItems);

        try {
            List<FeedItem> resultItems = new ArrayList<>();
            for (ValidatedCandidate candidate : candidates) {
                Optional<FeedItem> byExtId = candidate.externalId() != null
                        ? feedItemRepository.findByFeedSourceIdAndExternalId(sourceId, candidate.externalId())
                        : Optional.empty();

                Optional<FeedItem> byHash = feedItemRepository.findByFeedSourceIdAndUrlHash(sourceId, candidate.urlHash());

                if (byExtId.isPresent() && byHash.isPresent()) {
                    if (!byExtId.get().getId().equals(byHash.get().getId())) {
                        throw new FeedItemConflictException("Feed item key ambiguity: external ID and URL hash match different items");
                    }
                    FeedItem existing = byExtId.get();
                    existing.update(
                            candidate.externalId(),
                            candidate.title(),
                            candidate.url(),
                            candidate.urlHash(),
                            candidate.author(),
                            candidate.summary(),
                            candidate.publishedAt(),
                            fetchedAt,
                            candidate.rawMetadata()
                    );
                    resultItems.add(feedItemRepository.save(existing));
                } else if (byExtId.isPresent()) {
                    FeedItem existing = byExtId.get();
                    existing.update(
                            candidate.externalId(),
                            candidate.title(),
                            candidate.url(),
                            candidate.urlHash(),
                            candidate.author(),
                            candidate.summary(),
                            candidate.publishedAt(),
                            fetchedAt,
                            candidate.rawMetadata()
                    );
                    resultItems.add(feedItemRepository.save(existing));
                } else if (byHash.isPresent()) {
                    FeedItem existing = byHash.get();
                    existing.update(
                            candidate.externalId(),
                            candidate.title(),
                            candidate.url(),
                            candidate.urlHash(),
                            candidate.author(),
                            candidate.summary(),
                            candidate.publishedAt(),
                            fetchedAt,
                            candidate.rawMetadata()
                    );
                    resultItems.add(feedItemRepository.save(existing));
                } else {
                    FeedItem newItem = new FeedItem(
                            sourceId,
                            candidate.externalId(),
                            candidate.title(),
                            candidate.url(),
                            candidate.urlHash(),
                            candidate.author(),
                            candidate.summary(),
                            candidate.publishedAt(),
                            fetchedAt,
                            candidate.rawMetadata()
                    );
                    resultItems.add(feedItemRepository.save(newItem));
                }
            }

            // Update feed source fetch timestamps under lock with authoritative configuration
            FeedSource sourceToUpdate = feedSourceRepository.findByIdForUpdate(sourceId)
                    .orElseThrow(() -> new FeedSourceNotFoundException(sourceId));
            entityManager.refresh(sourceToUpdate);

            if (!sourceToUpdate.isEnabled()) {
                throw new InvalidFeedSourceException("Feed source is disabled");
            }

            Instant nextFetch = null;
            if (sourceToUpdate.isScheduledRefreshEnabled() && sourceToUpdate.getRefreshIntervalMinutes() != null) {
                nextFetch = fetchedAt.plus(Duration.ofMinutes(sourceToUpdate.getRefreshIntervalMinutes()));
            }
            sourceToUpdate.recordFetch(fetchedAt, nextFetch);
            feedSourceRepository.saveAndFlush(sourceToUpdate);
            feedItemRepository.flush();

            return resultItems.stream().map(this::toView).toList();
        } catch (DataIntegrityViolationException ex) {
            log.warn("Feed item constraint violation during ingestion for sourceId: {}", sourceId);
            throw new FeedItemConflictException("Feed item unique constraint violation during ingestion");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FeedItemView> findItemById(Long id) {
        return feedItemRepository.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedItemView> findRecentItemsBySource(Long sourceId, int limit) {
        if (limit <= 0) {
            throw new InvalidFeedItemException("Limit must be positive");
        }
        return feedItemRepository.findRecentBySource(sourceId, PageRequest.of(0, limit))
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public com.vhvkhangg.personalprivatevault.feed.view.FeedRadarPageView getRadar(Long sourceId, int limit) {
        if (sourceId != null) {
            FeedSource source = feedSourceRepository.findById(sourceId)
                    .orElseThrow(() -> new FeedSourceNotFoundException(sourceId));
            if (!source.isEnabled()) {
                throw new com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException(
                        com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason.SOURCE_DISABLED
                );
            }
            if (source.getType() != com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType.HACKER_NEWS) {
                throw new com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException(
                        com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason.UNSUPPORTED_SOURCE_TYPE
                );
            }
        }

        Instant now = Instant.now(clock);
        Instant windowEnd = now;
        Instant windowStart = now.minus(Duration.ofHours(24));

        List<com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedRadarProjection> rows =
                feedItemRepository.findRadarItems(
                        windowStart,
                        windowEnd,
                        com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType.HACKER_NEWS,
                        sourceId,
                        PageRequest.of(0, limit)
                );

        List<com.vhvkhangg.personalprivatevault.feed.view.FeedRadarItemView> items = rows.stream()
                .filter(row -> com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsNormalizer.isValidArticleUrl(row.getUrl()))
                .map(this::mapToRadarItemView)
                .toList();

        return new com.vhvkhangg.personalprivatevault.feed.view.FeedRadarPageView(items, windowStart, windowEnd, now);
    }

    @Override
    @Transactional(readOnly = true)
    public com.vhvkhangg.personalprivatevault.feed.view.FeedRadarPageView getCombinedRadar(int limit) {
        if (limit <= 0 || limit > 100) {
            throw new InvalidFeedItemException("Limit must be between 1 and 100");
        }
        Instant now = Instant.now(clock);
        Instant windowEnd = now;
        Instant windowStart = now.minus(Duration.ofHours(24));

        List<com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedRadarProjection> rows =
                feedItemRepository.findCombinedRadarItems(
                        windowStart,
                        windowEnd,
                        List.of(com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType.HACKER_NEWS, com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType.RSS),
                        PageRequest.of(0, limit)
                );

        List<com.vhvkhangg.personalprivatevault.feed.view.FeedRadarItemView> items = rows.stream()
                .filter(row -> com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsNormalizer.isValidArticleUrl(row.getUrl()))
                .map(this::mapToCombinedRadarItemView)
                .toList();

        return new com.vhvkhangg.personalprivatevault.feed.view.FeedRadarPageView(items, windowStart, windowEnd, now);
    }

    private com.vhvkhangg.personalprivatevault.feed.view.FeedRadarItemView mapToCombinedRadarItemView(
            com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedRadarProjection row
    ) {
        if (row.getSourceType() == com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType.RSS) {
            return new com.vhvkhangg.personalprivatevault.feed.view.FeedRadarItemView(
                    row.getId(),
                    row.getFeedSourceId(),
                    row.getSourceName(),
                    row.getSourceType(),
                    row.getTitle(),
                    row.getUrl(),
                    row.getPublishedAt(),
                    row.getFetchedAt(),
                    "RSS_SOURCE_CACHE",
                    null,
                    row.getFetchedAt(),
                    null,
                    null
            );
        }
        return mapToRadarItemView(row);
    }

    private com.vhvkhangg.personalprivatevault.feed.view.FeedRadarItemView mapToRadarItemView(
            com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedRadarProjection row
    ) {
        Map<String, Object> metadata = row.getRawMetadata();
        Long hnStoryId = null;
        String hnDiscussionUrl = null;
        Integer sourceRank = null;
        Instant signalCapturedAt = null;
        String signalType = "SOURCE_CACHE";

        if (metadata != null) {
            Object storyIdObj = metadata.get("hnStoryId");
            Object discUrlObj = metadata.get("hnDiscussionUrl");
            Object rankObj = metadata.get("hnRank");
            Object capAtObj = metadata.get("hnCapturedAt");

            Long candidateId = parseExactPositiveLong(storyIdObj);
            Integer candidateRank = parseExactRank(rankObj);

            if (candidateId != null
                    && candidateRank != null
                    && discUrlObj instanceof String discUrl
                    && capAtObj instanceof String capAtStr) {
                try {
                    String expectedDiscUrl = "https://news.ycombinator.com/item?id=" + candidateId;
                    if (expectedDiscUrl.equals(discUrl)) {
                        signalCapturedAt = Instant.parse(capAtStr);
                        hnStoryId = candidateId;
                        hnDiscussionUrl = discUrl;
                        sourceRank = candidateRank;
                        signalType = "HN_TOP_STORIES_AT_CAPTURE";
                    }
                } catch (Exception ex) {
                    hnStoryId = null;
                    hnDiscussionUrl = null;
                    sourceRank = null;
                    signalCapturedAt = null;
                    signalType = "SOURCE_CACHE";
                }
            }
        }

        return new com.vhvkhangg.personalprivatevault.feed.view.FeedRadarItemView(
                row.getId(),
                row.getFeedSourceId(),
                row.getSourceName(),
                row.getSourceType(),
                row.getTitle(),
                row.getUrl(),
                row.getPublishedAt(),
                row.getFetchedAt(),
                signalType,
                sourceRank,
                signalCapturedAt,
                hnStoryId,
                hnDiscussionUrl
        );
    }


    private List<ValidatedCandidate> validateAndDeduplicateBatch(List<NormalizedFeedItemInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return List.of();
        }

        Map<String, ValidatedCandidate> byExternalId = new LinkedHashMap<>();
        Map<String, ValidatedCandidate> byUrlHash = new LinkedHashMap<>();
        List<ValidatedCandidate> distinctCandidates = new ArrayList<>();

        for (NormalizedFeedItemInput input : inputs) {
            if (input == null) {
                throw new InvalidFeedItemException("Normalized feed item input must not be null");
            }
            if (input.title() == null || input.title().isBlank()) {
                throw new InvalidFeedItemException("Feed item title must not be blank");
            }
            if (input.title().trim().length() > 1000) {
                throw new InvalidFeedItemException("Feed item title must not exceed 1000 characters");
            }
            if (input.url() == null || input.url().isBlank()) {
                throw new InvalidFeedItemException("Feed item URL must not be blank");
            }
            String trimmedUrl = input.url().trim();
            if (trimmedUrl.length() > 2048) {
                throw new InvalidFeedItemException("Feed item URL must not exceed 2048 characters");
            }
            String externalId = (input.externalId() != null && !input.externalId().isBlank())
                    ? input.externalId().trim()
                    : null;
            if (externalId != null && externalId.length() > 500) {
                throw new InvalidFeedItemException("Feed item external ID must not exceed 500 characters");
            }
            String author = (input.author() != null && !input.author().isBlank()) ? input.author().trim() : null;
            if (author != null && author.length() > 500) {
                throw new InvalidFeedItemException("Feed item author must not exceed 500 characters");
            }
            String summary = input.summary();
            String urlHash = Sha256Util.computeSha256(trimmedUrl);

            ValidatedCandidate candidate = new ValidatedCandidate(
                    externalId,
                    input.title().trim(),
                    trimmedUrl,
                    urlHash,
                    author,
                    summary,
                    input.publishedAt(),
                    input.rawMetadata()
            );

            // In-batch conflict/deduplication checks
            if (externalId != null) {
                ValidatedCandidate existingSameExtId = byExternalId.get(externalId);
                if (existingSameExtId != null) {
                    if (!candidate.isEquivalent(existingSameExtId)) {
                        throw new FeedItemConflictException("Conflicting feed item candidates with same external ID in batch");
                    }
                    continue; // Identical candidate, deduplicated
                }
            }

            ValidatedCandidate existingSameUrl = byUrlHash.get(urlHash);
            if (existingSameUrl != null) {
                if (!candidate.isEquivalent(existingSameUrl)) {
                    throw new FeedItemConflictException("Conflicting feed item candidates with same URL in batch");
                }
                continue; // Identical candidate, deduplicated
            }

            if (externalId != null) {
                byExternalId.put(externalId, candidate);
            }
            byUrlHash.put(urlHash, candidate);
            distinctCandidates.add(candidate);
        }

        return distinctCandidates;
    }

    private FeedItemView toView(FeedItem i) {
        return new FeedItemView(
                i.getId(),
                i.getFeedSourceId(),
                i.getExternalId(),
                i.getTitle(),
                i.getUrl(),
                i.getUrlHash(),
                i.getAuthor(),
                i.getSummary(),
                i.getPublishedAt(),
                i.getFetchedAt(),
                i.getRawMetadata()
        );
    }

    private static Long parseExactPositiveLong(Object obj) {
        if (obj == null || obj instanceof Double || obj instanceof Float) {
            return null;
        }
        if (obj instanceof Long l) {
            return l > 0 ? l : null;
        }
        if (obj instanceof Integer i) {
            return i > 0 ? i.longValue() : null;
        }
        if (obj instanceof Short s) {
            return s > 0 ? s.longValue() : null;
        }
        if (obj instanceof Byte b) {
            return b > 0 ? b.longValue() : null;
        }
        if (obj instanceof BigInteger bi) {
            try {
                long val = bi.longValueExact();
                return val > 0 ? val : null;
            } catch (ArithmeticException ex) {
                return null;
            }
        }
        if (obj instanceof BigDecimal bd) {
            try {
                long val = bd.toBigIntegerExact().longValueExact();
                return val > 0 ? val : null;
            } catch (ArithmeticException ex) {
                return null;
            }
        }
        return null;
    }

    private static Integer parseExactRank(Object obj) {
        if (obj == null || obj instanceof Double || obj instanceof Float) {
            return null;
        }
        if (obj instanceof Integer i) {
            return (i >= 1 && i <= 50) ? i : null;
        }
        if (obj instanceof Long l) {
            return (l >= 1 && l <= 50) ? l.intValue() : null;
        }
        if (obj instanceof Short s) {
            return (s >= 1 && s <= 50) ? s.intValue() : null;
        }
        if (obj instanceof Byte b) {
            return (b >= 1 && b <= 50) ? b.intValue() : null;
        }
        if (obj instanceof BigInteger bi) {
            try {
                int val = bi.intValueExact();
                return (val >= 1 && val <= 50) ? val : null;
            } catch (ArithmeticException ex) {
                return null;
            }
        }
        if (obj instanceof BigDecimal bd) {
            try {
                int val = bd.toBigIntegerExact().intValueExact();
                return (val >= 1 && val <= 50) ? val : null;
            } catch (ArithmeticException ex) {
                return null;
            }
        }
        return null;
    }

    private record ValidatedCandidate(
            String externalId,
            String title,
            String url,
            String urlHash,
            String author,
            String summary,
            Instant publishedAt,
            Map<String, Object> rawMetadata
    ) {
        boolean isEquivalent(ValidatedCandidate other) {
            return Objects.equals(this.externalId, other.externalId)
                    && Objects.equals(this.title, other.title)
                    && Objects.equals(this.url, other.url)
                    && Objects.equals(this.urlHash, other.urlHash)
                    && Objects.equals(this.author, other.author)
                    && Objects.equals(this.summary, other.summary)
                    && Objects.equals(this.publishedAt, other.publishedAt)
                    && Objects.equals(this.rawMetadata, other.rawMetadata);
        }
    }
}
