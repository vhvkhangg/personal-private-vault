package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedSourceNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.InvalidFeedSourceException;
import com.vhvkhangg.personalprivatevault.feed.view.FeedItemView;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Spring-proxied transactional finalizer executing generic ingestion and authoritative
 * locked source re-validation within one short database transaction.
 */
@Component
@RequiredArgsConstructor
public class HackerNewsRefreshFinalizer {

    private final FeedItemOperations feedItemOperations;
    private final FeedSourceRepository feedSourceRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRED, timeout = 5)
    public List<FeedItemView> finalizeRefresh(Long sourceId, Instant fetchedAt, List<NormalizedFeedItemInput> normalizedItems) {
        return finalizeRefresh(sourceId, fetchedAt, normalizedItems, false);
    }

    @Transactional(propagation = Propagation.REQUIRED, timeout = 5)
    public List<FeedItemView> finalizeRefresh(
            Long sourceId,
            Instant fetchedAt,
            List<NormalizedFeedItemInput> normalizedItems,
            boolean scheduledMode
    ) {
        List<FeedItemView> itemViews;
        try {
            itemViews = feedItemOperations.ingestFetch(sourceId, fetchedAt, normalizedItems);
        } catch (FeedSourceNotFoundException | InvalidFeedSourceException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH, "Source changed or disabled during refresh", ex);
        }

        FeedSource lockedSource = feedSourceRepository.findByIdForUpdate(sourceId)
                .orElseThrow(() -> new FeedRefreshException(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH));
        entityManager.refresh(lockedSource);

        if (!lockedSource.isEnabled() || lockedSource.getType() != FeedSourceType.HACKER_NEWS) {
            throw new FeedRefreshException(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH);
        }
        if (scheduledMode && !lockedSource.isScheduledRefreshEnabled()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH);
        }

        return itemViews;
    }
}
